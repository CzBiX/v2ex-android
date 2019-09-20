package com.czbix.v2ex.util

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import io.kotlintest.tables.row

class MiscUtilsTest : FunSpec({
     test("decode cf email") {
         forall(
                 row("99aeaba9e9d9aeafa1a9ffe9ea", "720p@7680fps"),
                 row("19282921296959202f297f696a", "1080p@960fps")
         ) { encoded, decoded ->
             MiscUtils.decodeCfEmail(encoded) shouldBe decoded
         }
    }
})