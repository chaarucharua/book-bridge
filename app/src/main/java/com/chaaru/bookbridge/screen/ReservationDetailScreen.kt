package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservationId: String,
    booksViewModel: BooksViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val reservation = booksViewModel.reservations.find { it.id == reservationId }
    val userProfile = authViewModel.profile.value

    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val gold = Color(0xFFD4AF37)
    val charcoal = Color(0xFF333333)

    Scaffold(
        containerColor = creamBackground,
        topBar = {
            TopAppBar(
                title = { Text("RESERVATION DETAILS", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = burgundy, titleContentColor = gold)
            )
        }
    ) { paddingValues ->
        if (reservation == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Reservation not found", color = charcoal, fontFamily = FontFamily.Serif)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                Text(
                    text = reservation.bookTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                    color = burgundy
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                StatusBadge(reservation.status, burgundy, gold)

                Spacer(modifier = Modifier.height(24.dp))

                DetailItem("Store", reservation.storeName, charcoal)
                DetailItem("Reserved By", reservation.userName, charcoal)
                DetailItem("Email", reservation.userEmail, charcoal)
                DetailItem("Phone", reservation.userPhone, charcoal)
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                DetailItem("Start Date", dateFormat.format(Date(reservation.startDate)), charcoal)
                DetailItem("End Date", dateFormat.format(Date(reservation.endDate)), charcoal)

                Spacer(modifier = Modifier.weight(1f))

                if (userProfile?.role == "owner" && reservation.status == "PENDING") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { 
                                booksViewModel.updateReservationStatus(reservation.id, "REJECTED", userProfile.storeId ?: "")
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = gold),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text("REJECT", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { 
                                booksViewModel.updateReservationStatus(reservation.id, "APPROVED", userProfile.storeId ?: "")
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = burgundy, contentColor = gold),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text("APPROVE", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, charcoal: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
            color = charcoal.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
            color = charcoal
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp, color = charcoal.copy(alpha = 0.1f))
    }
}
