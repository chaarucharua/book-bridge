package com.chaaru.bookbridge.screen

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.data.model.Booking
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    viewModel: BooksViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val bookings by viewModel.bookings.collectAsState()
    val booking = bookings.find { it.id == bookingId }
    val userProfile = authViewModel.profile.value

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Burgundy)
        }
        return
    }

    val isOwner = userProfile?.role == "owner"
    val remainingAmount = booking.totalPrice - booking.advancePaid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Burgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Parchment)
            )
        },
        containerColor = Parchment
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        BookImage(booking.bookImageUrl, Modifier.fillMaxSize())
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(booking.bookTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isOwner) {
                            DetailRow("Student Name", booking.studentName)
                            DetailRow("Student Contact", booking.studentPhone)
                            DetailRow("Student Email", booking.studentEmail)
                        } else {
                            DetailRow("Store ID", booking.storeId)
                        }
                        
                        DetailRow("Booking ID", booking.id)
                        DetailRow("Payment ID", booking.paymentId)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Price", style = MaterialTheme.typography.bodyLarge)
                            Text("₹${booking.totalPrice}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Advance Paid", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF2E7D32))
                            Text("₹${booking.advancePaid}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Remaining Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹$remainingAmount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Burgundy)
                        }
                    }
                }
            }

            if (booking.status == "advance_paid") {
                Spacer(modifier = Modifier.height(24.dp))
                
                if (!isOwner) {
                    Text("Show this QR at the store to pay the remaining ₹$remainingAmount", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = Slate600,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Text("Scan to Pay Remaining ₹$remainingAmount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val upiId = booking.storeUpi.ifEmpty { "bookbridge.store@upi" }
                val qrContent = "upi://pay?pa=$upiId&pn=BookBridge&am=$remainingAmount&cu=INR&tn=Booking_${booking.id}"
                
                val qrBitmap = remember(booking.id) {
                    generateQRCode(qrContent)
                }
                qrBitmap?.let {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(8.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Payment QR",
                            modifier = Modifier.size(240.dp).padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                if (isOwner) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { viewModel.updateBookingStatus(booking.id, "completed") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Mark as Completed")
                        }
                        OutlinedButton(
                            onClick = { viewModel.updateBookingStatus(booking.id, "cancelled") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Present this screen to the store owner to complete your purchase and pay the remaining balance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            } else if (booking.status == "completed") {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Payment Completed", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

fun generateQRCode(content: String): Bitmap? {
    val writer = QRCodeWriter()
    return try {
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

