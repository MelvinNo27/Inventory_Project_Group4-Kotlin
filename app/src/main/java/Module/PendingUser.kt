package com.example.myapplication

data class PendingUser(
    val email: String = "",
    val name: String = "",
    val password: String = "",
    var status: String = "pending" // "pending" or "approved"
)