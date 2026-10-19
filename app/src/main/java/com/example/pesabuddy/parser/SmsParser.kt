package com.example.pesabuddy.parser

import com.example.pesabuddy.database.TransactionEntity

interface SmsParser {
    fun parse(sender: String, body: String, timestamp: Long): TransactionEntity?
}
