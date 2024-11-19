package com.alihantaycu.elterminali.ui.match.activity

import MatchOperation
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alihantaycu.elterminali.databinding.ActivityMatchOptionsBinding

class MatchOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatchOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            cardMatchProduct.setOnClickListener {
                startMatchOperation(MatchOperation.MATCH_PRODUCT)
            }
            cardRemoveProduct.setOnClickListener {
                startMatchOperation(MatchOperation.REMOVE_PRODUCT)
            }
            cardAddParts.setOnClickListener {
                startMatchOperation(MatchOperation.ADD_PARTS)
            }
            cardRemoveParts.setOnClickListener {
                startMatchOperation(MatchOperation.REMOVE_PARTS)
            }
        }
    }

    private fun startMatchOperation(operation: MatchOperation) {
        val intent = Intent(this, ProductMatchActivity::class.java).apply {
            putExtra("operation", operation.name)
        }
        startActivity(intent)
    }
}