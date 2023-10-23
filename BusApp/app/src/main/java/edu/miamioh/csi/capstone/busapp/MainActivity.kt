package edu.miamioh.csi.capstone.busapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import edu.miamioh.csi.capstone.busapp.ui.theme.BusAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BusAppTheme() {
                // Main view of the app
                MainView()

            }
        }
    }
}