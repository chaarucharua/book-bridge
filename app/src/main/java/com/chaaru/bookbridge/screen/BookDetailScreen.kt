package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.data.model.Reservation
import com.chaaru.bookbridge.data.model.Review
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(bookId: String, booksViewModel: BooksViewModel, authViewModel: AuthViewModel, onBack: () -> Unit) {
    val allBooks by booksViewModel.allBooks.collectAsState()
    val book = allBooks.find { it.id == bookId }
    val userProfile = authViewModel.profile.value

    var rating by remember { mutableStateOf(5f) }
    var comment by remember { mutableStateOf("") }
    
    // Reservation date states
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.timeInMillis) }
    calendar.add(Calendar.DAY_OF_YEAR, 7)
    var endDate by remember { mutableStateOf(calendar.timeInMillis) }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Book not found", modifier = Modifier.padding(16.dp))
        }
        return
    }

    LaunchedEffect(bookId) {
        booksViewModel.loadReviews(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                Text(book.title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("by ${book.author}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Store: ${book.displayStoreName}", style = MaterialTheme.typography.bodyLarge)
                        Text("Genre: ${book.category}", style = MaterialTheme.typography.bodyMedium)
                        Text("Condition: ${book.condition}", style = MaterialTheme.typography.bodyMedium)
                        Text("Price: ₹${book.price}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Description", style = MaterialTheme.typography.titleMedium)
                Text(book.description, style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Reservation Period", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("From: ${dateFormatter.format(Date(startDate))}")
                    Text("To: ${dateFormatter.format(Date(endDate))}")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        if (userProfile != null) {
                            booksViewModel.reserveBook(
                                Reservation(
                                    userId = userProfile.uid,
                                    userName = userProfile.name,
                                    userEmail = userProfile.email,
                                    userPhone = userProfile.phone,
                                    bookId = book.id,
                                    bookTitle = book.title,
                                    storeId = book.storeId,
                                    startDate = startDate,
                                    endDate = endDate
                                ),
                                onComplete = { onBack() },
                                onError = { /* handle error */ }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reserve This Book")
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("Reviews", style = MaterialTheme.typography.titleMedium)
                
                OutlinedTextField(
                    value = comment, 
                    onValueChange = { comment = it }, 
                    label = { Text("Write a review...") }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Rating: ${rating.toInt()}/5")
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )
                }

                Button(
                    onClick = {
                        if (userProfile != null && comment.isNotBlank()) {
                            booksViewModel.addReview(Review(userId = userProfile.uid, bookId = book.id, rating = rating, comment = comment, userName = userProfile.name))
                            comment = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Post Review")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (booksViewModel.reviews.isEmpty()) {
                item {
                    Text("No reviews yet. Be the first to review!", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(booksViewModel.reviews) { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(review.userName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Text("⭐ ${review.rating.toInt()}/5")
                            }
                            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
