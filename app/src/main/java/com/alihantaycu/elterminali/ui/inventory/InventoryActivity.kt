package com.alihantaycu.elterminali.ui.inventory

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityInventoryBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var inventoryAdapter: InventoryAdapter
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var warehouseName: String
    private var cachedProducts: List<Product> = emptyList()

    // Arama debounce için Job değişkeni
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        warehouseName = intent.getStringExtra("WAREHOUSE_NAME") ?: "Bilinmeyen Depo"

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        observeInventory()

        // Verileri yenile
        viewModel.refreshProducts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = warehouseName
        }
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter(emptyList())
        binding.inventoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@InventoryActivity)
            adapter = inventoryAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.addTextChangedListener { query ->
            // Arama işlevi için debounce kullanıyoruz
            searchJob?.cancel()  // Önceki işi iptal et
            searchJob = MainScope().launch {
                delay(500)  // Kullanıcı yazmayı bitirmesini bekleyelim (500ms)
                filterProducts(query.toString())  // Arama işlemi
            }
        }
    }

    private fun observeInventory() {
        // ViewModel'den gelen ürünleri gözlemleyelim
        viewModel.getProductsByLocation(warehouseName).observe(this) { products ->
            if (products.isEmpty()) {
                showError("Ürünler yüklenemedi.")
            } else {
                cachedProducts = products // Ürünleri bellekte tut
                inventoryAdapter.updateProducts(products) // UI'yı güncelle
            }
        }
    }

    private fun filterProducts(query: String) {
        val filteredProducts = cachedProducts.filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                    product.rfidTag.contains(query, ignoreCase = true) ||
                    product.imei.contains(query, ignoreCase = true) ||
                    product.address.contains(query, ignoreCase = true)
        }
        inventoryAdapter.updateProducts(filteredProducts) // Arama sonuçlarını güncelle
    }

    private fun showError(message: String) {
        // Kullanıcıya hata mesajı göster
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()  // Geri butonuna basıldığında Activity'yi kapat
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
