package com.alihantaycu.elterminali.ui.count

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.databinding.ActivityCountBinding
import java.text.SimpleDateFormat
import java.util.*

class CountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountBinding
    private lateinit var scannedItemsAdapter: ScannedItemsAdapter
    private val scannedItems = mutableListOf<Product>()
    private var isCountingActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupStartCountButton()
    }

    private fun setupRecyclerView() {
        scannedItemsAdapter = ScannedItemsAdapter(scannedItems)
        binding.scannedItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CountActivity)
            adapter = scannedItemsAdapter
        }
    }

    private fun setupStartCountButton() {
        binding.startCountButton.setOnClickListener {
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
    }

    private fun stopCounting() {
        isCountingActive = false
        binding.countStatusTextView.text = "Sayım durduruldu"
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
    }
}