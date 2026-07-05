package com.example.myapplication.booking.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.booking.model.Booking
import com.example.myapplication.booking.model.BookingSegment
import com.example.myapplication.booking.model.LocationInfo
import com.example.myapplication.booking.model.OriginDestinationPair
import com.example.myapplication.booking.viewmodel.BookingViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun BookingListRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = viewModel(
        factory = BookingViewModel.factory(LocalContext.current.applicationContext as Application)
    )
) {
    val booking by viewModel.booking.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.loadIfNeeded()
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(booking) {
        booking?.let { Log.d(TAG, "Booking data: $it") }
    }

    BookingListScreen(
        booking = booking,
        isRefreshing = isRefreshing,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )
}

@Composable
fun BookingListScreen(
    booking: Booking?,
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(
                isRefreshing = isRefreshing,
                onBack = onBack,
                onRefresh = onRefresh
            )
        }

        if (booking == null) {
            item {
                Text(
                    text = "No booking data",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(
                items = booking.segments,
                key = { it.id }
            ) { segment ->
                BookingSegmentRow(segment)
            }
        }
    }
}

private const val TAG = "BookingListScreen"

@Composable
private fun Header(
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onRefresh,
            enabled = !isRefreshing,
        ) {
            Text(text = if (isRefreshing) "Refreshing" else "Refresh")
        }
        Button(onClick = onBack) {
            Text(text = "Back")
        }
    }
}

@Composable
private fun BookingSegmentRow(segment: BookingSegment) {
    val pair = segment.originAndDestinationPair
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ID ${segment.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            LocationLine(
                label = "From",
                code = pair.origin.code,
                city = pair.originCity,
                name = pair.origin.displayName,
            )
            LocationLine(
                label = "To",
                code = pair.destination.code,
                city = pair.destinationCity,
                name = pair.destination.displayName,
            )
        }
    }
}

@Composable
private fun LocationLine(
    label: String,
    code: String,
    city: String,
    name: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.24f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(0.76f)) {
            Text(
                text = "$code - $city",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookingListScreenPreview() {
    MyApplicationTheme {
        BookingListScreen(
            booking = Booking(
                shipReference = "ABCDEF",
                shipToken = "TOKEN",
                canIssueTicketChecking = false,
                expiryTime = (System.currentTimeMillis() + 60000) / 1000L,
                duration = 30,
                segments = listOf(
                    BookingSegment(
                        id = 1,
                        originAndDestinationPair = OriginDestinationPair(
                            origin = LocationInfo("AAA", "AAA", "www.ship.com"),
                            originCity = "Origin-ABC123",
                            destination = LocationInfo("BBB", "BBB", "www.ship.com"),
                            destinationCity = "Destination-XYZ789",
                        ),
                    ),
                ),
            ),
            isRefreshing = false,
            onRefresh = {},
            onBack = {},
        )
    }
}
