package com.czbix.v2ex.res

import com.czbix.v2ex.BuildConfig

import java.util.Calendar
import java.util.Random
import java.util.concurrent.TimeUnit

object GoogleImg {
    val INDEX_DAWN = 0
    val INDEX_DAY = 1
    val INDEX_DUSK = 2
    val INDEX_NIGHT = 3

    val SAN_FRANCISCO = arrayOf(
            "https://lh4.ggpht.com/IBBjIVcMzPNui1BwBg2xK9VrA03RZ6l4PPr9nlMWJSe8lS1bI1q_0ObcIZ7aIsER",
            "https://lh4.ggpht.com/eSJx22wFR4rSVcVI_yOVDmrrF696eptumrxJLlmWpL3T-riRuXoRvgBhesx8mg",
            "https://lh6.ggpht.com/G4Pl--INh2dHgjjYF6Kr_0Cc85SK9eP6UespNcBwimb1G0HmG9Y99T0OVL6lHTSd",
            "https://lh5.ggpht.com/6rLbkZWhGVPxAVCosBf3AMhEtiie5jUl9qAfOS0HV6bExLpNOmnwezZnAlHwDA"
    )

    val LONDON = arrayOf(
            "https://lh5.ggpht.com/_yM2g1jXS3GxmovyMTAo5-8q7mNY8CYZlh8rQK0xNeIfJMytLlf2FoIXM_F3wg",
            "https://lh6.ggpht.com/SdhQ1CORhZsvITVgd6MPRJ2Nx3U4JupHEs9qEPjImVkZf-sLB6hhDWIsGpALlvo",
            "https://lh6.ggpht.com/6OWsC7rvXQX8D4pzAlfOk9qaRvszP4vDuTERy42h2dMADmEHI26AYYDrUcgMyQ",
            "https://lh3.ggpht.com/b95yYOipfo5QmknDxUhqyJz52pfncOYwhXhMpt7Yu3W3ycwZidLpWtC388SmRxB5"
    )

    val DEFAULT = arrayOf(
            "https://lh5.ggpht.com/LeDpxkfCDssG2jwo20Tg01UxnUc4-PZUojwKsPzIQoGJ_CgbXc7KVko8o3nk5zA",
            "https://lh5.ggpht.com/bosDZkBJxNdwo-dXGZeBkYtfCVnTFq96zqC08UV4dmIccI4YBr5p0CyCE7vmj2w",
            "https://lh4.ggpht.com/DCGfFj7ILzkFXXDgCliyTAq-cjKs8eyoTstREjhB2grAzzjYnlelGfpIQ4cEX4c",
            "https://lh6.ggpht.com/QgqUFGYoAxRkyvbl_5Hq2L6CTsaGXt9kaqrMdSxga-462Uyv2IViGw7OBzDMWNI"
    )

    val GREAT_PLAINS = arrayOf(
            "https://lh3.ggpht.com/-eRaUK3T0bbuJdhKDMJ-wgwNmkvOgp1t7qvw-0-4Y27jFauvwyFyAPNxQCqrlgc",
            "https://lh4.ggpht.com/OBlnWOdeWdlVR0fgVsbSWYuXVHudBGd382Yv6ckNNBPQbFIdWWJFMdch5H3a7Kw",
            "https://lh5.ggpht.com/a7BiG1mNWojYk95fHBJCJKuJIarvQqvr6TaDTdTNY0DjQckdKq4MX3mJQkGI3LI",
            "https://lh4.ggpht.com/BkE3y4c_8D3gTl2k6dLfmnKWha5e45XnKZP8NJ4pG4pXUb_J19_QBrxrthRk1A"
    )

    val SEATTLE = arrayOf(
            "https://lh5.ggpht.com/x7wYMKpb2CGWK7Gqck0kSAX9uOHVvduWBresMNwtlokpVzOwQ9lOfEXsZgM75Gti",
            "https://lh4.ggpht.com/4kPg4fdvMJgnIgfFfCso_Ui8BHaVibW4VQb6vYq2WuUXJ7g_xU-qmUYJoZ1Y6w",
            "https://lh5.ggpht.com/_QetNT7lfWzeWXZArk0y-kq3ZQCps9l4PZK9G2UxHXLMiK_nDEKDGhmQvqbYBs0",
            "https://lh3.ggpht.com/3ctptPGg5pMe2gjbqNpDX8ZzgrSz1HWa0YfyshLsom8XAvFQJJtsJZIuB_uqyQ"
    )

