package com.moment.app.main_chat

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.moment.app.MomentApp
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_chat.fragments.entities.MomentConversation
import com.moment.app.utils.JsonUtil

@Database(
    entities = [MomentConversation::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ConversationDatabase() : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}

object Converters {
    @TypeConverter
    fun fromString(value: String?): UserInfo? {
        if (value == null) return null

        return JsonUtil.parse(value, UserInfo::class.java)
    }

    @TypeConverter
    fun toString(value: UserInfo?): String? {
        if (value == null) return null
        return JsonUtil.toJson(value)
    }
}


/**
 * 在 Android 开发中，特别是使用 Room 数据库时，@TypeConverters 注解用于指定一个或多个类型转换器
 * （TypeConverter）类。这些转换器类负责将不支持直接存储在数据库中的数据类型（如自定义对象、枚举、复杂数据结构等）转换为可以存储的类型（如字符串、整数等），以及在查询数据库时将存储的类型转换回原始类型。
 *
 * @TypeConverters({Converters.class}) 这一行代码的意思是，你有一个名为 Converters 的类（或者多个类，用逗号分隔），里面定义了一个或多个类型转换方法，这些方法会被 Room 用来处理数据类型的转换。
 *
 * 下面是一个简单的例子来说明 Converters 类如何工作：
 *
 * ‌定义转换器类‌：
 *
 * java
 * Copy Code
 * public class Converters {
 *     @TypeConverter
 *     public static String fromDate(Date date) {
 *         return new SimpleDateFormat("yyyy-MM-dd").format(date);
 *     }
 *
 *     @TypeConverter
 *     public static Date toDate(String dateString) {
 *         return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
 *     }
 * }
 * 在这个例子中，Converters 类包含两个方法：一个用于将 Date 类型转换为 String 类型，另一个用于将 String 类型转换回 Date 类型。
 *
 * ‌在数据库抽象类上使用 @TypeConverters‌：
 *
 * java
 * Copy Code
 * @Database(entities = {YourEntity.class}, version = 1)
 * @TypeConverters({Converters.class})
 * public abstract class YourDatabase extends RoomDatabase {
 *     // ... 数据库相关的代码
 * }
 * 通过在数据库抽象类上添加 @TypeConverters 注解，并指定 Converters 类，Room 就能知道在需要处理 Date 类型时应该调用哪个转换方法。
 *
 * ‌在实体类中使用转换的类型‌：
 *
 * java
 * Copy Code
 * @Entity
 * public class YourEntity {
 *     @PrimaryKey
 *     public int id;
 *
 *     public Date yourDate;
 *
 *     // 其他字段和方法
 * }
 * 现在，当你在 YourEntity 实体类中使用 Date 类型时，Room 会自动调用 Converters 类中的方法来处理数据的存储和检索。
 *
 * 总之，@TypeConverters 注解在 Room 数据库中的作用是指定类型转换器的类，这些转换器用于在数据库存储和 Java 对象之间进行数据类型的转换。
 */