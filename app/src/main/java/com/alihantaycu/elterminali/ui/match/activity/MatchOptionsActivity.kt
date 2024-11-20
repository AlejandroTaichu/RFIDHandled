package com.alihantaycu.elterminali.ui.match.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupClickListeners() {
        binding.cardProductOperations.setOnClickListener {
            val intent = Intent(this, com.alihantaycu.elterminali.ui.match.activity.PartsMatchActivity::class.java)
            intent.putExtra("operation_type", "PRODUCT")
            startActivity(intent)
        }

        binding.cardSparePartOperations.setOnClickListener {
            val intent = Intent(this, com.alihantaycu.elterminali.ui.match.activity.PartsMatchActivity::class.java)
            intent.putExtra("operation_type", "SPARE_PART")
            startActivity(intent)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}