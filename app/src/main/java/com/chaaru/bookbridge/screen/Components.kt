package com.chaaru.bookbridge.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import android.util.Log
import java.io.File

@Composable
fun BookImage(imageUrl: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(imageUrl) {
        val result: Any = when {
            imageUrl.isNullOrBlank() -> {
                Log.w("BOOK_IMAGE_LOAD", "ImageUrl is null or blank, using placeholder")
                "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=1000&auto=format&fit=crop"
            }
            imageUrl.startsWith("http") -> imageUrl
            imageUrl.startsWith("content") -> android.net.Uri.parse(imageUrl)
            else -> {
                val file = File(imageUrl)
                if (file.exists()) {
                    file
                } else {
                    // Handle both forward and backward slashes for cross-platform compatibility
                    val fileName = imageUrl.substringAfterLast("/").substringAfterLast("\\")
                    val fallbackFile = File(context.filesDir, fileName)
                    if (fallbackFile.exists()) {
                        fallbackFile
                    } else {
                        // One more attempt: maybe the imageUrl is just the filename itself
                        val simpleFile = File(context.filesDir, imageUrl)
                        if (simpleFile.exists()) {
                            simpleFile
                        } else {
                            Log.e("BOOK_IMAGE_LOAD", "File not found locally: $imageUrl (Tried $fileName and $imageUrl in filesDir)")
                            // Return a pretty placeholder instead of a broken path
                            "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=1000&auto=format&fit=crop"
                        }
                    }
                }
            }
        }
        Log.d("BOOK_IMAGE_LOAD", "Resolved [$imageUrl] to [$result]")
        result
    }

    AsyncImage(
        model = model,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
        error = painterResource(android.R.drawable.ic_menu_report_image),
        onError = { Log.e("BOOK_IMAGE_LOAD", "Coil Error for $imageUrl: ${it.result.throwable.message}") },
        onSuccess = { Log.d("BOOK_IMAGE_LOAD", "Coil Success for $imageUrl") }
    )
}

@Composable
fun BookStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (text, bgColor, textColor) = when(status) {
        "sold" -> Triple("SOLD OUT", RedBg, Color.Red)
        "reserved" -> Triple("RESERVED", AmberBg, AmberText)
        else -> Triple("AVAILABLE", GreenBg, GreenText)
    }
    Surface(
        color = bgColor,
        modifier = modifier,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (statusText, bgColor, textColor) = when(status) {
        "advance_paid" -> Triple("Reserved", AmberBg, AmberText)
        "completed" -> Triple("Completed", GreenBg, GreenText)
        "cancelled" -> Triple("Cancelled", RedBg, Color.Red)
        else -> Triple(status, Slate200, Slate600)
    }
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = Slate500, fontWeight = FontWeight.Bold))
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(color = Slate900))
    }
}


@Composable
fun VintageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Serif, 
                    letterSpacing = 1.sp
                )
            ) 
        },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = primaryColor,
            unfocusedTextColor = primaryColor,
            focusedLabelColor = secondaryColor,
            unfocusedLabelColor = primaryColor.copy(alpha = 0.5f),
            cursorColor = secondaryColor,
            focusedBorderColor = secondaryColor,
            unfocusedBorderColor = primaryColor.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(4.dp),
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif)
    )
}
