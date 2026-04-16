package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.chaaru.bookbridge.data.model.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(onBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf(ChatMessage("Hello! How can I help you today?", false)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif)) },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask something...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedBorderColor = Burgundy
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMsg = inputText
                            messages.add(ChatMessage(userMsg, true))
                            inputText = ""
                            // Predefined smart responses
                            val response = when {
                                userMsg.contains("availability", ignoreCase = true) -> 
                                    "Most books are available for immediate booking. Check the 'Available' tag on book details!"
                                userMsg.contains("price", ignoreCase = true) || userMsg.contains("pricing", ignoreCase = true) -> 
                                    "Prices are set by store owners. They range from ₹50 to ₹1000+ depending on the condition."
                                userMsg.contains("how to book", ignoreCase = true) || userMsg.contains("payment", ignoreCase = true) -> 
                                    "Simply find a book you like, click 'Reserve Now', pay the advance amount via UPI/Razorpay, and your booking is confirmed!"
                                userMsg.contains("return", ignoreCase = true) || userMsg.contains("refund", ignoreCase = true) ->
                                    "Returns are subject to individual store policies. Please contact the store owner directly via the details provided in your booking."
                                userMsg.contains("delivery", ignoreCase = true) ->
                                    "Currently, we only support store pickups for reserved books. This helps you inspect the book's condition before final payment!"
                                userMsg.contains("sell", ignoreCase = true) || userMsg.contains("owner", ignoreCase = true) ->
                                    "If you want to sell books, you'll need to register as a Store Owner. You can manage your entire inventory through your dashboard."
                                userMsg.contains("location", ignoreCase = true) || userMsg.contains("where", ignoreCase = true) ->
                                    "Stores are located in various areas. You can see the store's location in the book details page."
                                userMsg.contains("hello", ignoreCase = true) || userMsg.contains("hi", ignoreCase = true) ->
                                    "Hello! I'm your Book Bridge AI assistant. How can I help you find your next book today?"
                                else -> "I'm here to help with availability, pricing, and booking questions. Try asking about those!"
                            }
                            messages.add(ChatMessage(response, false))
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Burgundy, contentColor = White)
                ) {
                    Icon(Icons.Default.Send, "Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (message.isUser) Burgundy else White,
            contentColor = if (message.isUser) White else Slate900,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (message.isUser) 12.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 12.dp
            ),
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
