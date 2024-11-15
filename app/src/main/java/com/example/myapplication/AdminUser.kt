package com.example.myapplication

data class AdminUser
        (val id: String = "",
         val name: String = "",
         val email: String = "",
         val password: String = "",
         val uid: String = "",
         val role: String = "User")