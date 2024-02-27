package edu.miamioh.csi.capstone.busapp.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.CSVHandler

@Composable
fun StopsView() {
    val stops = CSVHandler.getStops()
    val context = LocalContext.current
    val uiSettings = remember {
        MapUiSettings(myLocationButtonEnabled = true)
    }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = true
    }

    val initialPosition = LatLng(38.9048, 16.5952)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
    ) {
        val stopCount = minOf(stops.size, 50)
        for (i in 0 until stopCount) {
            val stop = stops[i]
            Marker(
                state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                title = "Stop",
                snippet = "Latitude: ${stop.stopLat}, Longitude: ${stop.stopLon}"
            )
        }
    }
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}