package com.chaaru.bookbridge.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Fiction" -> Icons.Default.AutoStories
        "Non-Fiction" -> Icons.Default.Book
        "Science" -> Icons.Default.Science
        "Technology" -> Icons.Default.Computer
        "History" -> Icons.Default.History
        "Biography" -> Icons.Default.Psychology
        "Business" -> Icons.Default.Business
        "Education" -> Icons.Default.School
        else -> Icons.AutoMirrored.Filled.MenuBook
    }
}
