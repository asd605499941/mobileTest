package com.example.myapplication.booking.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.booking.datasource.local.SharedPreferencesBookingLocalDataSource
import com.example.myapplication.booking.datasource.remote.MockBookingRemoteDataSource
import com.example.myapplication.booking.model.Booking
import com.example.myapplication.booking.service.BookingService
import com.example.myapplication.booking.service.BookingServiceImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel(
    serviceFactory: () -> BookingService,
) : ViewModel() {
    private val bookingService = serviceFactory()
    private val _toastEvents = MutableSharedFlow<String>()

    val booking: StateFlow<Booking?> = bookingService.observeBooking()

    val isRefreshing: StateFlow<Boolean> = bookingService.observeRefreshing()

    val toastEvents: SharedFlow<String> = _toastEvents

    fun loadIfNeeded() {
        requestBooking { bookingService.loadIfNeeded() }
    }

    fun refresh() {
        requestBooking { bookingService.refresh() }
    }

    private fun requestBooking(request: suspend () -> Boolean) {
        viewModelScope.launch {
            if (!request()) {
                _toastEvents.emit("Refresh failed")
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
                        val preferences = application.getSharedPreferences(
                            BOOKING_PREFERENCES,
                            Context.MODE_PRIVATE,
                        )
                        return BookingViewModel {
                            BookingServiceImpl(
                                localDataSource = SharedPreferencesBookingLocalDataSource(preferences),
                                remoteDataSource = MockBookingRemoteDataSource(),
                            )
                        } as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        private const val BOOKING_PREFERENCES = "booking_preferences"
    }
}
