import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxAssignmentDao {

    @Query("SELECT * FROM box_assignments WHERE boxRfid = :rfid")
    suspend fun getBoxByRfid(rfid: String): BoxAssignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoxAssignment(boxAssignment: BoxAssignment)

    @Update
    suspend fun updateBoxAssignment(boxAssignment: BoxAssignment)
}
