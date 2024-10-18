package com.alihantaycu.elterminali.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val rfidTag: String,
    val imei: String,
    val name: String,
    val location: String,
    val address: String,
    val createdDate: String
)