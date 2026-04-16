package com.chaaru.bookbridge.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.chaaru.bookbridge.data.model.Booking
import com.chaaru.bookbridge.viewmodel.BooksViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    booksViewModel: BooksViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val bookings by booksViewModel.bookings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Burgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Parchment)
            )
        },
        containerColor = Parchment
    ) { padding ->
        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No bookings found", style = MaterialTheme.typography.bodyLarge, color = Slate500)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bookings) { booking ->
                    BookingCard(
                        booking = booking, 
                        onClick = onNavigateToDetail,
                        onDelete = { booksViewModel.deleteBooking(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking, onClick: (String) -> Unit, onDelete: (String) -> Unit) {
    Log.d("BOOK_IMAGE_LOAD", "BookingCard rendering for ID: ${booking.id}, Title: ${booking.bookTitle}, Image: ${booking.bookImageUrl}")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(booking.id) },
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate100)
                        .clickable { onClick(booking.id) },
                    contentAlignment = Alignment.Center
                ) {
                    BookImage(imageUrl = booking.bookImageUrl, modifier = Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.bookTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Text(
                        "Store: ${booking.storeId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val statusColor = when(booking.status) {
                        "advance_paid" -> Color(0xFF1976D2)
                        "completed" -> Color(0xFF388E3C)
                        "cancelled" -> Color(0xFFD32F2F)
                        else -> Slate600
                    }
                    
                    Text(
                        booking.status.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${booking.totalPrice}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Paid: ₹${booking.advancePaid}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF388E3C)
                    )
                }
            }

            // Delete option
            if (booking.status == "cancelled" || booking.status == "completed") {
                HorizontalDivider(color = Slate100)
                TextButton(
                    onClick = { onDelete(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove from History", style = MaterialTheme.typography.labelLarge)
                }
            } else if (booking.status == "advance_paid") {
                HorizontalDivider(color = Slate100)
                TextButton(
                    onClick = { onDelete(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate500)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Reservation", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
