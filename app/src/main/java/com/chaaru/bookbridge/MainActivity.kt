package com.chaaru.bookbridge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaaru.bookbridge.screen.*
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel
import com.chaaru.bookbridge.viewmodel.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultListener {
    private lateinit var booksViewModel: BooksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(applicationContext)
        
        setContent {
            val factory = ViewModelFactory()
            booksViewModel = viewModel(factory = factory)
            val authViewModel: AuthViewModel = viewModel(factory = factory)

            BookBridgeTheme {
                AppNavigation(authViewModel, booksViewModel)
            }
        }
    }

    fun startPayment(amount: Double, email: String, contact: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_ScvZyKnCdPQ8gl") // Replace with actual key
        try {
            val options = JSONObject()
            options.put("name", "Book Bridge")
            options.put("description", "Advance Booking Payment")
            options.put("theme.color", "#722F37")
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt()) // Amount in paise
            options.put("prefill.email", email)
            options.put("prefill.contact", contact)
            
            // Explicitly enable UPI and other methods
            val methodObj = JSONObject()
            methodObj.put("netbanking", true)
            methodObj.put("card", true)
            methodObj.put("upi", true)
            methodObj.put("wallet", true)
            options.put("method", methodObj)

            // Prioritize UPI and enable retries
            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)
            
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        booksViewModel.onPaymentSuccess(razorpayPaymentId ?: "")
    }

    override fun onPaymentError(code: Int, response: String?) {
        booksViewModel.onPaymentError("Payment Failed: $response")
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel, booksViewModel: BooksViewModel) {
    val navController = rememberNavController()

    val currentUser = authViewModel.profile.value

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }

        composable("login") {
            LoginScreen(viewModel = authViewModel) { role ->
                val isOwner = role?.trim()?.equals("owner", ignoreCase = true) == true
                val destination = if (isOwner) "owner_dashboard" else "student_home"
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
            BookDetailScreen(bookId, booksViewModel, authViewModel, navController)
        }

        composable("manage_books") {
            ManageBooksScreen(null, booksViewModel, authViewModel) { navController.popBackStack() }
        }

        composable("manage_books/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId")
            ManageBooksScreen(bookId, booksViewModel, authViewModel) { navController.popBackStack() }
        }

        composable("booking_detail/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            BookingDetailScreen(bookingId, booksViewModel, authViewModel) { navController.popBackStack() }
        }

        composable("chat") {
            AIChatScreen { navController.popBackStack() }
        }

        composable("my_bookings") {
            MyBookingsScreen(
                booksViewModel = booksViewModel,
                onNavigateToDetail = { id -> navController.navigate("booking_detail/$id") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                onLogout = { 
                    booksViewModel.clearState()
                    navController.navigate("login") { 
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
