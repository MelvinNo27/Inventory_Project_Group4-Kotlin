package com.example.myapplication

data class Report(
    val unitID: String = "",
    val timestamp: Any? = null,
    val reason: String = "",
    val monitorID: String = "",
    val mouseID: String = "",
    val keyboardID: String = "",
    val mousePadID: String = "",
    val AVRID: String = "",
    val monitorQuantity: Int = 0,
    val mouseQuantity: Int = 0,
    val keyboardQuantity: Int = 0,
    val mousePadQuantity: Int = 0,
    val unitQuantity: Int = 0,
    val AVRQuantity: Int = 0,
    val roomNumber: String = "",
    var status: String = "Issue Noted",
    val unitName: String? = null,
    var isComplete: Boolean = false,
    var isRepairInProcess: Boolean = false,
    var dialogIssueNoted: Boolean = false,
    var reportId: String? = null
)
