package edu.miamioh.csi.capstone.busapp

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import edu.miamioh.csi.capstone.busapp.backend.CSVHandler
import edu.miamioh.csi.capstone.busapp.navigation.AppNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.Calendar

class MainActivity : ComponentActivity() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Your initialization code...
        val url = "https://mobilita.regione.calabria.it/gtfs/otp_gtfs.zip"
        val destinationFile = File(applicationContext.getExternalFilesDir(null), "otp_gtfs.zip")

        progressDialog = ProgressDialog(this).apply {
            setMessage("Updating/Initializing Data, Please Wait...")
            setCancelable(false)
        }

        lifecycleScope.launch {
            showProgressDialog(true)
            if (shouldUpdateFiles()) {
                try {
                    // Switch to Dispatchers.IO for network operation
                    withContext(Dispatchers.IO) {
                        CSVHandler.downloadFile(url, destinationFile)
                    }
                    Log.i("DOWNLOAD", "Download job is complete (Stage 1).")

                    // Following operations might also need to be offloaded to Dispatchers.IO
                    // if they involve I/O operations. Make sure to wrap them in withContext(Dispatchers.IO) as necessary.

                    withContext(Dispatchers.IO) {
                        UnzipUtils.unzip(
                            destinationFile,
                            "/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/"
                        )
                    }
                    Log.i("UNZIP", "Unzip job is complete (Stage 2).")

                    withContext(Dispatchers.IO) {
                        CSVHandler.renameToCSV("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core")
                    }
                    Log.i("RENAME", "Rename Job is complete (Stage 3).")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error in downloading or processing files: ${e.message}")
                }
            }

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
            showProgressDialog(false)
            Log.i("CSV", "CSV initialization complete!")
        }

        setContent {
            AppNavigation()
        }

        checkAndRequestLocationPermissions()
    }

    private fun shouldUpdateFiles(): Boolean {
        // Directory where the files after extraction are stored
        val coreDirectoryPath = "/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core"

        // List of expected file names
        val expectedFileNames = listOf(
            "agency.csv",
            "calendar.csv",
            "calendar_dates.csv",
            "feed_info.csv",
            "routes.csv",
            "stop_times.csv",
            "stops.csv",
            "trips.csv"
        )

        // Check if all expected files exist
        val allFilesExist = expectedFileNames.all { fileName ->
            File(coreDirectoryPath, fileName).exists()
        }

        if (!allFilesExist) {
            Log.i("MainActivityMessage", "Update Required: Not all files found.")
            return true // Update if any of the files doesn't exist
        }

        // Your existing last modified check can remain here for further refinement
        // Ensure you also update the `destinationFile` to match the actual zip file's directory if needed
        val destinationFile = File(applicationContext.getExternalFilesDir(null), "otp_gtfs.zip")
        if (!destinationFile.exists()) {
            Log.i("MainActivityMessage", "Update Required: Core directory doesn't exist.")
            return true // Update if the zip file doesn't exist
        }

        val lastModified = destinationFile.lastModified()
        val lastModifiedDate = Calendar.getInstance().apply { timeInMillis = lastModified }
        val now = Calendar.getInstance()

        val fourAmToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Check if last modified date is before today's 4:00 AM and now is after today's 4:00 AM
        return lastModifiedDate.before(fourAmToday) && now.after(fourAmToday)
    }

    private fun showProgressDialog(show: Boolean) {
        runOnUiThread {
            if (show) progressDialog.show() else progressDialog.dismiss()
        }
    }


    private fun checkAndRequestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permissions granted, you can start location updates or work with location data as needed
        } else {
            // Permissions denied, handle the failure scenario
        }
    }
}

