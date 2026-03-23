package com.chaaru.bookbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chaaru.bookbridge.screen.AddBookScreen
import com.chaaru.bookbridge.screen.BookDetailScreen
import com.chaaru.bookbridge.screen.DashboardScreen
import com.chaaru.bookbridge.screen.ManageBooksScreen
import com.chaaru.bookbridge.screen.NotificationsScreen
import com.chaaru.bookbridge.screen.OwnerLoginScreen
import com.chaaru.bookbridge.screen.ReservationRequestsScreen
import com.chaaru.bookbridge.screen.ReservationsScreen
import com.chaaru.bookbridge.screen.StudentHomeScreen
import com.chaaru.bookbridge.screen.StudentLoginScreen
import com.chaaru.bookbridge.screen.sampleBooks
import com.chaaru.bookbridge.ui.theme.BookBridgeTheme

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

// ── Route constants ───────────────────────────────────────────
object Routes {
    const val STUDENT_LOGIN     = "student_login"
    const val STUDENT_HOME      = "student_home"
    const val BOOK_DETAIL       = "book_detail/{bookIndex}"
    const val RESERVATIONS      = "reservations"
    const val NOTIFICATIONS     = "notifications"
    const val OWNER_LOGIN       = "owner_login"
    const val DASHBOARD         = "dashboard"
    const val ADD_BOOK          = "add_book"
    const val MANAGE_BOOKS      = "manage_books"
    const val EDIT_BOOK         = "edit_book/{bookId}"
    const val RESERVATION_REQS  = "reservation_requests"

    fun bookDetail(index: Int) = "book_detail/$index"
    fun editBook(id: String)   = "edit_book/$id"
}

// ── Main Activity ─────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookBridgeTheme {
                val nav = rememberNavController()
                BookBridgeNavHost(nav)
            }
        }
    }
}

// ── Nav Host ──────────────────────────────────────────────────
@Composable
fun BookBridgeNavHost(nav: NavHostController) {
    NavHost(nav, startDestination = Routes.STUDENT_LOGIN) {

        // ── Student ───────────────────────────────────────
        composable(Routes.STUDENT_LOGIN) {
            StudentLoginScreen(
                onLoginClick = {
                    nav.navigate(Routes.STUDENT_HOME) {
                        popUpTo(Routes.STUDENT_LOGIN) {
                            inclusive = true
                        }
                    }
                },
                onSwitchToOwner = { nav.navigate(Routes.OWNER_LOGIN) }
            )
        }
        composable(Routes.STUDENT_HOME) {
            StudentHomeScreen(
                onBookClick = { nav.navigate(Routes.bookDetail(it.toIntOrNull() ?: 0)) },
                onNotifClick = { nav.navigate(Routes.NOTIFICATIONS) },
                onReservationsClick = { nav.navigate(Routes.RESERVATIONS) }
            )
        }
        composable(
            Routes.BOOK_DETAIL,
            arguments = listOf(navArgument("bookIndex") { type = NavType.IntType })
        ) { back ->
            val idx = back.arguments?.getInt("bookIndex") ?: 0
            BookDetailScreen(
                book = sampleBooks.getOrElse(idx) { sampleBooks[0] },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.RESERVATIONS) {
            ReservationsScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { nav.popBackStack() })
        }

        // ── Owner ─────────────────────────────────────────
        composable(Routes.OWNER_LOGIN) {
            OwnerLoginScreen(
                onLoginClick = {
                    nav.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.OWNER_LOGIN) {
                            inclusive = true
                        }
                    }
                },
                onSwitchToStudent = { nav.navigate(Routes.STUDENT_LOGIN) }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddBook = { nav.navigate(Routes.ADD_BOOK) },
                onManageBooks = { nav.navigate(Routes.MANAGE_BOOKS) },
                onReservations = { nav.navigate(Routes.RESERVATION_REQS) }
            )
        }
        composable(Routes.ADD_BOOK) {
            AddBookScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.MANAGE_BOOKS) {
            ManageBooksScreen(
                onBack = { nav.popBackStack() },
                onEditBook = { nav.navigate(Routes.editBook(it)) }
            )
        }
        composable(
            Routes.EDIT_BOOK,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) {
            AddBookScreen(isEditing = true, onBack = { nav.popBackStack() })
        }
        composable(Routes.RESERVATION_REQS) {
            ReservationRequestsScreen(onBack = { nav.popBackStack() })
        }
    }
}