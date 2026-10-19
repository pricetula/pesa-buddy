package com.example.pesabuddy.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["transactionCode"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionCode: String?,
    val senderOrRecipient: String,
    val amount: Double,
    val currency: String,
    val transactionFee: Double?,
    val accountBalance: Double?,
    val type: String,
    val category: String,
    val timestamp: Long,
    val rawMessage: String,
    val isSynced: Boolean = false,
    val isManualEdit: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
