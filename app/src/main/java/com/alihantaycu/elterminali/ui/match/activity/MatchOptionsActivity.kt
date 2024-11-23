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
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)  // Geri tuşunu göster
            setDisplayShowHomeEnabled(true)  // Home ikonunu göster
            setDisplayShowTitleEnabled(false) // Varsayılan başlığı gizle
        }
    }

    private fun setupClickListeners() {
        binding.cardProductOperations.setOnClickListener {
            startProductMatchActivity(isSparePartOperation = false)
        }

        binding.cardSparePartOperations.setOnClickListener {
            startProductMatchActivity(isSparePartOperation = true)
        }
    }

    private fun startProductMatchActivity(isSparePartOperation: Boolean) {
        val intent = Intent(this, ProductMatchActivity::class.java).apply {
            putExtra("isSparePartOperation", isSparePartOperation)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()  // Activity'yi sonlandır
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}