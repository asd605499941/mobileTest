package com.example.myapplication.booking.service

import com.example.myapplication.booking.datasource.local.BookingLocalDataSource
import com.example.myapplication.booking.datasource.remote.BookingRemoteDataSource
import com.example.myapplication.booking.model.Booking
import com.example.myapplication.booking.model.BookingSegment
import com.example.myapplication.booking.model.LocationInfo
import com.example.myapplication.booking.model.OriginDestinationPair
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingServiceImplTest {
    @Test
    fun loadIfNeeded_usesLocalCacheWhenCacheIsFresh() = runTest {
        val cachedBooking = sampleBooking(
            reference = "CACHED",
            expiryTimeMillis = System.currentTimeMillis() + 30_000L,
        )
        val remoteBooking = sampleBooking("REMOTE", 3_000L)
        val localDataSource = FakeBookingLocalDataSource(cachedBooking)
        val remoteDataSource = FakeBookingRemoteDataSource(Result.success(remoteBooking))
        val service = BookingServiceImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
        )
        val collectJob = backgroundScope.launch { service.observeBooking().collect() }

        val loaded = service.loadIfNeeded()
        advanceUntilIdle()

        assertEquals(true, loaded)
        assertEquals(0, remoteDataSource.fetchCalls)
        assertSame(cachedBooking, localDataSource.currentBooking)

        collectJob.cancel()
    }

    @Test
    fun loadIfNeeded_fetchesRemoteWhenCacheIsMissing() = runTest {
        val remoteBooking = sampleBooking(reference = "REMOTE", expiryTimeMillis = 4_000L)
        val localDataSource = FakeBookingLocalDataSource(initialBooking = null)
        val remoteDataSource = FakeBookingRemoteDataSource(Result.success(remoteBooking))
        val service = BookingServiceImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
        )
        val collectJob = backgroundScope.launch { service.observeBooking().collect() }

        service.loadIfNeeded()
        advanceUntilIdle()

        assertEquals(1, remoteDataSource.fetchCalls)
        assertSame(remoteBooking, localDataSource.currentBooking)
        assertEquals(false, service.observeRefreshing().value)

        collectJob.cancel()
    }

    @Test
    fun loadIfNeeded_fetchesRemoteWhenCacheIsExpiredAndReplacesCache() = runTest {
        val expiredBooking = sampleBooking(reference = "OLD", expiryTimeMillis = 1_000L)
        val remoteBooking = sampleBooking(reference = "NEW", expiryTimeMillis = 4_000L)
        val localDataSource = FakeBookingLocalDataSource(expiredBooking)
        val remoteDataSource = FakeBookingRemoteDataSource(Result.success(remoteBooking))
        val service = BookingServiceImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
        )
        val collectJob = backgroundScope.launch { service.observeBooking().collect() }

        service.loadIfNeeded()
        advanceUntilIdle()

        assertEquals(1, remoteDataSource.fetchCalls)
        assertSame(remoteBooking, localDataSource.currentBooking)

        collectJob.cancel()
    }

    @Test
    fun refresh_fetchesRemoteEvenWhenCacheIsFresh() = runTest {
        val cachedBooking = sampleBooking(
            reference = "CACHED",
            expiryTimeMillis = System.currentTimeMillis() + 30_000L,
        )
        val remoteBooking = sampleBooking(reference = "REMOTE", expiryTimeMillis = 4_000L)
        val localDataSource = FakeBookingLocalDataSource(cachedBooking)
        val remoteDataSource = FakeBookingRemoteDataSource(Result.success(remoteBooking))
        val service = BookingServiceImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
        )
        val collectJob = backgroundScope.launch { service.observeBooking().collect() }

        val refreshed = service.refresh()
        advanceUntilIdle()

        assertEquals(true, refreshed)
        assertEquals(1, remoteDataSource.fetchCalls)
        assertSame(remoteBooking, localDataSource.currentBooking)
        assertEquals(false, service.observeRefreshing().value)

        collectJob.cancel()
    }

    @Test
    fun refresh_keepsOldCacheAndExposesErrorWhenRemoteFails() = runTest {
        val cachedBooking = sampleBooking(reference = "OLD", expiryTimeMillis = 1_000L)
        val localDataSource = FakeBookingLocalDataSource(cachedBooking)
        val remoteDataSource = FakeBookingRemoteDataSource(Result.failure(IllegalStateException("Network failed")))
        val service = BookingServiceImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
        )
        val collectJob = backgroundScope.launch { service.observeBooking().collect() }

        val refreshed = service.refresh()
        advanceUntilIdle()

        service.observeBooking().first { it === cachedBooking }
        service.observeRefreshing().first { !it }
        assertEquals(false, refreshed)
        assertEquals(1, remoteDataSource.fetchCalls)
        assertSame(cachedBooking, localDataSource.currentBooking)

        collectJob.cancel()
    }

    private class FakeBookingRemoteDataSource(
        private val result: Result<Booking>,
    ) : BookingRemoteDataSource {
        var fetchCalls = 0
            private set

        override suspend fun fetchBooking(): Booking {
            fetchCalls += 1
            return result.getOrThrow()
        }
    }

    private class FakeBookingLocalDataSource(
        initialBooking: Booking?,
    ) : BookingLocalDataSource {
        private val bookingState = MutableStateFlow(initialBooking)

        override fun observeBooking(): StateFlow<Booking?> = bookingState

        val currentBooking: Booking?
            get() = bookingState.value

        override fun replaceBooking(booking: Booking) {
            bookingState.value = booking
        }
    }

    private fun sampleBooking(
        reference: String,
        expiryTimeMillis: Long,
    ): Booking {
        return Booking(
            shipReference = reference,
            shipToken = "TOKEN-$reference",
            canIssueTicketChecking = true,
            expiryTime = expiryTimeMillis / 1_000L,
            duration = 2_430L,
            segments = listOf(
                BookingSegment(
                    id = 1,
                    originAndDestinationPair = OriginDestinationPair(
                        origin = LocationInfo(
                            code = "AAA",
                            displayName = "AAA DisplayName",
                            url = "www.ship.com",
                        ),
                        originCity = "AAA City",
                        destination = LocationInfo(
                            code = "BBB",
                            displayName = "BBB DisplayName",
                            url = "www.ship.com",
                        ),
                        destinationCity = "BBB City",
                    ),
                ),
            ),
        )
    }
}
