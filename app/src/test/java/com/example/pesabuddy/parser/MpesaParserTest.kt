package com.example.pesabuddy.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MpesaParserTest {

    @Test
    fun `sad path MPESA missing amount returns null`() {
        val parser = MpesaParser()
        val body = "REF: You have received . New balance 5,200.00 KES."
        val tx = parser.parse("MPESA", body, System.currentTimeMillis())
        org.junit.Assert.assertNull(tx)
    }

    @Test
    fun `happy path MPESA income`() {
        val parser = MpesaParser()
        val body = "9ZQ8K7L9 REF: You have received 1,500.00 KES. New balance 5,200.00 KES."
        val tx = parser.parse("MPESA", body, System.currentTimeMillis())
        assertNotNull(tx)
        assertEquals("9ZQ8K7L9", tx!!.transactionCode)
        assertEquals(1500.0, tx.amount, 0.01)
        assertEquals("KES", tx.currency)
        assertEquals("INCOME", tx.type)
    }
}
