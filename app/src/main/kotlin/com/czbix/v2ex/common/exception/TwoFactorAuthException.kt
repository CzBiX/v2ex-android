package com.czbix.v2ex.common.exception

import okhttp3.Response

class TwoFactorAuthException(response: Response, tr: Throwable? = null) : UnauthorizedException(response, tr)
