package com.example.pesabuddy.database

import androidx.room.Room
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TransactionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.transactionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insert idempotent on transactionCode`() = runTest {
        val entity = TransactionEntity(
            transactionCode = "IDEMP1234",
            senderOrRecipient = "MPESA",
            amount = 1000.0,
            currency = "KES",
            transactionFee = 10.0,
            accountBalance = 5000.0,
            type = "INCOME",
            category = "General",
            timestamp = System.currentTimeMillis(),
            rawMessage = "test"
        )

        val firstId = dao.insert(entity)
        assertNotNull(firstId)
        assertEquals(true, firstId > 0)

        // Insert same code again with different amount
        val duplicate = entity.copy(amount = 9999.0, senderOrRecipient = "MPESA2")
        val secondId = dao.insert(duplicate)
        assertEquals(-1L, secondId)

        val fetched = dao.findByCode("IDEMP1234")
        assertNotNull(fetched)
        assertEquals(1000.0, fetched!!.amount, 0.01)
        assertEquals("MPESA", fetched.senderOrRecipient)
    }

    @Test
    fun `insert null code allows multiple inserts`() = runTest {
        val entity1 = TransactionEntity(
            transactionCode = null,
            senderOrRecipient = "MPESA",
            amount = 100.0,
            currency = "KES",
            transactionFee = null,
            accountBalance = null,
            type = "EXPENSE",
            category = "General",
            timestamp = System.currentTimeMillis(),
            rawMessage = "test1"
        )
        val entity2 = entity1.copy(amount = 200.0, rawMessage = "test2")

        val id1 = dao.insert(entity1)
        val id2 = dao.insert(entity2)
        assertNotNull(id1)
        assertNotNull(id2)
        assertEquals(true, id1 > 0)
        assertEquals(true, id2 > 0)
    }
}
