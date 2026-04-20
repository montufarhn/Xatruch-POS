package com.xatruch.pos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_data")
data class BusinessData(
    @PrimaryKey val id: Int = 1,
    val logoUri: String? = null,
    val name: String = "",
    val address: String = "",
    val phone1: String = "",
    val phone2: String? = null,
    val email: String? = null,
    val rtn: String = "",
    val cai: String = "",
    val billingRange: String = "",
    val initialInvoiceNumber: Int = 1,
    val currentInvoiceNumber: Int = 1
)
