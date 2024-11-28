package com.alihantaycu.elterminali.ui.count.activity

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.databinding.ActivityRfidSettingsBinding
import com.ubx.usdk.USDKManager


class RFIDSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRfidSettingsBinding
    private var mRfidManager = USDKManager.getInstance().rfidManager

    // Ayarlar için SharedPreferences
    private val prefs by lazy {
        getSharedPreferences("rfid_settings", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRfidSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupPowerSettings()
        setupFilterSettings()
        setupSoundSettings()
        setupStatsDisplay()
        setupToolbar()
    }

    private fun setupPowerSettings() {
        // Güç presetleri
        binding.powerPresetGroup.setOnCheckedChangeListener { _, checkedId ->
            val power = when (checkedId) {
                R.id.powerLow -> 15  // Yakın mesafe
                R.id.powerMedium -> 25 // Orta mesafe
                R.id.powerHigh -> 30  // Uzak mesafe
                else -> 20
            }
            mRfidManager?.setOutputPower(power.toByte())
            prefs.edit().putInt("power_preset", checkedId).apply()
        }

        // Önceki seçimi yükle
        binding.powerPresetGroup.check(prefs.getInt("power_preset", R.id.powerMedium))
    }

    private fun setupFilterSettings() {
        // RSSI filtresi
        binding.rssiThresholdSwitch.isChecked = prefs.getBoolean("rssi_filter_enabled", false)
        binding.rssiThresholdSlider.value = prefs.getFloat("rssi_threshold", -60f)

        binding.rssiThresholdSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.rssiThresholdSlider.isEnabled = isChecked
            prefs.edit().putBoolean("rssi_filter_enabled", isChecked).apply()
        }

        binding.rssiThresholdSlider.addOnChangeListener { slider, value, fromUser ->
            prefs.edit().putFloat("rssi_threshold", value).apply()
        }
    }

    private fun setupSoundSettings() {
        // Ses ayarları
        binding.soundEnabledSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        binding.vibrationEnabledSwitch.isChecked = prefs.getBoolean("vibration_enabled", true)

        binding.soundEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
        }

        binding.vibrationEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }
    }

    private fun setupStatsDisplay() {
        // İstatistik gösterimi
        mRfidManager?.let { rfid ->
            // binding üzerinden erişim
            binding.apply {
                deviceIdText.text = "Cihaz ID: ${rfid.deviceId}"
                firmwareText.text = "Firmware: ${rfid.firmwareVersion}"
                totalReadsText.text = "Toplam Okuma: ${prefs.getLong("total_reads", 0)}"
                successRateText.text = "Başarı Oranı: ${prefs.getFloat("success_rate", 0f)}%"
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Sayıma Geri DÖn"
        }
    }

    // Geri butonunun işlevini tanımlıyoruz
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Geri butonuna tıklanırsa, önceden çağrılan aktiviteye geri dön
                onBackPressed()  // veya finish() kullanabilirsiniz
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
