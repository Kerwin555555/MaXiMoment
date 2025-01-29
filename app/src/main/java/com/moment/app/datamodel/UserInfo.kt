package com.moment.app.datamodel

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.moment.app.utils.BaseBean
import java.lang.reflect.Type

const val INITIAL_INFO = 0
const val SECOND_INFO = 1
const val FINISHED_INFO = 2

data class UserInfo(
    var user_id: String? = "",
    var gender: String? = "",
    var register_status: Int? = 0, //第一次只login的话register status是0，第一个页面的信息提交完就是1，第二个页面提交完是2
    var birthday: String? = "",
    var age: Int? = 0,
    var nickname: String? = "MomentFan",
    var session: String? = "",
    var bio: String? = "",
    var country: String? = "",
    var forbidden_session: String? ="",
    var huanxin:HuanxinBean? = HuanxinBean(),
    var followed: Boolean? = false,
    var say_hi: Boolean? = false,
    var avatar: String? = null,
    var imagesWallList: MutableList<String> = mutableListOf(),
    var friends_count: Int? = 0,
    var following_count: Int? = 0,
    var follower_count: Int? = 0,
): BaseBean() {
}
class HuanxinBean : BaseBean() {
    /**
     * password : b7702c283c823038ce4776cf9a3735fb
     * user_id : love1236383185000000731
     */
    var password: String? = ""
    var user_id: String? = ""
}


class UserSettings : BaseBean() {
    //var allowCrossRegionMatch: Boolean = true //默认勾选
}

class LoginSessionResult : BaseBean() {
    var session: String? = null
    var user_info: UserInfo? = null
}


data class UpdateInfoResult(
    var update_info_loc: Boolean = false,
    var show_loc_as_region: Boolean = false,
    var loc_list: List<String> = emptyList(),
    var updated_info: UserInfo?,
    var register_prefer_select: Boolean = false,
//    var user_settings: UserSift?
): BaseBean()



class GsonDefaultAdapterFactory: TypeAdapterFactory {
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.type == String::class.java) {
            return createStringAdapter()
        }
        if (type.rawType == List::class.java || type.rawType == Collection::class.java) {
            return createCollectionAdapter(type, gson)
        }
        return null
    }
    /**
     * null替换成空List
     */
    private fun <T : Any> createCollectionAdapter(
        type: TypeToken<T>,
        gson: Gson
    ): TypeAdapter<T>? {
        val rawType = type.rawType
        if (!Collection::class.java.isAssignableFrom(rawType)) {
            return null
        }
        val elementType: Type = `$Gson$Types`.getCollectionElementType(type.type, rawType)
        val elementTypeAdapter: TypeAdapter<Any> =
            gson.getAdapter(TypeToken.get(elementType)) as TypeAdapter<Any>
        return object : TypeAdapter<Collection<Any>>() {
            override fun write(writer: JsonWriter, value: Collection<Any>?) {
                writer.beginArray()
                value?.forEach {
                    elementTypeAdapter.write(writer, it)
                }
                writer.endArray()
            }
            override fun read(reader: JsonReader): Collection<Any> {
                val list = mutableListOf<Any>()
                // null替换为空list
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    return list
                }
                reader.beginArray()
                while (reader.hasNext()) {
                    val element = elementTypeAdapter.read(reader)
                    list.add(element)
                }
                reader.endArray()
                return list
            }
        } as TypeAdapter<T>
    }
    /**
     * null 替换成空字符串
     */
    private fun <T : Any> createStringAdapter(): TypeAdapter<T> {
        return object : TypeAdapter<String>() {
            override fun write(writer: JsonWriter, value: String?) {
                if (value == null) {
                    writer.value("")
                } else {
                    writer.value(value)
                }
            }
            override fun read(reader: JsonReader): String {
                // null替换为""
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    return ""
                }
                return reader.nextString()
            }
        } as TypeAdapter<T>
    }
}