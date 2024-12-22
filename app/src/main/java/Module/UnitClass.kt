package com.example.myapplication


data class UnitClass(
    val name: String = "",
    val monitorID: String = "",
    val mouseID: String = "",
    val keyboardID: String = "",
    val mousePadID: String = "",
    val unitID: String = "",
    val avrID: String = "",
    val monitorQuantity: Int = 0,
    val mouseQuantity: Int = 0,
    val keyboardQuantity: Int = 0,
    val mousePadQuantity: Int = 0,
    val unitQuantity: Int = 0,
    val avrQuantity: Int = 0,
    val reason: String? = null,
    val timestamp: Any? = null
)
