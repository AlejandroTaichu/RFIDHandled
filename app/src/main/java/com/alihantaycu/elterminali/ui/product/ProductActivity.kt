package com.alihantaycu.elterminali.ui.product

<<<<<<< HEAD
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.model.Product
import com.alihantaycu.elterminali.databinding.ActivityProductBinding
import com.alihantaycu.elterminali.databinding.DialogAddEditProductBinding
=======
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
>>>>>>> Demo2
import java.text.SimpleDateFormat
import java.util.*

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
<<<<<<< HEAD

=======
    private val filteredProducts = mutableListOf<Product>()

    // ProductDao'yu veritabanına erişim için ekleyelim
    private lateinit var productDao: ProductDao
>>>>>>> Demo2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

<<<<<<< HEAD
        setupRecyclerView()
        setupAddProductFab()
=======
        // DAO'yu başlatıyoruz
        val productDatabase = AppDatabase.getDatabase(this)  // Veritabanını başlat
        productDao = productDatabase.productDao()  // ProductDao'yu al

        setupRecyclerView()
        setupAddProductFab()
        setupSearchView()
>>>>>>> Demo2
        loadProducts()
    }

    private fun setupRecyclerView() {
<<<<<<< HEAD
        productAdapter = ProductAdapter(products,
=======
        productAdapter = ProductAdapter(filteredProducts,
>>>>>>> Demo2
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
<<<<<<< HEAD
            showAddProductDialog()
        }
    }

    private fun loadProducts() {
        // TODO: Load products from a data source (e.g., local database or API)
        // For now, we'll add some dummy data
        products.addAll(listOf(
            Product("1", "RFID001", "IMEI001", "Ürün 1", "Gelen Kargo Depo", "Raf A", getCurrentDate()),
            Product("2", "RFID002", "IMEI002", "Ürün 2", "Giden Kargo Depo", "Raf B", getCurrentDate()),
            Product("3", "RFID003", "IMEI003", "Ürün 3", "Yedek Parça Depo", "Raf C", getCurrentDate())
        ))
        productAdapter.notifyDataSetChanged()
=======
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
>>>>>>> Demo2
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showAddProductDialog() {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)
<<<<<<< HEAD
=======

        // Spinner için ArrayAdapter tanımlıyoruz
        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerLocation.adapter = locationAdapter

>>>>>>> Demo2
        AlertDialog.Builder(this)
            .setTitle("Yeni Ürün Ekle")
            .setView(dialogBinding.root)
            .setPositiveButton("Ekle") { _, _ ->
                val newProduct = Product(
<<<<<<< HEAD
                    id = (products.size + 1).toString(),
                    rfidTag = dialogBinding.editTextRfid.text.toString(),
                    imei = dialogBinding.editTextImei.text.toString(),
                    name = dialogBinding.editTextName.text.toString(),
                    location = dialogBinding.editTextLocation.text.toString(),
                    address = dialogBinding.editTextAddress.text.toString(),
                    createdDate = java.util.Date().toString()
                )
                products.add(newProduct)
                productAdapter.notifyItemInserted(products.size - 1)
=======
                    id = UUID.randomUUID().toString(), // ID'yi UUID ile dinamik oluştur
                    rfidTag = dialogBinding.editTextRfid.text.toString(),
                    imei = dialogBinding.editTextImei.text.toString(),
                    name = dialogBinding.editTextName.text.toString(),
                    location = dialogBinding.spinnerLocation.selectedItem.toString(),
                    address = dialogBinding.editTextAddress.text.toString(),
                    createdDate = getCurrentDate()
                )
                addProductToDatabase(newProduct)
>>>>>>> Demo2
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)
<<<<<<< HEAD
=======

        // Spinner için ArrayAdapter tanımlıyoruz
        val locations = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
        val locationAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerLocation.adapter = locationAdapter

>>>>>>> Demo2
        dialogBinding.apply {
            editTextRfid.setText(product.rfidTag)
            editTextImei.setText(product.imei)
            editTextName.setText(product.name)
<<<<<<< HEAD
            editTextLocation.setText(product.location)
=======
            spinnerLocation.setSelection(locations.indexOf(product.location))
>>>>>>> Demo2
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
<<<<<<< HEAD
                    location = dialogBinding.editTextLocation.text.toString(),
                    address = dialogBinding.editTextAddress.text.toString()
                )
                val index = products.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    products[index] = updatedProduct
                    productAdapter.notifyItemChanged(index)
=======
                    location = dialogBinding.spinnerLocation.selectedItem.toString(),
                    address = dialogBinding.editTextAddress.text.toString()
                )
                lifecycleScope.launch {
                    productDao.updateProduct(updatedProduct)
                    loadProducts()  // Veritabanı güncellendikten sonra listeyi yeniden yükleyin
>>>>>>> Demo2
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }
<<<<<<< HEAD
=======

>>>>>>> Demo2
    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Ürünü Sil")
            .setMessage("Bu ürünü silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _, _ ->
<<<<<<< HEAD
                val index = products.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    products.removeAt(index)
                    productAdapter.notifyItemRemoved(index)
=======
                lifecycleScope.launch {
                    productDao.deleteProduct(product)
                    loadProducts()  // Veritabanı güncellendikten sonra listeyi yeniden yükleyin
>>>>>>> Demo2
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
<<<<<<< HEAD
}
=======

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
>>>>>>> Demo2
