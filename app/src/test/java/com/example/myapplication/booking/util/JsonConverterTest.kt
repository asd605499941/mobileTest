package com.example.myapplication.booking.util

import com.example.myapplication.booking.datasource.remote.getBookingJson
import com.example.myapplication.booking.model.Booking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonConverterTest {
    @Test
    fun fromJson_parsesBookingJson() {
        val booking = JsonConverter.fromJson(getBookingJson(), Booking::class.java)

        requireNotNull(booking)
        assertEquals("ABCDEF", booking.shipReference)
        assertEquals("AAAABBBCCCCDDD", booking.shipToken)
        assertFalse(booking.canIssueTicketChecking)
        assertEquals(1_722_409_261L, booking.expiryTime)
        assertEquals(2_430L, booking.duration)
        assertEquals(2, booking.segments.size)
        assertEquals("AAA", booking.segments.first().originAndDestinationPair.origin.code)
        assertEquals("BBB", booking.segments.first().originAndDestinationPair.destination.code)
    }

    @Test
    fun fromJson_returnsNullWhenJsonIsInvalid() {
        val booking = JsonConverter.fromJson("{", Booking::class.java)

        assertNull(booking)
    }

    @Test
    fun toJson_serializesObject() {
        val json = JsonConverter.toJson(mapOf("ok" to true))

        requireNotNull(json)
        assertTrue(json.contains("\"ok\":true"))
    }
}
