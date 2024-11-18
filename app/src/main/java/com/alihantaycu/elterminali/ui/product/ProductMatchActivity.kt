package com.alihantaycu.elterminali.ui.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var selectedProductsAdapter: SelectedProductsAdapter

    private var isBatchMode = false
    private val selectedProducts = mutableListOf<Product>()
    private var selectedBox: String? = null

    private fun setupRecyclerView() {
        selectedProductsAdapter = SelectedProductsAdapter(
            products = emptyList(),
            onRemoveClick = { product ->
                selectedProducts.remove(product)
                updateSelectedProductsList()
                binding.scanProductButton.text = "Ürün Ekle (${selectedProducts.size})"
            }
        )

        binding.selectedProductsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductMatchActivity)
            adapter = selectedProductsAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        productDao = database.productDao()

        // Toolbar ayarları
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)  // Geri butonu
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)  // Varsayılan başlığı gizle
        }
        // Toolbar text ayarları
        binding.toolbarTitle.text = "Ürün-Kutu Eşleştirme"
        binding.toolbarSubtitle.text = "Ana Sayfa > Ürünler > Eşleştirme"

        setupUI()
        setupBatchMode() // Yeni eklenen
    }

    private fun setupBatchMode() {
        binding.batchModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isBatchMode = isChecked
            updateUIForBatchMode(isChecked)
        }

        binding.matchAllButton.setOnClickListener {
            if (selectedBox != null) {
                lifecycleScope.launch {
                    matchMultipleProducts()
                }
            } else {
                Toast.makeText(this, "Önce kutu okutun", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUIForBatchMode(isBatchMode: Boolean) {
        binding.selectedProductsRecyclerView.isVisible = isBatchMode
        binding.matchAllButton.isVisible = isBatchMode

        if (isBatchMode) {
            binding.scanProductButton.text = "Ürün Ekle (${selectedProducts.size})"
            binding.scanBoxButton.isEnabled = true
        } else {
            binding.scanProductButton.text = "Ürün QR Okut"
            binding.scanBoxButton.isEnabled = scannedProduct != null
            selectedProducts.clear()
            selectedBox = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()  // Activity'yi kapat
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

            var product = productDao.findByImei(imei)

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

            if (isBatchMode) {
                // Toplu modda ürünü listeye ekle
                selectedProducts.add(product)
                updateSelectedProductsList()
                binding.scanProductButton.text = "Ürün Ekle (${selectedProducts.size})"
                Toast.makeText(this, "Ürün listeye eklendi: ${product.name}", Toast.LENGTH_SHORT).show()
            } else {
                // Normal mod
                scannedProduct = product
                binding.productInfoText.text = "Ürün: ${product.name} (IMEI: ${product.imei})"
                binding.scanBoxButton.isEnabled = true
                Toast.makeText(this, "Ürün okutuldu: ${product.name}", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "QR kod formatı hatalı", Toast.LENGTH_SHORT).show()
            throw e
        }
    }

    private suspend fun processBoxScan(content: String) {
        try {
            val boxRfid = content.substringAfter("RFID:")

            if (isBatchMode) {
                selectedBox = boxRfid
                binding.boxInfoText.text = "Kutu: $boxRfid"
                binding.matchAllButton.isEnabled = true
                Toast.makeText(this, "Kutu seçildi. Eşleştirmeye hazır.", Toast.LENGTH_SHORT).show()
            } else {
                scannedProduct?.let { product ->
                    val updatedProduct = product.copy(
                        matchedBoxRfid = boxRfid,
                        status = "MATCHED"
                    )
                    productDao.updateProduct(updatedProduct)
                    binding.boxInfoText.text = "Kutu: $boxRfid"
                    Toast.makeText(this, "Eşleştirme başarılı", Toast.LENGTH_SHORT).show()
                    scannedProduct = null
                    binding.scanBoxButton.isEnabled = false
                    finish()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Kutu QR kodu hatalı", Toast.LENGTH_SHORT).show()
            throw e
        }
    }

    private suspend fun matchMultipleProducts() {
        val boxRfid = selectedBox ?: return

        try {
            selectedProducts.forEach { product ->
                val updatedProduct = product.copy(
                    matchedBoxRfid = boxRfid,
                    status = "MATCHED"
                )
                productDao.updateProduct(updatedProduct)
            }

            Toast.makeText(this,
                "${selectedProducts.size} ürün eşleştirildi",
                Toast.LENGTH_SHORT).show()

            // Temizlik
            selectedProducts.clear()
            selectedBox = null
            updateUIForBatchMode(false)
            binding.batchModeSwitch.isChecked = false
            finish()

        } catch (e: Exception) {
            Toast.makeText(this,
                "Eşleştirme hatası: ${e.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun updateSelectedProductsList() {
        selectedProductsAdapter.updateProducts(selectedProducts)
    }
}