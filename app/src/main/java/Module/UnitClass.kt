package com.example.myapplication

data class UnitClass(
    val monitorID: Int = 0,
    val mouseID: Int = 0,
    val keyboardID: Int = 0,
    val mousePadID: Int = 0,
    val unitID: Int = 0,
    val monitorQuantity: Int = 0,
    val mouseQuantity: Int = 0,
    val keyboardQuantity: Int = 0,
    val mousePadQuantity: Int = 0,
    val unitQuantity: Int = 0,
    val roomName: String? = null, // Room Number (nullable)
    val unitNumber: String? = null  // Unit Number (nullable)
)
