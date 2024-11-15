package localService

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PersonalizedChatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalizedChat(personalizedChat: PersonalizedChatsEntity)

    @Query("DELETE FROM personalizedChatsEntity")
    suspend fun deleteAllPersonalizedChats()

    @Query("SELECT * FROM personalizedChatsEntity WHERE `mssId` =:id ORDER BY `sentAt` ASC")
    suspend fun getAllPersonalizedChats(id: String): List<PersonalizedChatsEntity>
}

