package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.chaaru.bookbridge.data.model.Book
import com.chaaru.bookbridge.ui.getCategoryIcon
import com.chaaru.bookbridge.viewmodel.AuthViewModel
import com.chaaru.bookbridge.viewmodel.BooksViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBooksScreen(bookId: String? = null, booksViewModel: BooksViewModel, authViewModel: AuthViewModel, onBack: () -> Unit) {
    val userProfile = authViewModel.profile.value ?: return
    val allBooks by booksViewModel.allBooks.collectAsState()
    val existingBook = remember(bookId, allBooks) { allBooks.find { it.id == bookId } }
    val isLoading = booksViewModel.isLoading.value
    val errorMessage = booksViewModel.error.value
    val context = LocalContext.current

    // Show Toast for error
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Simple Palette
    val creamBackground = Color(0xFFF5F5DC)
    val burgundy = Color(0xFF800020)
    val gold = Color(0xFFD4AF37)
    val charcoal = Color(0xFF333333)

    val categories = listOf("Fiction", "Tech", "Science", "History", "Children", "Non-Fiction")
    var title by remember(existingBook) { mutableStateOf(existingBook?.title ?: "") }
    var author by remember(existingBook) { mutableStateOf(existingBook?.author ?: "") }
    var category by remember(existingBook) { mutableStateOf(existingBook?.category ?: "Fiction") }
    var description by remember(existingBook) { mutableStateOf(existingBook?.description ?: "") }
    var price by remember(existingBook) { mutableStateOf(existingBook?.price?.toString() ?: "") }
    var condition by remember(existingBook) { mutableStateOf(existingBook?.condition ?: "Good") }
    var expanded by remember { mutableStateOf(false) }

    // Reservation period in Add Book
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf(calendar.timeInMillis) }
    calendar.add(Calendar.DAY_OF_YEAR, 7)
    var endDate by remember { mutableStateOf(calendar.timeInMillis) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = creamBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (bookId == null) "Add New Book" else "Edit Book", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = burgundy,
                    titleContentColor = gold
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Book Information",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                color = burgundy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Provide details about the book you want to share.",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                color = charcoal.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Icon Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(burgundy.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .border(1.dp, burgundy.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = category,
                        modifier = Modifier.size(80.dp),
                        tint = burgundy.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "CATEGORY ICON PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif, letterSpacing = 1.sp),
                        color = burgundy.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("Book Title", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = charcoal,
                    unfocusedTextColor = charcoal,
                    focusedBorderColor = burgundy,
                    unfocusedBorderColor = burgundy.copy(alpha = 0.3f),
                    focusedLabelColor = burgundy,
                    cursorColor = burgundy
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = author, 
                onValueChange = { author = it }, 
                label = { Text("Author", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = charcoal,
                    unfocusedTextColor = charcoal,
                    focusedBorderColor = burgundy,
                    unfocusedBorderColor = burgundy.copy(alpha = 0.3f),
                    focusedLabelColor = burgundy,
                    cursorColor = burgundy
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = charcoal,
                        unfocusedTextColor = charcoal,
                        focusedLabelColor = burgundy,
                        unfocusedLabelColor = burgundy.copy(alpha = 0.5f),
                        cursorColor = burgundy,
                        focusedBorderColor = burgundy,
                        unfocusedBorderColor = burgundy.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(creamBackground)
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption, fontFamily = FontFamily.Serif, color = burgundy) },
                            onClick = {
                                category = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = condition, 
                onValueChange = { condition = it }, 
                label = { Text("Condition", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = charcoal,
                    unfocusedTextColor = charcoal,
                    focusedBorderColor = burgundy,
                    unfocusedBorderColor = burgundy.copy(alpha = 0.3f),
                    focusedLabelColor = burgundy,
                    cursorColor = burgundy
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = { Text("Description", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = charcoal,
                    unfocusedTextColor = charcoal,
                    focusedBorderColor = burgundy,
                    unfocusedBorderColor = burgundy.copy(alpha = 0.3f),
                    focusedLabelColor = burgundy,
                    cursorColor = burgundy
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = price, 
                onValueChange = { price = it }, 
                label = { Text("Price (₹)", fontFamily = FontFamily.Serif) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = charcoal,
                    unfocusedTextColor = charcoal,
                    focusedBorderColor = burgundy,
                    unfocusedBorderColor = burgundy.copy(alpha = 0.3f),
                    focusedLabelColor = burgundy,
                    cursorColor = burgundy
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Initial Reservation Availability", 
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                color = burgundy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Set the default availability period for this book.",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                color = charcoal.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { showStartDatePicker = true }) {
                    Text("Available From", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif), color = burgundy.copy(alpha = 0.6f))
                    Surface(
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, burgundy.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            dateFormatter.format(Date(startDate)), 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            color = burgundy
                        )
                    }
                }
                
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(horizontal = 8.dp).size(20.dp), tint = burgundy.copy(alpha = 0.3f))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).clickable { showEndDatePicker = true }) {
                    Text("Available To", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif), color = burgundy.copy(alpha = 0.6f))
                    Surface(
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, burgundy.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            dateFormatter.format(Date(endDate)), 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            color = burgundy
                        )
                    }
                }
            }

            // Simple Date Pickers (Material3)
            if (showStartDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
                DatePickerDialog(
                    onDismissRequest = { showStartDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            startDate = datePickerState.selectedDateMillis ?: startDate
                            showStartDatePicker = false
                        }) { Text("OK", color = burgundy) }
                    }
                ) { DatePicker(state = datePickerState) }
            }
            
            if (showEndDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
                DatePickerDialog(
                    onDismissRequest = { showEndDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            endDate = datePickerState.selectedDateMillis ?: endDate
                            showEndDatePicker = false
                        }) { Text("OK", color = burgundy) }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = burgundy)
                }
            } else {
                val isFormValid = title.isNotBlank() && 
                                author.isNotBlank() && 
                                category.isNotBlank() && 
                                price.toDoubleOrNull() != null

                Button(
                    onClick = {
                        if (isFormValid) {
                            val book = Book(
                                title = title.trim(),
                                author = author.trim(),
                                category = category,
                                condition = condition.trim(),
                                description = description.trim(),
                                price = price.toDoubleOrNull() ?: 0.0,
                                storeId = userProfile.storeId ?: "",
                                storeName = userProfile.name,
                                displayStoreName = "${userProfile.name}'s Store",
                                ownerId = userProfile.uid,
                                available = existingBook?.available ?: true,
                                rating = existingBook?.rating ?: 0.0,
                                reviewCount = existingBook?.reviewCount ?: 0L,
                                startDate = startDate,
                                endDate = endDate
                            )
                            if (bookId == null) {
                                booksViewModel.addBook(book).invokeOnCompletion { 
                                    if (booksViewModel.error.value == null) onBack() 
                                }
                            } else {
                                booksViewModel.updateBook(book.copy(id = bookId)).invokeOnCompletion { 
                                    if (booksViewModel.error.value == null) onBack() 
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = isFormValid,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = burgundy,
                        disabledContainerColor = burgundy.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, gold)
                ) {
                    Text(
                        if (bookId == null) "SAVE BOOK" else "UPDATE BOOK", 
                        color = gold,
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                    )
                }
                if (!isFormValid) {
                    Text(
                        "Please fill all required fields.",
                        color = burgundy,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


