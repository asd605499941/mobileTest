package com.example.myapplication.booking.service

import com.example.myapplication.booking.datasource.local.BookingLocalDataSource
import com.example.myapplication.booking.datasource.remote.BookingRemoteDataSource
import com.example.myapplication.booking.model.Booking
import com.example.myapplication.booking.model.isExpired
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface BookingService {
    fun observeBooking(): StateFlow<Booking?>

    fun observeRefreshing(): StateFlow<Boolean>

    suspend fun loadIfNeeded(): Boolean

    suspend fun refresh(): Boolean
}

class BookingServiceImpl(
    private val localDataSource: BookingLocalDataSource,
    private val remoteDataSource: BookingRemoteDataSource,
) : BookingService {
    private val isRefreshingState = MutableStateFlow(false)
    private val refreshMutex = Mutex()
    private val bookingState: StateFlow<Booking?> = localDataSource.observeBooking()

    override fun observeBooking(): StateFlow<Booking?> = bookingState

    override fun observeRefreshing(): StateFlow<Boolean> = isRefreshingState

    override suspend fun loadIfNeeded(): Boolean {
        return refreshMutex.withLock {
            val nowMillis = System.currentTimeMillis()
            val localBooking = localDataSource.observeBooking().value
            if (localBooking != null && !localBooking.isExpired(nowMillis)) {
                return@withLock true
            }

            fetchRemoteAndCache(updateRefreshingState = false)
        }
    }

    override suspend fun refresh(): Boolean {
        return refreshMutex.withLock {
            fetchRemoteAndCache(updateRefreshingState = true)
        }
    }

    private suspend fun fetchRemoteAndCache(updateRefreshingState: Boolean): Boolean {
        if (updateRefreshingState) {
            isRefreshingState.value = true
        }
        return try {
            val remoteBooking = remoteDataSource.fetchBooking()
            localDataSource.replaceBooking(remoteBooking)
            if (updateRefreshingState) {
                isRefreshingState.value = false
            }
            true
        } catch (_: Exception) {
            if (updateRefreshingState) {
                isRefreshingState.value = false
            }
            false
        }
    }
}
