package com.example.expense.domain.model

enum class ExpenseCategory(val displayName: String) {
    FOOD("Food & Dining"),
    SHOPPING("Shopping"),
    TRANSPORT("Transportation"),
    RENT("Rent & Housing"),
    BILLS("Bills & Utilities"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other");

    companion object {
        fun fromName(name: String): ExpenseCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}
