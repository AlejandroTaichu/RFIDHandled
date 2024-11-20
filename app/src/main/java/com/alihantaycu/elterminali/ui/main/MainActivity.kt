package com.alihantaycu.elterminali.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alihantaycu.elterminali.databinding.ActivityMainBinding
import com.alihantaycu.elterminali.ui.warehouse.WarehouseSelectionActivity
import com.alihantaycu.elterminali.ui.count.CountActivity
import com.alihantaycu.elterminali.ui.match.activity.MatchOptionsActivity
import com.alihantaycu.elterminali.ui.product.ProductActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupCardClicks()
    }

    private fun setupCardClicks() {
        binding.viewInventoryCard.setOnClickListener {
            startActivity(Intent(this, WarehouseSelectionActivity::class.java))
        }

        binding.countInventoryCard.setOnClickListener {
            startActivity(Intent(this, CountActivity::class.java))
        }

        binding.manageProductsCard.setOnClickListener {
            startActivity(Intent(this, ProductActivity::class.java))
        }

        binding.matchOperationsCard.setOnClickListener {
            startActivity(Intent(this, MatchOptionsActivity::class.java))
        }
    }
}