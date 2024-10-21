package com.alihantaycu.elterminali.data

import com.alihantaycu.elterminali.data.entity.Product
import java.text.SimpleDateFormat
import java.util.*

object SampleDataProvider {
    private val warehouses = listOf("Gelen Kargo Depo", "Giden Kargo Depo", "Yedek Parça Depo")
    private val productTypes = listOf("Telefon", "Tablet", "Laptop", "Kulaklık", "Şarj Aleti")

    fun createSampleProducts(count: Int): List<Product> {
        val products = mutableListOf<Product>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 1..count) {
            val warehouse = warehouses[i % warehouses.size]
            val productType = productTypes[i % productTypes.size]
            val product = Product(
                id = UUID.randomUUID().toString(),
                rfidTag = "RFID${String.format("%04d", i)}",
                imei = "IMEI${String.format("%08d", i)}",
                name = "$productType ${i}",
                location = warehouse,
                address = "Raf ${('A'..'Z').random()}-${(1..10).random()}",
                createdDate = dateFormat.format(Date())
            )
            products.add(product)
        }
        return products
    }
}