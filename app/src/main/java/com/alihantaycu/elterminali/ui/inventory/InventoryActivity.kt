package com.alihantaycu.elterminali.ui.inventory

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.databinding.ActivityInventoryBinding
import com.alihantaycu.elterminali.data.model.Product

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var inventoryAdapter: InventoryAdapter
    private val allProducts = mutableListOf<Product>()
    private val displayedProducts = mutableListOf<Product>()
    private lateinit var warehouseName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        warehouseName = intent.getStringExtra("WAREHOUSE_NAME") ?: "Bilinmeyen Depo"

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        loadInventory()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            title = warehouseName
        }
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter(displayedProducts)
        binding.inventoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@InventoryActivity)
            adapter = inventoryAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.addTextChangedListener { editable ->
            filterProducts(editable.toString())
        }
    }

    private fun loadInventory() {
        allProducts.clear()
        displayedProducts.clear()

        val productsToAdd = when (warehouseName) {
            "Gelen Kargo Depo" -> listOf(
                Product("1", "RFID001", "IMEI001", "Gelen Ürün 1", warehouseName, "Raf A-1", "2023-05-15"),
                Product("2", "RFID002", "IMEI002", "Gelen Ürün 2", warehouseName, "Raf A-2", "2023-05-16"),
                Product("3", "RFID003", "IMEI003", "Gelen Ürün 3", warehouseName, "Raf A-3", "2023-05-17")
            )
            "Giden Kargo Depo" -> listOf(
                Product("4", "RFID004", "IMEI004", "Giden Ürün 1", warehouseName, "Raf B-1", "2023-05-18"),
                Product("5", "RFID005", "IMEI005", "Giden Ürün 2", warehouseName, "Raf B-2", "2023-05-19"),
                Product("6", "RFID006", "IMEI006", "Giden Ürün 3", warehouseName, "Raf B-3", "2023-05-20")
            )
            "Yedek Parça Depo" -> listOf(
                Product("7", "RFID007", "IMEI007", "Yedek Parça 1", warehouseName, "Raf C-1", "2023-05-21"),
                Product("8", "RFID008", "IMEI008", "Yedek Parça 2", warehouseName, "Raf C-2", "2023-05-22"),
                Product("9", "RFID009", "IMEI009", "Yedek Parça 3", warehouseName, "Raf C-3", "2023-05-23")
            )
            else -> emptyList()
        }

        allProducts.addAll(productsToAdd)
        displayedProducts.addAll(productsToAdd)
        inventoryAdapter.notifyDataSetChanged()
    }

    private fun filterProducts(query: String) {
        displayedProducts.clear()
        if (query.isEmpty()) {
            displayedProducts.addAll(allProducts)
        } else {
            displayedProducts.addAll(allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.rfidTag.contains(query, ignoreCase = true) ||
                        product.imei.contains(query, ignoreCase = true) ||
                        product.address.contains(query, ignoreCase = true)
            })
        }
        inventoryAdapter.notifyDataSetChanged()
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
}