package com.chaaru.bookbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaaru.bookbridge.screen.*
import com.chaaru.bookbridge.ui.theme.BookBridgeTheme
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Clear cached auth session on every app start to avoid using stale local data
        FirebaseAuth.getInstance().signOut()

        setContent {
            BookBridgeTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val booksViewModel: BooksViewModel = viewModel()

    val currentUser = authViewModel.profile.value

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(viewModel = authViewModel) { role ->
                val trimmedRole = role.trim().lowercase()
                val destination = if (trimmedRole == "owner") "owner_dashboard" else "student_home"
                navController.navigate(destination) {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("student_home") {
            StudentHomeScreen(booksViewModel, authViewModel) { route -> navController.navigate(route) }
        }

        composable("owner_dashboard") {
            OwnerDashboardScreen(booksViewModel, authViewModel) { route -> navController.navigate(route) }
        }

        composable("book_detail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            BookDetailScreen(bookId, booksViewModel, authViewModel) { navController.popBackStack() }
        }

        composable("manage_books") {
            ManageBooksScreen(null, booksViewModel, authViewModel) { navController.popBackStack() }
        }

        composable("manage_books/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            ManageBooksScreen(bookId, booksViewModel, authViewModel) { navController.popBackStack() }
        }

        composable("reservations") {
            ReservationsScreen(
                booksViewModel = booksViewModel,
                authViewModel = authViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                onLogout = { 
                    navController.navigate("login") { 
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
