package com.moment.app.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParseException

object SerializeManager {
    val gson: Gson = Gson()

    fun <T> parseArray(json: String?, cls: Class<T>?): List<T?>? {
        try {
            val jsonElement = gson.fromJson(
                json,
                JsonArray::class.java
            ) as JsonElement
            if (!jsonElement.isJsonArray) {
                return null
            } else {
                val arrayList: ArrayList<T?> = ArrayList()
                val jsonArray = jsonElement.asJsonArray
                var i = 0

                val count = jsonArray.size()
                while (i < count) {
                    val element = jsonArray[i]
                    val t = gson.fromJson(element, cls)
                    arrayList.add(t)
                    ++i
                }

                return arrayList
            }
        } catch (var9: JsonParseException) {
            return null
        }
    }

    fun toJson(obj: Any?): String {
        return gson.toJson(obj)
    }

    fun <T> parse(json: String?, classOfT: Class<T>?): T {
        return gson.fromJson(json, classOfT)
    }
}
