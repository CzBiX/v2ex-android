package com.czbix.v2ex.network

import android.os.Build
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.czbix.v2ex.AppCtx
import com.czbix.v2ex.BuildConfig
import com.czbix.v2ex.common.DeviceStatus
import com.czbix.v2ex.common.UserState
import com.czbix.v2ex.common.exception.*
import com.czbix.v2ex.model.*
import com.czbix.v2ex.parser.*
import com.czbix.v2ex.parser.Parser.PageType
import com.czbix.v2ex.util.*
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.common.base.Preconditions
import com.google.common.base.Stopwatch
import com.google.common.base.Strings
import com.google.common.net.HttpHeaders
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.SingleSubject
import okhttp3.*
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

object RequestHelper {
    const val BASE_URL = "https://www.v2ex.com"
    private val USER_AGENT = "V2EX+/" + BuildConfig.VERSION_NAME
    private val USER_AGENT_ANDROID = String.format("%s (Android %s)", USER_AGENT, Build.VERSION.RELEASE)

    private val DEBUG_NETWORK_FAIL_PROBABILITY = 95

    private val TAG = RequestHelper::class.java.simpleName
    private val API_GET_ALL_NODES = BASE_URL + "/api/nodes/all.json"
    private val URL_SIGN_IN = BASE_URL + "/signin"
    private val URL_MISSION_DAILY = BASE_URL + "/mission/daily"
    private val URL_ONCE_TOKEN = URL_SIGN_IN
    private val URL_NOTIFICATIONS = BASE_URL + "/notifications"
    private val URL_FAVORITE_NODES = BASE_URL + "/my/nodes"
    private val URL_UNREAD_NOTIFICATIONS = BASE_URL + "/mission"
    private val URL_TWO_FACTOR_AUTH = BASE_URL + "/2fa"
    private val URL_NEW_TOPIC = BASE_URL + "/new/%s"
    private val URL_CAPTCHA = BASE_URL + "/_captcha"

    val client: OkHttpClient

    private var cookieJar: PersistentCookieJar

    init {
        cookieJar = PersistentCookieJar(SetCookieCache(),
                SharedPrefsCookiePersistor(AppCtx.instance))

        client = OkHttpClient.Builder().apply {
            cache(buildCache())
            connectTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            followRedirects(false)
            cookieJar(cookieJar)
        }.build()
    }

    private fun buildCache(): Cache {
        val cacheDir = IoUtils.webCachePath
        val cacheSize = 16 * 1024 * 1024

        return Cache(cacheDir, cacheSize.toLong())
    }

    internal fun newRequest(useMobile: Boolean = false): Request.Builder {
        val ua = if (useMobile) {
            USER_AGENT_ANDROID
        } else {
            USER_AGENT
        }

        return Request.Builder().apply {
            header(HttpHeaders.USER_AGENT, ua)
        }
    }

