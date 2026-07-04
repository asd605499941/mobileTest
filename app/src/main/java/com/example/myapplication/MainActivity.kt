package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

}

fun getBookingJson(): String {
    return "{\"shipReference\":\"ABCDEF\",\"shipToken\":\"AAAABBBCCCCDDD\",\"canIssueTicketChecking\":false,\"expiryTime\":\"1722409261\",\"duration\":2430,\"segments\":[{\"id\":1,\"originAndDestinationPair\":{\"destination\":{\"code\":\"BBB\",\"displayName\":\"BBB DisplayName\",\"url\":\"www.ship.com\"},\"destinationCity\":\"BBB City\",\"origin\":{\"code\":\"AAA\",\"displayName\":\"AAA DisplayName\",\"url\":\"www.ship.com\"},\"originCity\":\"AAA City\"}},{\"id\":2,\"originAndDestinationPair\":{\"destination\":{\"code\":\"CCC\",\"displayName\":\"CCC DisplayName\",\"url\":\"www.ship.com\"},\"destinationCity\":\"CCC\",\"origin\":{\"code\":\"BBB\",\"displayName\":\"BBB DisplayName\",\"url\":\"www.ship.com\"},\"originCity\":\"BBB City\"}}]}"
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android123")
    }
}