package com.example.myapplication.booking.util

import com.google.gson.Gson

object JsonConverter {
    private val gson = Gson()

    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        return try {
            gson.fromJson(json, clazz)
        } catch (_: Exception) {
            null
        }
    }

    fun toJson(value: Any): String? {
        return try {
            gson.toJson(value)
        } catch (_: Exception) {
            null
        }
    }
}
