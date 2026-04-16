package com.chaaru.bookbridge.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.data.model.Booking
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    booksViewModel: BooksViewModel,
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    val user = authViewModel.profile.value
    val ownerBooks by booksViewModel.allBooks.collectAsState()
    val bookings by booksViewModel.bookings.collectAsState()
    val isLoading by booksViewModel.isLoading
    
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(user?.storeId) {
        user?.storeId?.let { 
            booksViewModel.observeOwnerBooks(it)
            booksViewModel.observeStoreBookings(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Store Dashboard", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Burgundy))
                        Text(user?.name ?: "Managing Store", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate("profile") }) {
                        Icon(Icons.Default.Person, "Profile", tint = Burgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Parchment)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { onNavigate("manage_books") },
                    containerColor = Burgundy,
                    contentColor = White,
                    icon = { Icon(Icons.Default.Add, "Add Book") },
                    text = { Text("Add New Book", fontFamily = FontFamily.Serif) }
                )
            }
        },
        containerColor = Parchment
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Total Books",
                    value = ownerBooks.size.toString(),
                    icon = Icons.Default.Book,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Bookings",
                    value = bookings.count { it.status == "advance_paid" }.toString(),
                    icon = Icons.Default.ConfirmationNumber,
                    modifier = Modifier.weight(1f)
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Parchment,
                contentColor = Burgundy,
                divider = { HorizontalDivider(color = Burgundy.copy(alpha = 0.1f)) },
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Burgundy
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Inventory", fontFamily = FontFamily.Serif, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Bookings", fontFamily = FontFamily.Serif, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Burgundy)
                }
            } else {
                when (selectedTab) {
                    0 -> InventoryList(ownerBooks, onNavigate, booksViewModel)
                    1 -> BookingList(bookings, booksViewModel, onNavigate)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = Burgundy, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Slate500)
        }
    }
}

@Composable
fun InventoryList(books: List<Book>, onNavigate: (String) -> Unit, viewModel: BooksViewModel) {
    if (books.isEmpty()) {
        EmptyState(Icons.Default.Book, "No books in inventory.", "Start by adding your first book to the store.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books) { book ->
                InventoryItem(
                    book = book,
                    onEdit = { onNavigate("manage_books/${book.id}") },
                    onDelete = { viewModel.deleteBook(book.id, book.imageUrl) }
                )
            }
        }
    }
}

@Composable
fun BookingList(bookings: List<Booking>, viewModel: BooksViewModel, onNavigate: (String) -> Unit) {
    if (bookings.isEmpty()) {
        EmptyState(Icons.Default.ConfirmationNumber, "No bookings yet.", "When customers reserve books, they'll appear here.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bookings) { booking ->
                OwnerBookingItem(booking, { newStatus ->
                    viewModel.updateBookingStatus(booking.id, newStatus)
                }) {
                    onNavigate("booking_detail/${booking.id}")
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = Burgundy.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Slate900, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun OwnerBookingItem(booking: Booking, onUpdateStatus: (String) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BookImage(booking.bookImageUrl, Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.bookTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Customer: ${booking.studentName}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                }
                StatusBadge(booking.status)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Advance Paid", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text("₹${booking.advancePaid}", style = MaterialTheme.typography.titleMedium, color = GreenText, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Price", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Text("₹${booking.totalPrice}", style = MaterialTheme.typography.titleSmall, color = Slate900)
                }
            }
            
            if (booking.status == "advance_paid") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onUpdateStatus("cancelled") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onUpdateStatus("completed") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Mark Sold")
                    }
                }
            }
        }
    }
}


@Composable
fun InventoryItem(book: Book, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookImage(
                imageUrl = book.imageUrl,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Slate900), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${book.price}", style = MaterialTheme.typography.bodyMedium.copy(color = Burgundy, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberText, modifier = Modifier.size(14.dp))
                    Text(
                        "${book.rating} (${book.reviewCount})", 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp),
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BookStatusBadge(book.status)
                }
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = Slate600)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}


