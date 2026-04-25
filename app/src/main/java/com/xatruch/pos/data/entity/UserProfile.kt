package com.xatruch.pos.data.entity

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val registrationDate: Long = System.currentTimeMillis()
)
