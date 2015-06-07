package com.czbix.v2ex.res;

import com.czbix.v2ex.BuildConfig;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SpellCheckingInspection")
public class GoogleImg {
    public static final int INDEX_DAWN = 0;
    public static final int INDEX_DAY = 1;
    public static final int INDEX_DUSK = 2;
    public static final int INDEX_NIGHT = 3;

    public static final String[] SAN_FRANCISCO = {
            "http://lh4.ggpht.com/IBBjIVcMzPNui1BwBg2xK9VrA03RZ6l4PPr9nlMWJSe8lS1bI1q_0ObcIZ7aIsER",
            "http://lh4.ggpht.com/eSJx22wFR4rSVcVI_yOVDmrrF696eptumrxJLlmWpL3T-riRuXoRvgBhesx8mg",
            "http://lh6.ggpht.com/G4Pl--INh2dHgjjYF6Kr_0Cc85SK9eP6UespNcBwimb1G0HmG9Y99T0OVL6lHTSd",
            "http://lh5.ggpht.com/6rLbkZWhGVPxAVCosBf3AMhEtiie5jUl9qAfOS0HV6bExLpNOmnwezZnAlHwDA",
    };

    public static final String[] LONDO = {
            "http://lh5.ggpht.com/_yM2g1jXS3GxmovyMTAo5-8q7mNY8CYZlh8rQK0xNeIfJMytLlf2FoIXM_F3wg",
            "http://lh6.ggpht.com/SdhQ1CORhZsvITVgd6MPRJ2Nx3U4JupHEs9qEPjImVkZf-sLB6hhDWIsGpALlvo",
            "http://lh6.ggpht.com/6OWsC7rvXQX8D4pzAlfOk9qaRvszP4vDuTERy42h2dMADmEHI26AYYDrUcgMyQ",
            "http://lh3.ggpht.com/b95yYOipfo5QmknDxUhqyJz52pfncOYwhXhMpt7Yu3W3ycwZidLpWtC388SmRxB5=",
    };

    public static final String[] DEFAULT = {
            "http://lh5.ggpht.com/LeDpxkfCDssG2jwo20Tg01UxnUc4-PZUojwKsPzIQoGJ_CgbXc7KVko8o3nk5zA",
            "http://lh5.ggpht.com/bosDZkBJxNdwo-dXGZeBkYtfCVnTFq96zqC08UV4dmIccI4YBr5p0CyCE7vmj2w",
            "http://lh4.ggpht.com/DCGfFj7ILzkFXXDgCliyTAq-cjKs8eyoTstREjhB2grAzzjYnlelGfpIQ4cEX4c",
            "http://lh6.ggpht.com/QgqUFGYoAxRkyvbl_5Hq2L6CTsaGXt9kaqrMdSxga-462Uyv2IViGw7OBzDMWNI",
    };

    public static final String[] GREAT_PLAINS = {
            "http://lh3.ggpht.com/-eRaUK3T0bbuJdhKDMJ-wgwNmkvOgp1t7qvw-0-4Y27jFauvwyFyAPNxQCqrlgc",
            "http://lh4.ggpht.com/OBlnWOdeWdlVR0fgVsbSWYuXVHudBGd382Yv6ckNNBPQbFIdWWJFMdch5H3a7Kw",
            "http://lh5.ggpht.com/a7BiG1mNWojYk95fHBJCJKuJIarvQqvr6TaDTdTNY0DjQckdKq4MX3mJQkGI3LI",
            "http://lh4.ggpht.com/BkE3y4c_8D3gTl2k6dLfmnKWha5e45XnKZP8NJ4pG4pXUb_J19_QBrxrthRk1A",
    };

    public static final String[] SEATTLE = {
            "http://lh5.ggpht.com/x7wYMKpb2CGWK7Gqck0kSAX9uOHVvduWBresMNwtlokpVzOwQ9lOfEXsZgM75Gti",
            "http://lh4.ggpht.com/4kPg4fdvMJgnIgfFfCso_Ui8BHaVibW4VQb6vYq2WuUXJ7g_xU-qmUYJoZ1Y6w",
            "http://lh5.ggpht.com/_QetNT7lfWzeWXZArk0y-kq3ZQCps9l4PZK9G2UxHXLMiK_nDEKDGhmQvqbYBs0",
            "http://lh3.ggpht.com/3ctptPGg5pMe2gjbqNpDX8ZzgrSz1HWa0YfyshLsom8XAvFQJJtsJZIuB_uqyQ",
    };

    public static final String[] NEW_YORK = {
            "http://lh6.ggpht.com/40TWudSUSlS-D-RNu0ZQRlOrifj7K3C-hKKwstSjYsNjvWGkVQ_-QPRU5b76XAI",
            "http://lh4.ggpht.com/OONnhdgrhh1x14gmAeHL6joB09j0Yn7ycEgSx2Dk75cEfZFVDeQp_hFHMkZxeWw",
            "http://lh5.ggpht.com/qhVIITlUyAKh0l1fy1sIA32b0vnj-g5n07vhnhFqD2YovgXST5N4up4Mtb0Aa4Y",
            "http://lh6.ggpht.com/dq1rtn76Xertxh17J02UknLxRUgLX04f6dXjeTctgEnHudDHro_ViIJ1F68fyu3z",
    };

