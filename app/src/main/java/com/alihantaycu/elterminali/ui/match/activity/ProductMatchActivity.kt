package com.alihantaycu.elterminali.ui.match.activity

import MatchOperation
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.database.AppDatabase
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityProductMatchBinding
import com.alihantaycu.elterminali.ui.product.SelectedProductsAdapter
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



class ProductMatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductMatchBinding
    private lateinit var productDao: ProductDao
    private var scannedProduct: Product? = null
    private lateinit var selectedProductsAdapter: SelectedProductsAdapter

    private var isRemoveMode = false
    private var isBatchMode = false
    private val selectedProducts = mutableListOf<Product>()
    private var selectedBox: String? = null
    private lateinit var operation: MatchOperation

    private fun updateModeUI(isRemoveMode: Boolean) {
        binding.apply {
            // Renk tanımları
            val backgroundColor = if (isRemoveMode) {
                ContextCompat.getColor(this@ProductMatchActivity, R.color.yenilio_kırmızısı)
            } else {
                ContextCompat.getColor(this@ProductMatchActivity, R.color.yenililo_turuncusu)
            }

            // Mode description güncelleme
            modeDescription.apply {
                val modeText = if (isRemoveMode) {
                    "❌ ÇIKARMA MODU AKTİF"
                } else {
                    "✅ EKLEME MODU AKTİF"
                }

                text = modeText
                setBackgroundColor(backgroundColor)
                setTextColor(Color.WHITE)
            }

            // Instruction güncelleme
            instructionText1.text = if (isRemoveMode) {
                when (operation) {
                    MatchOperation.REMOVE_PRODUCT -> "Kutudan ürün çıkarma işlemi yapabilirsiniz"
                    MatchOperation.REMOVE_PARTS -> "Kutudan yedek parça çıkarma işlemi yapabilirsiniz"
                    else -> "Çıkarma modu aktif"
                }
            } else {
                when (operation) {
                    MatchOperation.MATCH_PRODUCT -> "Kutuya ürün ekleme işlemi yapabilirsiniz"
                    MatchOperation.ADD_PARTS -> "Kutuya yedek parça ekleme işlemi yapabilirsiniz"
                    else -> "Ekleme modu aktif"
                }
            }

            // Button güncelleme
            scanProductButton.apply {
                backgroundTintList = ColorStateList.valueOf(backgroundColor)
                text = when {
                    selectedBox == null -> "KUTU QR OKUT"
                    isRemoveMode -> if (operation == MatchOperation.REMOVE_PRODUCT) "ÇIKARILACAK ÜRÜNÜ OKUT" else "ÇIKARILACAK PARÇAYI OKUT"
                    else -> if (operation == MatchOperation.MATCH_PRODUCT) "EKLENECEK ÜRÜNÜ OKUT" else "EKLENECEK PARÇAYI OKUT"
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        operation = MatchOperation.valueOf(
            intent.getStringExtra("operation") ?: MatchOperation.MATCH_PRODUCT.name
        )

        val database = AppDatabase.getDatabase(this)
        productDao = database.productDao()

        setupToolbar()
        setupUI()
        setupBatchMode()
        setupRecyclerView()
        setupOperationUI(operation)
        updateModeUI(isRemoveMode)  // Burada çağıralım
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

                        when (operation) {
                            MatchOperation.MATCH_PRODUCT -> {
                                if (scannedProduct == null) {
                                    processProductScan(scannedContent)
                                } else {
                                    processBoxScan(scannedContent)
                                }
                            }
                            MatchOperation.REMOVE_PRODUCT -> {
                                if (selectedBox == null) {
                                    // Kutu okutma
                                    val boxRfid = scannedContent.substringAfter("RFID:")
                                    selectedBox = boxRfid
                                    binding.boxInfoText.text = "Kutu: $boxRfid"
                                    binding.scanProductButton.isEnabled = true
                                    Toast.makeText(this@ProductMatchActivity,
                                        "Kutu seçildi, çıkarılacak ürünü okutun",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    processRemoveProduct(scannedContent)
                                }
                            }
                            MatchOperation.ADD_PARTS -> {
                                if (selectedBox == null) {
                                    // Kutu okutma
                                    val boxRfid = scannedContent.substringAfter("RFID:")
                                    selectedBox = boxRfid
                                    binding.boxInfoText.text = "Kutu: $boxRfid"
                                    binding.scanProductButton.isEnabled = true
                                    Toast.makeText(this@ProductMatchActivity,
                                        "Kutu seçildi, yedek parça okutun",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    processAddSparePart(scannedContent)
                                }
                            }
                            MatchOperation.REMOVE_PARTS -> {
                                if (selectedBox == null) {
                                    // Kutu okutma
                                    val boxRfid = scannedContent.substringAfter("RFID:")
                                    selectedBox = boxRfid
                                    binding.boxInfoText.text = "Kutu: $boxRfid"
                                    binding.scanProductButton.isEnabled = true
                                    Toast.makeText(this@ProductMatchActivity,
                                        "Kutu seçildi, çıkarılacak yedek parçayı okutun",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    processRemoveSparePart(scannedContent)
                                }
                            }
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

    private suspend fun matchMultipleProducts() {
        val boxRfid = selectedBox ?: return

        try {
            when (operation) {
                MatchOperation.MATCH_PRODUCT -> {
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
                }
                MatchOperation.ADD_PARTS -> {
                    selectedProducts.forEach { product ->
                        val updatedProduct = product.copy(
                            sparePartBoxRfid = boxRfid,
                            status = "ADDED_AS_SPARE",
                            isSparePartBox = true
                        )
                        productDao.updateProduct(updatedProduct)
                    }
                    Toast.makeText(this,
                        "${selectedProducts.size} yedek parça eklendi",
                        Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Diğer operasyonlar için toplu işlem yok
                    return
                }
            }

            // Temizlik
            selectedProducts.clear()
            selectedBox = null
            updateUIForBatchMode(false)
            binding.batchModeSwitch.isChecked = false
            finish()

        } catch (e: Exception) {
            Toast.makeText(this,
                "İşlem hatası: ${e.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun processRemoveProduct(content: String) {
        try {
            val parts = content.split("|")
            val imei = parts[1].substringAfter("IMEI:")
            val product = productDao.findByImei(imei)

            if (product != null) {
                if (product.matchedBoxRfid != selectedBox) {
                    Toast.makeText(this, "Bu ürün seçilen kutuya ait değil!", Toast.LENGTH_SHORT).show()
                    return
                }

                val updatedProduct = product.copy(
                    matchedBoxRfid = null,
                    status = "REMOVED",
                    removedDate = getCurrentDate()
                )
                productDao.updateProduct(updatedProduct)
                Toast.makeText(this, "Ürün başarıyla çıkarıldı: ${product.name}", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Ürün bulunamadı!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "İşlem hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun processAddSparePart(content: String) {
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
                    location = "Yedek Parça Deposu",
                    address = "",
                    createdDate = getCurrentDate(),
                    status = "NEW",
                    isSparePartBox = true,
                    sparePartBoxRfid = selectedBox
                )
                productDao.insertProduct(product)

                if (isBatchMode) {
                    selectedProducts.add(product)
                    updateSelectedProductsList()
                    binding.scanProductButton.text = "Parça Ekle (${selectedProducts.size})"
                    Toast.makeText(this, "Parça listeye eklendi: ${product.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Yedek parça eklendi: ${product.name}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Bu parça zaten sistemde kayıtlı!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "İşlem hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun processRemoveSparePart(content: String) {
        try {
            val parts = content.split("|")
            val imei = parts[1].substringAfter("IMEI:")
            val product = productDao.findByImei(imei)

            if (product != null) {
                if (product.sparePartBoxRfid != selectedBox) {
                    Toast.makeText(this, "Bu parça seçilen kutuya ait değil!", Toast.LENGTH_SHORT).show()
                    return
                }

                val updatedProduct = product.copy(
                    sparePartBoxRfid = null,
                    status = "REMOVED",
                    removedDate = getCurrentDate()
                )
                productDao.updateProduct(updatedProduct)
                Toast.makeText(this, "Yedek parça başarıyla çıkarıldı: ${product.name}", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Yedek parça bulunamadı!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "İşlem hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupOperationUI(operation: MatchOperation) {
        when (operation) {
            MatchOperation.MATCH_PRODUCT -> {
                binding.apply {
                    toolbarTitle.text = "Ürün Eşleştirme"
                    toolbarSubtitle.text = "Ana Sayfa > Ürünler > Eşleştirme"
                    instructionText.text = "Önce ürünü, ardından kutuyu okutun"
                    scanProductButton.text = "Ürün Okut"
                    scanBoxButton.text = "Kutu Seç"

                    // Batch mode ayarları
                    batchModeSwitch.isVisible = true
                    selectedProductsRecyclerView.isVisible = isBatchMode
                    matchAllButton.isVisible = isBatchMode
                    matchAllButton.text = "Ürünleri Eşleştir"
                }
            }
            MatchOperation.REMOVE_PRODUCT -> {
                binding.apply {
                    toolbarTitle.text = "Ürün Çıkarma"
                    toolbarSubtitle.text = "Ana Sayfa > Ürünler > Çıkarma"
                    instructionText.text = "Önce kutuyu, sonra çıkarılacak ürünü okutun"
                    scanBoxButton.text = "Kutu Seç"
                    scanProductButton.text = "Çıkarılacak Ürünü Seç"

                    // UI ayarları
                    selectedProductsRecyclerView.isVisible = false
                    matchAllButton.isVisible = false
                    batchModeSwitch.isVisible = false
                    scanBoxButton.isEnabled = true
                    scanProductButton.isEnabled = selectedBox != null
                }
            }
            MatchOperation.ADD_PARTS ->{
                binding.apply {
                    toolbarTitle.text = "Yedek Parça Ekle"
                    toolbarSubtitle.text = "Anasayfa > Yedek Parça > Ekleme"
                    instructionText.text = "Kutuyu okutun Yedek Parçayı Ekleyin"
                    scanBoxButton.text = "Kutu Seç"
                    scanProductButton.text = "Eklenecek Yedek Parçayı Seç"

                    // Batch mode ayarları
                    batchModeSwitch.isVisible = true
                    selectedProductsRecyclerView.isVisible = isBatchMode
                    matchAllButton.isVisible = isBatchMode
                    matchAllButton.text = "Yedek Parçaları Eşleştir"
                }
            }
            MatchOperation.REMOVE_PARTS ->{
                binding.apply {
                    toolbarTitle.text = "Yedek Parça Çıkarma"
                    toolbarSubtitle.text = "Ana Sayfa > Yedek Parça > Çıkarma"
                    instructionText.text = "Önce kutuyu, Sonra Çıkarılacak Yedek Parçayı Okutun"
                    scanBoxButton.text = "Kutu Seç"
                    scanProductButton.text = "Çıkarılacak Yedek Parça Seç"

                    // UI ayarları
                    selectedProductsRecyclerView.isVisible = false
                    matchAllButton.isVisible = false
                    batchModeSwitch.isVisible = false
                    scanBoxButton.isEnabled = true
                    scanProductButton.isEnabled = selectedBox != null
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

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
        when (operation) {
            MatchOperation.MATCH_PRODUCT -> {
                if (!isBatchMode && scannedProduct != null) {
                    Toast.makeText(this, "Önce kutu okutun", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            MatchOperation.REMOVE_PRODUCT,
            MatchOperation.ADD_PARTS,
            MatchOperation.REMOVE_PARTS -> {
                if (selectedBox == null) {
                    Toast.makeText(this, "Önce kutu okutun", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt(when (operation) {
                MatchOperation.MATCH_PRODUCT -> "Ürün QR kodunu okutun"
                MatchOperation.REMOVE_PRODUCT -> "Çıkarılacak ürün QR kodunu okutun"
                MatchOperation.ADD_PARTS -> "Eklenecek yedek parça QR kodunu okutun"
                MatchOperation.REMOVE_PARTS -> "Çıkarılacak yedek parça QR kodunu okutun"
            })
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            initiateScan()
        }
    }

    private fun startBoxScan() {
        // MATCH_PRODUCT için özel kontrol
        if (operation == MatchOperation.MATCH_PRODUCT && !isBatchMode && scannedProduct == null) {
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

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun updateSelectedProductsList() {
        selectedProductsAdapter.updateProducts(selectedProducts)
    }
}