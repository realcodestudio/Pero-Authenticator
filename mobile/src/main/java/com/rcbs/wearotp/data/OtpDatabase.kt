package com.rcbs.wearotp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context

@Database(
    entities = [OtpAccount::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OtpDatabase : RoomDatabase() {
    abstract fun otpDao(): OtpDao

    companion object {
        @Volatile
        private var INSTANCE: OtpDatabase? = null

        fun getDatabase(context: Context): OtpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OtpDatabase::class.java,
                    "otp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromOtpType(type: OtpType): String = type.name

    @TypeConverter
    fun toOtpType(type: String): OtpType = OtpType.valueOf(type)
}