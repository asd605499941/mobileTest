package com.example.myapplication.booking.model

data class Booking(
    val shipReference: String,
    val shipToken: String,
    val canIssueTicketChecking: Boolean,
    val expiryTime: Long,
    val duration: Long,
    val segments: List<BookingSegment>,
)

data class BookingSegment(
    val id: Int,
    val originAndDestinationPair: OriginDestinationPair,
)

data class OriginDestinationPair(
    val origin: LocationInfo,
    val originCity: String,
    val destination: LocationInfo,
    val destinationCity: String,
)

data class LocationInfo(
    val code: String,
    val displayName: String,
    val url: String,
)

fun Booking.isExpired(nowMillis: Long): Boolean = expiryTime.toEpochMillis() <= nowMillis

fun Long.toEpochMillis(): Long = this * 1000L
