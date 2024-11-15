package localService

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface chatsDaoClass {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: chatsEntityClass)
    @Query("SELECT * FROM chats")
     fun getAllChats(): Flow<List<chatsEntityClass>>
     @Query("DELETE FROM chats ")
     suspend fun deleteAllChats()
     @Query("SELECT * FROM chats WHERE `from` = :from")
     suspend fun getChatsByFrom(from: String):List<chatsEntityClass>
    @Update
    suspend fun updateChat(chat: chatsEntityClass)
}
