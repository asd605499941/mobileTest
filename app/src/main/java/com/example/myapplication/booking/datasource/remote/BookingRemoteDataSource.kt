package com.example.myapplication.booking.datasource.remote

import com.example.myapplication.booking.model.Booking
import com.example.myapplication.booking.util.JsonConverter
import kotlinx.coroutines.delay

interface BookingRemoteDataSource {
    suspend fun fetchBooking(): Booking
}

class MockBookingRemoteDataSource : BookingRemoteDataSource {
    override suspend fun fetchBooking(): Booking {
        val networkDelayMillis = (MIN_NETWORK_DELAY_MILLIS..MAX_NETWORK_DELAY_MILLIS).random()
        delay(minOf(networkDelayMillis, REQUEST_FAILURE_DELAY_MILLIS))
        if (networkDelayMillis > REQUEST_FAILURE_DELAY_MILLIS) {
            throw IllegalStateException("Network request failed")
        }

        val json = buildResponseJson()
        return JsonConverter.fromJson(json, Booking::class.java)!!
    }

    private fun buildResponseJson(): String {
        val nowMillis = System.currentTimeMillis()
        val expirySeconds = (nowMillis + CACHE_TTL_MILLIS) / MILLIS_PER_SECOND
        val response = JsonConverter.fromJson(getBookingJson(), Booking::class.java)!!
        val updatedResponse = response.copy(
            expiryTime = expirySeconds,
            segments = response.segments.map { segment ->
                val pair = segment.originAndDestinationPair
                segment.copy(
                    originAndDestinationPair = pair.copy(
                        originCity = randomCity("Origin"),
                        destinationCity = randomCity("Destination"),
                    ),
                )
            },
        )
        return JsonConverter.toJson(updatedResponse)!!
    }

    private fun randomCity(prefix: String): String {
        val suffix = (1..RANDOM_SUFFIX_LENGTH)
            .map { RANDOM_CHARS.random() }
            .joinToString(separator = "")
        return "$prefix-$suffix"
    }

    private companion object {
        const val MIN_NETWORK_DELAY_MILLIS = 1000L
        const val MAX_NETWORK_DELAY_MILLIS = 5000L
        const val REQUEST_FAILURE_DELAY_MILLIS = 3000L
        const val CACHE_TTL_MILLIS = 10000L
        const val MILLIS_PER_SECOND = 1000L
        const val RANDOM_SUFFIX_LENGTH = 6
        const val RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }
}
