package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.booking.ui.BookingListRoute
import com.example.myapplication.booking.ui.HomeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Home) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.Home -> HomeScreen(
                            onOpenBooking = { currentScreen = AppScreen.Booking },
                            modifier = Modifier.padding(innerPadding),
                        )

                        AppScreen.Booking -> BookingListRoute(
                            onBack = { currentScreen = AppScreen.Home },
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }

}

private enum class AppScreen {
    Home,
    Booking,
}

@Preview
@Composable
private fun HomePreview() {
    MyApplicationTheme {
        HomeScreen(onOpenBooking = {})
    }
}
