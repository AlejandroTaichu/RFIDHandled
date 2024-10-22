package com.alihantaycu.elterminali.ui.product

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
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
            showAddProductDialog()
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
