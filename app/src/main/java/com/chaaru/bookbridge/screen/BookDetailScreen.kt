package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.ui.getCategoryIcon
import com.chaaru.bookbridge.data.model.Reservation
import com.chaaru.bookbridge.data.model.Review
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(bookId: String, booksViewModel: BooksViewModel, authViewModel: AuthViewModel, onBack: () -> Unit) {
    val allBooks by booksViewModel.allBooks.collectAsState()
    val book = allBooks.find { it.id == bookId }
    val userProfile = authViewModel.profile.value
    val isLoading = booksViewModel.isLoading.value

    var rating by remember { mutableStateOf(5f) }
    var comment by remember { mutableStateOf("") }
    
    // Simple Palette
    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val charcoal = Color(0xFF333333)
    val gold = Color(0xFFD4AF37)

    // Reservation date states
    var startDate by remember(book) { mutableStateOf(book?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember(book) { mutableStateOf(book?.endDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize().background(creamBackground), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(color = burgundy)
        }
        return
    }

    LaunchedEffect(bookId) {
        booksViewModel.loadReviews(bookId)
    }

    Scaffold(
        containerColor = creamBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "BOOK DETAILS", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = burgundy,
                    titleContentColor = gold
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            item {
                // Book Icon Placeholder (Replacing Image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 24.dp)
                        .background(burgundy.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .border(1.dp, burgundy.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = getCategoryIcon(book.category ?: ""),
                            contentDescription = book.category,
                            modifier = Modifier.size(120.dp),
                            tint = burgundy.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = gold,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = book.category ?: "Uncategorized",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = burgundy
                            )
                        }
                    }
                }

                Text(
                    book.title ?: "Untitled", 
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), 
                    color = burgundy
                )
                Text(
                    "by ${book.author ?: "Unknown Author"}", 
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                    color = charcoal.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = burgundy,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        DetailRow("Store", book.effectiveStoreName, gold, creamBackground)
                        DetailRow("Category", book.category ?: "Uncategorized", gold, creamBackground)
                        DetailRow("Condition", book.condition ?: "Used", gold, creamBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Price: ₹${book.price}", 
                            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), 
                            color = gold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Description", 
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                    color = burgundy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    book.description ?: "No description available.", 
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, lineHeight = 24.sp),
                    color = charcoal
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(thickness = 1.dp, color = burgundy.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    "Reservation Period", 
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                    color = burgundy
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    DateBox("Start Date", dateFormatter.format(Date(startDate)), burgundy, gold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp), tint = burgundy.copy(alpha = 0.3f))
                    DateBox("End Date", dateFormatter.format(Date(endDate)), burgundy, gold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator(color = burgundy)
                    }
                } else {
                    Button(
                        onClick = {
                            if (userProfile != null) {
                                booksViewModel.reserveBook(
                                    res = Reservation(
                                        userId = userProfile.uid,
                                        userName = userProfile.name,
                                        userEmail = userProfile.email,
                                        userPhone = userProfile.phone,
                                        bookId = book.id,
                                        bookTitle = book.title ?: "Unknown Book",
                                        storeId = book.storeId ?: "Unknown Store",
                                        startDate = startDate,
                                        endDate = endDate,
                                        status = "PENDING",
                                        storeName = book.effectiveStoreName
                                    ),
                                    onSuccess = { onBack() },
                                    onError = { /* handle error */ }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = burgundy),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                    ) {
                        Text(
                            "RESERVE NOW", 
                            color = gold,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "Reviews", 
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                    color = burgundy
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = comment, 
                    onValueChange = { if (it.length <= 500) comment = it }, 
                    placeholder = { Text("Write a review...", fontFamily = FontFamily.Serif) }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = charcoal,
                        unfocusedTextColor = charcoal,
                        focusedBorderColor = burgundy,
                        unfocusedBorderColor = burgundy.copy(alpha = 0.2f),
                        cursorColor = burgundy
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("Rating: ${rating.toInt()}/5", fontFamily = FontFamily.Serif, color = charcoal)
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = burgundy,
                            activeTrackColor = burgundy,
                            inactiveTrackColor = burgundy.copy(alpha = 0.1f)
                        )
                    )
                }

                Button(
                    onClick = {
                        if (userProfile != null && comment.isNotBlank()) {
                            booksViewModel.addReview(Review(userId = userProfile.uid, bookId = book.id, rating = rating.toDouble(), comment = comment.trim(), userName = userProfile.name))
                            comment = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, burgundy)
                ) {
                    Text("POST REVIEW", color = burgundy, style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (booksViewModel.reviews.isEmpty()) {
                item {
                    Text(
                        "No reviews yet.", 
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = charcoal.copy(alpha = 0.5f)
                    )
                }
            } else {
                items(booksViewModel.reviews) { review ->
                    ReviewItem(review, burgundy, charcoal, gold)
                }
            }
        }
    }
}



@Composable
fun DetailRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif), color = valueColor.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = labelColor)
    }
}

@Composable
fun DateBox(label: String, date: String, burgundy: Color, gold: Color) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif), color = burgundy.copy(alpha = 0.6f))
        Surface(
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, burgundy.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                date, 
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = burgundy
            )
        }
    }
}

@Composable
fun ReviewItem(review: Review, burgundy: Color, charcoal: Color, gold: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, burgundy.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    review.userName.uppercase(), 
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, letterSpacing = 1.sp), 
                    color = burgundy
                )
                Text("⭐ ${review.rating.toInt()}/5", color = gold, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                review.comment, 
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif, lineHeight = 20.sp),
                color = charcoal
            )
        }
    }
}
