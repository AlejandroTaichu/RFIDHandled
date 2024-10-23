package com.alihantaycu.elterminali.ui.product

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityProductBinding
import com.alihantaycu.elterminali.databinding.DialogAddEditProductBinding
import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.database.AppDatabase
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    private val filteredProducts = mutableListOf<Product>()

    // ProductDao'yu veritabanına erişim için ekleyelim
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // DAO'yu başlatıyoruz
        val productDatabase = AppDatabase.getDatabase(this)  // Veritabanını başlat
        productDao = productDatabase.productDao()  // ProductDao'yu al

        setupRecyclerView()
        setupAddProductFab()
        setupSearchView()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(filteredProducts,
            onItemClick = { showProductDetails(it) },
            onEditClick = { showEditProductDialog(it) },
            onDeleteClick = { showDeleteConfirmationDialog(it) }
        )
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductActivity)
            adapter = productAdapter
        }
    }

    private fun setupAddProductFab() {
        binding.addProductFab.setOnClickListener {
            showAddOptionsDialog() // Seçenekler diyalogunu göster
        }
    }


    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // Sadece QR kod taraması için
        integrator.setPrompt("Lütfen QR kodu okutun")
        integrator.setCameraId(0) // Arka kamerayı kullan
        integrator.setBeepEnabled(true) // QR kod okunduğunda bip sesi çal
        integrator.setBarcodeImageEnabled(true) // QR kodun görüntüsünü kaydet
        integrator.initiateScan() // Tarayıcıyı başlat
    }

    private fun showAddOptionsDialog() {
        val options = arrayOf("QR Okut", "Manuel Ekle")

        AlertDialog.Builder(this)
            .setTitle("Ürün Ekleme Seçenekleri")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> startQRScanner() // QR Okut seçildiğinde tarayıcıyı başlat
                    1 -> showAddProductDialog() // Manuel Ekle seçildiğinde ürün ekleme diyalogunu aç
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "QR kod taranamadı", Toast.LENGTH_LONG).show()
            } else {
                // QR kodun içeriği burada işlenecek, örneğin:
                Toast.makeText(this, "Tarama başarılı: ${result.contents}", Toast.LENGTH_LONG).show()

                // İsterseniz bu noktada ürünü QR koddan ekleyebilirsiniz
                val newProduct = Product(
                    id = UUID.randomUUID().toString(),
                    rfidTag = result.contents, // QR koddan gelen içerik
                    imei = "", // Diğer bilgileri daha sonra doldurabilirsiniz
                    name = "",
                    location = "",
                    address = "",
                    createdDate = getCurrentDate()
                )
                addProductToDatabase(newProduct)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Ürünleri Yönet"
        }
    }


    private fun setupSearchView() {
        binding.productSearchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productAdapter.filter.filter(newText)  // Arama işlemi
                return true
            }
        })
    }

    // Veritabanından ürünleri yükleme
    private fun loadProducts() {
        lifecycleScope.launch {
            productDao.getAllProducts().collect { productList ->
                products.clear()
                products.addAll(productList)
                filteredProducts.clear()
                filteredProducts.addAll(products)  // İlk başta tüm ürünleri göster
                productAdapter.notifyDataSetChanged()  // Adapteri güncelle
            }
        }
    }

    // Ürün eklerken veritabanına eklemek
    private fun addProductToDatabase(newProduct: Product) {
        lifecycleScope.launch {
            productDao.insertProduct(newProduct)
            loadProducts()  // Veritabanı güncellendikten sonra listeyi yeniden yükleyin
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showAddProductDialog() {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)

        // Spinner için ArrayAdapter tanımlıyoruz
        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerLocation.adapter = locationAdapter

        AlertDialog.Builder(this)
            .setTitle("Yeni Ürün Ekle")
            .setView(dialogBinding.root)
            .setPositiveButton("Ekle") { _, _ ->
                val newProduct = Product(
                    id = UUID.randomUUID().toString(), // ID'yi UUID ile dinamik oluştur
                    rfidTag = dialogBinding.editTextRfid.text.toString(),
                    imei = dialogBinding.editTextImei.text.toString(),
                    name = dialogBinding.editTextName.text.toString(),
                    location = dialogBinding.spinnerLocation.selectedItem.toString(),
                    address = dialogBinding.editTextAddress.text.toString(),
                    createdDate = getCurrentDate()
                )
                addProductToDatabase(newProduct)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)

        // Spinner için ArrayAdapter tanımlıyoruz
        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
        val locationAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerLocation.adapter = locationAdapter

        dialogBinding.apply {
            editTextRfid.setText(product.rfidTag)
            editTextImei.setText(product.imei)
            editTextName.setText(product.name)
            spinnerLocation.setSelection(locations.indexOf(product.location))
            editTextAddress.setText(product.address)
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
                    loadProducts()  // Veritabanı güncellendikten sonra listeyi yeniden yükleyin
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
                    loadProducts()  // Veritabanı güncellendikten sonra listeyi yeniden yükleyin
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

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Geri butonuna tıklandığında activity'i kapat
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
