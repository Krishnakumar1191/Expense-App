package com.example.expense.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.expense.notification.NotificationHelper
import com.example.expense.util.TransactionType
import com.example.expense.util.UpiSmsParser
import java.text.NumberFormat
import java.util.Locale

class UpiSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

            for (sms in messages) {
                val address = sms.originatingAddress ?: ""
                val body = sms.messageBody ?: ""
                val timestamp = sms.timestampMillis

                val parsed = UpiSmsParser.parseSms(address, body, timestamp)
                if (parsed != null && parsed.type == TransactionType.DEBIT) {
                    val formattedAmt = currencyFormat.format(parsed.amount)
                    NotificationHelper.showNotification(
                        context = context,
                        title = "UPI Expense Detected 💳",
                        message = "$formattedAmt paid to ${parsed.merchantOrSender} (${parsed.category.displayName})",
                        notificationId = (System.currentTimeMillis() % 10000).toInt()
                    )
                }
            }
        }
    }
}
