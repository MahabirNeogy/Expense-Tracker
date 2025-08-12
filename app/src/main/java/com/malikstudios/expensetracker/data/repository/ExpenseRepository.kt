package com.malikstudios.expensetracker.data.repository

import com.malikstudios.expensetracker.data.model.Expense
import com.malikstudios.expensetracker.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExpenseRepository {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: Flow<List<Expense>> = _expenses.asStateFlow()


    init {
        val mockExpenses = listOf(
            Expense(
                title = "Office Lunch",
                amount = 450.0,
                category = ExpenseCategory.FOOD,
                notes = "Team lunch at restaurant"
            ),
            Expense(
                title = "Petrol",
                amount = 2500.0,
                category = ExpenseCategory.TRAVEL,
                notes = "Weekly fuel expense"
            ),
            Expense(
                title = "Internet Bill",
                amount = 1200.0,
                category = ExpenseCategory.UTILITY,
                notes = "Monthly broadband"
            )
        )
//        _expenses.value = mockExpenses
    }

    suspend fun addExpense(expense: Expense) {
        val currentExpenses = _expenses.value.toMutableList()
        currentExpenses.add(0, expense) // Add to beginning for latest first
        _expenses.value = currentExpenses
    }

    fun getExpensesForDate(date: LocalDate): Flow<List<Expense>> {
        return expenses.map { expenseList ->
            expenseList.filter { it.date == date }
        }
    }

    fun getTotalForDate(date: LocalDate): Flow<Double> {
        return getExpensesForDate(date).map { expenseList ->
            expenseList.sumOf { it.amount }
        }
    }

    fun getExpensesByCategory(): Flow<Map<ExpenseCategory, List<Expense>>> {
        return expenses.map { expenseList ->
            expenseList.groupBy { it.category }
        }
    }

    fun getExpensesForLastWeek(): Flow<List<Expense>> {
        val weekAgo = LocalDate.now().minusDays(7)
        return expenses.map { expenseList ->
            expenseList.filter { it.date.isAfter(weekAgo) || it.date.isEqual(weekAgo) }
        }
    }
}