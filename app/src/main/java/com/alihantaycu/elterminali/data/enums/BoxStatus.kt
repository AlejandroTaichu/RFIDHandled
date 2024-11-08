package com.alihantaycu.elterminali.data.enums

enum class BoxStatus {
    EMPTY,           // Boş kutu
    RECEIVED,        // Mal kabul
    IN_STORAGE,      // Depoda
    AT_TECHNICIAN,   // Teknisyende
    IN_QC,          // Kalite kontrolde
    IN_PACKAGING,    // Kutulamada
    COMPLETED,       // Tamamlandı
}