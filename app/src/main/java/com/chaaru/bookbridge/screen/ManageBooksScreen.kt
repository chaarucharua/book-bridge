package com.chaaru.bookbridge.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBooksScreen(
    bookId: String? = null,
    booksViewModel: BooksViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val user = authViewModel.profile.value ?: return
    val allBooks by booksViewModel.allBooks.collectAsState()
    val existingBook = remember(bookId, allBooks) { allBooks.find { it.id == bookId } }
    val isLoading by booksViewModel.isLoading
    
    // Fetch store info for UPI ID
    var storeUpi by remember { mutableStateOf("") }
    LaunchedEffect(user.storeId) {
        // In a real app, you'd fetch the store details here
        // For now, we'll assume it's stored in the user profile or just use a default
        storeUpi = "bookbridge.store@upi" // Default fallback
    }

    var title by remember { mutableStateOf(existingBook?.title ?: "") }
    var author by remember { mutableStateOf(existingBook?.author ?: "") }
    var category by remember { mutableStateOf(existingBook?.category ?: "") }
    var description by remember { mutableStateOf(existingBook?.description ?: "") }
    var price by remember { mutableStateOf(existingBook?.price?.toString() ?: "") }
    var advancePrice by remember { mutableStateOf(existingBook?.advancePrice?.toString() ?: "") }
    var condition by remember { mutableStateOf(existingBook?.condition ?: "") }
    var imageUrl by remember { mutableStateOf(existingBook?.imageUrl ?: "") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            booksViewModel.uploadImage(context, it) { path -> imageUrl = path }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (bookId == null) "Add Book" else "Edit Book", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Upload Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(White)
                    .border(1.dp, Burgundy.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotEmpty()) {
                    BookImage(
                        imageUrl = imageUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, "Upload", tint = Burgundy, modifier = Modifier.size(48.dp))
                        Text("Upload Book Cover", color = Burgundy)
                    }
                }
                if (isLoading) CircularProgressIndicator(color = Burgundy)
            }

            VintageTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = "Book Title",
                primaryColor = Slate900,
                secondaryColor = Burgundy
            )
            VintageTextField(
                value = author, 
                onValueChange = { author = it }, 
                label = "Author",
                primaryColor = Slate900,
                secondaryColor = Burgundy
            )
            VintageTextField(
                value = category, 
                onValueChange = { category = it }, 
                label = "Category",
                primaryColor = Slate900,
                secondaryColor = Burgundy
            )
            VintageTextField(
                value = condition, 
                onValueChange = { condition = it }, 
                label = "Condition",
                primaryColor = Slate900,
                secondaryColor = Burgundy
            )
            VintageTextField(
                value = price, 
                onValueChange = { price = it }, 
                label = "Total Price (₹)",
                primaryColor = Slate900,
                secondaryColor = Burgundy
            )
            VintageTextField(
                value = advancePrice, 
                onValueChange = { advancePrice = it }, 
                label = "Advance Amount (₹)",
                primaryColor = Slate900,
                secondaryColor = Burgundy
            )
            VintageTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = "Description",
                primaryColor = Slate900,
                secondaryColor = Burgundy,
                minLines = 3
            )

            val isFormValid = title.isNotBlank() && author.isNotBlank() && category.isNotBlank() && 
                             condition.isNotBlank() && price.isNotBlank() && advancePrice.isNotBlank() && imageUrl.isNotBlank()

            Button(
                onClick = {
                    val book = Book(
                        id = bookId ?: "",
                        title = title,
                        author = author,
                        category = category,
                        description = description,
                        condition = condition,
                        price = price.toDoubleOrNull() ?: 0.0,
                        advancePrice = advancePrice.toDoubleOrNull() ?: 0.0,
                        storeId = user.storeId ?: "",
                        storeName = user.name,
                        storeUpi = storeUpi,
                        ownerId = user.uid,
                        imageUrl = imageUrl
                    )
                    if (bookId == null) booksViewModel.addBook(book) else booksViewModel.updateBook(book)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (bookId == null) "Add Book" else "Update Book", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

