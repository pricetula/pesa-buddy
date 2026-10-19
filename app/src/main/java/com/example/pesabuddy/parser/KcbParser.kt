package com.example.pesabuddy.parser

import com.example.pesabuddy.database.TransactionEntity

class KcbParser : BaseParser() {
    override fun parse(sender: String, body: String, timestamp: Long): TransactionEntity? {
        val code = extractCode(body) ?: return null
        val (amount, currency) = extractAmount(body) ?: return null
        val fee = extractFee(body)
        val balance = extractBalance(body)
        val type = if (body.contains(Regex("credit", RegexOption.IGNORE_CASE))) "INCOME" else "EXPENSE"
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
