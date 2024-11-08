package com.alihantaycu.elterminali.ui.inventory

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.databinding.ActivityInventoryBinding

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var viewModel: InventoryViewModel
    private lateinit var warehouseName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        warehouseName = intent.getStringExtra("WAREHOUSE_NAME") ?: "Bilinmeyen Depo"

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupSearchView()
        loadInventory()

        viewModel.refreshProducts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            title = warehouseName
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(InventoryViewModel::class.java)
    }

    private fun setupRecyclerView() {
        inventoryAdapter = InventoryAdapter(emptyList())
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
        viewModel.getProductsByLocation(warehouseName).observe(this) { products ->
            inventoryAdapter.updateProducts(products)
        }
    }

    private fun filterProducts(query: String) {
        viewModel.getProductsByLocation(warehouseName).observe(this) { products ->
            val filteredProducts = products.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.rfidTag.contains(query, ignoreCase = true) ||
                        product.imei.contains(query, ignoreCase = true) ||
                        product.address.contains(query, ignoreCase = true)
            }
            inventoryAdapter.updateProducts(filteredProducts)
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
}