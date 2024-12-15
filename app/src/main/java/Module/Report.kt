package com.example.myapplication

data class Report(
    val unitID: Int,
    val monitorID: Int,
    val mouseID: Int,
    val keyboardID: Int,
    val mousePadID: Int,
    val unitQuantity: Int,
    val reason: String
)