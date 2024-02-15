package edu.miamioh.csi.capstone.busapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import edu.miamioh.csi.capstone.busapp.navigation.AppNavigation
import java.io.File
import java.io.FileInputStream

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // toggle below to draw behind status and nav bar.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val url = "https://mobilita.regione.calabria.it/gtfs/otp_gtfs.zip"
        val destinationFile = File(applicationContext.getExternalFilesDir(null), "otp_gtfs.zip")
        // The below three must MUST be completed in proper order for the app to
        // use the updated data properly.
        Log.i("CSV", "Starting CSV initialization...")
        CSVHandler.downloadFile(url, destinationFile)
        Log.i("DOWNLOAD", "Download job is complete (Stage 1).")
        // unzip all the files that were just downloaded
        UnzipUtils.unzip(
            destinationFile,
            "/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/"
        )
        Log.i("UNZIP", "Unzip job is complete (Stage 2).")
        // rename all the files
        CSVHandler.renameToCSV("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core")
        Log.i("RENAME", "Rename Job is complete (Stage 3).")
        // create the objects
        CSVHandler.initialize(
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/agency.csv"),
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/calendar.csv"),
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/calendar_dates.csv"),
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/feed_info.csv"),
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/routes.csv"),
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/stop_times.csv"),
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/stops.csv"),
            FileInputStream("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/trips.csv"),
        )
        Log.i("CSV", "CSV initialization complete!")
        // End of CSV initialization.

        setContent {
            // Function call to display NavBar at bottom of screen
            AppNavigation()
        }

    }
}

