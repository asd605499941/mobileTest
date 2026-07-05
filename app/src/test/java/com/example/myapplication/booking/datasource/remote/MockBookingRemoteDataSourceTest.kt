package com.example.myapplication.booking.datasource.remote

import kotlinx.coroutines.test.runTest
import com.example.myapplication.booking.model.toEpochMillis
import org.junit.Assert.assertTrue
import org.junit.Test

class MockBookingRemoteDataSourceTest {
    @Test
    fun fetchBooking_updatesExpiryAndVisibleDataOnEachRequest() = runTest {
        val remoteDataSource = MockBookingRemoteDataSource()

        val beforeFirstFetch = System.currentTimeMillis()
        val first = fetchSuccessfulBooking(remoteDataSource)
        val afterFirstFetch = System.currentTimeMillis()
        val second = fetchSuccessfulBooking(remoteDataSource)
        val afterSecondFetch = System.currentTimeMillis()

        assertTrue(first.expiryTime.toEpochMillis() in beforeFirstFetch + 29_000L..afterFirstFetch + 30_000L)
        assertTrue(second.expiryTime.toEpochMillis() in afterFirstFetch + 29_000L..afterSecondFetch + 30_000L)
        assertTrue(first.segments.first().originAndDestinationPair.originCity.startsWith("Origin-"))
        assertTrue(first.segments.first().originAndDestinationPair.destinationCity.startsWith("Destination-"))
        assertTrue(second.segments.first().originAndDestinationPair.originCity.startsWith("Origin-"))
        assertTrue(second.segments.first().originAndDestinationPair.destinationCity.startsWith("Destination-"))
    }

    @Test
    fun fetchBooking_failsWhenNetworkDelayIsLongerThanThreeSeconds() = runTest {
        val remoteDataSource = MockBookingRemoteDataSource()

        repeat(MAX_ATTEMPTS) {
            try {
                remoteDataSource.fetchBooking()
            } catch (_: IllegalStateException) {
                return@runTest
            }
        }

        error("Expected at least one simulated network failure")
    }

    private suspend fun fetchSuccessfulBooking(
        remoteDataSource: MockBookingRemoteDataSource,
    ) = repeatUntilSuccess {
        remoteDataSource.fetchBooking()
    }

    private suspend fun repeatUntilSuccess(
        block: suspend () -> com.example.myapplication.booking.model.Booking,
    ): com.example.myapplication.booking.model.Booking {
        repeat(MAX_ATTEMPTS) {
            try {
                return block()
            } catch (_: IllegalStateException) {
                // Try again because the mock remote intentionally fails at random.
            }
        }
        error("Expected at least one successful simulated network request")
    }

    private companion object {
        const val MAX_ATTEMPTS = 20
    }
}
