import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.data.enums.BoxStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WorkflowManager(
    private val boxAssignmentDao: BoxAssignmentDao,
    private val productDao: ProductDao
) {
    private var currentBox: BoxAssignment? = null
    private var currentProduct: Product? = null
    private var currentLocation: String? = null
    var currentStep: WorkflowStep = WorkflowStep.IDLE

    enum class WorkflowStep {
        IDLE,               // Başlangıç durumu
        SCAN_PRODUCT,       // Ürün QR/Barkod tarama
        SCAN_BOX,          // Kutu QR tarama
        SCAN_LOCATION,     // Lokasyon QR tarama
        COMPLETED          // İşlem tamamlandı
    }

    suspend fun processQrScan(content: String) {
        when (currentStep) {
            WorkflowStep.IDLE -> {
                // İlk tarama - ürün veya kutu olabilir
                checkInitialScan(content)
            }
            WorkflowStep.SCAN_PRODUCT -> {
                processProductScan(content)
            }
            WorkflowStep.SCAN_BOX -> {
                processBoxScan(content)
            }
            WorkflowStep.SCAN_LOCATION -> {
                processLocationScan(content)
            }
            WorkflowStep.COMPLETED -> {
                // İşlem zaten tamamlandı, yeni işlem başlatılmalı
                reset()
            }
        }
    }

    private suspend fun checkInitialScan(content: String) {
        // Önce ürün olarak kontrol et
        val product = productDao.getProductByImei(content)
        if (product != null) {
            currentProduct = product
            currentStep = WorkflowStep.SCAN_BOX
            return
        }

        // Ürün değilse, kutu olarak kontrol et
        val box = boxAssignmentDao.getBoxByRfid(content)
        if (box != null) {
            currentBox = box
            currentStep = if (box.productImei == null) {
                WorkflowStep.SCAN_PRODUCT
            } else {
                WorkflowStep.SCAN_LOCATION
            }
            return
        }

        throw IllegalArgumentException("Geçersiz QR kod")
    }

    private suspend fun processProductScan(content: String) {
        val product = productDao.getProductByImei(content)
            ?: throw IllegalArgumentException("Ürün bulunamadı")

        currentProduct = product
        currentStep = WorkflowStep.SCAN_LOCATION
    }

    private suspend fun processBoxScan(content: String) {
        val box = boxAssignmentDao.getBoxByRfid(content)

        if (box == null) {
            // Yeni kutu oluştur
            currentBox = BoxAssignment(
                boxRfid = content,
                productImei = currentProduct?.imei,
                currentLocation = "MAL_KABUL",
                status = BoxStatus.RECEIVED.name,
                assignedWorkerId = null,
                createdDate = getCurrentDate(),
                lastUpdatedDate = getCurrentDate()
            )
            boxAssignmentDao.insertBoxAssignment(currentBox!!)
        } else {
            if (box.productImei != null) {
                throw IllegalStateException("Bu kutu zaten kullanımda")
            }
            currentBox = box
        }

        currentStep = WorkflowStep.SCAN_LOCATION
    }

    private suspend fun processLocationScan(content: String) {
        currentLocation = content

        // Mevcut kutuyu güncelle
        currentBox?.let { box ->
            val updatedBox = box.copy(
                currentLocation = content,
                lastUpdatedDate = getCurrentDate()
            )
            boxAssignmentDao.updateBoxAssignment(updatedBox)
        }

        currentStep = WorkflowStep.COMPLETED
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    fun reset() {
        currentBox = null
        currentProduct = null
        currentLocation = null
        currentStep = WorkflowStep.IDLE
    }

    fun getCurrentStepMessage(): String {
        return when (currentStep) {
            WorkflowStep.IDLE -> "QR kod okutun"
            WorkflowStep.SCAN_PRODUCT -> "Ürün QR kodunu okutun"
            WorkflowStep.SCAN_BOX -> "Kutu QR kodunu okutun"
            WorkflowStep.SCAN_LOCATION -> "Lokasyon QR kodunu okutun"
            WorkflowStep.COMPLETED -> "İşlem tamamlandı"
        }
    }
}