package com.czbix.v2ex.util

import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.Reader
import java.lang.reflect.Field
import java.util.regex.Pattern

private class AndroidFieldNamingStrategy : FieldNamingStrategy {
    override fun translateName(f: Field): String {
        if (f.name.startsWith("m")) {
            return handleWords(f.name.substring(1))
        } else {
            return f.name
        }
    }

    private fun handleWords(fieldName: String): String {
        val words = UPPERCASE_PATTERN.split(fieldName)
        val sb = StringBuilder()
        for (word in words) {
            if (sb.length > 0) {
                sb.append(JSON_WORD_DELIMITER)
            }
            sb.append(word.toLowerCase())
        }
        return sb.toString()
    }

    companion object {
        private val JSON_WORD_DELIMITER = "_"

        private val UPPERCASE_PATTERN = Pattern.compile("(?=\\p{Lu})")
    }
}


val GSON: Gson = GsonBuilder().apply {
    setFieldNamingStrategy(AndroidFieldNamingStrategy())
}.create()

inline fun <reified T : Any> String.fromJson(): T {
    return GSON.fromJson(this, T::class.java)
}

@Suppress("UNUSED_PARAMETER")
inline fun <reified T : Any> String.fromJson(isGenericType: Boolean): T {
    return GSON.fromJson(this, object : TypeToken<T>(){}.type)
}

inline fun <reified T : Any> Reader.fromJson(): T {
    return GSON.fromJson(this, T::class.java)
}

@Suppress("UNUSED_PARAMETER")
inline fun <reified T : Any> Reader.fromJson(isGenericType: Boolean): T {
    return GSON.fromJson(this, object : TypeToken<T>(){}.type)
}

inline fun <reified T : Any> T.toJson(): String {
    return GSON.toJson(this, T::class.java)
}

@Suppress("UNUSED_PARAMETER")
inline fun <reified T : Any> T.toJson(isGenericType: Boolean): String {
    return GSON.toJson(this, object : TypeToken<T>(){}.type)
}
