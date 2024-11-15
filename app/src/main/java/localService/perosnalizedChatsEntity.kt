package localService

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class PersonalizedChatsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val mssId: String,
    val senderId: String,
    val message: String,
    val liked: Int,
    val sentAt: Date,
    val type: String
)
