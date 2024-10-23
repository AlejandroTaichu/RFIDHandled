package com.alihantaycu.elterminali.ui.count

import android.os.Bundle
<<<<<<< HEAD
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.databinding.ActivityCountBinding
import com.alihantaycu.elterminali.data.model.Product
import java.text.SimpleDateFormat
import java.util.*
=======
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityCountBinding
import com.alihantaycu.elterminali.ui.inventory.InventoryViewModel
import kotlinx.coroutines.*
>>>>>>> Demo2

class CountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountBinding
<<<<<<< HEAD
    private lateinit var scannedItemsAdapter: ScannedItemsAdapter
    private val scannedItems = mutableListOf<Product>()
    private var isCountingActive = false
=======
    private lateinit var viewModel: InventoryViewModel
    private lateinit var scannedItemsAdapter: ScannedItemsAdapter
    private var isCountingActive = false
    private var countJob: Job? = null
>>>>>>> Demo2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

<<<<<<< HEAD
        setupRecyclerView()
        setupStartCountButton()
    }

    private fun setupRecyclerView() {
        scannedItemsAdapter = ScannedItemsAdapter(scannedItems)
=======
        setupViewModel()
        setupRecyclerView()
        setupPlayButton()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(InventoryViewModel::class.java)
    }

    private fun setupRecyclerView() {
        scannedItemsAdapter = ScannedItemsAdapter()
>>>>>>> Demo2
        binding.scannedItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CountActivity)
            adapter = scannedItemsAdapter
        }
    }

<<<<<<< HEAD
    private fun setupStartCountButton() {
        binding.startCountButton.setOnClickListener {
=======
    private fun setupPlayButton() {
        binding.playButton.setOnClickListener {
>>>>>>> Demo2
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
<<<<<<< HEAD
        binding.startCountButton.text = "Sayımı Durdur"
        // TODO: RFID okuyucu API'sini başlat
        // Simülasyon için bir ürün ekleyelim
        addScannedItem(Product(
            id = "1",
            rfidTag = "RFID001",
            imei = "IMEI001",
            name = "Gelen Ürün 1",
            location = "Gelen Kargo Depo",
            address = "A1",
            createdDate = getCurrentDate()
        ))
=======
        binding.playButton.setImageResource(R.drawable.ic_stop) // Stop ikonu oluşturmanız gerekecek
        simulateRFIDScanning()
>>>>>>> Demo2
    }

    private fun stopCounting() {
        isCountingActive = false
        binding.countStatusTextView.text = "Sayım durduruldu"
<<<<<<< HEAD
        binding.startCountButton.text = "Sayımı Başlat"
        // TODO: RFID okuyucu API'sini durdur
    }

    private fun addScannedItem(product: Product) {
        scannedItems.add(product)
        scannedItemsAdapter.notifyItemInserted(scannedItems.size - 1)
        binding.itemCountTextView.text = "Sayılan ürün: ${scannedItems.size}"
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
=======
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
>>>>>>> Demo2
    }
}