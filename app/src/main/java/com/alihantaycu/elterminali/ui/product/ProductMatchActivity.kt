package com.alihantaycu.elterminali.ui.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.database.AppDatabase
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityProductMatchBinding
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
            setPrompt("Ürün QR kodunu okutun")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
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
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Tarama iptal edildi", Toast.LENGTH_LONG).show()
            } else {
                lifecycleScope.launch {
                    try {
                        val scannedContent = result.contents
                        Log.d("ProductMatch", "Okunan içerik: $scannedContent")

                        if (scannedProduct == null) {
                            processProductScan(scannedContent)
                        } else {
                            processBoxScan(scannedContent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ProductMatchActivity,
                            "Hata: ${e.message}",
                            Toast.LENGTH_LONG).show()
                        Log.e("ProductMatch", "Hata: ${e.message}", e)
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private suspend fun processProductScan(content: String) {
        try {
            val parts = content.split("|")
            val rfid = parts[0].substringAfter("RFID:")
            val imei = parts[1].substringAfter("IMEI:")
            val name = parts[2].substringAfter("NAME:")

            // Önce ürünü ara
            var product = productDao.findByImei(imei)

            // Ürün yoksa yeni oluştur
            if (product == null) {
                product = Product(
                    id = UUID.randomUUID().toString(),
                    rfidTag = rfid,
                    imei = imei,
                    name = name,
                    location = "Gelen Kargo Depo",
                    address = "",
                    createdDate = getCurrentDate(),
                    status = "NEW"
                )
                productDao.insertProduct(product)
            }

            scannedProduct = product
            binding.productInfoText.text = "Ürün: ${product.name} (IMEI: ${product.imei})"
            binding.scanBoxButton.isEnabled = true

            Toast.makeText(this, "Ürün okutuldu: ${product.name}", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "QR kod formatı hatalı", Toast.LENGTH_SHORT).show()
            throw e
        }
    }

    private suspend fun processBoxScan(content: String) {
        try {
            val boxRfid = content.substringAfter("RFID:")

            scannedProduct?.let { product ->
                val updatedProduct = product.copy(
                    matchedBoxRfid = boxRfid,
                    status = "MATCHED"
                )
                productDao.updateProduct(updatedProduct)

                binding.boxInfoText.text = "Kutu: $boxRfid"
                Toast.makeText(this, "Eşleştirme başarılı", Toast.LENGTH_SHORT).show()

                // Reset ve ana ekrana dön
                scannedProduct = null
                binding.scanBoxButton.isEnabled = false
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Kutu QR kodu hatalı", Toast.LENGTH_SHORT).show()
            throw e
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }
}