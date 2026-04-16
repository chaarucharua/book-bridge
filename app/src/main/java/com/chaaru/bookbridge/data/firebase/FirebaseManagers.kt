package com.chaaru.bookbridge.data.firebase

import android.content.Context
import android.net.Uri
import com.chaaru.bookbridge.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun currentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): UserProfile? = try {
        db.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
    } catch (e: Exception) {
        null
    }

    suspend fun login(email: String, password: String): Result<UserProfile> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val profile = getUserProfile(result.user!!.uid)
        if (profile != null) Result.success(profile)
        else Result.failure(Exception("Profile not found"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(name: String, email: String, password: String, role: String, phone: String, storeName: String? = null, location: String? = null): Result<UserProfile> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        
        var storeId: String? = null
        if (role == "owner") {
            val storeRef = db.collection("stores").document()
            storeId = storeRef.id
            db.collection("stores").document(storeId).set(Store(
                id = storeId,
                name = storeName ?: "$name's Store",
                ownerId = uid,
                location = location ?: "TBD",
                description = "Book marketplace store"
            )).await()
        }
        val profile = UserProfile(uid = uid, name = name, email = email, role = role, phone = phone, storeId = storeId)
        db.collection("users").document(uid).set(profile).await()

        Result.success(profile)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() = auth.signOut()

    suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProfile(profile: UserProfile): Result<Unit> = try {
        db.collection("users").document(profile.uid).set(profile).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAccount(): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("Not logged in")
        val uid = user.uid
        val profile = getUserProfile(uid)
        db.collection("users").document(uid).delete().await()
        profile?.storeId?.let { storeId ->
            db.collection("stores").document(storeId).delete().await()
            val books = db.collection("books").whereEqualTo("storeId", storeId).get().await()
            for (doc in books) doc.reference.delete().await()
        }
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getBooksFlow(storeId: String? = null): Flow<List<Book>> = callbackFlow {
        val query = if (storeId != null) db.collection("books").whereEqualTo("storeId", storeId)
                    else db.collection("books")
        
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val books = snapshot.toObjects(Book::class.java)
                trySend(books)
            }
        }
        awaitClose { subscription.remove() }
    }

    fun getBookingsFlow(userId: String? = null, storeId: String? = null): Flow<List<Booking>> = callbackFlow {
        var query: Query = db.collection("bookings")
        if (userId != null) query = query.whereEqualTo("userId", userId)
        if (storeId != null) query = query.whereEqualTo("storeId", storeId)
        
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val bookings = snapshot.toObjects(Booking::class.java)
                trySend(bookings)
            }
        }
        awaitClose { subscription.remove() }
    }

    fun getReviewsFlow(bookId: String): Flow<List<Review>> = callbackFlow {
        val subscription = db.collection("reviews")
            .whereEqualTo("bookId", bookId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    trySend(reviews)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addReview(review: Review): Result<Unit> = try {
        val reviewRef = db.collection("reviews").document()
        val reviewWithId = review.copy(id = reviewRef.id)
        reviewRef.set(reviewWithId).await()
        
        // Update book rating
        val bookRef = db.collection("books").document(review.bookId)
        db.runTransaction { transaction ->
            val bookSnapshot = transaction.get(bookRef)
            val book = bookSnapshot.toObject(Book::class.java)
            if (book != null) {
                val newCount = book.reviewCount + 1
                val newRating = (book.rating * book.reviewCount + review.rating) / newCount
                transaction.update(bookRef, "rating", newRating)
                transaction.update(bookRef, "reviewCount", newCount)
            }
        }.await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadImage(context: Context, uri: Uri): String {
        val fileName = "book_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("book_covers/$fileName")
        
        // Save locally first as a cache
        val localFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(localFile).use { output ->
                input.copyTo(output)
            }
        }

        // Upload to Firebase Storage
        return try {
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            // If upload fails, return local path as fallback (though it won't be cross-device)
            localFile.absolutePath
        }
    }

    suspend fun saveImageLocally(context: Context, uri: Uri): String {
        return uploadImage(context, uri)
    }

    suspend fun createBooking(booking: Booking): Result<Unit> = try {
        val bookingRef = db.collection("bookings").document()
        val bookingWithId = booking.copy(id = bookingRef.id)
        bookingRef.set(bookingWithId).await()
        db.collection("books").document(booking.bookId).update(
            "status", "reserved",
            "available", false
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = try {
        val bookingRef = db.collection("bookings").document(bookingId)
        val snapshot = bookingRef.get().await()
        val booking = snapshot.toObject(Booking::class.java)
        
        if (booking != null) {
            bookingRef.update("status", status).await()
            // Sync book status
            val bookStatus = when(status) {
                "completed" -> "sold"
                "cancelled" -> "available"
                "advance_paid" -> "reserved"
                else -> null
            }
            
            bookStatus?.let {
                db.collection("books").document(booking.bookId).update(
                    "status", it,
                    "available", it == "available"
                ).await()
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Booking not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteBooking(bookingId: String): Result<Unit> = try {
        val bookingRef = db.collection("bookings").document(bookingId)
        val snapshot = bookingRef.get().await()
        val booking = snapshot.toObject(Booking::class.java)
        
        if (booking != null) {
            // Revert book status to available
            db.collection("books").document(booking.bookId).update(
                "status", "available",
                "available", true
            ).await()
            
            bookingRef.delete().await()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Booking not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addBook(book: Book) {
        val bookRef = db.collection("books").document()
        val bookWithId = book.copy(id = bookRef.id)
        bookRef.set(bookWithId).await()
    }

    suspend fun updateBook(book: Book) {
        db.collection("books").document(book.id).set(book).await()
    }

    suspend fun deleteBook(id: String, localImagePath: String? = null) {
        db.collection("books").document(id).delete().await()
        localImagePath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }
}