    fun cleanCookies() {
        LogUtils.d(TAG, "clean cookies")

        cookieJar.clear()
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun getTopics(page: Page, pageNum: Int? = null): Single<TopicListParser.TopicList> {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Request latest topic for page: " + page.title)
        }

        val url = if (pageNum == null) {
            HttpUrl.parse(page.url)!!
        } else {
            require(page is Node) {
                "Only node support page."
            }

            HttpUrl.parse(page.url)!!.newBuilder().apply {
                setQueryParameter("p", pageNum.toString())
            }.build()
        }

        val request = newRequest()
                .url(url)
                .build()

        return sendRequest(request) { response ->
            if (response.isRedirect) {
                throw ExIllegalStateException("topics page should not redirect")
            }

            val topics = try {
                val doc = Parser.toDoc(response.body()!!.string())
                processUserState(doc, if (page is Tab) PageType.Tab else PageType.Node)
                TopicListParser.parseDoc(doc, page)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Received topics, count: " + topics.size)
            }

            topics
        }
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun getTopicWithComments(topic: Topic, page: Int): TopicWithComments {
        Preconditions.checkArgument(page > 0, "page must greater than zero")

        LogUtils.v(TAG, "request topic with comments, id: %d, title: %s", topic.id, topic.title)

        val request = newRequest(useMobile = true)
                .url(topic.url + "?p=" + page)
                .build()
        return sendRequest(request) { response ->
            if (response.isRedirect) {
                throw ExIllegalStateException("topic page shouldn't redirect")
            }

            val doc: Document
            val result: TopicWithComments

            try {
                doc = Parser.toDoc(response.body()!!.string())
                processUserState(doc, PageType.Topic)

                val stopwatch = Stopwatch.createStarted()
                result = TopicParser.parseDoc(doc, topic)
                TrackerUtils.onParseTopic(stopwatch.elapsed(TimeUnit.MILLISECONDS),
                        Integer.toString(result.mComments.size))
            } catch (e: IOException) {
                throw ConnectionException(e)
            }

            result
        }.result()
    }

    private fun processUserState(doc: Document, pageType: PageType) {
        if (!UserState.isLoggedIn()) {
            return
        }

        val info = MyselfParser.parseDoc(doc, pageType)
        UserState.handleInfo(info, pageType)
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun getAllNodes(etag: Etag): Single<List<Node>> {
        Preconditions.checkNotNull(etag)

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "request all nodes")
        }

        val request = newRequest().url(API_GET_ALL_NODES).build()
        return sendRequest(request) { response ->
            val newEtag = response.header(HttpHeaders.ETAG)
            if (!etag.setNewEtag(newEtag)) {
                return@sendRequest listOf<Node>()
            }

            try {
                val json = response.body()!!.string()

                json.fromJson<List<Node>>(true)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }
    }

    fun getFavNodes(): List<Node> {
        Preconditions.checkState(UserState.isLoggedIn(), "guest can't check notifications")
        LogUtils.v(TAG, "get favorite nodes")

        val request = newRequest().url(URL_FAVORITE_NODES).build()
        return sendRequest(request) { response ->
            try {
                val html = response.body()!!.string()

                val doc = Parser.toDoc(html)
                MyselfParser.parseFavNodes(doc)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }.result()
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun getUnreadNum(): Single<Int> {
        Preconditions.checkState(UserState.isLoggedIn(), "guest can't check notifications")
        LogUtils.v(TAG, "get unread num")

        val request = newRequest(useMobile = true)
                .url(URL_UNREAD_NOTIFICATIONS).build()
        return sendRequest(request) { response ->
            try {
                val html = response.body()!!.string()

                val doc = Parser.toDoc(html)
                NotificationParser.parseUnreadCount(doc)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }
    }

    fun getNotifications(): List<Notification> {
        Preconditions.checkState(UserState.isLoggedIn(), "guest can't check notifications")
        LogUtils.v(TAG, "get notifications")

        val request = newRequest(useMobile = true)
                .url(URL_NOTIFICATIONS)
                .build()
        return sendRequest(request) { response ->
            try {
                val html = response.body()!!.string()

                val doc = Parser.toDoc(html)
                NotificationParser.parseDoc(doc)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }.result()
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun reply(topic: Topic, content: String, once: String?): Boolean {
        LogUtils.v(TAG, "reply to topic: %s", topic.title)

        @Suppress("NAME_SHADOWING")
        val once = if (Strings.isNullOrEmpty(once)) {
            getOnceToken().result()
        } else {
            once!!
        }
        val requestBody = FormBody.Builder().add("once", once)
                .add("content", content.replace("\n", "\r\n"))
                .build()

        val request = newRequest(useMobile = true)
                .url(topic.url)
                .post(requestBody).build()
        return sendRequest(request, false) { response ->
            // v2ex will redirect if reply success
            response.code() == HttpStatus.SC_MOVED_TEMPORARILY
        }.result()
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun ignore(obj: Ignorable, onceToken: String) {
        val builder = newRequest().url(obj.ignoreUrl + "?once=" + onceToken)
        val isComment = obj is Comment
        if (isComment) {
            builder.post(RequestBody.create(null, ByteArray(0)))
        }
        val request = builder.build()

        sendRequest(request, isComment).result()
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun favor(obj: Favable, isFavor: Boolean, token: String): Single<Unit> {
        val url = if (isFavor) obj.getFavUrl(token) else obj.getUnFavUrl(token)
        val request = newRequest().url(url)
                .build()

        return sendRequest<Unit>(request) {
            error("Shouldn't go to here")
        }.onErrorReturn {
            checkIsRedirectException(it) {
                String.format("favor %s failed, is fav: %b", obj, isFavor)
            }
        }
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun thank(obj: Thankable, csrfToken: String) {
        val request = newRequest().url(obj.thankUrl + "?t=" + csrfToken)
                .post(RequestBody.create(null, ByteArray(0))).build()

        sendRequest(request).result()
    }

    private fun checkIsRedirectException(e: Throwable, messageExp: (() -> String)? = null) {
        if (e is RequestException) {
            if (e is UrlRedirectException) {
                return
            }

            val message = if (messageExp == null) {
                e.message
            } else {
                messageExp()
            }
            throw RequestException(message, e.response, e.cause)
        }

        throw e
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun dailyBonus(): Single<Unit> {
        LogUtils.v(TAG, "daily bonus")

        return getOnceToken().flatMap { onceCode ->
            val request = newRequest().apply {
                url("%s/redeem?once=%s".format(URL_MISSION_DAILY, onceCode))
                header(HttpHeaders.REFERER, URL_MISSION_DAILY)
            }.build()

            sendRequest<Unit>(request) {
                error("Shouldn't go to here")
            }.onErrorReturn {
                checkIsRedirectException(it)
            }
        }
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun newTopic(nodeName: String, title: String, content: String): Int {
        LogUtils.v(TAG, "new topic in node: %s, title: %s", nodeName, title)

        val once = getOnceToken().result()
        val requestBody = FormBody.Builder().add("once", once)
                .add("title", title)
                .add("content", content.replace("\n", "\r\n"))
                .build()

        val request = newRequest().url(String.format(URL_NEW_TOPIC, nodeName))
                .post(requestBody).build()
        return sendRequest(request) { response ->
            // v2ex will redirect if reply success
            if (response.code() == HttpStatus.SC_MOVED_TEMPORARILY) {
                val location = response.header(HttpHeaders.LOCATION)
                return@sendRequest Topic.getIdFromUrl(location)
            }

            val exception = RequestException("post new topic failed", response)
            try {
                exception.errorHtml = TopicParser.parseProblemInfo(response.body()!!.string())
            } catch (e: IOException) {
                throw ConnectionException(e)
            }

            throw exception
        }.result()
    }

    @Throws(ConnectionException::class, RemoteException::class)
    fun hasDailyAward(): Boolean {
        val request = newRequest(useMobile = true)
                .url(URL_MISSION_DAILY).build()

        return sendRequest(request) { response ->
            val html: String
            try {
                html = response.body()!!.string()
            } catch (e: IOException) {
                throw ConnectionException(e)
            }

            MyselfParser.hasAward(html)
        }.result()
    }

    fun login(account: String, password: String,
              captcha: String, signInFormData: Parser.SignInFormData): Single<LoginResult> {
        LogUtils.v(TAG, "login user: " + account)

        return Single.just(signInFormData).flatMap { signInForm ->
            val nextUrl = "/mission"
            val requestBody = FormBody.Builder().add("once", signInForm.once)
                    .add(signInForm.username, account)
                    .add(signInForm.password, password)
                    .add(signInForm.captcha, captcha)
                    .add("next", nextUrl)
                    .build()
            val request = newRequest().url(URL_SIGN_IN)
                    .header(HttpHeaders.REFERER, URL_SIGN_IN)
                    .post(requestBody).build()

            innerLogin(request, nextUrl)
        }
    }

    fun twoFactorAuth(code: String): Single<LoginResult> {
        LogUtils.v(TAG, "two factor auth")

        val body = FormBody.Builder().apply {
            add("code", code)
        }.build()

        val request = newRequest(useMobile = true).apply {
            url(URL_TWO_FACTOR_AUTH)
            header(HttpHeaders.REFERER, URL_TWO_FACTOR_AUTH)
            post(body)
        }.build()

        return innerLogin(request, "/")
    }

    private fun innerLogin(request: Request, nextUrl: String): Single<LoginResult> {
        return sendRequest(request, false) { response ->
            // v2ex will redirect if login success
            if (response.code() != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw RequestException("code should not be " + response.code(), response)
            }

            val location = checkNotNull(response.header(HttpHeaders.LOCATION)) {
                "Redirect response missing location"
            }
            if (location != nextUrl) {
                throw RequestException("location should not be " + location, response)
            }

            location
        }.flatMap {
            parseLoginResult(it)
        }
    }

    private fun parseLoginResult(location: String): Single<LoginResult> {
        val request = newRequest().url(BASE_URL + location).build()

        return sendRequest(request) { response ->
            try {
                val html = response.body()!!.string()
                val document = Parser.toDoc(html)
                MyselfParser.parseLoginResult(document)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }
    }

    private fun getOnceToken(): Single<String> {
        LogUtils.v(TAG, "get once token")

        val request = newRequest(useMobile = true)
                .url(URL_ONCE_TOKEN).build()
        return sendRequest(request) { response ->
            try {
                val html = response.body()!!.string()
                Parser.parseOnceCode(html)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }
    }

    fun getSignInForm(): Single<Parser.SignInFormData> {
        LogUtils.v(TAG, "get sign in form")

        val request = newRequest(useMobile = true)
                .url(URL_ONCE_TOKEN).build()
        return sendRequest(request) { response ->
            try {
                val html = response.body()!!.string()
                Parser.parseSignInForm(html)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }
    }

    fun getNotificationsToken(): String {
        LogUtils.v(TAG, "get notifications token")

        val request = newRequest()
                .url(URL_NOTIFICATIONS).build()
        return sendRequest(request) { response ->
            try {
                val html = response.body()!!.string()
                NotificationParser.parseToken(html)
            } catch (e: IOException) {
                throw ConnectionException(e)
            }
        }.result()
    }

    /**
     * You take the responsibility to close response body
     */
    internal fun sendRequest(request: Request, checkResponse: Boolean = true): Single<Response> {
        if (!DeviceStatus.getInstance().isNetworkConnected) {
            return Single.error(ConnectionException("Network not connected"))
        }

        if (BuildConfig.DEBUG && Random().nextInt(100) > DEBUG_NETWORK_FAIL_PROBABILITY) {
            return Single.error(ConnectionException("Network exception test"))
        }

        val subject = SingleSubject.create<Response>()
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                subject.onError(ConnectionException(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (checkResponse) {
                        checkResponse(response)
                    }

                    subject.onSuccess(response)
                } catch (e: Throwable) {
                    subject.onError(e)
                }
            }
        })

        return subject.doOnDispose {
            call.cancel()
        }.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
    }

    internal inline fun <T : Any> sendRequest(request: Request, checkResponse: Boolean = true,
                                              crossinline callback: (Response) -> T): Single<T> {
        return sendRequest(request, checkResponse).map {
            it.use(callback)
        }
    }

    @Throws(RemoteException::class, RequestException::class, ConnectionException::class)
    private fun checkResponse(response: Response) {
        if (response.isSuccessful) {
            return
        }

        val code = response.code()
        if (response.isRedirect) {
            val location = checkNotNull(response.header(HttpHeaders.LOCATION)) {
                "Redirect response missing location header."
            }
            when {
                location.startsWith("/signin") -> throw UnauthorizedException(response)
                location.startsWith("/2fa") -> throw TwoFactorAuthException(response)
                location.startsWith("/restricted") -> throw RestrictedException(response)
                else -> throw UrlRedirectException(location, "unknown response", response)
            }
        }

        if (code >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            throw RemoteException(response)
        }


        Crashlytics.log("request url: " + response.request().url())
        if (code == HttpStatus.SC_FORBIDDEN || code == HttpStatus.SC_NOT_FOUND) {
            try {
                val body = response.body()!!

                if (body.contentLength() == 0L) {
                    // it's blocked for guest
                } else {
                    // topic deleted, record logs for debug
                    val bodyStr = body.string()
                    Crashlytics.log(String.format("response code %d, %s", code,
                            bodyStr.substring(0, Math.min(4096, bodyStr.length))))
                }

                val ex = RequestException(response)
                ex.isShouldLogged = false
                throw ex
            } catch (e: IOException) {
                throw ConnectionException(e)
            }

        }

        throw RequestException(response)
    }

    fun getCaptchaImageUrl(once: String): String = "$URL_CAPTCHA?once=$once"

}
