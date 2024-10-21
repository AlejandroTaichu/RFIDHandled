package com.alihantaycu.elterminali.ui.count

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityCountBinding
import com.alihantaycu.elterminali.ui.inventory.InventoryViewModel
import kotlinx.coroutines.*

class CountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountBinding
    private lateinit var viewModel: InventoryViewModel
    private lateinit var scannedItemsAdapter: ScannedItemsAdapter
    private var isCountingActive = false
    private var countJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViewModel()
        setupRecyclerView()
        setupPlayButton()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(InventoryViewModel::class.java)
    }

    private fun setupRecyclerView() {
        scannedItemsAdapter = ScannedItemsAdapter()
        binding.scannedItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CountActivity)
            adapter = scannedItemsAdapter
        }
    }

    private fun setupPlayButton() {
        binding.playButton.setOnClickListener {
            if (!isCountingActive) {
                startCounting()
            } else {
                stopCounting()
            }
        }
    }

    private fun startCounting() {
        isCountingActive = true
        binding.countStatusTextView.text = "Sayım devam ediyor..."
        binding.playButton.setImageResource(R.drawable.ic_stop) // Stop ikonu oluşturmanız gerekecek
        simulateRFIDScanning()
    }

    private fun stopCounting() {
        isCountingActive = false
        binding.countStatusTextView.text = "Sayım durduruldu"
        binding.playButton.setImageResource(R.drawable.ic_play)
        countJob?.cancel()
    }

    private fun simulateRFIDScanning() {
        viewModel.allProducts.observe(this) { allProducts ->
            countJob = CoroutineScope(Dispatchers.Default).launch {
                val remainingProducts = allProducts.toMutableList()

                while (isActive && remainingProducts.isNotEmpty()) {
                    delay(1000) // 1 saniye bekle
                    val scannedProduct = remainingProducts.random()
                    remainingProducts.remove(scannedProduct)

                    withContext(Dispatchers.Main) {
                        addScannedItem(scannedProduct)
                    }
                }

                if (remainingProducts.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        stopCounting()
                        binding.countStatusTextView.text = "Tüm ürünler sayıldı!"
                    }
                }
            }
        }
    }

    private fun addScannedItem(product: Product) {
        scannedItemsAdapter.addItem(product)
        binding.itemCountTextView.text = "Sayılan ürün: ${scannedItemsAdapter.itemCount}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countJob?.cancel()
    }
}