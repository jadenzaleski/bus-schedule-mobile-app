package edu.miamioh.csi.capstone.busapp.views

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import edu.miamioh.csi.capstone.busapp.UnzipUtils
import edu.miamioh.csi.capstone.busapp.backend.CSVHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
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

    // Setup/initializations for app colors based on system theme
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // Setup for "Update Data" feature and its associated dialogues
    var showUpdateDataAlert by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf("No manual updates triggered since last" +
            " full app refresh") }
    UpdateProgressDialog(isVisible = isUpdating)

    // Alert Dialog for "Update Data" functionality.
    if (showUpdateDataAlert) {
        AlertDialog(
            onDismissRequest = {
                showUpdateDataAlert = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateDataAlert = false
                        isUpdating = true // Update process now begins here

                        coroutineScope.launch {
                            updateData(context) {
                                isUpdating = false
                                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss",
                                    Locale.getDefault())
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

    // Setup for "About App" feature and its associated dialogues
    var showAboutDialog by remember { mutableStateOf(false) }  // State for About dialog

    // Dialog for "About App" functionality.
    if (showAboutDialog) {
        AboutDialog { showAboutDialog = false }
    }

    // Setup for "Email" feature and its associated dialogues
    var showEmailDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Dialog for "Report An Issue" functionality.
    if (showEmailDialog) {
        EmailDialog(email, message, onEmailChange = { email = it }, onMessageChange = { message = it },
            onSend = {
                sendEmail(context, email, message)
                showEmailDialog = false
            },
            onClose = { showEmailDialog = false })
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
        /*
         * Most of the code inside here deals with the utilization of the custom-made preference
         * library which is included in the imports list. Refer to the GitHub for this external
         * library for further documentation purposes.
         * Link: https://github.com/zhanghai/ComposePreference
         */
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(4.dp)) {
            // Triggers dialogues related to the "Update Data" functionality.
            preference(
                key = "updateData",
                title = { Text(text = "Update Data") },
                summary = { Text(text = "Last Update: $lastUpdateTime" ) },
                onClick = {
                    showUpdateDataAlert = true
                }
            )
            // Triggers dialogues related to the "About This App" functionality.
            preference(
                key = "about",
                title = { Text(text = "About/App Info") },
                summary = { Text(text = "Learn more about this application") },
                onClick = {
                    showAboutDialog = true
                }
            )
            // Triggers dialogues related to the "Report an Issue" functionality.
            preference(
                key = "reportIssue",
                title = { Text("Report An Issue") },
                summary = { Text(text = "Submit a service ticket here") },
                onClick = {
                    showEmailDialog = true
                }
            )
            // A simple footer for the Settings page - doesn't have any functionality.
            preference(
                key = "footer",
                title = { Text(text = "") },
                summary = { Text(text = "Bus App Capstone Project - All rights reserved.") }
            ) {}
        }
    }
}

/*
 * Allows for the system theme to interact with and control the theme and appearance of the
 * Settings page.
 */
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

/**
 * A dialog that gets displayed based on certain conditions. It notifies the user that the app is
 * presently grabbing the most recent CORe bus data, and it disables the app, preventing the user
 * from accessing it until the data update process is complete.
 * @param isVisible - A Boolean value designating whether the dialog should be visible or not
 */
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

/**
 * Deletes a file directory, and all contents inside of it, if that directory previously existed.
 * @param directory - the files to be searched for and potentially deleted
 * @return a Boolean value indicating if the directory was deleted or not
 */
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

/**
 * Initiates the process where data gets pulled from the CORe website and populates all the
 * structures which were designated to hold the data. Most of this code was taken from the
 * CSVHandler.kt file, with some adjustments made in the context of this page's code.
 * @param context - The current active context
 * @param onComplete - A lambda function indicting what action should occur when the process is
 *                     complete
 */
suspend fun updateData(context: Context, onComplete: () -> Unit) {
    try {
        withContext(Dispatchers.IO) {
            val url = "https://mobilita.regione.calabria.it/gtfs/otp_gtfs.zip"
            val destinationFile = File(context.getExternalFilesDir(null), "otp_gtfs.zip")

            val coreDir = File(context.getExternalFilesDir(null), "core")
            if (deleteDirectoryWithContents(coreDir)) {
                Log.i("DELETE", "Previous data deleted successfully.")
            }

            if (!coreDir.exists()) {
                coreDir.mkdirs()
            }

            CSVHandler.downloadFile(url, destinationFile)
            Log.i("DOWNLOAD", "Download job is complete (Stage 1).")

            UnzipUtils.unzip(
                destinationFile,
                context.getExternalFilesDir(null)?.absolutePath + "/core/"
            )
            Log.i("UNZIP", "Unzip job is complete (Stage 2).")

            CSVHandler.renameToCSV(context.getExternalFilesDir(null)?.absolutePath + "/core")
            Log.i("RENAME", "Rename Job is complete (Stage 3).")

            /*
             * Initialization Process - Tells the CSVHandler where to store the data in the device
             * files
             */
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
        onComplete()
    }
}

/**
 * A composable function that details the dialog that should be displayed when the user clicks
 * on the "About Data" preference.
 * @param onDismiss - A lambda function indicting what action should occur when the dialog is
 *                    dismissed
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About This App") },
        text = {
            Text("Bus Scheduling Mobile App Capstone Project\n\nMembers:\nAyo Obisesan," +
                    " Daniel Tai, Jaden Zaleski, Neal Wolfrant\n\nThis custom mobile application" +
                    " was designed to enable reliable usage of the city of Cosenza's bus transit" +
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

/**
 * A composable function that details the dialog that should be displayed when the user clicks
 * on the "Report an Issue" preference. Most of the "Log" code here is for debugging purposes, but
 * it does help even outside of that context.
 * @param email - The email address the message will be sent to
 * @param message - The message to be sent to the designated email
 * @param onEmailChange - A lambda function indicting what action should occur when the inputted
 *                        email address gets changed in the text field
 * @param onMessageChange - A lambda function indicting what action should occur when the inputted
 *                          message gets changed in the text field
 * @param onSend - A lambda function indicting what action should occur when the "Send" button is
 *                 clicked, initiating the email sending process
 * @param onClose - A lambda function indicting what action should occur when the "Close" button is
 *                  clicked, dismissing the email dialog from visibility
 */
@Composable
fun EmailDialog(
    email: String,
    message: String,
    onEmailChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Report an Issue") },
        text = {
            Column {
                TextField(
                    value = email,
                    onValueChange = {
                        onEmailChange(it)
                        Log.d("EmailInput", "Email updated to: $it")
                    },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                TextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = { Text("Describe Your Issue") },
                    modifier = Modifier.height(150.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onSend) { Text("Send") }
        },
        dismissButton = {
            Button(onClick = onClose) { Text("Close") }
        }
    )
}

/**
 * A function that initiates a "send email" request. When the "Send" button is clicked, it will
 * prompt the user to pick an email client/platform from which an email can be typed and sent. It
 * will save the previously inputted email from the text fields, but essentially provides a second
 * text editor where the user can make final changes, review their service request, and finally
 * submit the request.
 * @param context - The current context
 * @param recipient - The intended recipient of the email (the email address)
 * @param content - The message to be delivered to the recipient
 */
fun sendEmail(context: Context, recipient: String, content: String) {
    Log.d("EmailIntent", "Preparing to send email...")
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, "Issue Report")
        putExtra(Intent.EXTRA_TEXT, content)
    }
    Log.d("EmailIntent", "Intent extras set - recipient: $recipient, content: $content")

    if (intent.resolveActivity(context.packageManager) != null) {
        Log.d("EmailIntent", "Email client found, launching intent.")
        context.startActivity(intent)
    } else {
        Log.e("EmailIntent", "No email client available.")
    }
}

@Composable
@Preview(showBackground = true)
fun SampleAppPreview() {
    SettingsView()
}