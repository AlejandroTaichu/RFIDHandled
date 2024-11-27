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
import jp.wasabeef.recyclerview.animators.BaseItemAnimator
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator

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

        setupUI()
        loadProducts()
        observeMatchedProducts()
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        binding.addProductFab.setOnClickListener { showWorkflowOptionsDialog() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Ürünleri Yönet"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onItemClick = { showProductDetails(it) },
            onEditClick = { showProductDialog(it, isEditMode = true) },
            onDeleteClick = { confirmDeleteProduct(it) },
            onGenerateQRClick = { generateQRForProduct(it) }
        )

        binding.productsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.productsRecyclerView.adapter = productAdapter

        // ItemAnimator kullanımını burada ekliyoruz
        binding.productsRecyclerView.itemAnimator = FadeInUpAnimator()

        // Eğer animasyon ayarları yapılmak isteniyorsa:
        (binding.productsRecyclerView.itemAnimator as? BaseItemAnimator)?.apply {
            addDuration = 300    // Ekleme animasyonu süresi
            removeDuration = 300 // Silme animasyonu süresi
            moveDuration = 300   // Taşıma animasyonu süresi
            changeDuration = 300 // Değişim animasyonu süresi
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

    private fun loadProducts() {
        lifecycleScope.launch {
            productDao.getAllProducts().collect { products ->
                productAdapter.updateProducts(products)
            }
        }
    }

    private fun observeMatchedProducts() {
        lifecycleScope.launch {
            productDao.getMatchedProducts().collect { matchedProducts ->
                Log.d("ProductMatch", "Eşleştirilmiş Ürünler: $matchedProducts")
            }
        }
    }

    private fun showWorkflowOptionsDialog() {
        val options = arrayOf("QR Okut", "Manuel Ekle")
        AlertDialog.Builder(this)
            .setTitle("İşlem Seçin")
            .setItems(options) { _, which ->
                if (which == 0) startQRScanner() else showProductDialog(null, isEditMode = false)
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            lifecycleScope.launch {
                processQRContent(result.contents)
            }
        } else {
            Toast.makeText(this, "Tarama iptal edildi", Toast.LENGTH_LONG).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private suspend fun processQRContent(content: String) {
        try {
            val parts = content.split("|")
            val rfid = parts.getOrNull(0)?.substringAfter("RFID:") ?: ""
            val imei = parts.getOrNull(1)?.substringAfter("IMEI:") ?: ""
            val name = parts.getOrNull(2)?.substringAfter("NAME:") ?: ""

            val newProduct = Product(
                id = (productDao.getNextId() ?: 1).toString(),
                rfidTag = rfid,
                imei = imei,
                name = name,
                location = "Gelen Kargo Depo",
                address = "",
                createdDate = getCurrentDate(),
                status = "NEW", // Status eklendi
                matchedBoxRfid = null,
                removedDate = null,
                isSparePartBox = false,
                sparePartBoxRfid = null
            )

            showProductDialog(newProduct, isEditMode = false)
        } catch (e: Exception) {
            Toast.makeText(this, "QR kod formatı hatalı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProductDialog(product: Product?, isEditMode: Boolean) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)
        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        dialogBinding.apply {
            spinnerLocation.adapter = adapter
            product?.let {
                editTextRfid.setText(it.rfidTag)
                editTextImei.setText(it.imei)
                editTextName.setText(it.name)
                editTextAddress.setText(it.address)
                spinnerLocation.setSelection(locations.indexOf(it.location))
            }
            editTextRfid.isEnabled = !isEditMode
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEditMode) "Ürünü Düzenle" else "Yeni Ürün Ekle")
            .setView(dialogBinding.root)
            .setPositiveButton(if (isEditMode) "Güncelle" else "Ekle") { _, _ ->
                lifecycleScope.launch {
                    val updatedProduct = Product(
                        id = product?.id ?: (productDao.getNextId() ?: 1).toString(),
                        rfidTag = dialogBinding.editTextRfid.text.toString(),
                        imei = dialogBinding.editTextImei.text.toString(),
                        name = dialogBinding.editTextName.text.toString(),
                        location = dialogBinding.spinnerLocation.selectedItem.toString(),
                        address = dialogBinding.editTextAddress.text.toString(),
                        createdDate = product?.createdDate ?: getCurrentDate(),
                        status = product?.status ?: "NEW", // Status eklendi
                        matchedBoxRfid = product?.matchedBoxRfid,
                        removedDate = product?.removedDate,
                        isSparePartBox = product?.isSparePartBox ?: false,
                        sparePartBoxRfid = product?.sparePartBoxRfid
                    )

                    if (isEditMode) productDao.updateProduct(updatedProduct)
                    else productDao.insertProduct(updatedProduct)
                    loadProducts()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun confirmDeleteProduct(product: Product) {
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
        Toast.makeText(this, "QR kod oluşturuluyor: ${product.name}, ${product.imei}, ${product.rfidTag}", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
