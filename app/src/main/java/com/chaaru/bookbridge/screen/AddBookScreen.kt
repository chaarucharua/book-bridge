package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 8: Add Book ────────────────────────────────────────
@Composable
fun AddBookScreen(
    isEditing: Boolean = false,
    onBack: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    var title      by remember { mutableStateOf("") }
    var author     by remember { mutableStateOf("") }
    var category   by remember { mutableStateOf("") }
    var edition    by remember { mutableStateOf("") }
    var price      by remember { mutableStateOf("") }
    var origPrice  by remember { mutableStateOf("") }
    var condition  by remember { mutableStateOf("Good") }
    var submitted  by remember { mutableStateOf(false) }

    val conditions = listOf("New", "Good", "Fair", "Poor")

    Scaffold(
        topBar         = { BBTopBar(if (isEditing) "Edit Book" else "Add New Book", onBack = onBack) },
        containerColor = Parchment
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Parchment)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Upload area ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(White)
                    .border(2.dp, BurgundyLight, RoundedCornerShape(14.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Upload, null,
                        tint = BurgundyLight, modifier = Modifier.size(26.dp))
                    Text("Upload Book Cover",
                        fontSize   = 12.sp,
                        color      = Burgundy,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(top = 4.dp))
                    Text("JPG, PNG up to 5MB",
                        fontSize = 10.sp, color = Slate400)
                }
            }

            // ── Book Information card ─────────────────────────
            FormSection(title = "Book Information") {
                BBTextField(title, { title = it }, "Book Title *", leadingIcon = Icons.AutoMirrored.Filled.MenuBook)
                Spacer(Modifier.height(10.dp))
                BBTextField(author, { author = it }, "Author Name *", leadingIcon = Icons.Default.Person)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BBTextField(category, { category = it }, "Category *",
                        modifier = Modifier.weight(1f))
                    BBTextField(edition, { edition = it }, "Edition",
                        modifier = Modifier.weight(1f))
                }
            }

            // ── Pricing card ──────────────────────────────────
            FormSection(title = "Pricing") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BBTextField(price, { price = it }, "Your Price ₹ *",
                        leadingIcon = Icons.Default.CurrencyRupee,
                        modifier    = Modifier.weight(1f))
                    BBTextField(origPrice, { origPrice = it }, "Original Price ₹",
                        modifier = Modifier.weight(1f))
                }
            }

            // ── Condition card ────────────────────────────────
            FormSection(title = "Book Condition *") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    conditions.forEach { cond ->
                        val isSelected = condition == cond
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BurgundyFaint else White)
                                .border(
                                    1.5.dp,
                                    if (isSelected) Burgundy else Slate200,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { condition = cond },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                cond,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isSelected) Burgundy else Slate500
                            )
                        }
                    }
                }
            }

            // Success message
            if (submitted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenBg)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null,
                        tint = GreenText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Book updated!" else "Book listed successfully! 🎉",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenText)
                }
            }

            // Submit button
            BurgundyButton(
                text       = if (isEditing) "Update Book" else "List This Book",
                onClick    = { submitted = true; onSubmit() },
                modifier   = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Add
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Form Section Card ──────────────────────────────────────────
@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = Slate700,
                modifier   = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}