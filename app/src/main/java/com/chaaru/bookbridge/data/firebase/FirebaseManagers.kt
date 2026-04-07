package com.chaaru.bookbridge.data.firebase

import com.chaaru.bookbridge.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun currentUser() = auth.currentUser

    suspend fun getUserProfile(uid: String): UserProfile? = try {
        db.collection("users").document(uid).get(com.google.firebase.firestore.Source.SERVER).await().toObject(UserProfile::class.java)
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
        
        // 1. Get profile to check for storeId
        val profile = getUserProfile(uid)
        
        // 2. Delete User Profile
        db.collection("users").document(uid).delete().await()
        
        // 3. If owner, delete store and associated books
        profile?.storeId?.let { storeId ->
            db.collection("stores").document(storeId).delete().await()
            val books = db.collection("books").whereEqualTo("storeId", storeId).get().await()
            for (doc in books) {
                doc.reference.delete().await()
            }
        }
        
        // 4. Delete user's reservations
        val reservations = db.collection("reservations").whereEqualTo("userId", uid).get().await()
        for (doc in reservations) {
            doc.reference.delete().await()
        }

        // 5. Delete Auth User
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<UserProfile> = try {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val uid = result.user!!.uid
        val existing = getUserProfile(uid)
        if (existing == null) {
            val profile = UserProfile(uid = uid, name = result.user!!.displayName ?: "User", email = result.user!!.email ?: "", role = "student")
            db.collection("users").document(uid).set(profile).await()
            Result.success(profile)
        } else {
            Result.success(existing)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getBooks(category: String = "All", queryText: String = "", storeId: String? = null): List<Book> = try {
        var query: Query = db.collection("books")
        if (storeId != null) query = query.whereEqualTo("storeId", storeId)
        
        val snapshot = query.get().await()
        snapshot.toObjects(Book::class.java)
    } catch (e: Exception) {
        android.util.Log.e("FirestoreManager", "Error fetching books", e)
        emptyList()
    }

    suspend fun getAllStores(): List<Store> = try {
        db.collection("stores").get().await().toObjects(Store::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun toggleFavorite(userId: String, bookId: String): Boolean = try {
        val ref = db.collection("users").document(userId).collection("favorites").document(bookId)
        if (ref.get().await().exists()) {
            ref.delete().await()
            false
        } else {
            ref.set(mapOf("bookId" to bookId)).await()
            true
        }
    } catch (e: Exception) {
        false
    }

    suspend fun getFavorites(userId: String): List<String> = try {
        db.collection("users").document(userId).collection("favorites").get().await().map { it.id }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun reserveBook(res: Reservation): Result<Unit> = try {
        val existing = db.collection("reservations")
            .whereEqualTo("bookId", res.bookId)
            .whereEqualTo("status", "APPROVED")
            .get().await()
        
        if (!existing.isEmpty) throw Exception("Book already reserved")
        
        // Ensure storeName is populated in reservation for better UI
        val bookDoc = db.collection("books").document(res.bookId).get().await()
        val storeName = bookDoc.getString("storeName") ?: "Unknown Store"
        
        db.collection("reservations").add(res).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getReservations(userId: String? = null, storeId: String? = null): List<Reservation> = try {
        var query: Query = db.collection("reservations")
        if (userId != null) query = query.whereEqualTo("userId", userId)
        if (storeId != null) query = query.whereEqualTo("storeId", storeId)
        query.get().await().toObjects(Reservation::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun updateReservationStatus(id: String, status: String) {
        db.collection("reservations").document(id).update("status", status).await()
    }

    suspend fun updateReservationDates(id: String, startDate: Long, endDate: Long) {
        db.collection("reservations").document(id).update(
            "startDate", startDate,
            "endDate", endDate
        ).await()
    }

    suspend fun deleteReservation(id: String) {
        db.collection("reservations").document(id).delete().await()
    }

    suspend fun addBook(book: Book) { 
        val storeId = book.storeId ?: ""
        val store = if (storeId.isNotEmpty()) db.collection("stores").document(storeId).get().await().toObject(Store::class.java) else null
        val bookWithInfo = book.copy(
            storeName = store?.name ?: book.storeName,
            displayStoreName = store?.name ?: book.storeName,
            ownerId = store?.ownerId ?: book.ownerId
        )
        // Explicitly remove id to let Firestore generate one
        val bookMap = hashMapOf(
            "title" to bookWithInfo.title,
            "author" to bookWithInfo.author,
            "category" to bookWithInfo.category,
            "description" to bookWithInfo.description,
            "condition" to bookWithInfo.condition,
            "price" to bookWithInfo.price,
            "storeId" to bookWithInfo.storeId,
            "storeName" to bookWithInfo.storeName,
            "ownerId" to bookWithInfo.ownerId,
            "displayStoreName" to bookWithInfo.displayStoreName,
            "available" to bookWithInfo.available,
            "rating" to bookWithInfo.rating,
            "reviewCount" to bookWithInfo.reviewCount,
            "startDate" to bookWithInfo.startDate,
            "endDate" to bookWithInfo.endDate
        )
        db.collection("books").add(bookMap).await()
    }

    suspend fun updateBook(book: Book) { 
        val storeId = book.storeId ?: ""
        val store = if (storeId.isNotEmpty()) db.collection("stores").document(storeId).get().await().toObject(Store::class.java) else null
        val bookWithInfo = book.copy(
            storeName = store?.name ?: book.storeName,
            displayStoreName = store?.name ?: book.storeName,
            ownerId = store?.ownerId ?: book.ownerId
        )
        val bookMap = hashMapOf(
            "title" to bookWithInfo.title,
            "author" to bookWithInfo.author,
            "category" to bookWithInfo.category,
            "description" to bookWithInfo.description,
            "condition" to bookWithInfo.condition,
            "price" to bookWithInfo.price,
            "storeId" to bookWithInfo.storeId,
            "storeName" to bookWithInfo.storeName,
            "ownerId" to bookWithInfo.ownerId,
            "displayStoreName" to bookWithInfo.displayStoreName,
            "available" to bookWithInfo.available,
            "rating" to bookWithInfo.rating,
            "reviewCount" to bookWithInfo.reviewCount,
            "startDate" to bookWithInfo.startDate,
            "endDate" to bookWithInfo.endDate
        )
        db.collection("books").document(book.id).set(bookMap).await()
    }
    suspend fun deleteBook(id: String) { db.collection("books").document(id).delete().await() }

    suspend fun addReview(review: Review) {
        db.collection("reviews").add(review).await()
        val bookRef = db.collection("books").document(review.bookId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(bookRef)
            val oldCount = snapshot.getLong("reviewCount") ?: 0L
            val oldRating = snapshot.getDouble("rating") ?: 0.0
            val newCount = oldCount + 1
            val newRating = (oldRating * oldCount + review.rating) / newCount
            transaction.update(bookRef, "reviewCount", newCount)
            transaction.update(bookRef, "rating", newRating)
        }.await()
    }

    suspend fun getReviews(bookId: String): List<Review> = try {
        db.collection("reviews").whereEqualTo("bookId", bookId).get().await().toObjects(Review::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun resetAndSeed() {
        // Clear everything to allow a clean slate
        val collections = listOf("users", "books", "stores", "reservations", "reviews")
        for (collection in collections) {
            val snapshot = db.collection(collection).get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        }
    }
}
