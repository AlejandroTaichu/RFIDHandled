import com.alihantaycu.elterminali.data.entity.Product


data class BatchItem(
    val product: Product,
    val boxRfid: String,
    val timestamp: Long = System.currentTimeMillis()

)
