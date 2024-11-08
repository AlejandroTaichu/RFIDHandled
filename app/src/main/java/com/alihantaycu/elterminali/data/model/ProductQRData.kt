package com.alihantaycu.elterminali.data.model

data class ProductQRData(
    val rfidTag: String = "",
    val imei: String = "",
    val name: String = "",
    val location: String = "",
    val address: String = ""
) {
    override fun toString(): String {
        return "ProductQRData(rfidTag='$rfidTag', imei='$imei', name='$name', location='$location', address='$address')"
    }
}
