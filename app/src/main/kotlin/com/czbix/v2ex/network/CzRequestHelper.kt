package com.czbix.v2ex.network

import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.model.ServerConfig
import com.czbix.v2ex.network.RequestHelper.newRequest
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.fromJson
import com.czbix.v2ex.util.result
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import rx.Observable

object CzRequestHelper {
    private val BASE_URL = "https://v2ex.czbix.com"

    private val TAG = CzRequestHelper::class.java.simpleName
    private val API_BASE = BASE_URL + "/api"
    private val API_USER = API_BASE + "/user/%s"
    private val API_SETTINGS = API_USER + "/settings"
    private val API_SERVER_CONFIG = BASE_URL + "/static/config.json"

    private val CLIENT: OkHttpClient

    init {
        CLIENT = RequestHelper.client.newBuilder().apply {
            cookieJar(CookieJar.NO_COOKIES)
        }.build()
    }

    @JvmStatic
    @Throws(ConnectionException::class, RemoteException::class)
    fun registerUser(username: String) {
        LogUtils.v(TAG, "register user, username: %s", username)

        val request = newRequest().url(String.format(API_USER, username)).put(RequestBody.create(null, ByteArray(0))).build()
        RequestHelper.sendRequest(request, false) { response ->
            val code = response.code()

            if (code >= 500) {
                throw RemoteException(response)
            } else if (code != HttpStatus.SC_NOT_MODIFIED && code != HttpStatus.SC_CREATED) {
                throw RequestException("register user failed", response)
            }
        }.result()
    }

    @JvmStatic
    @Throws(ConnectionException::class, RemoteException::class)
    fun registerDevice(username: String, token: String) {
        LogUtils.v(TAG, "register device token: %s", token)

        val body = FormBody.Builder().apply {
            add("action", "add_gcm_token")
            add("token", token)
        }.build()
        val request = newRequest().apply {
            url(API_SETTINGS.format(username))
            post(body)
        }.build()

        RequestHelper.sendRequest(request).result()
    }

    @JvmStatic
    @Throws(ConnectionException::class, RemoteException::class)
    fun unregisterDevice(username: String, token: String) {
        LogUtils.v(TAG, "unregister device token: %s", token)

        val body = FormBody.Builder().add("action", "del_gcm_token").add("token", token).build()
        val request = newRequest().apply {
            url(API_SETTINGS.format(username))
            post(body)
        }.build()

        RequestHelper.sendRequest(request).result()
    }

    @JvmStatic
    @Throws(ConnectionException::class, RemoteException::class)
    fun updateNotificationsToken(username: String, token: String) {
        LogUtils.v(TAG, "update notifications token: %s", token)

        val body = FormBody.Builder().apply {
            add("action", "set_ntf_token")
            add("token", token)
        }.build()
        val request = newRequest().apply {
            url(API_SETTINGS.format(username))
            post(body)
        }.build()

        RequestHelper.sendRequest(request).result()
    }

    fun getServerConfig(): Observable<ServerConfig> {
        LogUtils.v(TAG, "get server config")

        val request = newRequest().url(API_SERVER_CONFIG).build()

        return RequestHelper.sendRequest(request) {
            it.body().charStream().fromJson<ServerConfig>()
        }
    }
}
