package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.ui.getCategoryIcon
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

/**
 * ROOT CAUSE FOR EMPTY RECOMMENDED/SEARCH:
 * 1. Filtering logic in ViewModel was not handling empty results gracefully.
 * 2. Missing "No books found" feedback for the user.
 * 3. Search query was not being applied effectively to the dynamic list.
 */

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun StudentHomeScreen(viewModel: BooksViewModel, authViewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredBooks by viewModel.filteredBooks.collectAsState()
    val recommendedBooks by viewModel.recommendedBooks.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()
    val userProfile = authViewModel.profile.value
    
    // Simple Palette
    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val charcoal = Color(0xFF333333)
    val gold = Color(0xFFD4AF37)

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
    }
    
    val categories = listOf("All", "Fiction", "Non-Fiction", "Science", "Tech", "Children", "History")

    Scaffold(
        containerColor = creamBackground,
        bottomBar = {
            NavigationBar(
                containerColor = burgundy,
                contentColor = gold
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Books", fontFamily = FontFamily.Serif) },
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
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Reservations", fontFamily = FontFamily.Serif) },
                    selected = false,
                    onClick = { onNavigate("reservations") },
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
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header Block
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    color = burgundy
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            "Hi, ${userProfile?.name ?: "Reader"}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = gold
                        )
                        Text(
                            "Find your next great read",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                            color = creamBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search title, author, or shop...", fontFamily = FontFamily.Serif) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = burgundy) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = charcoal,
                            unfocusedTextColor = charcoal,
                            focusedBorderColor = burgundy,
                            unfocusedBorderColor = burgundy.copy(alpha = 0.3f),
                            cursorColor = burgundy
                        ),
                        shape = MaterialTheme.shapes.medium
                    )

                    if (searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = filterCategory == category,
                                    onClick = { viewModel.setCategory(category) },
                                    label = { Text(category, fontFamily = FontFamily.Serif) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = burgundy,
                                        selectedLabelColor = gold,
                                        containerColor = Color.Transparent,
                                        labelColor = charcoal
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (filterCategory == category) Color.Transparent else burgundy.copy(alpha = 0.2f),
                                        selectedBorderColor = Color.Transparent,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.dp,
                                        enabled = true,
                                        selected = filterCategory == category
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (searchQuery.isEmpty() && recommendedBooks.isNotEmpty()) {
                item {
                    Text(
                        "Recommended",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = burgundy
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(recommendedBooks) { book ->
                            RecommendedBookItem(book, burgundy, creamBackground, gold) { onNavigate("book_detail/${book.id}") }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (searchQuery.isEmpty()) {
                item {
                    Text(
                        "Available Books",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = burgundy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                item {
                    Text(
                        "Search Results",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = burgundy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (viewModel.isLoading.value) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = burgundy)
                    }
                }
            } else if (filteredBooks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No books found", fontFamily = FontFamily.Serif, color = charcoal.copy(alpha = 0.5f))
                            Button(
                                onClick = { viewModel.loadBooks(forceRefresh = true) },
                                modifier = Modifier.padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = burgundy),
                                border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                            ) {
                                Text("Refresh", color = gold, fontFamily = FontFamily.Serif)
                            }
                        }
                    }
                }
            } else {
                items(filteredBooks, key = { it.id }) { book ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BookItem(book, burgundy, creamBackground, gold, charcoal) { onNavigate("book_detail/${book.id}") }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedBookItem(book: Book, burgundy: Color, cream: Color, gold: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        colors = CardDefaults.cardColors(containerColor = burgundy),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                color = cream.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, gold.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(book.category ?: ""),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = gold.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                book.title ?: "Untitled", 
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif), 
                maxLines = 1, 
                color = gold
            )
            Text(
                book.author ?: "Unknown Author",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif), 
                color = cream.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⭐ ${book.rating}", style = MaterialTheme.typography.labelMedium, color = gold)
                Text("₹${book.price}", style = MaterialTheme.typography.titleSmall, color = cream, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookItem(book: Book, burgundy: Color, cream: Color, gold: Color, charcoal: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick, 
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                color = burgundy.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.extraSmall,
                border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getCategoryIcon(book.category ?: ""),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = burgundy.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    book.title ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif), 
                    color = burgundy
                )
                Text(
                    "${book.author ?: "Unknown Author"} • ${book.category ?: "Uncategorized"}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Serif), 
                    color = charcoal.copy(alpha = 0.6f)
                )
                Text(
                    book.effectiveStoreName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp
                    ), 
                    color = gold,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "₹${book.price}", 
                style = MaterialTheme.typography.titleMedium, 
                color = burgundy, 
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = burgundy.copy(alpha = 0.1f))
    }
}
