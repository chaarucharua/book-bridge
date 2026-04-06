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
    val rating: Double = 0.0
)

data class Book(
    @DocumentId val id: String = "",
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val description: String = "",
    val condition: String = "",
    val price: Double = 0.0,
    val storeId: String = "",
    val storeName: String = "",
    val ownerId: String = "",
    val displayStoreName: String = "",
    val available: Boolean = true,
    val rating: Double = 0.0,
    val reviewCount: Long = 0L
) {
    val effectiveStoreName: String
        get() = when {
            !displayStoreName.isNullOrBlank() -> displayStoreName
            !storeName.isNullOrBlank() -> storeName
            else -> "Store $storeId"
        }
}

data class Review(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val userName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Reservation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val storeId: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED, COMPLETED
)
