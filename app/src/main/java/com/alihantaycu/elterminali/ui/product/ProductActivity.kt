package com.alihantaycu.elterminali.ui.product

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
import java.text.SimpleDateFormat
import java.util.*

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupAddProductFab()
        loadProducts()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products,
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

    private fun loadProducts() {
        // TODO: Load products from a data source (e.g., local database or API)
        // For now, we'll add some dummy data
        products.addAll(listOf(
            Product("1", "RFID001", "IMEI001", "Ürün 1", "Gelen Kargo Depo", "Raf A", getCurrentDate()),
            Product("2", "RFID002", "IMEI002", "Ürün 2", "Giden Kargo Depo", "Raf B", getCurrentDate()),
            Product("3", "RFID003", "IMEI003", "Ürün 3", "Yedek Parça Depo", "Raf C", getCurrentDate())
        ))
        productAdapter.notifyDataSetChanged()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun showAddProductDialog() {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle("Yeni Ürün Ekle")
            .setView(dialogBinding.root)
            .setPositiveButton("Ekle") { _, _ ->
                val newProduct = Product(
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
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)
        dialogBinding.apply {
            editTextRfid.setText(product.rfidTag)
            editTextImei.setText(product.imei)
            editTextName.setText(product.name)
            editTextLocation.setText(product.location)
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
                    location = dialogBinding.editTextLocation.text.toString(),
                    address = dialogBinding.editTextAddress.text.toString()
                )
                val index = products.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    products[index] = updatedProduct
                    productAdapter.notifyItemChanged(index)
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
                val index = products.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    products.removeAt(index)
                    productAdapter.notifyItemRemoved(index)
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
}