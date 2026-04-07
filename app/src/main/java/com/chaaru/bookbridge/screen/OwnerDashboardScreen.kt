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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.data.model.Reservation
import com.chaaru.bookbridge.ui.getCategoryIcon
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(booksViewModel: BooksViewModel, authViewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val userProfile = authViewModel.profile.value
    val allBooks by booksViewModel.allBooks.collectAsState()
    val reservations = booksViewModel.reservations
    val isLoading by remember { derivedStateOf { booksViewModel.isLoading.value } }

    // Dark Academia Palette
    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val charcoal = Color(0xFF333333)
    val gold = Color(0xFFD4AF37)

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
        Box(modifier = Modifier.fillMaxSize().background(creamBackground), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(color = burgundy)
        }
        return
    }

    Scaffold(
        containerColor = creamBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(
                            "OWNER DASHBOARD", 
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            userProfile.name.uppercase() + "'S STORE", 
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif, letterSpacing = 1.sp), 
                            color = gold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout { onNavigate("login") } }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = burgundy,
                    titleContentColor = gold
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("manage_books") },
                containerColor = burgundy,
                contentColor = gold,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = burgundy,
                contentColor = gold
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home", fontFamily = FontFamily.Serif) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Quick Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard("BOOKS", ownerBooks.size.toString(), burgundy, gold, Modifier.weight(1f))
                    StatCard("PENDING", reservations.filter { it.status == "PENDING" }.size.toString(), burgundy, gold, Modifier.weight(1f))
                }
            }

            item {
                SectionHeader("STORE INVENTORY", burgundy, gold)
            }
            
            if (ownerBooks.isEmpty() && !isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = burgundy.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, burgundy.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No books in your store inventory.", 
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = charcoal.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onNavigate("manage_books") },
                                colors = ButtonDefaults.buttonColors(containerColor = burgundy),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                            ) {
                                Text("ADD NEW BOOK", color = gold, fontFamily = FontFamily.Serif)
                            }
                        }
                    }
                }
            }
else {
                items(ownerBooks) { book ->
                    InventoryVintageItem(
                        book = book, 
                        burgundy = burgundy,
                        gold = gold,
                        charcoal = charcoal,
                        onEdit = { onNavigate("manage_books/${book.id}") },
                        onDelete = { userProfile.storeId?.let { booksViewModel.deleteBook(book.id, it) } }
                    )
                }
            }

            if (reservations.isNotEmpty()) {
                item {
                    SectionHeader("RESERVATIONS", burgundy, gold)
                }
                
                items(reservations.reversed()) { res ->
                    ReservationVintageItemOwner(
                        res = res,
                        burgundy = burgundy,
                        gold = gold,
                        charcoal = charcoal,
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
fun StatCard(label: String, value: String, burgundy: Color, gold: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = burgundy),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                value, 
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), 
                color = gold
            )
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif, letterSpacing = 2.sp), 
                color = gold.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, burgundy: Color, gold: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            title, 
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif, 
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ), 
            color = burgundy
        )
        TextButton(onClick = { /* View All */ }) {
            Text("ARCHIVE", color = gold, style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryVintageItem(book: Book, burgundy: Color, gold: Color, charcoal: Color, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, burgundy.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                color = burgundy,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, gold.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(book.category ?: ""),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = gold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title ?: "Untitled", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = burgundy, maxLines = 1)
                Text((book.category ?: "Uncategorized").uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif, letterSpacing = 1.sp), color = charcoal.copy(alpha = 0.6f))
                Text("₹${book.price}", style = MaterialTheme.typography.titleSmall, color = gold, fontWeight = FontWeight.Bold)
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = burgundy.copy(alpha = 0.6f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF800000).copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun ReservationVintageItemOwner(
    res: Reservation,
    burgundy: Color,
    gold: Color,
    charcoal: Color,
    onApprove: () -> Unit, 
    onReject: () -> Unit,
    onUpdateDates: (Long, Long) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = burgundy.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(4.dp),
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
                StatusBadgeOwner(res.status, burgundy, gold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("STUDENT: ${res.userName.uppercase()}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif, letterSpacing = 1.sp), color = burgundy)
            Text("EMAIL: ${res.userEmail}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = charcoal.copy(alpha = 0.7f))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = burgundy.copy(alpha = 0.1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("RESERVATION PERIOD:", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif), color = charcoal.copy(alpha = 0.5f))
                    Text(
                        "${dateFormatter.format(java.util.Date(res.startDate))} - ${dateFormatter.format(java.util.Date(res.endDate))}", 
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = burgundy
                    )
                }
                if (res.status == "PENDING") {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Dates", modifier = Modifier.size(18.dp), tint = gold)
                    }
                }
            }
            
            if (res.status == "PENDING") {
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = burgundy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                    ) {
                        Text("APPROVE", color = gold, style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = charcoal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, charcoal.copy(alpha = 0.3f))
                    ) {
                        Text("REJECT", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var newStart by remember { mutableStateOf(res.startDate) }
        var newEnd by remember { mutableStateOf(res.endDate) }
        
        AlertDialog(
            containerColor = Color(0xFFF5F5DC),
            onDismissRequest = { showEditDialog = false },
            title = { Text("ADJUST RESERVATION DATES", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = Color(0xFF800020)) },
            text = {
                Column {
                    Text("Update the reservation period for this book.", fontFamily = FontFamily.Serif, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newStart.toString(),
                        onValueChange = { newStart = it.toLongOrNull() ?: newStart },
                        label = { Text("START DATE (Timestamp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newEnd.toString(),
                        onValueChange = { newEnd = it.toLongOrNull() ?: newEnd },
                        label = { Text("END DATE (Timestamp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    onUpdateDates(newStart, newEnd)
                    showEditDialog = false 
                }) {
                    Text("UPDATE", color = Color(0xFF800020), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("CANCEL", color = Color(0xFF333333))
                }
            }
        )
    }
}

@Composable
fun StatusBadgeOwner(status: String, burgundy: Color, gold: Color) {
    val bgColor = when(status.uppercase()) {
        "APPROVED" -> burgundy
        "PENDING" -> gold.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    val textColor = if (status.uppercase() == "APPROVED") gold else burgundy
    val border = if (status.uppercase() == "APPROVED") null else androidx.compose.foundation.BorderStroke(1.dp, burgundy.copy(alpha = 0.3f))

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        border = border
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = textColor
        )
    }
}

