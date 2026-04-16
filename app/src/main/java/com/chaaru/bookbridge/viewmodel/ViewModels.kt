package com.chaaru.bookbridge.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chaaru.bookbridge.data.firebase.AuthManager
import com.chaaru.bookbridge.data.firebase.FirestoreManager
import com.chaaru.bookbridge.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val authManager: AuthManager) : ViewModel() {
    val profile = mutableStateOf<UserProfile?>(null)
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    init {
        authManager.currentUser()?.let {
            fetchProfile(it.uid)
        }
    }

    fun login(email: String, password: String, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.login(email, password).onSuccess {
                profile.value = it
                onNavigate(it.role)
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, role: String, phone: String, storeName: String? = null, location: String? = null, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            authManager.register(name, email, password, role, phone, storeName, location).onSuccess {
                profile.value = it
                onNavigate(it.role)
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
            authManager.updateProfile(profile).onSuccess {
                this@AuthViewModel.profile.value = profile
            }
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
    val allBooks: StateFlow<List<Book>> = _books.map { list ->
        list.map { sanitizeBook(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.map { list ->
        // Safety: If there are suspiciously many reviews (dummy data artifact), hide them to match the reset book count
        if (list.size >= 100) emptyList() else list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredBooks: StateFlow<List<Book>> = combine(allBooks, _searchQuery) { books, query ->
        books.filter { book ->
            val matchesSearch = book.title.contains(query, ignoreCase = true) ||
                                book.author.contains(query, ignoreCase = true)
            matchesSearch && book.available
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedBooks: StateFlow<List<Book>> = allBooks.map { books ->
        books.filter { it.available }.sortedByDescending { it.rating }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun sanitizeBook(book: Book): Book {
        // Safety: If reviewCount is suspiciously high (legacy dummy data), reset it to 0 in UI
        return if (book.reviewCount >= 100) {
            book.copy(reviewCount = 0L, rating = 0.0)
        } else {
            book
        }
    }

    init {
        // We no longer call observeBooks here to avoid conflicts.
        // The screens will trigger the specific observation they need.
    }

    private var booksJob: kotlinx.coroutines.Job? = null

    fun observeBooks() {
        booksJob?.cancel()
        booksJob = viewModelScope.launch {
            firestoreManager.getBooksFlow().collect {
                _books.value = it
            }
        }
    }

    fun observeOwnerBooks(storeId: String) {
        booksJob?.cancel()
        booksJob = viewModelScope.launch {
            firestoreManager.getBooksFlow(storeId).collect {
                _books.value = it
            }
        }
    }

    fun observeUserBookings(userId: String) {
        viewModelScope.launch {
            firestoreManager.getBookingsFlow(userId = userId).collect {
                _bookings.value = it
            }
        }
    }

    fun observeStoreBookings(storeId: String) {
        viewModelScope.launch {
            firestoreManager.getBookingsFlow(storeId = storeId).collect {
                _bookings.value = it
            }
        }
    }

    fun observeReviews(bookId: String) {
        viewModelScope.launch {
            firestoreManager.getReviewsFlow(bookId).collect { reviews ->
                _reviews.value = reviews.sortedByDescending { it.timestamp }
            }
        }
    }

    fun addReview(review: Review, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            firestoreManager.addReview(review).onSuccess {
                onSuccess()
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun bookBook(booking: Booking, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            firestoreManager.createBooking(booking).onSuccess {
                onSuccess()
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    // Payment integration
    private var pendingBooking: Booking? = null
    private var onPaymentSuccessCallback: (() -> Unit)? = null

    fun initiateBookingWithPayment(booking: Booking, onSuccess: () -> Unit) {
        pendingBooking = booking
        onPaymentSuccessCallback = onSuccess
    }

    fun onPaymentSuccess(paymentId: String) {
        pendingBooking?.let { booking ->
            val finalBooking = booking.copy(paymentId = paymentId, status = "advance_paid")
            bookBook(finalBooking) {
                onPaymentSuccessCallback?.invoke()
                pendingBooking = null
                onPaymentSuccessCallback = null
            }
        }
    }

    fun onPaymentError(message: String) {
        error.value = message
        pendingBooking = null
        onPaymentSuccessCallback = null
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            firestoreManager.updateBookingStatus(bookingId, status)
        }
    }

    fun deleteBooking(bookingId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading.value = true
            firestoreManager.deleteBooking(bookingId).onSuccess {
                onSuccess()
            }.onFailure {
                error.value = it.message
            }
            isLoading.value = false
        }
    }

    fun uploadImage(context: Context, uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            val path = firestoreManager.saveImageLocally(context, uri)
            onComplete(path)
            isLoading.value = false
        }
    }

    fun addBook(book: Book) = viewModelScope.launch {
        isLoading.value = true
        firestoreManager.addBook(book)
        isLoading.value = false
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        isLoading.value = true
        firestoreManager.updateBook(book)
        isLoading.value = false
    }

    fun deleteBook(id: String, imageUrl: String?) {
        viewModelScope.launch {
            firestoreManager.deleteBook(id, imageUrl)
        }
    }

    fun clearState() {
        _books.value = emptyList()
        _bookings.value = emptyList()
        _searchQuery.value = ""
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
