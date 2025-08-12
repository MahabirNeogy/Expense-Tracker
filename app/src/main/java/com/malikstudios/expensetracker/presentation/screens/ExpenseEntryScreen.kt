package com.malikstudios.expensetracker.presentation.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikstudios.expensetracker.presentation.components.AnimatedSuccessIndicator
import com.malikstudios.expensetracker.presentation.components.CategorySelector
import com.malikstudios.expensetracker.presentation.viewmodel.ExpenseViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    viewModel: ExpenseViewModel,
    onNavigateToList: () -> Unit
) {
    val entryState by viewModel.entryState.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()
    val context = LocalContext.current
    var showSuccess by remember { mutableStateOf(false) }

    // Handle error messages
    LaunchedEffect(entryState.errorMessage) {
        entryState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with today's total
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today's Total Spent",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                AnimatedContent(
                    targetState = todayTotal,
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                    },
                    label = "amount_animation"
                ) { amount ->
                    Text(
                        text = "₹${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Expense Entry Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Expense",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Title input
                OutlinedTextField(
                    value = entryState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("Expense Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Amount input
                OutlinedTextField(
                    value = entryState.amount,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("₹", fontSize = 18.sp) },
                    singleLine = true
                )

                // Category selector
                CategorySelector(
                    selectedCategory = entryState.selectedCategory,
                    onCategorySelected = viewModel::updateCategory
                )

                // Notes input
                OutlinedTextField(
                    value = entryState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Notes (${entryState.notes.length}/100)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = { Text("Optional notes about this expense") }
                )

                // Receipt upload (mock)
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Receipt upload feature coming soon!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Receipt Photo")
                }

                // Submit button
                AnimatedContent(
                    targetState = entryState.isSubmitting,
                    label = "submit_animation"
                ) { isSubmitting ->
                    Button(
                        onClick = {
                            if (viewModel.submitExpense()) {
                                showSuccess = true
                                Toast.makeText(context, "Expense added successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Expense", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Success animation
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSuccess = false
            }
            AnimatedSuccessIndicator()
        }
    }
}