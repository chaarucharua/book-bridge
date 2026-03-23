package com.chaaru.bookbridge.screen
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ManageBooksScreen(
    onBack: () -> Unit = {},
    onEditBook: (String) -> Unit = {}
) {
    var query       by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var deleteTarget by remember { mutableStateOf<ManageBookUiModel?>(null) }

    val filtered = manageSamples
        .filter { if (query.isBlank()) true else
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }
        .filter { when (selectedTab) { 1 -> it.isAvailable; 2 -> !it.isAvailable; else -> true } }

    // Delete dialog
    deleteTarget?.let { book ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title  = { Text("Delete Book", fontWeight = FontWeight.Bold) },
            text   = { Text("Remove \"${book.title}\" from your listing?") },
            confirmButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Delete", color = Red400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar         = { BBTopBar("Manage Books", onBack = onBack) },
        containerColor = Parchment
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Parchment)
                .padding(padding)
        ) {
            // Search + tabs header
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it },
                    modifier      = Modifier.fillMaxWidth().height(50.dp),
                    placeholder   = { Text("Search your inventory...", fontSize = 13.sp, color = Slate400) },
                    leadingIcon   = { Icon(Icons.Default.Search, null,
                        tint = Slate400, modifier = Modifier.size(18.dp)) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Burgundy,
                        unfocusedBorderColor = Slate200,
                        unfocusedContainerColor = ParchmentMid,
                        focusedContainerColor   = ParchmentLight
                    )
                )
                Spacer(Modifier.height(8.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = White,
                    contentColor     = Burgundy,
                    indicator        = { pos ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(pos[selectedTab]),
                            color = Burgundy
                        )
                    }
                ) {
                    listOf(
                        "All (${manageSamples.size})",
                        "Available",
                        "Reserved"
                    ).forEachIndexed { i, t ->
                        Tab(
                            selected             = selectedTab == i,
                            onClick              = { selectedTab = i },
                            text                 = { Text(t, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            selectedContentColor = Burgundy,
                            unselectedContentColor = Slate400
                        )
                    }
                }
            }

            // Book list
            LazyColumn(
                modifier       = Modifier.fillMaxSize().background(Parchment),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filtered.isEmpty()) item {
                    Box(Modifier.fillMaxWidth().padding(top = 60.dp), Alignment.Center) {
                        Text("No books found", color = Slate400, fontSize = 14.sp)
                    }
                }
                items(filtered) { book ->
                    ManageBookCard(
                        book     = book,
                        onEdit   = { onEditBook(book.id) },
                        onDelete = { deleteTarget = book }
                    )
                }
            }
        }
    }
}

// ── Manage Book Card ───────────────────────────────────────────
@Composable
fun ManageBookCard(
    book: ManageBookUiModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookThumb(emoji = book.emoji, width = 46, height = 58)
            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(book.title,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Slate800,
                    maxLines   = 1)
                Text("${book.author} · ${book.category}",
                    fontSize = 10.sp,
                    color    = Slate400,
                    modifier = Modifier.padding(top = 2.dp))

                // Tags row
                Row(
                    Modifier.padding(top = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val (condBg, condFg) = when (book.condition) {
                        "New", "Like New" -> GreenBg to GreenText
                        "Good"            -> GreenBg to GreenText
                        else              -> AmberBg to AmberText
                    }
                    MiniTag(book.condition, condBg, condFg)
                    MiniTag(
                        if (book.isAvailable) "Available" else "Reserved",
                        if (book.isAvailable) GreenBg else BurgundyFaint,
                        if (book.isAvailable) GreenText else Burgundy
                    )
                }
            }

            // Price + actions
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${book.price}",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Burgundy
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Edit button
                    IconButton(
                        onClick  = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BurgundyFaint)
                    ) {
                        Icon(Icons.Default.Edit, "Edit",
                            tint = Burgundy, modifier = Modifier.size(14.dp))
                    }
                    // Delete button
                    IconButton(
                        onClick  = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RedBg)
                    ) {
                        Icon(Icons.Default.Delete, "Delete",
                            tint = Red400, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ── UI Model + Sample Data ─────────────────────────────────────
data class ManageBookUiModel(
    val id: String, val title: String, val author: String,
    val category: String, val condition: String, val emoji: String,
    val price: Int, val isAvailable: Boolean
)

val manageSamples = listOf(
    ManageBookUiModel("1","Data Structures & Algorithms","T. Cormen","CS","Good","📘",220,false),
    ManageBookUiModel("2","Physics Vol II","H.C. Verma","Science","Fair","📙",150,true),
    ManageBookUiModel("3","Organic Chemistry","P. Morrison","Science","Like New","📗",185,true),
    ManageBookUiModel("4","Engineering Maths II","B.S. Grewal","Math","Fair","📕",130,true),
)
