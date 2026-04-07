package com.chaaru.bookbridge.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaaru.bookbridge.data.firebase.*
import com.chaaru.bookbridge.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class AuthViewModel(private val authManager: AuthManager) : ViewModel() {
    val profile = mutableStateOf<UserProfile?>(null)
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    // ... rest of AuthViewModel

    init {
        authManager.currentUser()?.let {
            fetchProfile(it.uid)
        }
    }

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.login(email, password).onSuccess {
                profile.value = it
                onSuccess(it.role)
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.firebaseAuthWithGoogle(idToken).onSuccess {
                profile.value = it
                onSuccess(it.role)
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, role: String, phone: String, storeName: String? = null, location: String? = null, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.register(name, email, password, role, phone, storeName, location).onSuccess {
                profile.value = it
                onSuccess(it.role)
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    fun logout(onSuccess: () -> Unit) {
        authManager.logout()
        profile.value = null
        onSuccess()
    }

    fun fetchProfile(uid: String) {
        viewModelScope.launch {
            profile.value = authManager.getUserProfile(uid)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            authManager.updateProfile(profile)
            this@AuthViewModel.profile.value = profile
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.deleteAccount().onSuccess {
                profile.value = null
                onSuccess()
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    fun resetPassword(email: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.resetPassword(email).onSuccess {
                onComplete()
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }
}

class BooksViewModel(private val firestoreManager: FirestoreManager) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _books.asStateFlow()
    val stores = mutableStateListOf<Store>()
    val favorites = mutableStateListOf<String>()
    val reservations = mutableStateListOf<Reservation>()
    val reviews = mutableStateListOf<Review>()

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    private val _filterCategory = MutableStateFlow("All")
    val filterCategory: StateFlow<String> = _filterCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredBooks: StateFlow<List<Book>> = combine(_books, _filterCategory, _searchQuery) { books, category, query ->
        books.filter { book ->
            val matchesCategory = category == "All" || book.category == category
            val matchesSearch = book.title?.contains(query, ignoreCase = true) == true ||
                                book.author?.contains(query, ignoreCase = true) == true
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedBooks: StateFlow<List<Book>> = _books.map { books ->
        books.sortedByDescending { it.rating }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBooks()
        loadStores()
    }

    fun loadBooks(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            isLoading.value = true
            _books.value = firestoreManager.getBooks()
            isLoading.value = false
        }
    }

    fun loadOwnerBooks(storeId: String) {
        viewModelScope.launch {
            isLoading.value = true
            _books.value = firestoreManager.getBooks(storeId = storeId)
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

    fun reserveBook(res: Reservation, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            firestoreManager.reserveBook(res).onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Error") }
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
        isLoading.value = true
        try {
            firestoreManager.addBook(book)
            loadOwnerBooks(book.storeId ?: "")
        } catch (e: Exception) {
            this@BooksViewModel.error.value = e.message
        } finally {
            isLoading.value = false
        }
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        isLoading.value = true
        try {
            firestoreManager.updateBook(book)
            loadOwnerBooks(book.storeId ?: "")
        } catch (e: Exception) {
            this@BooksViewModel.error.value = e.message
        } finally {
            isLoading.value = false
        }
    }

    fun deleteBook(id: String, storeId: String) {
        viewModelScope.launch {
            firestoreManager.deleteBook(id)
            loadOwnerBooks(storeId)
        }
    }

    fun clearState() {
        _books.value = emptyList()
        stores.clear()
        favorites.clear()
        reservations.clear()
        reviews.clear()
        _searchQuery.value = ""
        _filterCategory.value = "All"
    }
}

class ViewModelFactory : ViewModelProvider.Factory {
    private val authManager = AuthManager()
    private val firestoreManager = FirestoreManager()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authManager) as T
            modelClass.isAssignableFrom(BooksViewModel::class.java) -> BooksViewModel(firestoreManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
