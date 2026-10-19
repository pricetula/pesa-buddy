package com.example.pesabuddy.parser

abstract class BaseParser : SmsParser {
    protected val amountPattern = java.util.regex.Pattern.compile(
        "(\\d[\\d,]*\\.\\d{2})\\s*(KES|Ksh|CDF|USD|\\$)",
        java.util.regex.Pattern.CASE_INSENSITIVE
    )
    protected val codePattern = java.util.regex.Pattern.compile(
        "^([A-Z0-9]{8,10})",
        java.util.regex.Pattern.CASE_INSENSITIVE
    )
    protected val balancePattern = java.util.regex.Pattern.compile(
        "balance[:\\s]*([\\d,]+\\.\\d{2})\\s*(KES|Ksh|CDF|USD|\\$)?",
        java.util.regex.Pattern.CASE_INSENSITIVE
    )
    protected val feePattern = java.util.regex.Pattern.compile(
        "fee[:\\s]*([\\d,]+\\.\\d{2})\\s*(KES|Ksh|CDF|USD|\\$)?",
        java.util.regex.Pattern.CASE_INSENSITIVE
    )

    protected fun extractAmount(body: String): Pair<Double, String>? {
        val m = amountPattern.matcher(body)
        return if (m.find()) {
            val rawStr = m.group(1) ?: return null
            val raw = rawStr.replace(",", "").toDoubleOrNull() ?: return null
            val curStr = m.group(2) ?: return null
            val cur = curStr.uppercase().let { if (it == "KSH") "KES" else it }
            raw to cur
        } else null
    }

    protected fun extractCode(body: String): String? =
        codePattern.matcher(body).let { if (it.find()) it.group(1) else null }

    protected fun extractBalance(body: String): Double? =
        balancePattern.matcher(body).let {
            if (it.find()) {
                val g = it.group(1) ?: return@let null
                g.replace(",", "").toDoubleOrNull()
            } else null
        }

    protected fun extractFee(body: String): Double? =
        feePattern.matcher(body).let {
            if (it.find()) {
                val g = it.group(1) ?: return@let null
                g.replace(",", "").toDoubleOrNull()
            } else null
        }
}
