package com.alihantaycu.elterminali.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String, // Burada id, UUID olarak kullanılabilir.
    val rfidTag: String,
    val imei: String,
    val name: String,
    val location: String,
    val address: String,
    val createdDate: String,
    val status: String,
    val matchedBoxRfid: String? = null,
    val removedDate: String? = null,
    val isSparePartBox: Boolean = false,
    val sparePartBoxRfid: String? = null
)