package com.czbix.v2ex.common.exception

import java.io.IOException

class ConnectionException : IOException {
    constructor() {
    }

    constructor(detailMessage: String) : super(detailMessage) {
    }

    constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable) {
    }

    constructor(throwable: Throwable) : super(throwable) {
    }
}
