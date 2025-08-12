package com.malikstudios.expensetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malikstudios.expensetracker.data.model.Expense
import com.malikstudios.expensetracker.data.model.ExpenseCategory
import com.malikstudios.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExpenseEntryState(
    val title: String = "",
    val amount: String = "",
    val selectedCategory: ExpenseCategory = ExpenseCategory.FOOD,
    val notes: String = "",
    val receiptImagePath: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

data class ExpenseListState(
    val expenses: List<Expense> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val totalAmount: Double = 0.0,
    val groupByCategory: Boolean = false,
    val isLoading: Boolean = false
)

data class ExpenseReportState(
    val weeklyExpenses: List<Expense> = emptyList(),
    val dailyTotals: Map<LocalDate, Double> = emptyMap(),
    val categoryTotals: Map<ExpenseCategory, Double> = emptyMap(),
    val totalWeeklyAmount: Double = 0.0
)

class ExpenseViewModel : ViewModel() {
    private val repository = ExpenseRepository()

    // Theme state
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Entry screen state
    private val _entryState = MutableStateFlow(ExpenseEntryState())
    val entryState: StateFlow<ExpenseEntryState> = _entryState.asStateFlow()

    // List screen state
    private val _listState = MutableStateFlow(ExpenseListState())
    val listState: StateFlow<ExpenseListState> = _listState.asStateFlow()

    // Report screen state
    private val _reportState = MutableStateFlow(ExpenseReportState())
    val reportState: StateFlow<ExpenseReportState> = _reportState.asStateFlow()

    // Today's total for real-time display
    val todayTotal: StateFlow<Double> = repository.getTotalForDate(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    init {
        loadExpensesForToday()
        loadWeeklyReport()
    }

    // Theme functions
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Entry screen functions
    fun updateTitle(title: String) {
        _entryState.value = _entryState.value.copy(title = title)
    }

    fun updateAmount(amount: String) {
        _entryState.value = _entryState.value.copy(amount = amount)
    }

    fun updateCategory(category: ExpenseCategory) {
        _entryState.value = _entryState.value.copy(selectedCategory = category)
    }

    fun updateNotes(notes: String) {
        if (notes.length <= 100) {
            _entryState.value = _entryState.value.copy(notes = notes)
        }
    }

    fun submitExpense(): Boolean {
        val state = _entryState.value

        // Validation
        if (state.title.isBlank()) {
            _entryState.value = state.copy(errorMessage = "Title is required")
            return false
        }

        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _entryState.value = state.copy(errorMessage = "Valid amount is required")
            return false
        }

        _entryState.value = state.copy(isSubmitting = true, errorMessage = null)

        val expense = Expense(
            title = state.title,
            amount = amount,
            category = state.selectedCategory,
            notes = state.notes,
            receiptImagePath = state.receiptImagePath
        )

        viewModelScope.launch {
            repository.addExpense(expense)
            _entryState.value = ExpenseEntryState() // Reset form
            loadExpensesForToday()
            loadWeeklyReport()
        }

        return true
    }

    fun clearError() {
        _entryState.value = _entryState.value.copy(errorMessage = null)
    }

    // List screen functions
    fun loadExpensesForDate(date: LocalDate) {
        _listState.value = _listState.value.copy(selectedDate = date, isLoading = true)

        viewModelScope.launch {
            repository.getExpensesForDate(date).collect { expenses ->
                val total = expenses.sumOf { it.amount }
                _listState.value = _listState.value.copy(
                    expenses = expenses,
                    totalAmount = total,
                    isLoading = false
                )
            }
        }
    }

    private fun loadExpensesForToday() {
        loadExpensesForDate(LocalDate.now())
    }

    fun toggleGroupBy() {
        _listState.value = _listState.value.copy(
            groupByCategory = !_listState.value.groupByCategory
        )
    }

    // Report screen functions
    private fun loadWeeklyReport() {
        viewModelScope.launch {
            repository.getExpensesForLastWeek().collect { expenses ->
                val dailyTotals = expenses.groupBy { it.date }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val categoryTotals = expenses.groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val totalWeekly = expenses.sumOf { it.amount }

                _reportState.value = ExpenseReportState(
                    weeklyExpenses = expenses,
                    dailyTotals = dailyTotals,
                    categoryTotals = categoryTotals,
                    totalWeeklyAmount = totalWeekly
                )
            }
        }
    }

    fun simulateExport(format: String): String {
        val expenses = _reportState.value.weeklyExpenses
        return when (format) {
            "CSV" -> {
                val header = "Date,Title,Amount,Category,Notes\n"
                val rows = expenses.joinToString("\n") { expense ->
                    "${expense.date},${expense.title},₹${expense.amount},${expense.category.displayName},${expense.notes}"
                }
                header + rows
            }
            "PDF" -> "PDF export simulated for ${expenses.size} expenses"
            else -> "Unknown format"
        }
    }

    suspend fun exportToPdf(context: android.content.Context): String? {
        return try {
            val expenses = _reportState.value.weeklyExpenses
            val reportState = _reportState.value

            // Create PDF using Android's built-in PDF API
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            val paint = android.graphics.Paint()

            // Setup paint styles
            val titlePaint = android.graphics.Paint().apply {
                textSize = 24f
                color = android.graphics.Color.BLACK
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val headerPaint = android.graphics.Paint().apply {
                textSize = 16f
                color = android.graphics.Color.BLACK
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val bodyPaint = android.graphics.Paint().apply {
                textSize = 12f
                color = android.graphics.Color.BLACK
            }

            var yPosition = 50f
            val leftMargin = 40f
            val lineHeight = 20f

            // Title
            canvas.drawText("Expense Report", leftMargin, yPosition, titlePaint)
            yPosition += lineHeight * 2

            // Date range
            canvas.drawText("Last 7 Days Report", leftMargin, yPosition, headerPaint)
            yPosition += lineHeight * 1.5f

            // Total summary
            canvas.drawText("Total Weekly Spending: ₹${String.format("%.2f", reportState.totalWeeklyAmount)}",
                leftMargin, yPosition, headerPaint)
            yPosition += lineHeight * 2

            // Category breakdown
            canvas.drawText("Category Breakdown:", leftMargin, yPosition, headerPaint)
            yPosition += lineHeight * 1.5f

            reportState.categoryTotals.forEach { (category, total) ->
                canvas.drawText(" ${category.displayName}: ₹${String.format("%.2f", total)}",
                    leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineHeight
            }

            yPosition += lineHeight

            // Expense details
            canvas.drawText("Detailed Expenses:", leftMargin, yPosition, headerPaint)
            yPosition += lineHeight * 1.5f

            // Table headers
            canvas.drawText("Date", leftMargin, yPosition, headerPaint)
            canvas.drawText("Title", leftMargin + 80f, yPosition, headerPaint)
            canvas.drawText("Category", leftMargin + 200f, yPosition, headerPaint)
            canvas.drawText("Amount", leftMargin + 300f, yPosition, headerPaint)
            yPosition += lineHeight * 1.5f

            // Draw a line under headers
            canvas.drawLine(leftMargin, yPosition - 5f, 550f, yPosition - 5f, bodyPaint)
            yPosition += 5f

            // Expense rows
            expenses.sortedByDescending { it.dateTime }.forEach { expense ->
                if (yPosition > 750f) { // Check if we need a new page
                    pdfDocument.finishPage(page)
                    val newPage = pdfDocument.startPage(pageInfo)
                    canvas = newPage.canvas
                    yPosition = 50f
                }

                canvas.drawText(expense.date.toString(), leftMargin, yPosition, bodyPaint)
                canvas.drawText(expense.title, leftMargin + 80f, yPosition, bodyPaint)
                canvas.drawText(expense.category.displayName, leftMargin + 200f, yPosition, bodyPaint)
                canvas.drawText("₹${String.format("%.2f", expense.amount)}", leftMargin + 300f, yPosition, bodyPaint)
                yPosition += lineHeight
            }

            pdfDocument.finishPage(page)

            // Save to file
            val fileName = "expense_report_${System.currentTimeMillis()}.pdf"
            val file = java.io.File(context.getExternalFilesDir(null), fileName)

            val fileOutputStream = java.io.FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
