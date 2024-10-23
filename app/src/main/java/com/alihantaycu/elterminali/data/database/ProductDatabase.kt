// ProductDatabase.kt
package com.alihantaycu.elterminali.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.data.dao.ProductDao

@Database(entities = [Product::class], version = 1)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
