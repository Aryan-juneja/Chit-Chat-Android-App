package localService

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chats")
data class chatsEntityClass(
    @PrimaryKey(autoGenerate = false) // Set to false if you want to use custom IDs
    val id: String, // Consider using a combination of fields or a unique identifier
    val name: String,
    val image: String,
    val message: String,
    val from: String,
    val time: Date,
    val count: Int
)

