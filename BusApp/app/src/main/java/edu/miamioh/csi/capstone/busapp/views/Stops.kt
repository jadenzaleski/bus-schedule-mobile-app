package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.CSVHandler
import org.apache.commons.lang3.math.NumberUtils.toInt

// Declare stopCount as a top-level variable
var stopCount by mutableStateOf(0)

@Composable
fun StopsView() {
    GoogleMapCentralHQ()
}

@Composable
fun GoogleMapCentralHQ() {
    val stops = CSVHandler.getStops()
    val context = LocalContext.current
    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    // This block of code checks to see if the locationPermission has been granted beforehand
    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val initialPosition = LatLng(38.9048, 16.5952)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted, compassEnabled = true),
        properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
    ) {
        stopCount = minOf(stops.size, 50)
        for (i in 0 until stopCount) {
            val stop = stops[i]
            Marker(
                state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                title = "Stop",
                snippet = "Latitude: ${stop.stopLat}, Longitude: ${stop.stopLon}"
            )
        }
    }

    trackMapInteraction(cameraPositionState = cameraPositionState)
}

@Composable
fun trackMapInteraction(
    cameraPositionState: CameraPositionState
) {
    // store the initial position of the camera
    var initialCameraPosition by remember { mutableStateOf(cameraPositionState.position) }

    // called when the camera just starts moving
    val onMapCameraMoveStart: (cameraPosition: CameraPosition) -> Unit = {
        // store the camera's position when map started moving
        initialCameraPosition = it
    }

    // called when the map camera stops moving
    val onMapCameraIdle: (cameraPosition: CameraPosition) -> Unit = { newCameraPosition ->
        // this is the reason why the camera was moving.
        val cameraMovementReason = cameraPositionState.cameraMoveStartedReason

        if (cameraMovementReason == CameraMoveStartedReason.GESTURE) {

            if (newCameraPosition.zoom < initialCameraPosition.zoom) {
                // this is zoom out
                stopCount = toInt("" + newCameraPosition.zoom) * 2
                Log.i("TEST", "" + newCameraPosition)
                Log.i("STOP COUNT", "" + stopCount)
            }

            if (newCameraPosition.zoom > initialCameraPosition.zoom) {
                // this is zoom in
                stopCount = toInt("" + newCameraPosition.zoom) * 2
                Log.i("TEST", "" + newCameraPosition)
                Log.i("STOP COUNT", "" + stopCount)
            }

            if (newCameraPosition.bearing != initialCameraPosition.bearing) {
                // this is map rotation
                Log.i("TEST", "" + newCameraPosition)
            }

            // Please note target can change in any of the above 3 interactions as well.
            if (newCameraPosition.target != initialCameraPosition.target) {
                // this is zoom out
                Log.i("TEST", "" + newCameraPosition)
            }
        }

        initialCameraPosition = newCameraPosition
    }

    LaunchedEffect(key1 = cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            onMapCameraMoveStart(cameraPositionState.position)
        } else {
            onMapCameraIdle(cameraPositionState.position)
        }
    }
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}