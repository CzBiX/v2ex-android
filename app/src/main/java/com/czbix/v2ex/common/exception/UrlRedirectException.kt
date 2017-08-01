package com.czbix.v2ex.common.exception

import okhttp3.Response

class UrlRedirectException(val url: String, message: String, response: Response, tr: Throwable? = null)
    : RequestException(message, response, tr)
