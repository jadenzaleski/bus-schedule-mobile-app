package edu.miamioh.csi.capstone.busapp

import android.os.Bundle
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
import edu.miamioh.csi.capstone.busapp.ui.theme.BusAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(MapboxExperimental::class)
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
                    //TODO: adding search section
                } else {
                    Button(onClick = { permissionLauncher.launch(permissionList.toTypedArray()) }) {
                        Text("Request Permissions")
                    }
                }
                MainMapViewComposable(mapViewportState, currentLocation)
            }

            BusAppTheme() {
                // Main view of the app
                MainView()

            }
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

