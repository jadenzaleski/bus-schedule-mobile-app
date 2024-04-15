package edu.miamioh.csi.capstone.busapp.views

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import edu.miamioh.csi.capstone.busapp.UnzipUtils
import edu.miamioh.csi.capstone.busapp.backend.CSVHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.footerPreference
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.sliderPreference
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun SettingsView() {
    SetTheme { ProvidePreferenceLocals { SettingScreen() } }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // State to control the visibility of the update data dialog
    var showUpdateDataAlert by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf("No manual updates triggered since last full app refresh") }

    // Show progress dialog based on isUpdating state
    UpdateProgressDialog(isVisible = isUpdating)

    // Dialog showing logic
    if (showUpdateDataAlert) {
        AlertDialog(
            onDismissRequest = {
                // Handle the dismiss action here
                showUpdateDataAlert = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateDataAlert = false // Close the dialog
                        isUpdating = true // Begin updating

                        coroutineScope.launch {
                            updateData(context) {
                                isUpdating = false // Update completion handler
                                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                                lastUpdateTime = "${dateFormat.format(Date())}"
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showUpdateDataAlert = false }) {
                    Text("Cancel")
                }
            },
            title = { Text(text = "Confirm Action") },
            text = { Text(text = "Do you want to update the data now? Depending on your current"
                    + " network speed, this action may take a couple of minutes.") }
        )
    }

    var showAboutDialog by remember { mutableStateOf(false) }  // State for About dialog

    // About dialog
    if (showAboutDialog) {
        AboutDialog { showAboutDialog = false }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.weight(1f),
                color = onBackgroundColor
            )
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = onBackgroundColor
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(4.dp)) {
            sliderPreference(
                key = "fontSize",
                defaultValue = 1f,
                title = { Text(text = "Font Size") },
                valueRange = 0f..2f,
                valueSteps = 1,
                //summary = { Text(text = "Slide to adjust") },
                valueText = {
                    if (it == 0f) {
                        Text(text = "Small")
                    } else if (it == 1f) {
                        Text(text = "Medium")
                    } else {
                        Text(text = "Large")
                    }
                }
            )
            preference(
                key = "updateData",
                title = { Text(text = "Update Data") },
                summary = { Text(text = "Last Update: $lastUpdateTime" ) },
                onClick = {
                    // When the preference is clicked, show the dialog
                    showUpdateDataAlert = true
                }
            )
            preference(
                key = "about",
                title = { Text(text = "About/App Info") },
                summary = { Text(text = "Learn more about this application") },
                onClick = { showAboutDialog = true }  // Toggle About dialog visibility
            )
            preference(
                key = "help",
                title = { Text(text = "Help") },
                summary = { Text(text = "Basic Troubleshooting / Frequently-Asked Questions") }
            ) {}
            footerPreference(
                key = "footer_preference",
                summary = { Text(text = "Bus App Capstone Project - All rights reserved.") }
            )
        }
    }
}

@Composable
fun SetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowBackgroundColor = colorScheme.background.toArgb()
            window.setBackgroundDrawable(ColorDrawable(windowBackgroundColor))
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = if (darkTheme) Color.Black.toArgb() else Color.White.toArgb()
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

@Composable
fun UpdateProgressDialog(isVisible: Boolean) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = { /* Disallow dismissing while updating */ },
            title = { Text("Updating Now") },
            text = { Text("Retrieving most recent bus data from CORe, please wait...") },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

fun deleteDirectoryWithContents(directory: File): Boolean {
    if (directory.exists()) {
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    deleteDirectoryWithContents(file)
                } else {
                    file.delete()
                }
            }
        }
    }
    return directory.delete()
}

suspend fun updateData(context: Context, onComplete: () -> Unit) {
    try {
        // Perform operations in IO dispatcher
        withContext(Dispatchers.IO) {
            val url = "https://mobilita.regione.calabria.it/gtfs/otp_gtfs.zip"
            val destinationFile = File(context.getExternalFilesDir(null), "otp_gtfs.zip")

            // Ensure the directory for extracted files is clear
            val coreDir = File(context.getExternalFilesDir(null), "core")
            if (deleteDirectoryWithContents(coreDir)) {
                Log.i("DELETE", "Previous data deleted successfully.")
            }

            // Recreate directories
            if (!coreDir.exists()) {
                coreDir.mkdirs()
            }

            // Replace this with the actual download logic
            CSVHandler.downloadFile(url, destinationFile)
            Log.i("DOWNLOAD", "Download job is complete (Stage 1).")

            // Add your other operations here
            // Ensure you replace the placeholder operations with actual implementation
            UnzipUtils.unzip(
                destinationFile,
                context.getExternalFilesDir(null)?.absolutePath + "/core/"
            )
            Log.i("UNZIP", "Unzip job is complete (Stage 2).")

            CSVHandler.renameToCSV(context.getExternalFilesDir(null)?.absolutePath + "/core")
            Log.i("RENAME", "Rename Job is complete (Stage 3).")

            // Initialize CSVHandler with the correct paths
            withContext(Dispatchers.IO) {
                CSVHandler.initialize(
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/agency.csv"),
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/calendar.csv"),
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/calendar_dates.csv"),
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/feed_info.csv"),
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/routes.csv"),
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/stop_times.csv"),
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/stops.csv"),
                    FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/trips.csv")
                )
            }

            Log.i("CSV", "CSV initialization complete!")
        }
    } catch (e: Exception) {
        Log.e("UpdateError", "Error during update: ${e.message}")
    } finally {
        onComplete() // Invoke the completion handler
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About This App") },
        text = {
            Text("Bus Scheduling Mobile App Capstone Project\n\nMembers:\nAyo Obisesan, Daniel" +
                    " Tai, Jaden Zaleski, Neal Wolfrant\n\nThis custom mobile application was" +
                    " designed to enable reliable usage of the city of Cosenza's bus transit" +
                    " system. It utilizes up-to-date data from CORe, a parent company which " +
                    " manages over 20 different Italian bus agencies.\n\nUsers can expect a" +
                    " variety of features, including route generation and basic navigation, a" +
                    " dynamic interface that displays the closest bus stops based on the map" +
                    " position, and the ability to change certain application settings.\n\n" +
                    "Technologies Used: Java, Kotlin, Jetpack Compose for Android Development," +
                    "Google for Developers: Google Maps Platform, and others.")
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun SampleAppPreview() {
    SettingsView()
}