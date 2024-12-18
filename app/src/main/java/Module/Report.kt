package com.example.myapplication

data class Report(
    val unitID: Int = 0,
    val monitorID: Int = 0,
    val mouseID: Int = 0,
    val keyboardID: Int = 0,
    val mousePadID: Int = 0,
    val unitQuantity: Int = 0,
    val timestamp: Any? = null,
    val reason: String? = null,
    var isComplete: Boolean = false,  // New field for "Complete" checkbox
    var isRepairInProcess: Boolean = false,
    var dialogIssueNoted: Boolean = false,// New field for "Repair in Process" checkbox
)








