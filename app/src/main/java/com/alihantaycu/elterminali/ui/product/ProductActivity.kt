package com.alihantaycu.elterminali.ui.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityProductBinding
import com.alihantaycu.elterminali.databinding.DialogAddEditProductBinding
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import com.alihantaycu.elterminali.data.database.AppDatabase
import java.util.*

class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        productDao = database.productDao()

        setupToolbar()
        setupRecyclerView()
        setupAddProductFab()
        setupSearchView()
        loadProducts()
        loadMatchedProducts()
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

    private fun loadMatchedProducts() {
        lifecycleScope.launch {
            productDao.getMatchedProducts().collect { matchedProducts ->
                Log.d("ProductMatch", "Eşleştirilmiş Ürünler: $matchedProducts")
                // İsterseniz eşleştirilmiş ürünleri farklı şekilde gösterebilirsiniz
                // Örneğin: adapter.updateMatchedProducts(matchedProducts)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Ürünleri Yönet"
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

                        // QR içeriğini parçala
                        val parts = scannedContent.split("|")
                        val rfid = parts[0].substringAfter("RFID:")
                        val imei = parts[1].substringAfter("IMEI:")
                        val name = parts[2].substringAfter("NAME:")

                        val nextId = productDao.getNextId() ?: 1
                        val newProduct = Product(
                            id = nextId.toString(),
                            rfidTag = rfid,
                            imei = imei,
                            name = name,
                            location = "Gelen Kargo Depo",
                            address = "",
                            createdDate = getCurrentDate()
                        )

                        // Form dialog'unu göster
                        showAddProductDialog(newProduct)

                    } catch (e: Exception) {
                        Log.e("QR_SCAN", "Hata: ${e.message}", e)
                        Toast.makeText(this@ProductActivity,
                            "QR kod formatı hatalı: ${e.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onItemClick = { showProductDetails(it) },
            onEditClick = { showEditProductDialog(it) },
            onDeleteClick = { showDeleteConfirmationDialog(it) },
            onGenerateQRClick = { generateQRForProduct(it) }
        )
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductActivity)
            adapter = productAdapter
        }
    }

    private fun setupAddProductFab() {
        binding.addProductFab.setOnClickListener {
            showWorkflowOptionsDialog()
        }
    }

    private fun setupSearchView() {
        binding.productSearchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                productAdapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun showWorkflowOptionsDialog() {
        val options = arrayOf("QR Okut", "Manuel Ekle")
        AlertDialog.Builder(this)
            .setTitle("İşlem Seçin")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startQRScanner()
                    1 -> showManualAddProductDialog() // Yeni bir fonksiyon kullanıyoruz
                }
            }
            .show()
    }

    private fun showManualAddProductDialog() {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)

        // Spinner için lokasyon listesi
        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Kargo Depo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerLocation.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Yeni Ürün Ekle")
            .setView(dialogBinding.root)
            .setPositiveButton("Ekle") { _, _ ->
                lifecycleScope.launch {
                    val nextId = productDao.getNextId() ?: 1
                    val newProduct = Product(
                        id = nextId.toString(),
                        rfidTag = dialogBinding.editTextRfid.text.toString(),
                        imei = dialogBinding.editTextImei.text.toString(),
                        name = dialogBinding.editTextName.text.toString(),
                        location = dialogBinding.spinnerLocation.selectedItem.toString(),
                        address = dialogBinding.editTextAddress.text.toString(),
                        createdDate = getCurrentDate()
                    )

                    productDao.insertProduct(newProduct)
                    loadProducts()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun startQRScanner() {
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("QR Kodu Okutun")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            initiateScan()
        }
    }

    private suspend fun processImeiQR(scannedContent: String) {
        try {
            // QR içeriğini parçala
            val parts = scannedContent.split("|")
            val rfid = parts[0].substringAfter("RFID:")
            val imei = parts[1].substringAfter("IMEI:")
            val name = parts[2].substringAfter("NAME:")

            val nextId = productDao.getNextId() ?: 1
            val newProduct = Product(
                id = nextId.toString(),
                rfidTag = rfid,
                imei = imei,
                name = name,
                location = "Gelen Kargo Depo",
                address = "",
                createdDate = getCurrentDate()
            )

            // Form dialog'unu göster
            showAddProductDialog(newProduct)

        } catch (e: Exception) {
            Toast.makeText(this,
                "QR kod formatı hatalı",
                Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun processLocationQR(scannedContent: String) {
        val location = scannedContent.substringAfter("LOC:")
        Toast.makeText(this, "Lokasyon: $location", Toast.LENGTH_SHORT).show()
        // Burada lokasyon işlemleri yapılacak
    }

    private suspend fun processNormalQR(scannedContent: String) {
        val nextId = productDao.getNextId() ?: 1
        val newProduct = Product(
            id = nextId.toString(),
            rfidTag = scannedContent,
            imei = "",
            name = "QR'dan Eklenen Ürün",
            location = "Gelen Kargo Depo",
            address = "",
            createdDate = getCurrentDate()
        )
        showAddProductDialog(newProduct)
    }

    private fun showAddProductDialog(product: Product) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)

        // Form alanlarını doldur
        dialogBinding.apply {
            // QR'dan gelen verileri yerleştir
            editTextRfid.setText(product.rfidTag)
            editTextImei.setText(product.imei)
            editTextName.setText(product.name)

            // Bu alanları değiştirilemez yap
            editTextRfid.isEnabled = false
            editTextImei.isEnabled = false
            editTextName.isEnabled = false

            // Spinner için lokasyon listesi
            val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Kargo Depo")
            val adapter = ArrayAdapter(this@ProductActivity,
                android.R.layout.simple_spinner_item, locations)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLocation.adapter = adapter
        }

        AlertDialog.Builder(this)
            .setTitle("Ürün Bilgilerini Doldurun")
            .setView(dialogBinding.root)
            .setPositiveButton("Ekle") { _, _ ->
                lifecycleScope.launch {
                    productDao.insertProduct(product)
                    loadProducts()
                    Toast.makeText(this@ProductActivity,
                        "Ürün eklendi: ${product.name}",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)

        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerLocation.adapter = adapter

        dialogBinding.apply {
            editTextRfid.setText(product.rfidTag)
            editTextImei.setText(product.imei)
            editTextName.setText(product.name)
            editTextAddress.setText(product.address)
            spinnerLocation.setSelection(locations.indexOf(product.location))
        }

        AlertDialog.Builder(this)
            .setTitle("Ürünü Düzenle")
            .setView(dialogBinding.root)
            .setPositiveButton("Güncelle") { _, _ ->
                val updatedProduct = product.copy(
                    rfidTag = dialogBinding.editTextRfid.text.toString(),
                    imei = dialogBinding.editTextImei.text.toString(),
                    name = dialogBinding.editTextName.text.toString(),
                    location = dialogBinding.spinnerLocation.selectedItem.toString(),
                    address = dialogBinding.editTextAddress.text.toString()
                )

                lifecycleScope.launch {
                    productDao.updateProduct(updatedProduct)
                    loadProducts()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Ürünü Sil")
            .setMessage("Bu ürünü silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _, _ ->
                lifecycleScope.launch {
                    productDao.deleteProduct(product)
                    loadProducts()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showProductDetails(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Ürün Detayları")
            .setMessage("""
                ID: ${product.id}
                Ad: ${product.name}
                RFID: ${product.rfidTag}
                IMEI: ${product.imei}
                Depo: ${product.location}
                Konum: ${product.address}
                Oluşturulma Tarihi: ${product.createdDate}
            """.trimIndent())
            .setPositiveButton("Tamam", null)
            .show()
    }

    private fun generateQRForProduct(product: Product) {
        Toast.makeText(this,
            "QR kod oluşturuluyor: ${product.name}",
            Toast.LENGTH_SHORT).show()
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            productDao.getAllProducts().collect { productList ->
                productAdapter.updateProducts(productList)
            }
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun showLocationDialog(product: Product) {
        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Kargo Depo")
        AlertDialog.Builder(this)
            .setTitle("Lokasyon Seçin")
            .setItems(locations.toTypedArray()) { _, which ->
                val newLocation = locations[which]
                lifecycleScope.launch {
                    val updatedProduct = product.copy(location = newLocation)
                    productDao.updateProduct(updatedProduct)
                    loadProducts()
                    Toast.makeText(this@ProductActivity,
                        "Lokasyon güncellendi: $newLocation",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}