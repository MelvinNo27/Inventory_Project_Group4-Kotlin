package com.example.myapplication

data class AdminUser
        (  val uid: String = "",
         val name: String = "",
         val email: String = "",
         val password: String = "",
         val role: String = "user"
                )