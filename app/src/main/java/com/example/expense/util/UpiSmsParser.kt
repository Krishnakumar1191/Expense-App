package com.example.expense.util

import com.example.expense.domain.model.ExpenseCategory
import java.util.regex.Pattern

object UpiSmsParser {

    private val DEBIT_PATTERNS = listOf(
        Pattern.compile("(?i)(?:debited|spent|paid|sent|payment|txn|transfer(?:red)?|debit|vpa)\\s*(?:by|of|for|towards)?\\s*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("(?i)(?:rs\\.?|inr)\\s*([\\d,]+\\.?\\d*)\\s*(?:debited|spent|paid|sent|payment|debit|transferred)"),
        Pattern.compile("(?i)(?:for|amount)?\\s*(?:rs\\.?|inr)\\s*([\\d,]+\\.?\\d*)\\s*(?:via|on|using)?\\s*upi")
    )

    private val CREDIT_PATTERNS = listOf(
        Pattern.compile("(?i)(?:credited|received|deposited)\\s*(?:by|of|for)?\\s*(?:rs\\.?|inr)?\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("(?i)(?:rs\\.?|inr)\\s*([\\d,]+\\.?\\d*)\\s*(?:credited|received|deposited)")
    )

    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("(?i)(?:to|vpa|at|info|paid to|sent to|towards|merchant|store|shop)\\s+([A-Za-z0-9\\.\\-_@&' ]{2,30})"),
        Pattern.compile("(?i)upi\\/p2m\\/[0-9]+\\/([A-Za-z0-9\\.\\-_@&' ]{2,30})"),
        Pattern.compile("(?i)upi\\/p2p\\/[0-9]+\\/([A-Za-z0-9\\.\\-_@&' ]{2,30})"),
        Pattern.compile("(?i)at\\s+([A-Za-z0-9\\.\\-_@&' ]{2,30})\\s+(?:via|on|ref)")
    )

    fun parseSms(sender: String, body: String, timestamp: Long): ParsedUpiTransaction? {
        if (body.isBlank()) return null

        var type: TransactionType? = null
        var amount: Double? = null

        for (pattern in DEBIT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val amtStr = matcher.group(1)?.replace(",", "")
                val amt = amtStr?.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    amount = amt
                    type = TransactionType.DEBIT
                    break
                }
            }
        }

        if (amount == null) {
            for (pattern in CREDIT_PATTERNS) {
                val matcher = pattern.matcher(body)
                if (matcher.find()) {
                    val amtStr = matcher.group(1)?.replace(",", "")
                    val amt = amtStr?.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        amount = amt
                        type = TransactionType.CREDIT
                        break
                    }
                }
            }
        }

        if (amount == null || type == null) return null

        var merchantName = "UPI Payment / Store"
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val extracted = matcher.group(1)?.trim()
                if (!extracted.isNull_orEmpty() &&
                    !extracted.equals("UPI", ignoreCase = true) &&
                    !extracted.equals("Rs", ignoreCase = true) &&
                    !extracted.equals("INR", ignoreCase = true)
                ) {
                    merchantName = cleanMerchantName(extracted ?: "")
                    break
                }
            }
        }

        val bankName = extractBankName(sender, body)
        val category = autoCategorize(merchantName, body)

        return ParsedUpiTransaction(
            amount = amount,
            type = type,
            merchantOrSender = merchantName,
            bankName = bankName,
            timestamp = if (timestamp > 0) timestamp else System.currentTimeMillis(),
            rawSms = body,
            category = category
        )
    }

    private fun cleanMerchantName(raw: String): String {
        var clean = raw
        if (clean.contains(" Ref", ignoreCase = true)) {
            clean = clean.substringBefore(" Ref", clean)
        }
        if (clean.contains(" via", ignoreCase = true)) {
            clean = clean.substringBefore(" via", clean)
        }
        if (clean.contains(" on", ignoreCase = true)) {
            clean = clean.substringBefore(" on", clean)
        }
        return clean.trim().take(30)
    }

    private fun extractBankName(sender: String, body: String): String {
        val upperSender = sender.uppercase()
        val upperBody = body.uppercase()

        return when {
            upperSender.contains("HDFCBK") || upperBody.contains("HDFC") -> "HDFC Bank"
            upperSender.contains("SBIBNK") || upperSender.contains("SBI") || upperBody.contains("STATE BANK") -> "SBI"
            upperSender.contains("ICICIB") || upperBody.contains("ICICI") -> "ICICI Bank"
            upperSender.contains("AXISBK") || upperBody.contains("AXIS") -> "Axis Bank"
            upperSender.contains("KOTAKB") || upperBody.contains("KOTAK") -> "Kotak Bank"
            upperSender.contains("PAYTM") || upperBody.contains("PAYTM") -> "Paytm"
            upperSender.contains("PNBSMS") || upperBody.contains("PNB") -> "PNB"
            upperSender.contains("BOBTXN") || upperBody.contains("BARODA") -> "Bank of Baroda"
            upperSender.contains("CANBNK") || upperBody.contains("CANARA") -> "Canara Bank"
            upperSender.contains("UNIONB") || upperBody.contains("UNION") -> "Union Bank"
            else -> "UPI / Bank"
        }
    }

    private fun autoCategorize(merchant: String, body: String): ExpenseCategory {
        val text = "$merchant $body".lowercase()
        return when {
            text.contains("swiggy") || text.contains("zomato") || text.contains("kfc") ||
            text.contains("mcdonald") || text.contains("restaurant") || text.contains("food") ||
            text.contains("cafe") || text.contains("bakery") || text.contains("tea") ||
            text.contains("dining") || text.contains("eatery") || text.contains("hotel") -> ExpenseCategory.FOOD

            text.contains("uber") || text.contains("ola") || text.contains("rapido") ||
            text.contains("irctc") || text.contains("metro") || text.contains("petrol") ||
            text.contains("fuel") || text.contains("auto") || text.contains("hpcl") ||
            text.contains("bpcl") || text.contains("iocl") -> ExpenseCategory.TRANSPORT

            text.contains("amazon") || text.contains("flipkart") || text.contains("myntra") ||
            text.contains("ajio") || text.contains("mart") || text.contains("store") ||
            text.contains("shop") || text.contains("supermarket") || text.contains("retail") ||
            text.contains("bazar") || text.contains("bazaar") || text.contains("dmart") -> ExpenseCategory.SHOPPING

            text.contains("rent") || text.contains("housing") || text.contains("society") -> ExpenseCategory.RENT

            text.contains("jio") || text.contains("airtel") || text.contains("bescom") ||
            text.contains("electricity") || text.contains("recharge") || text.contains("bill") ||
            text.contains("utility") || text.contains("water") || text.contains("gas") -> ExpenseCategory.BILLS

            text.contains("bookmyshow") || text.contains("netflix") || text.contains("prime") ||
            text.contains("hotstar") || text.contains("cinema") || text.contains("pvr") -> ExpenseCategory.ENTERTAINMENT

            else -> ExpenseCategory.OTHER
        }
    }

    private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()
}