    public static final String[] CHICAGO = {
            "http://lh4.ggpht.com/HkTiPVXRlpbfe8FDJz6jlLW7kVNL6lJo-PaedbBq6HcagzCjLNt6lQ3OgDWRBI8",
            "http://lh4.ggpht.com/XKhGtSHgY06OVIHNVxMfvvcKhWGOI7qhLbSmOJ9p58GA-v7xCFvhHCyWIXm_dg",
            "http://lh3.ggpht.com/Fc15BNNdT8-aBORa8THhrf4dF6a-mslmwd3pXu0-sqvQrynDeD0i17K0eAX4a2E",
            "http://lh6.ggpht.com/RlSDbCW2Fs5fHEzUXbVkAhjtzyaULSxG1zsUz0OmTgajiIL_cBMk4jLYnPxnB0cP",
    };

    public static final String[] BEACH = {
            "http://lh4.ggpht.com/IjAj-JLbrX9CtwOXvR5R94zWVd_-De7yb-KSSCauKFjL7fc7u05lh4KxlABhT1g",
            "http://lh5.ggpht.com/dhrhITD19-MNErPWdzr9s7gD3Dw6XdbqemQ9lFsXDDHwRgTBrRN2aAygtbjcWpY",
            "http://lh6.ggpht.com/E8ubHH8XziIK5t3ou9uj1xpq8XKNGKL-OIha6rnlhq1qCXPOvrI1eXqAn7z5A-A",
            "http://lh4.ggpht.com/83jTpNKw0R4OW3hecAeo_9D56xY1c9o3a-opn8e8j0UG8lHy6vE8x2Ocum1GctE",
    };

    public static final String[] TAHOE = {
            "http://lh6.ggpht.com/iM89ChxsvNmC7ajPUgbkZgHuuD6JB9xtUbmh5ov1hDZqSDDZNCpByENjjiv9WYq2",
            "http://lh5.ggpht.com/dhmfa5_k78ytD0v71S7EMyFN13MOQMzaLFwUVNCNsNXkjcRfgshLhyuBo3IRpQ",
            "http://lh5.ggpht.com/dswhgQX4GaEif9sy__jfVdf1vevCkdDHPmxV7Nu3JGu15aApntn3lYIkq_oY",
            "http://lh4.ggpht.com/2oiPv-lr_VLFIHKa1KEKbzlVs1LGZukNfu0BWVzXPUZuG6bMfwTWtaVsvKFJkZUu",
    };

    public static final String[] BERLIN = {
            "http://lh5.ggpht.com/8ubzNsDLQGQ8fYot0UcT0pWeqXPe_Z3jCpm398FuzE8ulYy5ysUXkVnUkwbFof8t",
            "http://lh3.ggpht.com/q7AvSpLNCAOjlQAzPyh73cJhUewNo6-fznEvQxP07U7Z7IzuV53oZG5Lyh4Mkw",
            "http://lh6.ggpht.com/-30idiQtgOyr5KF7mYdV8cc85SNqLFcgBzT_UQCBKCQcGg7LhTfiATkNiToVYg",
            "http://lh4.ggpht.com/2JEs07oznuuvgcgzgjAxRSskgqj3LI9gpATJuoYU58R4h5Yg4EmEfWy3QVQdXwQ",
    };

    public static final String[] AUSTIN = {
            "http://lh4.ggpht.com/zAQwPdGWDc8a11nnSPM9peFsPHHzXxa0v1BGttsWfFy4g-kUXipz0zvcrXuGsKE",
            "http://lh4.ggpht.com/GB25hdlTR-h07vrrB5XRNyQMfPxpTmP9DRu7DW2iTjj4_B0ywcbulk4MX5tmeQ",
            "http://lh5.ggpht.com/U7M2euecz7uMJg1KA_jhcJcCXg4ZQ7ZZmQIr2aUingqFm060K1FxZu2md6zfvF-T",
            "http://lh5.ggpht.com/weQrxcZZp-Ut00kjauDe_cE4lbwtjwU__Xv-XtrXMBbw2lLwpb6dsevmW86MWw",
    };

    public static final String[][] ALL_LOCATION = {
            DEFAULT, AUSTIN, BEACH, BERLIN, CHICAGO, GREAT_PLAINS, LONDO, NEW_YORK, SAN_FRANCISCO,
            SEATTLE, TAHOE
    };

    public static int getLocationIndex() {
        final Random random;
        if (BuildConfig.DEBUG) {
            random = new Random();
        } else {
            final long hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
            random = new Random(hours);
        }
        return random.nextInt(ALL_LOCATION.length);
    }

    public static int getTimeIndex() {
        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour < 4) {
            return INDEX_NIGHT;
        } else if (hour < 7) {
            return INDEX_DAWN;
        } else if (hour < 17) {
            return INDEX_DAY;
        } else if (hour < 20) {
            return INDEX_DUSK;
        } else {
            return INDEX_NIGHT;
        }
    }
}
