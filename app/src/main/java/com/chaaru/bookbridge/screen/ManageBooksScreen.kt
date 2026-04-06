package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBooksScreen(bookId: String? = null, booksViewModel: BooksViewModel, authViewModel: AuthViewModel, onBack: () -> Unit) {
    val userProfile = authViewModel.profile.value ?: return
    val allBooks by booksViewModel.allBooks.collectAsState()
    val existingBook = remember(bookId, allBooks) { allBooks.find { it.id == bookId } }

    val categories = listOf("Fiction", "Tech", "Science", "History", "Children", "Non-Fiction")
    var title by remember(existingBook) { mutableStateOf(existingBook?.title ?: "") }
    var author by remember(existingBook) { mutableStateOf(existingBook?.author ?: "") }
    var category by remember(existingBook) { mutableStateOf(existingBook?.category ?: "Fiction") }
    var description by remember(existingBook) { mutableStateOf(existingBook?.description ?: "") }
    var price by remember(existingBook) { mutableStateOf(existingBook?.price?.toString() ?: "") }
    var condition by remember(existingBook) { mutableStateOf(existingBook?.condition ?: "Good") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (bookId == null) "Add New Book" else "Edit Book") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )

            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("Book Title") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = author, 
                onValueChange = { author = it }, 
                label = { Text("Author") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = condition, 
                onValueChange = { condition = it }, 
                label = { Text("Condition (e.g., New, Good, Used)") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = { Text("Description") }, 
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price, 
                onValueChange = { price = it }, 
                label = { Text("Price (₹)") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (title.isNotBlank() && author.isNotBlank() && price.toDoubleOrNull() != null) {
                        val book = Book(
                            id = bookId ?: "",
                            title = title,
                            author = author,
                            category = category,
                            condition = condition,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            storeId = userProfile.storeId ?: "",
                            storeName = existingBook?.storeName ?: "",
                            displayStoreName = existingBook?.displayStoreName ?: "",
                            ownerId = userProfile.uid,
                            available = existingBook?.available ?: true,
                            rating = existingBook?.rating ?: 0.0,
                            reviewCount = existingBook?.reviewCount ?: 0L
                        )
                        if (bookId == null) booksViewModel.addBook(book)
                        else booksViewModel.updateBook(book)
                        
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && author.isNotBlank()
            ) {
                Text(if (bookId == null) "Add to Inventory" else "Update Book")
            }
        }
    }
}
