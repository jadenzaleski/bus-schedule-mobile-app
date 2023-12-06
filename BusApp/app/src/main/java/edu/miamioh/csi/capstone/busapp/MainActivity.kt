package edu.miamioh.csi.capstone.busapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.DefaultSettingsProvider
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import dev.shreyaspatil.permissionflow.compose.rememberPermissionFlowRequestLauncher
import dev.shreyaspatil.permissionflow.compose.rememberPermissionState
import edu.miamioh.csi.capstone.busapp.navigation.AppNavigation
import edu.miamioh.csi.capstone.busapp.ui.theme.BusAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.Properties

class MainActivity : ComponentActivity() {
    @OptIn(MapboxExperimental::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // toggle below to draw behind status and nav bar.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val url = "https://mobilita.regione.calabria.it/gtfs/otp_gtfs.zip"
        val destinationFile = File(applicationContext.getExternalFilesDir(null), "otp_gtfs.zip")
        // The below three must MUST be completed in proper order for the app to
        // use the updated data properly.

        CoroutineScope(Dispatchers.Default).launch {
            val downloadJob = async {
                CSVHandler.downloadFile(url, destinationFile)
                Log.i("DOWNLOAD", "Download job is complete (Stage 1).")
            }.await()
            val unzipJob = async {
                UnzipUtils.unzip(destinationFile, "/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core/")
                Log.i("UNZIP", "Unzip job is complete (Stage 2).")
            }.await()
            val renameJob = async {
                CSVHandler.renameToCSV("/storage/emulated/0/Android/data/edu.miamioh.csi.capstone.busapp/files/core")
                Log.i("RENAME", "Rename Job is complete (Stage 3).")
            }.await()

            // after all operations are complete initialize CSVHandler
                withContext(Dispatchers.IO) {
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
                }
        }
        // MAPBOX
        MapboxOptions.accessToken = "sk.eyJ1IjoiamFkZW56YWxlc2tpIiwiYSI6ImNsb3A5a2pmdzA3N3gyaW5xMWlhdXpkankifQ.0wzY9kVrxyI3zuoBy_SxMA"
        val permissionList = listOf(android.Manifest.permission.ACCESS_FINE_LOCATION)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            val permissionLauncher = rememberPermissionFlowRequestLauncher()
            val state by rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
            var currentLocation: Point? by remember { mutableStateOf(null) }
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(0.0, 0.0))
                    zoom(1.0)
                    pitch(0.0)
                }
            }

            LaunchedEffect(state){
                coroutineScope.launch {
                    if(state.isGranted) {
                        currentLocation = LocationService.getCurrentLocation(context)
                        val mapAnimationOptions =
                            MapAnimationOptions.Builder().duration(1500L).build()
                        mapViewportState.flyTo(
                            CameraOptions.Builder()
                                .center(currentLocation)
                                .zoom(12.0)
                                .build(),
                            mapAnimationOptions
                        )
                    }
                }
            }

            Column {
                if (state.isGranted) {
                    // adding search section ?
                } else {
                    Button(onClick = { permissionLauncher.launch(permissionList.toTypedArray()) }) {
                        Text("Request Permissions")
                    }
                }
                MainMapViewComposable(mapViewportState, currentLocation)
            }

            // Function call to display NavBar at bottom of screen
            AppNavigation()
        }

    }
}

@Composable
@OptIn(MapboxExperimental::class)
private fun MainMapViewComposable(
    mapViewportState: MapViewportState,
    currentLocation: Point?
) {
    val gesturesSettings by remember {
        mutableStateOf(DefaultSettingsProvider.defaultGesturesSettings)
    }

    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        gesturesSettings = gesturesSettings,
        mapInitOptionsFactory = { context ->
            MapInitOptions(
                context = context,
                styleUri = Style.TRAFFIC_DAY,
                cameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(24.9384, 60.1699))
                    .zoom(12.0)
                    .build()
            )
        }
    ) {
        //currentLocation?.let { AddSingleMarkerComposable(it, resources) }

    }
}

