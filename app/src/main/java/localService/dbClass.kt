package localService

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [chatsEntityClass::class,PersonalizedChatsEntity::class], version = 3)
@TypeConverters(DateTypeConverter::class)
abstract  class dbClass: RoomDatabase() {
    abstract fun chatsDao(): chatsDaoClass
    abstract fun personalizedChatsDao(): PersonalizedChatsDao
    companion object{
        @Volatile
        private var INSTANCE: dbClass? = null
        fun getDatabase(context: Context): dbClass {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    dbClass::class.java,
                    "chats"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}