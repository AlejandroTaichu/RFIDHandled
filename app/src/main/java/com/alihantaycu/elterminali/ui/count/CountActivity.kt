package com.alihantaycu.elterminali.ui.count

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alihantaycu.elterminali.R
import com.alihantaycu.elterminali.databinding.ActivityCountBinding
import com.alihantaycu.elterminali.data.entity.Product
import com.alihantaycu.elterminali.ui.count.activity.RFIDSettingsActivity
import com.ubx.usdk.USDKManager
import com.ubx.usdk.rfid.RfidManager
import com.ubx.usdk.rfid.aidl.IRfidCallback
import com.ubx.usdk.util.QueryMode
import java.util.*

class CountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCountBinding
    private lateinit var viewModel: CountViewModel
    private lateinit var scannedItemsAdapter: ScannedItemsAdapter
    private var isCountingActive = false
    private var mRfidManager: RfidManager? = null
    private var RFID_INIT_STATUS = false


    private val prefs by lazy {
        getSharedPreferences("rfid_settings", Context.MODE_PRIVATE)
    }

    private val callback = object : IRfidCallback {
        override fun onInventoryTag(EPC: String, TID: String, strRSSI: String) {
            // Her RFID okunduğunda yapılacak işlemler
            runOnUiThread {
                if (prefs.getBoolean("sound_enabled", true)) {  // Ayarlardan kontrol
                    SoundTool.getInstance(this@CountActivity).playBeep()
                }

                val product = Product(
                    id = UUID.randomUUID().toString(),
                    rfidTag = EPC,
                    imei = "",  // IMEI bilgisini veritabanından sorgulayabilirsiniz
                    name = "Ürün $EPC",
                    location = "",
                    address = "",
                    createdDate = Date().toString(),
                    status = "SCANNED"
                )
                scannedItemsAdapter.addItem(product)
                updateItemCount()
            }
        }

        override fun onInventoryTagEnd() {
            // Tarama döngüsü bittiğinde yapılacak işlemler
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViewModel()
        setupRecyclerView()
        setupPlayButton()
        initRfid()
        setupToolbar()
    }

    private fun initRfid() {
        USDKManager.getInstance().init(applicationContext, object : USDKManager.InitListener {
            override fun onStatus(status: USDKManager.STATUS) {
                Log.d("RFID", "RFID init status: $status")
                if (status == USDKManager.STATUS.SUCCESS) {
                    mRfidManager = USDKManager.getInstance().rfidManager
                    mRfidManager?.let {
                        it.setOutputPower(30.toByte())
                        it.setQueryMode(QueryMode.EPC)
                        RFID_INIT_STATUS = true
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CountActivity, "RFID başlatılamadı", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(CountViewModel::class.java)
    }

    private fun setupRecyclerView() {
        scannedItemsAdapter = ScannedItemsAdapter()
        binding.scannedItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CountActivity)
            adapter = scannedItemsAdapter
        }
    }

    private fun setupPlayButton() {
        binding.playButton.setOnClickListener {
            if (!isCountingActive) {
                startCounting()
            } else {
                stopCounting()
            }
        }
    }

    private fun startCounting() {
        if (!RFID_INIT_STATUS) {
            Toast.makeText(this, "RFID hazır değil", Toast.LENGTH_SHORT).show()
            return
        }

        // Sadece sayım başlarken listeyi temizle
        scannedItemsAdapter.clear()
        binding.itemCountTextView.text = "Sayılan ürün: 0"

        isCountingActive = true
        binding.countStatusTextView.text = "Sayım devam ediyor..."
        binding.playButton.setImageResource(R.drawable.ic_stop)

        mRfidManager?.let {
            it.registerCallback(callback)
            it.startInventory(0.toByte())
        }
    }

    private fun stopCounting() {
        isCountingActive = false
        binding.countStatusTextView.text = "Sayım durduruldu"
        binding.playButton.setImageResource(R.drawable.ic_play)

        mRfidManager?.let {
            it.stopInventory()
        }
        // Sayaç textini sıfırla
        binding.itemCountTextView.text = "Sayılan ürün: 0"
    }

    private fun updateItemCount() {
        binding.itemCountTextView.text = "Sayılan ürün: ${scannedItemsAdapter.itemCount}"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_count, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, RFIDSettingsActivity::class.java))
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundTool.getInstance(this).release()
        mRfidManager?.let {
            it.disConnect()
            it.release()
        }
        RFID_INIT_STATUS = false
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)  // Geri tuşunu göster
            setDisplayShowHomeEnabled(true)  // Home ikonunu göster
        }
    }

}