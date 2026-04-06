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
        // First check if a profile already exists for this email (for seeded users)
        val existingProfileSnap = db.collection("users").whereEqualTo("email", email).get().await()
        
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        
        var profile: UserProfile
        if (!existingProfileSnap.isEmpty) {
            // Seeded user case: Update the existing document with the new UID
            val doc = existingProfileSnap.documents[0]
            val oldUid = doc.id
            val data = doc.toObject(UserProfile::class.java)!!.copy(uid = uid)
            
            // Delete old doc and create new one with correct UID
            db.collection("users").document(oldUid).delete().await()
            db.collection("users").document(uid).set(data).await()
            profile = data
        } else {
            // Normal registration
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
            profile = UserProfile(uid = uid, name = name, email = email, role = role, phone = phone, storeId = storeId)
            db.collection("users").document(uid).set(profile).await()
        }
        Result.success(profile)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() = auth.signOut()

    suspend fun updateProfile(profile: UserProfile): Result<Unit> = try {
        db.collection("users").document(profile.uid).set(profile).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAccount(): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("Not logged in")
        db.collection("users").document(user.uid).delete().await()
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
        val store = db.collection("stores").document(book.storeId).get().await().toObject(Store::class.java)
        val bookWithInfo = book.copy(
            storeName = store?.name ?: book.storeName,
            displayStoreName = store?.name ?: book.storeName,
            ownerId = store?.ownerId ?: book.ownerId
        )
        db.collection("books").add(bookWithInfo).await() 
    }

    suspend fun updateBook(book: Book) { 
        val store = db.collection("stores").document(book.storeId).get().await().toObject(Store::class.java)
        val bookWithInfo = book.copy(
            storeName = store?.name ?: book.storeName,
            displayStoreName = store?.name ?: book.storeName,
            ownerId = store?.ownerId ?: book.ownerId
        )
        db.collection("books").document(book.id).set(bookWithInfo).await()
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
        // 1. Wipe out all books, stores, reservations, and reviews
        val collections = listOf("books", "stores", "reservations", "reviews")
        for (coll in collections) {
            val snap = db.collection(coll).get().await()
            for (doc in snap.documents) {
                doc.reference.delete().await()
            }
        }

        // 2. Wipe out users who are seed owners (using predefined IDs)
        val seedOwnerIds = listOf("owner_alpha", "owner_beta")
        for (uid in seedOwnerIds) {
            db.collection("users").document(uid).delete().await()
        }

        // 3. Create fresh seed owner users
        val owners = listOf(
            UserProfile(uid = "owner_alpha", name = "Alice Johnson", email = "alice@chapterone.com", role = "owner", phone = "1234567890"),
            UserProfile(uid = "owner_beta", name = "Bob Smith", email = "bob@booknook.com", role = "owner", phone = "0987654321")
        )

        for (profile in owners) {
            db.collection("users").document(profile.uid).set(profile).await()
        }

        // 4. Seed with fresh modern data
        val newStores = listOf(
            mapOf(
                "name" to "Chapter One",
                "location" to "City Center",
                "ownerId" to "owner_alpha",
                "desc" to "Specializing in classic literature and modern masterpieces."
            ),
            mapOf(
                "name" to "The Book Nook",
                "location" to "Westside Mall",
                "ownerId" to "owner_beta",
                "desc" to "Your friendly neighborhood spot for tech and sci-fi."
            )
        )

        newStores.forEach { storeData ->
            val storeRef = db.collection("stores").document()
            val storeId = storeRef.id
            val ownerId = storeData["ownerId"] as String
            
            // Update owner's profile with their storeId
            db.collection("users").document(ownerId).update("storeId", storeId).await()

            storeRef.set(Store(
                id = storeId,
                name = storeData["name"] as String,
                ownerId = ownerId,
                location = storeData["location"] as String,
                description = storeData["desc"] as String
            )).await()
            
            val categories = listOf("Fiction", "Tech", "Science", "History", "Children", "Non-Fiction")
            val bookTitles = if (storeData["name"] == "Chapter One") {
                listOf("The Great Gatsby", "Clean Code", "Cosmos", "Sapiens", "The Hobbit")
            } else {
                listOf("1984", "The Pragmatic Programmer", "A Brief History of Time", "Guns, Germs, and Steel", "Peter Pan")
            }

            bookTitles.forEachIndexed { i, title ->
                db.collection("books").add(Book(
                    title = title,
                    author = "Author ${i + 1}",
                    category = categories[i % categories.size],
                    price = 300.0 + (i * 150),
                    condition = "Like New",
                    description = "A meticulously maintained copy of $title. Perfect for collectors.",
                    rating = 4.0 + (i % 3) * 0.5,
                    reviewCount = 10L + i,
                    available = true,
                    storeId = storeId,
                    storeName = storeData["name"] as String,
                    displayStoreName = storeData["name"] as String,
                    ownerId = ownerId
                )).await()
            }
        }
    }
}
