package com.example.pesabuddy.parser

object ParserFactory {
    fun create(normalizedSender: String): SmsParser? = when {
        normalizedSender.startsWith("MPESA") || normalizedSender.contains("MPESA") -> MpesaParser()
        normalizedSender.contains("EQUITY") || normalizedSender.contains("BCDC") -> EquityBankParser()
        normalizedSender.contains("KCB") -> KcbParser()
        else -> null
    }
}
