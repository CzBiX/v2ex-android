package com.czbix.v2ex.network

import com.czbix.v2ex.common.exception.ConnectionException
import com.czbix.v2ex.common.exception.RemoteException
import com.czbix.v2ex.common.exception.RequestException
import com.czbix.v2ex.model.ServerConfig
import com.czbix.v2ex.network.RequestHelper.newRequest
import com.czbix.v2ex.util.LogUtils
import com.czbix.v2ex.util.async
import com.czbix.v2ex.util.fromJson
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import rx.Observable
import rx.schedulers.Schedulers
import java.io.IOException

object CzRequestHelper {
    private val BASE_URL = "https://v2ex.czbix.com"

    private val TAG = CzRequestHelper::class.java.simpleName
    private val API_BASE = BASE_URL + "/api"
    private val API_USER = API_BASE + "/user/%s"
    private val API_SETTINGS = API_USER + "/settings"
    private val API_SERVER_CONFIG = BASE_URL + "/static/config.json"

    private val CLIENT: OkHttpClient

    init {
        CLIENT = RequestHelper.getClient().newBuilder().let {
            it.cookieJar(CookieJar.NO_COOKIES)
            it.build()
        }
    }

    @JvmStatic
    @Throws(ConnectionException::class, RemoteException::class)
    fun registerUser(username: String) {
        LogUtils.v(TAG, "register user, username: %s", username)

        val request = newRequest().url(String.format(API_USER, username)).put(RequestBody.create(null, ByteArray(0))).build()
        val response = RequestHelper.sendRequest(request)

        val code = response.code()
        if (code != HttpStatus.SC_NOT_MODIFIED && code != HttpStatus.SC_CREATED) {
            throw RequestException("register user failed", response)
        }
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

        RequestHelper.sendRequest(request)
    }

    @JvmStatic
    @Throws(ConnectionException::class, RemoteException::class)
    fun unregisterDevice(username: String, token: String) {
        LogUtils.v(TAG, "unregister device token: %s", token)

        val body = FormBody.Builder().add("action", "del_gcm_token").add("token", token).build()
        val request = newRequest().apply {
            url(API_SETTINGS.format(body))
        }.build()

        RequestHelper.sendRequest(request)
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
            url(String.format(API_SETTINGS, username))
            post(body)
        }.build()

        RequestHelper.sendRequest(request)
    }

    fun getServerConfig(): Observable<ServerConfig> {
        LogUtils.v(TAG, "get server config")

        return async(Schedulers.io()) {
            val request = newRequest().url(API_SERVER_CONFIG).build()

            try {
                RequestHelper.sendRequest(request).body().use {
                    it.charStream().use {
                        it.fromJson<ServerConfig>()
                    }
                }
            } catch (e: Exception) {
                throw if (e is IOException) {
                    ConnectionException(e)
                } else {
                    e
                }
            }
        }
    }
}
