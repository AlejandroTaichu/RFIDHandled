package com.alihantaycu.elterminali.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alihantaycu.elterminali.data.dao.ProductDao
import com.alihantaycu.elterminali.data.entity.Product

@Database(
    entities = [Product::class],  // Sadece Product entity'si
    version = 1,
    exportSchema = false  // Schema export'u kapalı
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao  // Sadece ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}