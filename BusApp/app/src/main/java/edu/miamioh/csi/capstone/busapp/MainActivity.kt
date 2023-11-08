package edu.miamioh.csi.capstone.busapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import edu.miamioh.csi.capstone.busapp.ui.theme.BusAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // toggle below to draw behind status and nav bar.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        CSVHandler.initialize(
            assets.open("agency.csv"),
            assets.open("calendar.csv"),
            assets.open("calendar_dates.csv"),
            assets.open("feed_info.csv"),
            assets.open("routes.csv"),
            assets.open("stop_times.csv"),
            assets.open("stops.csv"),
            assets.open("trips.csv")
        )

        setContent {
            BusAppTheme() {
                // Main view of the app
                MainView()

            }
        }
    }
}