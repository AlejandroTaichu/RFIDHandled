package com.alihantaycu.elterminali.ui.match.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.device.ScanManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.alihantaycu.elterminali.ui.match.adapter.BatchItem
import com.alihantaycu.elterminali.ui.match.adapter.BatchItemAdapter
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID



class ProductMatchActivity : AppCompatActivity() {
    private lateinit var scanner: ScanManager
    private lateinit var binding: ActivityProductMatchBinding
    private lateinit var productDao: ProductDao
    private lateinit var batchAdapter: BatchItemAdapter  // Eklendi


    private var isSparePartOperation = false  // Yedek parça operasyonu mu?
    private var isRemoveOperation = false     // Çıkarma operasyonu mu?
    private var selectedBox: String? = null
    private var isBatchMode = false  // Eklendi

    companion object {
        private const val ACTION_DECODE = ScanManager.ACTION_DECODE
        private const val DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSparePartOperation = intent.getBooleanExtra("isSparePartOperation", false)

        val database = AppDatabase.getDatabase(this)
        productDao = database.productDao()

        setupToolbar()
        setupUI()
        updateUIForMode()
        setupBatchMode()
        initScanner()
        setupScannerListener()
    }

    private fun initScanner() {
        try {
            scanner = ScanManager()
            scanner.openScanner()
            scanner.switchOutputMode(2)  // Sadece broadcast modu
        } catch (e: Exception) {
            Log.e("Scanner", "Scanner başlatma hatası: ${e.message}")
        }
    }

