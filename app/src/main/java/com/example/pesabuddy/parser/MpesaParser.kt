package com.example.pesabuddy.parser

import com.example.pesabuddy.database.TransactionEntity

class MpesaParser : BaseParser() {
    override fun parse(sender: String, body: String, timestamp: Long): TransactionEntity? {
        val code = extractCode(body) ?: return null
        val (amount, currency) = extractAmount(body) ?: return null
        val fee = extractFee(body)
        val balance = extractBalance(body)
        val type = when {
            body.contains(Regex("received|deposited|credited", RegexOption.IGNORE_CASE)) -> "INCOME"
            body.contains(Regex("sent|paid|withdraw", RegexOption.IGNORE_CASE)) -> "EXPENSE"
            else -> "INCOME"
        }
        return TransactionEntity(
            transactionCode = code,
            senderOrRecipient = sender,
            amount = amount,
            currency = currency,
            transactionFee = fee,
            accountBalance = balance,
            type = type,
            category = "General",
            timestamp = timestamp,
            rawMessage = body
        )
    }
}
