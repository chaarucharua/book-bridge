package com.chaaru.bookbridge.data.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student", // student / owner
    val phone: String = "",
    val storeId: String? = null
)

data class Store(
    @DocumentId val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val location: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val upiId: String = ""
)

data class Book(
    @DocumentId val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val description: String = "",
    val condition: String = "",
    val price: Double = 0.0,
    val advancePrice: Double = 0.0,
    val storeId: String = "",
    val storeName: String = "",
    val storeUpi: String = "",
    val ownerId: String = "",
    val imageUrl: String = "",
    val status: String = "available", // available, reserved, sold
    val available: Boolean = true,
    val rating: Double = 0.0,
    val reviewCount: Long = 0L
)

data class Review(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val userName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Booking(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val bookImageUrl: String = "",
    val storeId: String = "",
    val storeUpi: String = "",
    val advancePaid: Double = 0.0,
    val totalPrice: Double = 0.0,
    val status: String = "advance_paid", // advance_paid, completed, cancelled
    val paymentType: String = "partial",
    val paymentId: String = "",
    val studentPhone: String = "",
    val studentEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
