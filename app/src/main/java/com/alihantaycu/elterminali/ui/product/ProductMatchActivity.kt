import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alihantaycu.elterminali.data.entity.Product
import com.google.zxing.integration.android.IntentIntegrator
import androidx.lifecycle.lifecycleScope
import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.database.AppDatabase
import com.alihantaycu.elterminali.databinding.ActivityProductMatchBinding
import kotlinx.coroutines.launch


class ProductMatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductMatchBinding
    private lateinit var productDao: ProductDao
    private var scannedProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        productDao = database.productDao()

        setupUI()
    }

    private fun setupUI() {
        binding.scanProductButton.setOnClickListener {
            startProductScan()
        }

        binding.scanBoxButton.setOnClickListener {
            startBoxScan()
        }
    }

    private fun startProductScan() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("Ürün barkodunu veya QR kodunu okutun")
            initiateScan()
        }
    }

    private fun startBoxScan() {
        if (scannedProduct == null) {
            Toast.makeText(this, "Önce ürün okutun", Toast.LENGTH_SHORT).show()
            return
        }

        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Kutu QR kodunu okutun")
            initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Tarama iptal edildi", Toast.LENGTH_SHORT).show()
            } else {
                processScannedResult(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processScannedResult(content: String) {
        if (scannedProduct == null) {
            // İlk tarama: Ürün
            processProductScan(content)
        } else {
            // İkinci tarama: Kutu
            processBoxScan(content)
        }
    }

    private fun processProductScan(content: String) {
        lifecycleScope.launch {
            // IMEI'ye göre ürünü bul
            val product = productDao.findByImei(content)
            if (product != null) {
                scannedProduct = product
                binding.productInfoText.text = "Ürün: ${product.name} (IMEI: ${product.imei})"
                binding.scanBoxButton.isEnabled = true
            } else {
                Toast.makeText(this@ProductMatchActivity,
                    "Ürün bulunamadı",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processBoxScan(content: String) {
        try {
            // RFID kutu QR kod formatı: RFID:0000000000000016
            val rfid = content.substringAfter("RFID:")
            binding.boxInfoText.text = "Kutu RFID: $rfid"

            lifecycleScope.launch {
                matchProductWithBox(scannedProduct!!, rfid)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Geçersiz kutu QR kodu", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun matchProductWithBox(product: Product, boxRfid: String) {
        try {
            val updatedProduct = product.copy(
                matchedBoxRfid = boxRfid,
                status = "MATCHED"
            )
            productDao.updateProduct(updatedProduct)

            Toast.makeText(this@ProductMatchActivity,
                "Eşleştirme başarılı",
                Toast.LENGTH_SHORT).show()

            finish()
        } catch (e: Exception) {
            Toast.makeText(this@ProductMatchActivity,
                "Eşleştirme hatası: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }
}