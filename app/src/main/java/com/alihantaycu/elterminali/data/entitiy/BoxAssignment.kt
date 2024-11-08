import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "box_assignments")
data class BoxAssignment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val boxRfid: String,  // Kutunun RFID'si
    val productImei: String?, // İçindeki ürünün IMEI'si (null olabilir - boş kutu)
    val currentLocation: String, // Mal Kabul, Depo-A1, Teknisyen-1, Kalite-2 gibi
    val status: String, // RECEIVED, IN_STORAGE, AT_TECHNICIAN, IN_QC, COMPLETED gibi
    val assignedWorkerId: String?, // Atanan teknisyen/çalışan ID'si
    val createdDate: String,
    val lastUpdatedDate: String
)