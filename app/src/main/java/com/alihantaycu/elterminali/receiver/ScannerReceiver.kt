package com.alihantaycu.elterminali.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScannerReceiver : BroadcastReceiver() {
    companion object {
        private var scanCallback: ((String) -> Unit)? = null

        fun setScanCallback(callback: (String) -> Unit) {
            scanCallback = callback
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Scanner", "Intent alındı: ${intent?.action}")

        // Tüm extra'ları logla
        intent?.extras?.let { bundle ->
            bundle.keySet().forEach { key ->
                Log.d("Scanner", "Key: $key, Value: ${bundle.get(key)}")
            }
        }

        val barcode = when (intent?.action) {
            "scan.rcv.message" -> intent.getStringExtra("barocode")
            "android.intent.action.DECODE_DATA" -> intent.getStringExtra("barcode_data")
            "android.intent.action.SCANRESULT" -> intent.getStringExtra("value")
            else -> null
        }

        barcode?.let {
            Log.d("Scanner", "Barkod okundu: $it")
            scanCallback?.invoke(it)
        }
    }
}