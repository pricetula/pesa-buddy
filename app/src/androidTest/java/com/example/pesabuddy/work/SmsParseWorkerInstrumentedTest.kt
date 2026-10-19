package com.example.pesabuddy.work

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsParseWorkerInstrumentedTest {

    @Test
    fun workerCanBeEnqueued() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val workManager = WorkManager.getInstance(context)
        val work = OneTimeWorkRequestBuilder<SmsParseWorker>()
            .setInputData(
                workDataOf(
                    "sender" to "MPESA",
                    "body" to "9ZQ8K7L9 REF: You have received 1,500.00 KES.",
                    "timestamp" to System.currentTimeMillis()
                )
            )
            .build()

        workManager.enqueue(work)
        assertNotNull(work.id)
    }
}
