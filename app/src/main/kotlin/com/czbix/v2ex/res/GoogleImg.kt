package com.czbix.v2ex.res

import com.czbix.v2ex.BuildConfig

import java.util.Calendar
import java.util.Random
import java.util.concurrent.TimeUnit

object GoogleImg {
    const val INDEX_DAWN = 0
    const val INDEX_DAY = 1
    const val INDEX_DUSK = 2
    const val INDEX_NIGHT = 3

    val SAN_FRANCISCO = arrayOf(
            "ggpht.com/IBBjIVcMzPNui1BwBg2xK9VrA03RZ6l4PPr9nlMWJSe8lS1bI1q_0ObcIZ7aIsER",
            "ggpht.com/eSJx22wFR4rSVcVI_yOVDmrrF696eptumrxJLlmWpL3T-riRuXoRvgBhesx8mg",
            "ggpht.com/G4Pl--INh2dHgjjYF6Kr_0Cc85SK9eP6UespNcBwimb1G0HmG9Y99T0OVL6lHTSd",
            "ggpht.com/6rLbkZWhGVPxAVCosBf3AMhEtiie5jUl9qAfOS0HV6bExLpNOmnwezZnAlHwDA"
    )

    val LONDON = arrayOf(
            "ggpht.com/_yM2g1jXS3GxmovyMTAo5-8q7mNY8CYZlh8rQK0xNeIfJMytLlf2FoIXM_F3wg",
            "ggpht.com/SdhQ1CORhZsvITVgd6MPRJ2Nx3U4JupHEs9qEPjImVkZf-sLB6hhDWIsGpALlvo",
            "ggpht.com/6OWsC7rvXQX8D4pzAlfOk9qaRvszP4vDuTERy42h2dMADmEHI26AYYDrUcgMyQ",
            "ggpht.com/b95yYOipfo5QmknDxUhqyJz52pfncOYwhXhMpt7Yu3W3ycwZidLpWtC388SmRxB5"
    )

    val DEFAULT = arrayOf(
            "ggpht.com/LeDpxkfCDssG2jwo20Tg01UxnUc4-PZUojwKsPzIQoGJ_CgbXc7KVko8o3nk5zA",
            "ggpht.com/bosDZkBJxNdwo-dXGZeBkYtfCVnTFq96zqC08UV4dmIccI4YBr5p0CyCE7vmj2w",
            "ggpht.com/DCGfFj7ILzkFXXDgCliyTAq-cjKs8eyoTstREjhB2grAzzjYnlelGfpIQ4cEX4c",
            "ggpht.com/QgqUFGYoAxRkyvbl_5Hq2L6CTsaGXt9kaqrMdSxga-462Uyv2IViGw7OBzDMWNI"
    )

    val GREAT_PLAINS = arrayOf(
            "ggpht.com/-eRaUK3T0bbuJdhKDMJ-wgwNmkvOgp1t7qvw-0-4Y27jFauvwyFyAPNxQCqrlgc",
            "ggpht.com/OBlnWOdeWdlVR0fgVsbSWYuXVHudBGd382Yv6ckNNBPQbFIdWWJFMdch5H3a7Kw",
            "ggpht.com/a7BiG1mNWojYk95fHBJCJKuJIarvQqvr6TaDTdTNY0DjQckdKq4MX3mJQkGI3LI",
            "ggpht.com/BkE3y4c_8D3gTl2k6dLfmnKWha5e45XnKZP8NJ4pG4pXUb_J19_QBrxrthRk1A"
    )

    val SEATTLE = arrayOf(
            "ggpht.com/x7wYMKpb2CGWK7Gqck0kSAX9uOHVvduWBresMNwtlokpVzOwQ9lOfEXsZgM75Gti",
            "ggpht.com/4kPg4fdvMJgnIgfFfCso_Ui8BHaVibW4VQb6vYq2WuUXJ7g_xU-qmUYJoZ1Y6w",
            "ggpht.com/_QetNT7lfWzeWXZArk0y-kq3ZQCps9l4PZK9G2UxHXLMiK_nDEKDGhmQvqbYBs0",
            "ggpht.com/3ctptPGg5pMe2gjbqNpDX8ZzgrSz1HWa0YfyshLsom8XAvFQJJtsJZIuB_uqyQ"
    )

    val NEW_YORK = arrayOf(
            "ggpht.com/40TWudSUSlS-D-RNu0ZQRlOrifj7K3C-hKKwstSjYsNjvWGkVQ_-QPRU5b76XAI",
            "ggpht.com/OONnhdgrhh1x14gmAeHL6joB09j0Yn7ycEgSx2Dk75cEfZFVDeQp_hFHMkZxeWw",
            "ggpht.com/qhVIITlUyAKh0l1fy1sIA32b0vnj-g5n07vhnhFqD2YovgXST5N4up4Mtb0Aa4Y",
            "ggpht.com/dq1rtn76Xertxh17J02UknLxRUgLX04f6dXjeTctgEnHudDHro_ViIJ1F68fyu3z"
    )

