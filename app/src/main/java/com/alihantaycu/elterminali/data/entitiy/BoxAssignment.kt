import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "box_assignments")
data class BoxAssignment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val boxRfid: String,
    val productImei: String?,
    val currentLocation: String,
    val status: String,
    val assignedWorkerId: String?,
    val createdDate: String,
    val lastUpdatedDate: String,
    val quantity: Int = 1,         // Yeni: Kutu içindeki adet
    val boxType: String = "SINGLE" // Yeni: SINGLE veya MULTIPLE
)