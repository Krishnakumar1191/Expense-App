package com.example.expense.util

import android.content.Context
import android.net.Uri
import android.provider.Telephony

object SmsHistoryScanner {

    fun scanInbox(context: Context): List<ParsedUpiTransaction> {
        val transactions = mutableListOf<ParsedUpiTransaction>()
        val uri = Uri.parse("content://sms/inbox")

        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val keywords = listOf(
            "%upi%", "%debited%", "%credited%", "%spent%", "%vpa%",
            "%paid%", "%sent%", "%payment%", "%txn%", "%debit%", "%a/c%"
        )

        val selection = keywords.joinToString(" OR ") { "${Telephony.Sms.BODY} LIKE ?" }
        val selectionArgs = keywords.toTypedArray()

        try {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${Telephony.Sms.DATE} DESC"
            )?.use { cursor ->
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    val address = if (addressIdx != -1) cursor.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx != -1) cursor.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) cursor.getLong(dateIdx) else System.currentTimeMillis()

                    val parsed = UpiSmsParser.parseSms(address, body, date)
                    if (parsed != null) {
                        transactions.add(parsed)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return transactions
    }
}
