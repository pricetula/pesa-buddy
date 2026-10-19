package com.example.pesabuddy.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unparsed_sms")
data class UnparsedSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val rawMessage: String,
    val timestamp: Long,
    val isParsed: Boolean = false
)
