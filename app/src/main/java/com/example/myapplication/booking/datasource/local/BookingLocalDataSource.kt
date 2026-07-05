package com.example.myapplication.booking.datasource.local

import android.content.SharedPreferences
import com.example.myapplication.booking.model.Booking
import com.example.myapplication.booking.util.JsonConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface BookingLocalDataSource {
    fun observeBooking(): StateFlow<Booking?>

    fun replaceBooking(booking: Booking)
}

class SharedPreferencesBookingLocalDataSource(
    private val sharedPreferences: SharedPreferences,
) : BookingLocalDataSource {
    private val bookingState = MutableStateFlow(readBooking())

    override fun observeBooking(): StateFlow<Booking?> {
        return bookingState
    }

    override fun replaceBooking(booking: Booking) {
        val json = JsonConverter.toJson(booking)
            ?: throw IllegalStateException("Booking could not be serialized")
        sharedPreferences.edit().putString(KEY_BOOKING, json).apply()
        bookingState.value = booking
    }

    private fun readBooking(): Booking? {
        val json = sharedPreferences.getString(KEY_BOOKING, null) ?: return null
        return JsonConverter.fromJson(json, Booking::class.java) ?: run {
            sharedPreferences.edit().remove(KEY_BOOKING).apply()
            null
        }
    }

    private companion object {
        const val KEY_BOOKING = "booking"
    }
}
