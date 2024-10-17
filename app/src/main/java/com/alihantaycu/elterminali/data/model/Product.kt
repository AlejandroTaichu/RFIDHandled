package com.alihantaycu.elterminali.data.model

data class Product(
    val id: String,
    val rfidTag: String,
    val imei: String,
    val name: String,
    val location: String,  // Bu, depo adını temsil eder (örn. "Giden Kargo Depo")
    val address: String,   // Bu, depodaki konumu temsil eder (örn. "Raf A-3")
    val createdDate: String
)