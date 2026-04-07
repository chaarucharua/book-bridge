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

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    booksViewModel: BooksViewModel, 
    authViewModel: AuthViewModel, 
    onNavigate: (String) -> Unit, 
    onBack: () -> Unit,
    onReservationClick: (String) -> Unit
) {
    val userProfile = authViewModel.profile.value ?: return
    
    // Dark Academia Palette
    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val charcoal = Color(0xFF333333)
    val gold = Color(0xFFD4AF37)

    LaunchedEffect(userProfile.uid) {
        if (userProfile.role == "student") {
            booksViewModel.loadStudentReservations(userProfile.uid)
        } else if (userProfile.storeId != null) {
            booksViewModel.loadStoreReservations(userProfile.storeId)
        }
    }

    Scaffold(
        containerColor = creamBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "RESERVATIONS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = burgundy,
                    titleContentColor = gold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = burgundy,
                contentColor = gold
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Books", fontFamily = FontFamily.Serif) },
                    selected = false,
                    onClick = { onNavigate("student_home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = gold,
                        selectedTextColor = gold,
                        unselectedIconColor = gold.copy(alpha = 0.5f),
                        unselectedTextColor = gold.copy(alpha = 0.5f),
                        indicatorColor = burgundy.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Reservations", fontFamily = FontFamily.Serif) },
                    selected = true,
                    onClick = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = gold,
                        selectedTextColor = gold,
                        unselectedIconColor = gold.copy(alpha = 0.5f),
                        unselectedTextColor = gold.copy(alpha = 0.5f),
                        indicatorColor = burgundy.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile", fontFamily = FontFamily.Serif) },
                    selected = false,
                    onClick = { onNavigate("profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = gold,
                        selectedTextColor = gold,
                        unselectedIconColor = gold.copy(alpha = 0.5f),
                        unselectedTextColor = gold.copy(alpha = 0.5f),
                        indicatorColor = burgundy.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (booksViewModel.isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = burgundy)
            } else if (booksViewModel.reservations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(64.dp), tint = burgundy.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "You have no reservations yet.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = charcoal.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(booksViewModel.reservations) { res ->
                        ReservationVintageItem(res, burgundy, gold, charcoal, userProfile.role, 
                            onCancel = {
                                booksViewModel.deleteReservation(res.id, userProfile.uid)
                            },
                            onClick = {
                                onReservationClick(res.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationVintageItem(
    res: com.chaaru.bookbridge.data.model.Reservation, 
    burgundy: Color, 
    gold: Color, 
    charcoal: Color, 
    role: String, 
    onCancel: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, burgundy.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    res.bookTitle, 
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), 
                    color = burgundy,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(res.status, burgundy, gold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "ID: ${res.id.take(8).uppercase()}", 
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = charcoal.copy(alpha = 0.4f)
            )

            if (role == "student") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = burgundy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, burgundy.copy(alpha = 0.3f))
                ) {
                    Text("CANCEL RESERVATION", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, burgundy: Color, gold: Color) {
    val bgColor = when(status.uppercase()) {
        "APPROVED" -> burgundy
        "REJECTED" -> Color.Black
        else -> Color.Transparent
    }
    val textColor = if (bgColor == Color.Transparent) burgundy else gold
    val border = if (bgColor == Color.Transparent) androidx.compose.foundation.BorderStroke(1.dp, burgundy) else null

    Surface(
        color = bgColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = border
    ) {
        Text(
            status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = textColor
        )
    }
}
