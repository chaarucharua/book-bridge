package com.chaaru.bookbridge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen 3: Book Details ────────────────────────────────────
@Composable
fun BookDetailScreen(
    book: BookUiModel = sampleBooks[0],
    onBack: () -> Unit = {},
    onReserve: () -> Unit = {}
) {
    var isWishlisted by remember { mutableStateOf(false) }
    var reserved     by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Parchment)) {

        // ── Hero section: parchment gradient ─────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(Brush.verticalGradient(listOf(Parchment, ParchmentDark)))
        ) {
            // Back button
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .padding(14.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(White)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                    tint = Slate700, modifier = Modifier.size(18.dp))
            }

            // Wishlist button
            IconButton(
                onClick  = { isWishlisted = !isWishlisted },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(White)
            ) {
                Icon(
                    if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Wishlist",
                    tint     = Red400,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Book cover centered
            BookThumb(
                emoji  = book.emoji,
                width  = 102,
                height = 134,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ── Detail body ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Parchment)
                .padding(16.dp)
        ) {
            Text(book.title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Slate900,
                lineHeight = 26.sp)

            Text("by ${book.author}",
                fontSize = 13.sp,
                color    = Slate500,
                modifier = Modifier.padding(top = 4.dp))

            // Meta pills row
            Row(
                modifier              = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Condition
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("✓ ${book.condition}",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = GreenText)
                }
                // Rating
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmberBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("⭐ ${book.rating}",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = AmberText)
                }
                // Category
                if (book.category.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Slate100)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(book.category,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Slate600)
                    }
                }
            }

            // Price row
            Row(
                modifier              = Modifier.padding(top = 14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "₹${book.price}",
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color      = BurgundyDark
                    )
                    if (book.originalPrice > 0) {
                        Text(
                            "₹${book.originalPrice} original",
                            fontSize = 12.sp,
                            color    = Slate400,
                            style    = LocalTextStyle.current.copy(
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }
                }
                // Discount badge
                val discount = if (book.originalPrice > 0)
                    ((1 - book.price.toFloat() / book.originalPrice) * 100).toInt() else 0
                if (discount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AmberBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("$discount% OFF",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = AmberText)
                    }
                }
            }

            // Store row
            Card(
                modifier  = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📖", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(book.storeName, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = Slate800)
                        Text("Anna Nagar, Chennai",
                            fontSize = 11.sp, color = Slate400)
                    }
                    Text(book.storeDistance,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Burgundy)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Reserve button
            if (reserved) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GreenBg)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null,
                        tint = GreenText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reserved Successfully! 🎉",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GreenText)
                }
            } else {
                BurgundyButton(
                    text       = "Reserve This Book",
                    onClick    = { reserved = true; onReserve() },
                    modifier   = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Default.Bookmark
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// Overloaded BookThumb to accept modifier
@Composable
fun BookThumb(
    emoji: String,
    width: Int = 46,
    height: Int = 58,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = width.dp, height = height.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (width * 0.45).sp)
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(5.dp)
                .background(Black12)
        )
    }
}