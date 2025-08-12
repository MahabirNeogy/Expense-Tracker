package com.malikstudios.expensetracker.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val notes: String = "",
    val receiptImagePath: String? = null,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val date: LocalDate = LocalDate.now()
)

enum class ExpenseCategory(val displayName: String) {
    STAFF("Staff"),
    TRAVEL("Travel"),
    FOOD("Food",),
    UTILITY("Utility")
}