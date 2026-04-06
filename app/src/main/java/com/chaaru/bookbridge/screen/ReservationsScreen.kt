package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

/**
 * ROOT CAUSE FOR WHITE SCREEN/CRASH:
 * 1. Using .forEach on a potentially mutating list inside Column without proper recomposition handling.
 * 2. Missing Scaffold with BottomBar and TopBar for consistent navigation.
 * 3. No empty state handling leading to a "dead" screen if no data exists.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(booksViewModel: BooksViewModel, authViewModel: AuthViewModel, onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val userProfile = authViewModel.profile.value ?: return

    LaunchedEffect(userProfile.uid) {
        if (userProfile.role == "student") {
            booksViewModel.loadStudentReservations(userProfile.uid)
        } else if (userProfile.storeId != null) {
            booksViewModel.loadStoreReservations(userProfile.storeId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reservations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { onNavigate("student_home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Reservations") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = { onNavigate("profile") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (booksViewModel.reservations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No reservations found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(booksViewModel.reservations) { res ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(res.bookTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Text("Status: ${res.status}", style = MaterialTheme.typography.bodyMedium)
                                
                                if (userProfile.role == "student") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { booksViewModel.deleteReservation(res.id, userProfile.uid) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Cancel Reservation")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