    val NEW_YORK = arrayOf(
            "https://lh6.ggpht.com/40TWudSUSlS-D-RNu0ZQRlOrifj7K3C-hKKwstSjYsNjvWGkVQ_-QPRU5b76XAI",
            "https://lh4.ggpht.com/OONnhdgrhh1x14gmAeHL6joB09j0Yn7ycEgSx2Dk75cEfZFVDeQp_hFHMkZxeWw",
            "https://lh5.ggpht.com/qhVIITlUyAKh0l1fy1sIA32b0vnj-g5n07vhnhFqD2YovgXST5N4up4Mtb0Aa4Y",
            "https://lh6.ggpht.com/dq1rtn76Xertxh17J02UknLxRUgLX04f6dXjeTctgEnHudDHro_ViIJ1F68fyu3z"
    )

    val CHICAGO = arrayOf(
            "https://lh4.ggpht.com/HkTiPVXRlpbfe8FDJz6jlLW7kVNL6lJo-PaedbBq6HcagzCjLNt6lQ3OgDWRBI8",
            "https://lh4.ggpht.com/XKhGtSHgY06OVIHNVxMfvvcKhWGOI7qhLbSmOJ9p58GA-v7xCFvhHCyWIXm_dg",
            "https://lh3.ggpht.com/Fc15BNNdT8-aBORa8THhrf4dF6a-mslmwd3pXu0-sqvQrynDeD0i17K0eAX4a2E",
            "https://lh6.ggpht.com/RlSDbCW2Fs5fHEzUXbVkAhjtzyaULSxG1zsUz0OmTgajiIL_cBMk4jLYnPxnB0cP"
    )

    val BEACH = arrayOf(
            "https://lh4.ggpht.com/IjAj-JLbrX9CtwOXvR5R94zWVd_-De7yb-KSSCauKFjL7fc7u05lh4KxlABhT1g",
            "https://lh5.ggpht.com/dhrhITD19-MNErPWdzr9s7gD3Dw6XdbqemQ9lFsXDDHwRgTBrRN2aAygtbjcWpY",
            "https://lh6.ggpht.com/E8ubHH8XziIK5t3ou9uj1xpq8XKNGKL-OIha6rnlhq1qCXPOvrI1eXqAn7z5A-A",
            "https://lh4.ggpht.com/83jTpNKw0R4OW3hecAeo_9D56xY1c9o3a-opn8e8j0UG8lHy6vE8x2Ocum1GctE"
    )

    val TAHOE = arrayOf(
            "https://lh6.ggpht.com/iM89ChxsvNmC7ajPUgbkZgHuuD6JB9xtUbmh5ov1hDZqSDDZNCpByENjjiv9WYq2",
            "https://lh5.ggpht.com/dhmfa5_k78ytD0v71S7EMyFN13MOQMzaLFwUVNCNsNXkjcRfgshLhyuBo3IRpQ",
            "https://lh5.ggpht.com/dswhgQX4GaEif9sy__jfVdf1vevCkdDHPmxV7Nu3JGu15aApntn3lYIkq_oY",
            "https://lh4.ggpht.com/2oiPv-lr_VLFIHKa1KEKbzlVs1LGZukNfu0BWVzXPUZuG6bMfwTWtaVsvKFJkZUu"
    )

    val BERLIN = arrayOf(
            "https://lh5.ggpht.com/8ubzNsDLQGQ8fYot0UcT0pWeqXPe_Z3jCpm398FuzE8ulYy5ysUXkVnUkwbFof8t",
            "https://lh3.ggpht.com/q7AvSpLNCAOjlQAzPyh73cJhUewNo6-fznEvQxP07U7Z7IzuV53oZG5Lyh4Mkw",
            "https://lh6.ggpht.com/-30idiQtgOyr5KF7mYdV8cc85SNqLFcgBzT_UQCBKCQcGg7LhTfiATkNiToVYg",
            "https://lh4.ggpht.com/2JEs07oznuuvgcgzgjAxRSskgqj3LI9gpATJuoYU58R4h5Yg4EmEfWy3QVQdXwQ"
    )

    val AUSTIN = arrayOf(
            "https://lh4.ggpht.com/zAQwPdGWDc8a11nnSPM9peFsPHHzXxa0v1BGttsWfFy4g-kUXipz0zvcrXuGsKE",
            "https://lh4.ggpht.com/GB25hdlTR-h07vrrB5XRNyQMfPxpTmP9DRu7DW2iTjj4_B0ywcbulk4MX5tmeQ",
            "https://lh5.ggpht.com/U7M2euecz7uMJg1KA_jhcJcCXg4ZQ7ZZmQIr2aUingqFm060K1FxZu2md6zfvF-T",
            "https://lh5.ggpht.com/weQrxcZZp-Ut00kjauDe_cE4lbwtjwU__Xv-XtrXMBbw2lLwpb6dsevmW86MWw"
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

        when {
            hour < 4 -> return INDEX_NIGHT
            hour < 7 -> return INDEX_DAWN
            hour < 17 -> return INDEX_DAY
            hour < 20 -> return INDEX_DUSK
            else -> return INDEX_NIGHT
        }
    }
}
