package com.chaaru.bookbridge.screen

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chaaru.bookbridge.MainActivity
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.data.model.Booking
import com.chaaru.bookbridge.data.model.Review
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    booksViewModel: BooksViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val books by booksViewModel.allBooks.collectAsState()
    val book = books.find { it.id == bookId }
    val user = authViewModel.profile.value
    val reviews by booksViewModel.reviews.collectAsState()
    var bookingStatus by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(bookId) {
        booksViewModel.observeReviews(bookId)
    }

    fun findActivity(context: Context): MainActivity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is MainActivity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Burgundy)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    Surface(
                        onClick = { navController.popBackStack() },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(8.dp).size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            "Back", 
                            tint = Burgundy,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (book.status == "available") {
                val advanceAmount = if (book.advancePrice > 0) book.advancePrice else book.price * 0.1
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Total: ₹${book.price.toInt()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate500
                            )
                            Text(
                                "Advance: ₹${advanceAmount.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Burgundy
                                )
                            )
                        }
                        
                        Button(
                            onClick = {
                                isProcessing = true
                                val booking = Booking(
                                    userId = user?.uid ?: "",
                                    studentName = user?.name ?: "Anonymous",
                                    studentPhone = user?.phone ?: "",
                                    studentEmail = user?.email ?: "",
                                    bookId = book.id,
                                    bookTitle = book.title,
                                    bookImageUrl = book.imageUrl,
                                    storeId = book.storeId,
                                    storeUpi = book.storeUpi,
                                    advancePaid = advanceAmount,
                                    totalPrice = book.price,
                                    status = "pending_payment"
                                )
                                
                                booksViewModel.initiateBookingWithPayment(booking) {
                                    isProcessing = false
                                    // bookingStatus = "Reserved! Visit store for final payment."
                                }

                                findActivity(context)?.startPayment(
                                    amount = advanceAmount,
                                    email = user?.email ?: "",
                                    contact = user?.phone ?: ""
                                )
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Reserve Now", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        },
        containerColor = Parchment
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(bottom = if(book.status == "available") 88.dp else 0.dp) // Avoid overlap with bottom bar
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                BookImage(book.imageUrl, Modifier.fillMaxSize())
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            book.title, 
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif, 
                                fontWeight = FontWeight.Bold, 
                                color = Slate900
                            )
                        )
                        Text(
                            "by ${book.author}", 
                            style = MaterialTheme.typography.titleMedium.copy(color = Slate600)
                        )
                    }
                    
                    Surface(
                        color = BurgundyLight.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            book.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Burgundy
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip(Icons.Default.Star, "${book.rating} (${book.reviewCount})", "Rating")
                    InfoChip(Icons.Default.MenuBook, book.condition, "Condition")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BookStatusBadge(book.status, Modifier.padding(bottom = 4.dp))
                        Text("Status", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    "Description", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    book.description,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Slate700, lineHeight = 24.sp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider(color = Slate200)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Store Information", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow("Store Name", book.storeName)
                DetailRow("UPI ID", book.storeUpi)
                DetailRow("Owner ID", book.ownerId) // Ideally fetch store phone/email

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Reviews (${reviews.size})", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                    )
                    TextButton(onClick = { showReviewDialog = true }) {
                        Text("Add Review", color = Burgundy, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (reviews.isNotEmpty()) {
                    reviews.forEach { review ->
                        ReviewItem(review)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (book.status != "available") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (book.status == "sold") RedBg else AmberBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = null, 
                                tint = if (book.status == "sold") Color.Red else AmberText
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (book.status == "sold") "This book has been sold." else "This book is currently reserved.",
                                color = if (book.status == "sold") Color.Red else AmberText
                            )
                        }
                    }
                }

                bookingStatus?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GreenBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            it, 
                            modifier = Modifier.padding(16.dp), 
                            color = GreenText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    if (showReviewDialog) {
        AddReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                val review = Review(
                    userId = user?.uid ?: "",
                    userName = user?.name ?: "Anonymous",
                    bookId = bookId,
                    rating = rating,
                    comment = comment
                )
                booksViewModel.addReview(review) {
                    showReviewDialog = false
                }
            }
        )
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.userName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Slate900)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${review.rating}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Slate700)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
            )
        }
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Double, String) -> Unit
) {
    var rating by remember { mutableStateOf(5.0) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Add a Review") },
        text = {
            Column {
                Text("Rating: ${rating.toInt()}/5")
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { if (!isSubmitting) rating = it.toDouble() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = Burgundy, activeTrackColor = Burgundy)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (!isSubmitting) comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !isSubmitting
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    isSubmitting = true
                    onSubmit(rating.toInt().toDouble(), comment) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancel", color = Burgundy)
            }
        }
    )
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Burgundy, modifier = Modifier.size(24.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Slate900))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500)
    }
}

