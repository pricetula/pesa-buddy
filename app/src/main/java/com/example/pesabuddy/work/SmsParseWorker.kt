package com.example.pesabuddy.work

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pesabuddy.database.DatabaseModule
import com.example.pesabuddy.database.TransactionEntity
import com.example.pesabuddy.database.UnparsedSmsEntity
import com.example.pesabuddy.parser.ParserFactory

class SmsParseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!hasSmsPermission()) {
            return Result.retry()
        }

        val sender = inputData.getString("sender") ?: return Result.failure()
        val body = inputData.getString("body") ?: return Result.failure()
        val timestamp = inputData.getLong("timestamp", System.currentTimeMillis())

        val normalized = sender.uppercase()
        val parser = ParserFactory.create(normalized)
        val db = DatabaseModule.provideDatabase(applicationContext)

        if (parser != null) {
            val tx = parser.parse(sender, body, timestamp)
            if (tx != null) {
                db.transactionDao().insert(tx)
                return Result.success()
            }
        }

        // Unparsed fallback
        val unparsed = UnparsedSmsEntity(
            sender = sender,
            rawMessage = body,
            timestamp = timestamp
        )
        db.unparsedSmsDao().insert(unparsed)
        return Result.success()
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
