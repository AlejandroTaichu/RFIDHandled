package com.alihantaycu.elterminali.data

import com.alihantaycu.elterminali.data.entity.Product
import java.text.SimpleDateFormat
import java.util.*

object SampleDataProvider {
    private val warehouses = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
    private val productTypes = listOf("Telefon", "Tablet", "Laptop", "Kulaklık", "Şarj Aleti")
    private val statuses = listOf("Aktif", "Beklemede", "Teslim Edildi")

    fun createSampleProducts(count: Int): List<Product> {
        val products = mutableListOf<Product>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 1..count) {
            val warehouse = warehouses[i % warehouses.size]
            val productType = productTypes[i % productTypes.size]
            val status = statuses[i % statuses.size]  // Random status

            val product = Product(
                id = UUID.randomUUID().toString(),
                rfidTag = "RFID${String.format("%04d", i)}",
                imei = "IMEI${String.format("%08d", i)}",
                name = "$productType ${i}",
                location = warehouse,
                address = "Raf ${('A'..'Z').random()}-${(1..10).random()}",
                createdDate = dateFormat.format(Date()),
                status = status,  // Pass the random status here
                matchedBoxRfid = "RFID${String.format("%04d", (i + 1) % count)}", // Optional, random RFID
                removedDate = null, // Can be set to a date if needed
                isSparePartBox = (i % 2 == 0),  // Randomly set if the box is a spare part
                sparePartBoxRfid = if (i % 2 == 0) "RFID${String.format("%04d", i + 10)}" else null // Random RFID for spare parts
            )
            products.add(product)
        }
        return products
    }
}
