package com.alihantaycu.elterminali.ui.warehouse

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.databinding.ActivityWarehouseSelectionBinding
import com.alihantaycu.elterminali.ui.inventory.InventoryActivity

class WarehouseSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWarehouseSelectionBinding
    private lateinit var warehouseAdapter: WarehouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWarehouseSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Depo Seçimi"
        }
    }

    private fun setupRecyclerView() {
        val warehouses = listOf(
            Warehouse("Gelen Kargo Depo", "Gelen kargolar için depo", R.drawable.incoming_cargo_warehouse),
            Warehouse("Giden Kargo Depo", "Giden kargolar için depo", R.drawable.outgoin_cargo_warehouse),
            Warehouse("Yedek Parça Depo", "Yedek parçalar için depo", R.drawable.spare_parts_warehouse)
        )

        warehouseAdapter = WarehouseAdapter(warehouses) { warehouse ->
            navigateToInventory(warehouse.name)
        }

        binding.warehouseRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WarehouseSelectionActivity)
            adapter = warehouseAdapter
        }
    }

    private fun navigateToInventory(warehouseName: String) {
        val intent = Intent(this, InventoryActivity::class.java).apply {
            putExtra("WAREHOUSE_NAME", warehouseName)
        }
        startActivity(intent)
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