import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxAssignmentDao {
    @Query("SELECT * FROM box_assignments ORDER BY lastUpdatedDate DESC")
    fun getAllBoxAssignments(): Flow<List<BoxAssignment>>

    @Query("SELECT * FROM box_assignments WHERE boxRfid = :rfid")
    suspend fun getBoxByRfid(rfid: String): BoxAssignment?

    @Query("SELECT * FROM box_assignments WHERE productImei = :imei")
    suspend fun getBoxByProductImei(imei: String): BoxAssignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoxAssignment(boxAssignment: BoxAssignment)

    @Update
    suspend fun updateBoxAssignment(boxAssignment: BoxAssignment)

    @Delete
    suspend fun deleteBoxAssignment(boxAssignment: BoxAssignment)

    @Query("UPDATE box_assignments SET currentLocation = :newLocation, lastUpdatedDate = :updateDate WHERE boxRfid = :rfid")
    suspend fun updateBoxLocation(rfid: String, newLocation: String, updateDate: String)

    @Query("UPDATE box_assignments SET status = :newStatus, lastUpdatedDate = :updateDate WHERE boxRfid = :rfid")
    suspend fun updateBoxStatus(rfid: String, newStatus: String, updateDate: String)

    @Query("UPDATE box_assignments SET productImei = NULL, status = 'EMPTY', lastUpdatedDate = :updateDate WHERE boxRfid = :rfid")
    suspend fun clearBox(rfid: String, updateDate: String)
}