    private fun startQRScan() {
        try {
            scanner.startDecode()
        } catch (e: Exception) {
            Toast.makeText(this, "Scanner hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun processBoxScan(content: String) {
        try {
            val parts = content.split("|")
            val boxRfid = parts[0].substringAfter("RFID:")
            val boxType = parts[1].substringAfter("TYPE:")

            // Kutu tipi kontrolü - Daha sıkı kontrol
            when {
                isSparePartOperation && boxType != "SPARE_BOX" -> {
                    Toast.makeText(this, "Bu kutu yedek parça kutusu değil! Yedek parça kutusu okutun.", Toast.LENGTH_LONG).show()
                    return
                }
                !isSparePartOperation && boxType != "PRODUCT_BOX" -> {
                    Toast.makeText(this, "Bu kutu ürün kutusu değil! Ürün kutusu okutun.", Toast.LENGTH_LONG).show()
                    return
                }
            }

            selectedBox = boxRfid
            updateUIAfterBoxScan(boxType)

        } catch (e: Exception) {
            Toast.makeText(this, "Geçersiz kutu QR kodu formatı", Toast.LENGTH_SHORT).show()
            Log.e("ProductMatch", "Kutu QR kodu hatası", e)
        }
    }

    private suspend fun processItemScan(content: String) {
        try {
            val parts = content.split("|")
            val rfid = parts[0].substringAfter("RFID:")
            val imei = parts[1].substringAfter("IMEI:")
            val name = parts[2].substringAfter("NAME:")
            val type = parts.getOrNull(3)?.substringAfter("TYPE:") // Yeni: Ürün tipi kontrolü

            // Ürün tipi kontrolü
            when {
                isSparePartOperation && type != "SPARE_PART" -> {
                    Toast.makeText(this, "Bu bir yedek parça değil! Yedek parça okutun.", Toast.LENGTH_LONG).show()
                    return
                }
                !isSparePartOperation && type != "PRODUCT" -> {
                    Toast.makeText(this, "Bu bir ürün değil! Ürün okutun.", Toast.LENGTH_LONG).show()
                    return
                }
            }

            // Mevcut ürün kontrolü
            var product = productDao.findByImei(imei)

            if (isRemoveOperation) {
                handleRemoveOperation(product, name)
            } else {
                handleAddOperation(product, rfid, imei, name)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Geçersiz ürün QR kodu", Toast.LENGTH_SHORT).show()
            Log.e("ProductMatch", "Ürün işleme hatası", e)
        }
    }

    private suspend fun handleRemoveOperation(product: Product?, name: String) {
        if (product == null) {
            Toast.makeText(this, "Ürün bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentBoxRfid = if (isSparePartOperation) product.sparePartBoxRfid else product.matchedBoxRfid
        if (currentBoxRfid != selectedBox) {
            Toast.makeText(this,
                "Bu ${if (isSparePartOperation) "parça" else "ürün"} seçili kutuya ait değil!",
                Toast.LENGTH_SHORT).show()
            return
        }

        val updatedProduct = product.copy(
            matchedBoxRfid = if (isSparePartOperation) product.matchedBoxRfid else null,
            sparePartBoxRfid = if (isSparePartOperation) null else product.sparePartBoxRfid,
            status = if (isSparePartOperation) "USED" else "REMOVED",
            removedDate = getCurrentDate(),
            location = if (isSparePartOperation) "Kullanıldı" else "Giden Kargo Depo"
        )

        productDao.updateProduct(updatedProduct)
        Toast.makeText(this,
            if (isSparePartOperation) "Parça kullanıldı olarak işaretlendi: $name"
            else "Ürün Giden Kargo Depo'ya taşındı: $name",
            Toast.LENGTH_SHORT).show()
        finish()
    }

    private suspend fun handleAddOperation(product: Product?, rfid: String, imei: String, name: String) {
        if (product == null) {
            // Yeni ürün oluştur
            val newProduct = Product(
                id = UUID.randomUUID().toString(),
                rfidTag = rfid,
                imei = imei,
                name = name,
                location = if (isSparePartOperation) "Yedek Parça Depo" else "Gelen Kargo Depo",
                address = "",
                createdDate = getCurrentDate(),
                status = "NEW",
                matchedBoxRfid = if (isSparePartOperation) null else selectedBox,
                sparePartBoxRfid = if (isSparePartOperation) selectedBox else null,
                isSparePartBox = isSparePartOperation
            )
            productDao.insertProduct(newProduct)
        } else {
            // Mevcut ürün kontrolü
            if ((isSparePartOperation && product.sparePartBoxRfid != null) ||
                (!isSparePartOperation && product.matchedBoxRfid != null)) {
                Toast.makeText(this,
                    "Bu ${if (isSparePartOperation) "parça" else "ürün"} zaten bir kutuya atanmış!",
                    Toast.LENGTH_SHORT).show()
                return
            }

            val updatedProduct = product.copy(
                matchedBoxRfid = if (isSparePartOperation) product.matchedBoxRfid else selectedBox,
                sparePartBoxRfid = if (isSparePartOperation) selectedBox else product.sparePartBoxRfid,
                status = "MATCHED"
            )
            productDao.updateProduct(updatedProduct)
        }

        Toast.makeText(this,
            "${if (isSparePartOperation) "Parça" else "Ürün"} eklendi: $name",
            Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateUIAfterBoxScan(boxType: String) {
        binding.apply {
            instructionText.text = if (isRemoveOperation) {
                "${if (isSparePartOperation) "Parça" else "Ürün"} çıkarmak için okutun"
            } else {
                "${if (isSparePartOperation) "Parça" else "Ürün"} eklemek için okutun"
            }

            scanButton.text = if (isBatchMode) {
                "ÜRÜN OKUT"
            } else {
                if (isRemoveOperation) "${if (isSparePartOperation) "PARÇA" else "ÜRÜN"} ÇIKAR"
                else "${if (isSparePartOperation) "PARÇA" else "ÜRÜN"} EKLE"
            }
        }

        Toast.makeText(this,
            "${if (boxType == "SPARE_BOX") "Yedek parça" else "Ürün"} kutusu seçildi",
            Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupScannerListener() {
        val filter = IntentFilter().apply {
            addAction(ACTION_DECODE)
        }
        // System broadcast olduğu için RECEIVER_EXPORTED kullanıyoruz
        registerReceiver(scannerReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    private val scannerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ScanManager.ACTION_DECODE) {
                intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG)?.let { barcodeData ->
                    val scannedData = String(barcodeData)
                    lifecycleScope.launch {
                        try {
                            processQRResult(scannedData)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ProductMatchActivity,
                                "Barkod işleme hatası: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupBatchMode() {
        // BatchAdapter'ı başlat
        batchAdapter = BatchItemAdapter { item ->
            // Listeden öğe kaldırma
            batchAdapter.removeItem(item)
            updateBatchUI()
        }

        binding.apply {
            // RecyclerView setup
            batchItemsRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@ProductMatchActivity)
                adapter = batchAdapter
            }

            processBatchButton.visibility = View.GONE


            // Batch mode switch listener
            batchModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                isBatchMode = isChecked
                updateBatchUI()
            }

            // Toplu işlem butonu listener
            processBatchButton.setOnClickListener {
                lifecycleScope.launch {
                    processBatchItems()
                }
            }
        }
    }

    private fun updateUIForMode() {
        binding.apply {
            val operationType = if (isSparePartOperation) "Yedek Parça" else "Ürün"
            val operationAction = if (isRemoveOperation) "Çıkarma" else "Ekleme"

            // Başlık güncelleme
            toolbarTitle.text = "$operationType $operationAction"

            // Mod bilgisi güncelleme
            modeDescription.apply {
                text = if (isRemoveOperation) {
                    "$operationType Çıkarma Modu"
                } else {
                    "$operationType Ekleme Modu"
                }
                setTextColor(Color.DKGRAY)
            }

            // Yönlendirme metni
            instructionText.text = if (selectedBox == null) {
                "Önce kutuyu okutun"
            } else {
                if (isRemoveOperation) {
                    "Kutudan çıkarılacak ${operationType.lowercase()} okutun"
                } else {
                    "Kutuya eklenecek ${operationType.lowercase()} okutun"
                }
            }

            // Buton metni ve rengi güncelleme
            val backgroundColor = if (isRemoveOperation) {
                ContextCompat.getColor(this@ProductMatchActivity, R.color.yenilio_kırmızısı)
            } else {
                ContextCompat.getColor(this@ProductMatchActivity, R.color.yenililo_turuncusu)
            }

            scanButton.apply {
                backgroundTintList = ColorStateList.valueOf(backgroundColor)
                text = if (selectedBox == null) {
                    "Kutuyu okutun"
                } else {
                    if (isRemoveOperation) "${operationType.uppercase()} Çıkar"
                    else "${operationType.uppercase()} Ekle"
                }
            }
        }
    }

    private suspend fun processBatchItems() {
        try {
            val items = batchAdapter.getAllItems()
            if (items.isEmpty()) {
                Toast.makeText(this, "İşlenecek ürün bulunamadı", Toast.LENGTH_SHORT).show()
                return
            }

            var processedCount = 0

            // Tüm ürünleri veritabanına ekle/güncelle
            items.forEach { batchItem ->
                var existingProduct = productDao.findByImei(batchItem.product.imei)

                if (existingProduct == null) {
                    if (isRemoveOperation) {
                        // Çıkarma işleminde olmayan ürün atlanır
                        return@forEach
                    }

                    // Yeni ürün ekleme
                    val newProduct = batchItem.product.copy(
                        matchedBoxRfid = if (isSparePartOperation) null else batchItem.boxRfid,
                        sparePartBoxRfid = if (isSparePartOperation) batchItem.boxRfid else null,
                        status = "MATCHED",
                        isSparePartBox = isSparePartOperation,
                        location = if (isSparePartOperation) "Yedek Parça Depo" else "Gelen Kargo Depo"
                    )
                    productDao.insertProduct(newProduct)
                    processedCount++
                } else {
                    if (isRemoveOperation) {
                        // Çıkarma işlemi
                        val currentBoxRfid = if (isSparePartOperation)
                            existingProduct.sparePartBoxRfid
                        else
                            existingProduct.matchedBoxRfid

                        if (currentBoxRfid != batchItem.boxRfid) {
                            // Yanlış kutu eşleşmesi atlanır
                            return@forEach
                        }

                        val updatedProduct = existingProduct.copy(
                            matchedBoxRfid = if (isSparePartOperation) existingProduct.matchedBoxRfid else null,
                            sparePartBoxRfid = if (isSparePartOperation) null else existingProduct.sparePartBoxRfid,
                            status = "REMOVED",
                            removedDate = getCurrentDate(),
                            location = if (!isSparePartOperation) "Giden Kargo Depo" else existingProduct.location
                        )
                        productDao.updateProduct(updatedProduct)
                        processedCount++
                    } else {
                        // Güncelleme işlemi (ekleme işlemi)
                        val updatedProduct = existingProduct.copy(
                            matchedBoxRfid = if (isSparePartOperation) existingProduct.matchedBoxRfid else batchItem.boxRfid,
                            sparePartBoxRfid = if (isSparePartOperation) batchItem.boxRfid else existingProduct.sparePartBoxRfid,
                            status = "MATCHED",
                            isSparePartBox = isSparePartOperation,
                            location = if (isSparePartOperation) "Yedek Parça Depo" else "Gelen Kargo Depo"
                        )
                        productDao.updateProduct(updatedProduct)
                        processedCount++
                    }
                }
            }

            // Başarılı mesajı göster
            val operationType = if (isSparePartOperation) "parça" else "ürün"
            val actionType = if (isRemoveOperation) "çıkarıldı" else "eklendi"
            val locationInfo = if (!isSparePartOperation && isRemoveOperation) " ve Giden Kargo Depo'ya taşındı" else ""

            Toast.makeText(this,
                "$processedCount adet $operationType $actionType$locationInfo",
                Toast.LENGTH_SHORT).show()

            // Listeyi temizle ve UI'ı güncelle
            batchAdapter.clear()
            binding.batchModeSwitch.isChecked = false
            isBatchMode = false
            updateBatchUI()

            // Activity'yi kapat
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "İşlem hatası: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("ProductMatch", "Toplu işlem hatası", e)
        }
    }

    private fun updateBatchUI() {
        binding.apply {
            // RecyclerView görünürlüğü
            batchItemsRecyclerView.isVisible = isBatchMode

            // Toplu işlem butonu sadece ürün eklendiğinde görünür olsun
            processBatchButton.isVisible = isBatchMode && batchAdapter.itemCount > 0

            if (isBatchMode) {
                // Yönlendirme metni
                instructionText.text = when {
                    selectedBox == null -> "Önce kutuyu okutun"
                    batchAdapter.itemCount > 0 -> "Yeni ürün eklemek için ürün okutun veya toplu işlemi tamamlayın"
                    else -> "Ürün okutun"
                }

                // Scan butonu metni
                scanButton.text = when {
                    selectedBox == null -> "KUTUYU OKUTUN"
                    else -> "${if (isSparePartOperation) "PARÇA" else "ÜRÜN"} OKUTUN"
                }
            } else {
                // Normal mod için buton metni
                scanButton.text = if (selectedBox == null) {
                    "KUTUYU OKUTUN"
                } else {
                    if (isRemoveOperation) "${if (isSparePartOperation) "PARÇA" else "ÜRÜN"} ÇIKAR"
                    else "${if (isSparePartOperation) "PARÇA" else "ÜRÜN"} EKLE"
                }
            }
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
                        processQRResult(result.contents)
                    } catch (e: Exception) {
                        Toast.makeText(this@ProductMatchActivity,
                            "Hata: ${e.message}",
                            Toast.LENGTH_LONG).show()
                        Log.e("ProductMatch", "QR işleme hatası", e)
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun setupUI() {
        binding.apply {
            // Material Switch kullanımı
            operationModeSwitch.apply {
                visibility = View.VISIBLE
                text = if (isSparePartOperation) "Yedek Parça İşlemi" else "Ürün İşlemi Değiştir"
                setOnCheckedChangeListener { _, isChecked ->
                    isRemoveOperation = isChecked
                    updateUIForMode()
                }
            }

            // Scan butonu
            scanButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    startQRScan()
                }
            }

            // İlk durumda batch mode switch'i kapalı ve buton gizli olmalı
            batchModeSwitch.isChecked = false
            processBatchButton.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun processQRResult(content: String) {
        try {
            if (isBatchMode) {
                val isPossibleBoxData = content.contains("TYPE:")

                if (isPossibleBoxData) {
                    // Kutu taraması
                    val boxRfid = content.substringBefore("|").substringAfter("RFID:")
                    selectedBox = boxRfid
                    binding.instructionText.text = "Şimdi ürün okutun"
                    updateBatchUI()
                } else {
                    // Ürün taraması
                    if (selectedBox == null) {
                        Toast.makeText(this, "Önce kutu okutun", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Ürün/parça verisi işleme
                    val parts = content.split("|")
                    val rfid = parts[0].substringAfter("RFID:")
                    val imei = parts[1].substringAfter("IMEI:")
                    val name = parts[2].substringAfter("NAME:")

                    // Yedek parça değilse ve kutu daha önce kullanıldıysa kontrol et
                    if (!isSparePartOperation && batchAdapter.getAllItems().any { it.boxRfid == selectedBox }) {
                        Toast.makeText(this, "Bu kutuya başka ürün eklenemez! Yeni kutu okutun.", Toast.LENGTH_LONG).show()
                        selectedBox = null // Kutuyu sıfırla
                        updateBatchUI()
                        return
                    }

                    val product = Product(
                        id = UUID.randomUUID().toString(),
                        rfidTag = rfid,
                        imei = imei,
                        name = name,
                        location = if (isSparePartOperation) "Yedek Parça Depo" else "Gelen Kargo Depo",
                        address = "",
                        createdDate = getCurrentDate(),
                        status = "NEW"
                    )

                    // Batch item'a ekle
                    batchAdapter.addItem(BatchItem(product, selectedBox!!))

                    // Yedek parça değilse yeni kutu gerekiyor
                    if (!isSparePartOperation) {
                        selectedBox = null
                        binding.instructionText.text = "Yeni kutu okutun"
                    } else {
                        binding.instructionText.text = "Yeni parça eklemek için okutun veya Toplu İşlemi Tamamla'ya basın"
                    }

                    updateBatchUI()
                }
            } else {
                // Normal mod işlemi (değişiklik yok)
                if (selectedBox == null) {
                    processBoxScan(content)
                } else {
                    processItemScan(content)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "QR işleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            scanner.stopDecode()
        } catch (e: Exception) {
            Log.e("ProductMatchActivity", "Failed to stop decoding: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(scannerReceiver)
            scanner.closeScanner()
        } catch (e: Exception) {
            Log.e("Scanner", "Scanner kapatma hatası: ${e.message}")
        }
    }
}