    val CHICAGO = arrayOf(
            "ggpht.com/HkTiPVXRlpbfe8FDJz6jlLW7kVNL6lJo-PaedbBq6HcagzCjLNt6lQ3OgDWRBI8",
            "ggpht.com/XKhGtSHgY06OVIHNVxMfvvcKhWGOI7qhLbSmOJ9p58GA-v7xCFvhHCyWIXm_dg",
            "ggpht.com/Fc15BNNdT8-aBORa8THhrf4dF6a-mslmwd3pXu0-sqvQrynDeD0i17K0eAX4a2E",
            "ggpht.com/RlSDbCW2Fs5fHEzUXbVkAhjtzyaULSxG1zsUz0OmTgajiIL_cBMk4jLYnPxnB0cP"
    )

    val BEACH = arrayOf(
            "ggpht.com/IjAj-JLbrX9CtwOXvR5R94zWVd_-De7yb-KSSCauKFjL7fc7u05lh4KxlABhT1g",
            "ggpht.com/dhrhITD19-MNErPWdzr9s7gD3Dw6XdbqemQ9lFsXDDHwRgTBrRN2aAygtbjcWpY",
            "ggpht.com/E8ubHH8XziIK5t3ou9uj1xpq8XKNGKL-OIha6rnlhq1qCXPOvrI1eXqAn7z5A-A",
            "ggpht.com/83jTpNKw0R4OW3hecAeo_9D56xY1c9o3a-opn8e8j0UG8lHy6vE8x2Ocum1GctE"
    )

    val TAHOE = arrayOf(
            "ggpht.com/iM89ChxsvNmC7ajPUgbkZgHuuD6JB9xtUbmh5ov1hDZqSDDZNCpByENjjiv9WYq2",
            "ggpht.com/dhmfa5_k78ytD0v71S7EMyFN13MOQMzaLFwUVNCNsNXkjcRfgshLhyuBo3IRpQ",
            "ggpht.com/dswhgQX4GaEif9sy__jfVdf1vevCkdDHPmxV7Nu3JGu15aApntn3lYIkq_oY",
            "ggpht.com/2oiPv-lr_VLFIHKa1KEKbzlVs1LGZukNfu0BWVzXPUZuG6bMfwTWtaVsvKFJkZUu"
    )

    val BERLIN = arrayOf(
            "ggpht.com/8ubzNsDLQGQ8fYot0UcT0pWeqXPe_Z3jCpm398FuzE8ulYy5ysUXkVnUkwbFof8t",
            "ggpht.com/q7AvSpLNCAOjlQAzPyh73cJhUewNo6-fznEvQxP07U7Z7IzuV53oZG5Lyh4Mkw",
            "ggpht.com/-30idiQtgOyr5KF7mYdV8cc85SNqLFcgBzT_UQCBKCQcGg7LhTfiATkNiToVYg",
            "ggpht.com/2JEs07oznuuvgcgzgjAxRSskgqj3LI9gpATJuoYU58R4h5Yg4EmEfWy3QVQdXwQ"
    )

    val AUSTIN = arrayOf(
            "ggpht.com/zAQwPdGWDc8a11nnSPM9peFsPHHzXxa0v1BGttsWfFy4g-kUXipz0zvcrXuGsKE",
            "ggpht.com/GB25hdlTR-h07vrrB5XRNyQMfPxpTmP9DRu7DW2iTjj4_B0ywcbulk4MX5tmeQ",
            "ggpht.com/U7M2euecz7uMJg1KA_jhcJcCXg4ZQ7ZZmQIr2aUingqFm060K1FxZu2md6zfvF-T",
            "ggpht.com/weQrxcZZp-Ut00kjauDe_cE4lbwtjwU__Xv-XtrXMBbw2lLwpb6dsevmW86MWw"
    )

    val ALL_LOCATION = arrayOf(DEFAULT, AUSTIN, BEACH, BERLIN, CHICAGO, GREAT_PLAINS, LONDON,
            NEW_YORK, SAN_FRANCISCO, SEATTLE, TAHOE)

    fun getRandomLocationIndex(): Int {
        val random = if (BuildConfig.DEBUG) {
            Random()
        } else {
            val hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis())
            Random(hours)
        }
        return random.nextInt(ALL_LOCATION.size)
    }

    fun getCurrentTimeIndex(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when {
            hour < 4 -> INDEX_NIGHT
            hour < 7 -> INDEX_DAWN
            hour < 17 -> INDEX_DAY
            hour < 20 -> INDEX_DUSK
            else -> INDEX_NIGHT
        }
    }
}
