package com.chaaru.bookbridge.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaaru.bookbridge.data.firebase.AuthManager
import com.chaaru.bookbridge.data.firebase.FirestoreManager
import com.chaaru.bookbridge.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val authManager: AuthManager = AuthManager()) : ViewModel() {
    var profile = mutableStateOf<UserProfile?>(null)
    var isLoading = mutableStateOf(false)
    var error = mutableStateOf<String?>(null)

    init {
        authManager.currentUser()?.let { fetchProfile(it.uid) }
    }

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.login(email, password).onSuccess {
                profile.value = it
                onSuccess(it.role)
            }.onFailure { error.value = it.message }
            isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.firebaseAuthWithGoogle(idToken).onSuccess {
                profile.value = it
                onSuccess(it.role)
            }.onFailure { error.value = it.message }
            isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, role: String, phone: String, storeName: String?, location: String?, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.register(name, email, password, role, phone, storeName, location).onSuccess {
                profile.value = it
                onSuccess(it.role)
            }.onFailure { error.value = it.message }
            isLoading.value = false
        }
    }

    fun logout(onComplete: () -> Unit) {
        authManager.logout()
        profile.value = null
        onComplete()
    }

    private fun fetchProfile(uid: String) {
        viewModelScope.launch {
            profile.value = authManager.getUserProfile(uid)
        }
    }

    fun updateProfile(newProfile: UserProfile) {
        viewModelScope.launch {
            authManager.updateProfile(newProfile)
            profile.value = newProfile
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            authManager.deleteAccount().onSuccess { onComplete() }
        }
    }
}

class BooksViewModel(private val firestoreManager: FirestoreManager = FirestoreManager()) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _books.asStateFlow()
    val stores = mutableStateListOf<Store>()
    val favorites = mutableStateListOf<String>()
    val reservations = mutableStateListOf<Reservation>()
    val reviews = mutableStateListOf<Review>()
    
    val isLoading = mutableStateOf(false)
    private val _filterCategory = MutableStateFlow("All")
    val filterCategory: StateFlow<String> = _filterCategory
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredBooks: StateFlow<List<Book>> = combine(_books, _searchQuery, _filterCategory) { books, query, category ->
        books.filter { book ->
            val matchesQuery = query.isBlank() || 
                               book.title.contains(query, ignoreCase = true) || 
                               book.author.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || book.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedBooks: StateFlow<List<Book>> = _books.map { books ->
        books.sortedByDescending { it.rating }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBooks()
        loadStores()
    }

    fun loadBooks(forceReset: Boolean = false) {
        viewModelScope.launch {
            isLoading.value = true
            if (forceReset) {
                firestoreManager.resetAndSeed()
            }
            val results = firestoreManager.getBooks()
            _books.value = results
            if (results.isEmpty() && !forceReset) {
                firestoreManager.resetAndSeed()
                _books.value = firestoreManager.getBooks()
            }
            isLoading.value = false
        }
    }

    fun loadOwnerBooks(storeId: String) {
        viewModelScope.launch {
            isLoading.value = true
            val results = firestoreManager.getBooks(storeId = storeId)
            // If storeId is provided, we still update _books which filteredBooks/recommendedBooks observe
            _books.value = results
            isLoading.value = false
        }
    }

    fun loadStores() {
        viewModelScope.launch {
            stores.clear()
            stores.addAll(firestoreManager.getAllStores())
        }
    }

    fun setCategory(category: String) {
        _filterCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            favorites.clear()
            favorites.addAll(firestoreManager.getFavorites(userId))
        }
    }

    fun toggleFavorite(userId: String, bookId: String) {
        viewModelScope.launch {
            if (firestoreManager.toggleFavorite(userId, bookId)) favorites.add(bookId)
            else favorites.remove(bookId)
        }
    }

    fun reserveBook(reservation: Reservation, onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            firestoreManager.reserveBook(reservation).onSuccess { 
                onComplete()
                loadStudentReservations(reservation.userId)
            }.onFailure { onError(it.message ?: "Failed") }
        }
    }

    fun loadStudentReservations(userId: String) {
        viewModelScope.launch {
            reservations.clear()
            reservations.addAll(firestoreManager.getReservations(userId = userId))
        }
    }

    fun loadStoreReservations(storeId: String) {
        viewModelScope.launch {
            reservations.clear()
            reservations.addAll(firestoreManager.getReservations(storeId = storeId))
        }
    }

    fun deleteReservation(id: String, userId: String) {
        viewModelScope.launch {
            firestoreManager.deleteReservation(id)
            loadStudentReservations(userId)
        }
    }

    fun loadReviews(bookId: String) {
        viewModelScope.launch {
            reviews.clear()
            reviews.addAll(firestoreManager.getReviews(bookId))
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            firestoreManager.addReview(review)
            loadReviews(review.bookId)
        }
    }

    fun updateReservationStatus(id: String, status: String, storeId: String) {
        viewModelScope.launch {
            firestoreManager.updateReservationStatus(id, status)
            loadStoreReservations(storeId)
        }
    }

    fun updateReservationDates(id: String, startDate: Long, endDate: Long, storeId: String) {
        viewModelScope.launch {
            firestoreManager.updateReservationDates(id, startDate, endDate)
            loadStoreReservations(storeId)
        }
    }

    fun addBook(book: Book) = viewModelScope.launch { 
        firestoreManager.addBook(book)
        book.storeId.takeIf { it.isNotEmpty() }?.let { loadOwnerBooks(it) } ?: loadBooks()
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        firestoreManager.updateBook(book)
        book.storeId.takeIf { it.isNotEmpty() }?.let { loadOwnerBooks(it) } ?: loadBooks()
    }
    
    fun deleteBook(id: String, storeId: String) = viewModelScope.launch { 
        firestoreManager.deleteBook(id)
        loadOwnerBooks(storeId) 
    }
}
