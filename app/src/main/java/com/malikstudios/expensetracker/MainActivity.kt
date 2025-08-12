package com.malikstudios.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.malikstudios.expensetracker.presentation.components.BottomNavigationBar
import com.malikstudios.expensetracker.presentation.screens.ExpenseEntryScreen
import com.malikstudios.expensetracker.presentation.screens.ExpenseListScreen
import com.malikstudios.expensetracker.presentation.screens.ExpenseReportScreen
import com.malikstudios.expensetracker.presentation.viewmodel.ExpenseViewModel
import com.malikstudios.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExpenseTrackerApp()
                }
            }
        }
    }
}

@Composable
fun ExpenseTrackerApp() {
    val expenseViewModel: ExpenseViewModel = viewModel()
    val isDarkTheme by expenseViewModel.isDarkTheme.collectAsState()

    ExpenseTrackerTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                ExpenseNavigation(
                    navController = navController,
                    expenseViewModel = expenseViewModel
                )
            }
        }
    }
}

@Composable
fun ExpenseNavigation(
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "expense_entry"
    ) {
        composable("expense_entry") {
            ExpenseEntryScreen(
                viewModel = expenseViewModel,
                onNavigateToList = { navController.navigate("expense_list") }
            )
        }
        composable("expense_list") {
            ExpenseListScreen(
                viewModel = expenseViewModel,
                onNavigateToEntry = { navController.navigate("expense_entry") }
            )
        }
        composable("expense_report") {
            ExpenseReportScreen(
                viewModel = expenseViewModel
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExpenseTrackerTheme {

    }
}