package com.alihantaycu.elterminali.ui.match.activity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.database.AppDatabase
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.PartsMatchActivityBinding
import com.alihantaycu.elterminali.ui.match.adapter.MatchedItemsAdapter
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PartsMatchActivity : AppCompatActivity() {
    private lateinit var binding: PartsMatchActivityBinding
    private lateinit var productDao: ProductDao
    private var operationType: String = "PRODUCT" // veya "SPARE_PART"
    private var isRemoveMode = false
    private var currentBox: String? = null
    private val scannedItems = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            binding = PartsMatchActivityBinding.inflate(layoutInflater)
            setContentView(binding.root)

            operationType = intent.getStringExtra("operation_type") ?: "PRODUCT"

            val database = AppDatabase.getDatabase(this)
            productDao = database.productDao()

            setupToolbar()
            setupUI()
            setupRecyclerView()
            setupListeners()
        } catch (e: Exception) {
            Log.e("ProductMatchOperation", "onCreate'de hata: ${e.message}")
            Toast.makeText(this, "Aktivite başlatılırken hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        val adapter = MatchedItemsAdapter(
            onRemoveClick = { product ->
                scannedItems.remove(product)
                (binding.scannedItemsRecyclerView.adapter as? MatchedItemsAdapter)?.removeItem(product)
                binding.completeButton.isEnabled = scannedItems.isNotEmpty()
                updateUIState()
            }
        )

        binding.scannedItemsRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@PartsMatchActivity)
        }
    }

    private fun updateRecyclerView() {
        (binding.scannedItemsRecyclerView.adapter as? MatchedItemsAdapter)?.updateItems(scannedItems)
    }

    private fun startQRScan() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt(when {
                currentBox == null -> "Kutu QR kodunu okutun"
                operationType == "PRODUCT" -> "${if (isRemoveMode) "Çıkarılacak" else "Eklenecek"} ürünün QR kodunu okutun"
                else -> "${if (isRemoveMode) "Çıkarılacak" else "Eklenecek"} yedek parçanın QR kodunu okutun"
            })
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                lifecycleScope.launch {
                    processQRResult(result.contents)
                }
            } else {
                Toast.makeText(this, "Tarama iptal edildi", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private suspend fun processQRResult(content: String) {
        try {
            if (currentBox == null) {
                // Kutu okutma
                val boxRfid = content.substringAfter("RFID:")
                currentBox = boxRfid
                binding.scanInfoText.text = "Kutu: $boxRfid"
                updateUIState()
                Toast.makeText(this, "Kutu seçildi", Toast.LENGTH_SHORT).show()
            } else {
                // Ürün/Parça okutma
                val parts = content.split("|")
                val rfid = parts[0].substringAfter("RFID:")
                val imei = parts[1].substringAfter("IMEI:")
                val name = parts[2].substringAfter("NAME:")

                val existingProduct = productDao.findByImei(imei)

                if (isRemoveMode) {
                    handleRemoveOperation(existingProduct)
                } else {
                    handleAddOperation(existingProduct, rfid, imei, name)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "QR kod formatı hatalı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun handleAddOperation(existingProduct: Product?, rfid: String, imei: String, name: String) {
        if (existingProduct == null) {
            // Yeni ürün oluştur
            val newProduct = Product(
                id = UUID.randomUUID().toString(),
                rfidTag = rfid,
                imei = imei,
                name = name,
                location = if (operationType == "PRODUCT") "Gelen Kargo Depo" else "Yedek Parça Deposu",
                address = "",
                createdDate = getCurrentDate(),
                status = "NEW",
                isSparePartBox = operationType == "SPARE_PART"
            )
            scannedItems.add(newProduct)
            (binding.scannedItemsRecyclerView.adapter as? MatchedItemsAdapter)?.addItem(newProduct)
        } else {
            if ((operationType == "PRODUCT" && existingProduct.matchedBoxRfid != null) ||
                (operationType == "SPARE_PART" && existingProduct.sparePartBoxRfid != null)) {
                Toast.makeText(this, "Bu ürün zaten bir kutuya atanmış!", Toast.LENGTH_SHORT).show()
                return
            }
            scannedItems.add(existingProduct)
            (binding.scannedItemsRecyclerView.adapter as? MatchedItemsAdapter)?.addItem(existingProduct)
        }
        updateUIState()
        binding.completeButton.isEnabled = scannedItems.isNotEmpty()
    }

    private suspend fun handleRemoveOperation(existingProduct: Product?) {
        if (existingProduct == null) {
            Toast.makeText(this, "Ürün bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        val boxRfid = if (operationType == "PRODUCT") existingProduct.matchedBoxRfid else existingProduct.sparePartBoxRfid

        if (boxRfid != currentBox) {
            Toast.makeText(this, "Bu ürün seçili kutuya ait değil!", Toast.LENGTH_SHORT).show()
            return
        }

        scannedItems.add(existingProduct)
        (binding.scannedItemsRecyclerView.adapter as? MatchedItemsAdapter)?.addItem(existingProduct)
        updateUIState()
        binding.completeButton.isEnabled = scannedItems.isNotEmpty()
    }

    private suspend fun completeOperation() {
        try {
            scannedItems.forEach { product ->
                val updatedProduct = if (operationType == "PRODUCT") {
                    if (isRemoveMode) {
                        product.copy(
                            matchedBoxRfid = null,
                            status = "REMOVED",
                            removedDate = getCurrentDate()
                        )
                    } else {
                        product.copy(
                            matchedBoxRfid = currentBox,
                            status = "MATCHED"
                        )
                    }
                } else {
                    if (isRemoveMode) {
                        product.copy(
                            sparePartBoxRfid = null,
                            status = "REMOVED",
                            removedDate = getCurrentDate()
                        )
                    } else {
                        product.copy(
                            sparePartBoxRfid = currentBox,
                            status = "MATCHED",
                            isSparePartBox = true
                        )
                    }
                }
                productDao.updateProduct(updatedProduct)
            }

            val itemType = if (operationType == "PRODUCT") "ürün" else "yedek parça"
            val operationText = if (isRemoveMode) "çıkarıldı" else "eklendi"
            Toast.makeText(this, "${scannedItems.size} $itemType $operationText", Toast.LENGTH_SHORT).show()

            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "İşlem hatası: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbarTitle.text = if (operationType == "PRODUCT") {
            "Ürün İşlemleri"
        } else {
            "Yedek Parça İşlemleri"
        }

        binding.toolbarSubtitle.text = "Ana Sayfa > Eşleştirme > ${binding.toolbarTitle.text}"
    }

    private fun setupUI() {
        updateUIState()
    }

    private fun setupListeners() {
        binding.operationModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isRemoveMode = isChecked
            updateUIState()
            resetOperation()
        }

        binding.scanButton.setOnClickListener {
            startQRScan()
        }

        binding.completeButton.setOnClickListener {
            lifecycleScope.launch {
                completeOperation()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetOperation() {
        currentBox = null
        scannedItems.clear()
        updateRecyclerView()
        binding.scanInfoText.text = ""
        binding.completeButton.isEnabled = false
        updateUIState()
    }

    private fun updateUIState() {
        val operationText = if (isRemoveMode) "Çıkarma" else "Eşleştirme"
        val itemType = if (operationType == "PRODUCT") "Ürün" else "Yedek Parça"

        binding.modeInfoText.text = "$itemType $operationText Modu"

        binding.instructionText.text = if (currentBox == null) {
            "Önce kutuyu okutun"
        } else {
            "${if (isRemoveMode) "Çıkarılacak" else "Eklenecek"} ${itemType.lowercase()} okutun"
        }
    }
}