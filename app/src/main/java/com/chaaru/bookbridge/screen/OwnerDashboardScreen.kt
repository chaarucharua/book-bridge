package com.chaaru.bookbridge.screen

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.chaaru.bookbridge.data.model.Store
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.data.model.Reservation
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(booksViewModel: BooksViewModel, authViewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val userProfile = authViewModel.profile.value
    val allBooks by booksViewModel.allBooks.collectAsState()
    val reservations = booksViewModel.reservations
    val isLoading by remember { derivedStateOf { booksViewModel.isLoading.value } }

    val ownerBooks = remember(allBooks, userProfile?.storeId) {
        if (userProfile?.storeId == null) emptyList<Book>()
        else allBooks.filter { it.storeId == userProfile.storeId }
    }

    LaunchedEffect(userProfile?.storeId) {
        userProfile?.storeId?.let { 
            booksViewModel.loadOwnerBooks(it)
            booksViewModel.loadStoreReservations(it)
        }
    }

    if (userProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Store Dashboard", style = MaterialTheme.typography.titleLarge)
                        Text(userProfile.name + "'s Hub", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout { onNavigate("login") } }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("manage_books") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Dashboard") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Quick Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard("Books", ownerBooks.size.toString(), Modifier.weight(1f))
                    StatCard("Requests", reservations.filter { it.status == "PENDING" }.size.toString(), Modifier.weight(1f))
                }
            }

            item {
                SectionHeader("Active Inventory")
            }
            
            if (ownerBooks.isEmpty() && !isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text("No books in your inventory yet.", style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = { onNavigate("manage_books") }) {
                                Text("Add your first book")
                            }
                        }
                    }
                }
            } else {
                items(ownerBooks) { book ->
                    InventoryItem(
                        book = book, 
                        onEdit = { onNavigate("manage_books/${book.id}") },
                        onDelete = { userProfile.storeId?.let { booksViewModel.deleteBook(book.id, it) } }
                    )
                }
            }

            if (reservations.isNotEmpty()) {
                item {
                    SectionHeader("Recent Reservations")
                }
                
                items(reservations.reversed()) { res ->
                    ReservationItem(
                        res = res,
                        onApprove = { 
                            if (res.status == "PENDING") {
                                booksViewModel.updateReservationStatus(res.id, "APPROVED", userProfile.storeId ?: "") 
                            }
                        },
                        onReject = { 
                            if (res.status == "PENDING") {
                                booksViewModel.updateReservationStatus(res.id, "REJECTED", userProfile.storeId ?: "")
                            }
                        },
                        onUpdateDates = { start, end ->
                            booksViewModel.updateReservationDates(res.id, start, end, userProfile.storeId ?: "")
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        TextButton(onClick = { /* View All */ }) {
            Text("See All", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryItem(book: Book, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Placeholder for book cover
            Surface(
                modifier = Modifier.size(60.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart, 
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(book.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Text("₹${book.price}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ReservationItem(
    res: Reservation,
    onApprove: () -> Unit, 
    onReject: () -> Unit,
    onUpdateDates: (Long, Long) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(res.bookTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusBadge(res.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Student Details
            Text("Student: ${res.userName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("Email: ${res.userEmail}", style = MaterialTheme.typography.bodySmall)
            Text("Phone: ${res.userPhone}", style = MaterialTheme.typography.bodySmall)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Duration:", style = MaterialTheme.typography.labelSmall)
                    Text("${dateFormatter.format(java.util.Date(res.startDate))} - ${dateFormatter.format(java.util.Date(res.endDate))}", 
                        style = MaterialTheme.typography.bodySmall)
                }
                if (res.status == "PENDING") {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Dates", modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            if (res.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var newStart by remember { mutableStateOf(res.startDate) }
        var newEnd by remember { mutableStateOf(res.endDate) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Reservation Dates") },
            text = {
                Column {
                    Text("Adjust the reservation period for this student.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Start Date (ms):")
                    OutlinedTextField(
                        value = newStart.toString(),
                        onValueChange = { newStart = it.toLongOrNull() ?: newStart },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("End Date (ms):")
                    OutlinedTextField(
                        value = newEnd.toString(),
                        onValueChange = { newEnd = it.toLongOrNull() ?: newEnd },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    onUpdateDates(newStart, newEnd)
                    showEditDialog = false 
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "APPROVED" -> Color(0xFF4CAF50)
        "PENDING" -> Color(0xFFFF9800)
        "REJECTED" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.secondary
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

