<<<<<<< HEAD
package com.alihantaycu.elterminali.data.model

data class Product(
    val id: String,
    val rfidTag: String,
    val imei: String,
    val name: String,
    val location: String,  // Bu, depo adını temsil eder (örn. "Giden Kargo Depo")
    val address: String,   // Bu, depodaki konumu temsil eder (örn. "Raf A-3")
=======
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
>>>>>>> Demo2
    val createdDate: String
)