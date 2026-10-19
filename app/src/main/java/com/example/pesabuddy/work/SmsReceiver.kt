package com.example.pesabuddy.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        if (!hasSmsPermission(context)) return

        val bundle = intent.extras ?: return
        val pdus = bundle.get("pdus") as? Array<*>
        val format = bundle.getString("format")
        if (pdus == null) return

        for (pdu in pdus) {
            val message = SmsMessage.createFromPdu(pdu as ByteArray, format)
            val sender = message.originatingAddress ?: continue
            val body = message.messageBody ?: continue
            val timestamp = message.timestampMillis

            val data = workDataOf(
                "sender" to sender,
                "body" to body,
                "timestamp" to timestamp
            )
            val work = OneTimeWorkRequestBuilder<SmsParseWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }

    private fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
