package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.CSVHandler
import edu.miamioh.csi.capstone.busapp.Stop
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Declare stopCount as a top-level variable
var stopCount by mutableStateOf(10000)

// At the top level, outside of your composables
var markerStates = mutableStateListOf<MarkerState>()

@Composable
fun StopsView() {
    GoogleMapCentralHQ()
}

@Composable
fun GoogleMapCentralHQ() {
    val stops = CSVHandler.getStops()
    val context = LocalContext.current
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    var currentZoomLevel by remember { mutableStateOf(9f) } // Initial zoom level

    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val initialPosition = LatLng(38.9048, 16.5952)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, currentZoomLevel)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted, compassEnabled = true),
        properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
    ) {
        markerStates.forEach { markerState ->
            Marker(
                state = markerState,
                title = "Stop",
                snippet = "Latitude: ${markerState.position.latitude}, Longitude: ${markerState.position.longitude}"
            )
        }
    }

    trackMapInteraction(cameraPositionState) { newZoomLevel, newPosition ->
        // Now this triggers on both zoom and position changes
        updateMarkersBasedOnZoomAndPosition(stops, newZoomLevel, newPosition)
    }
}

fun updateMarkersBasedOnZoomAndPosition(stops: List<Stop>, zoomLevel: Float, cameraCentralPosition: LatLng) {
    // Determine the number of markers to display based on the zoom level
    val markerCount = calculateNumberOfMarkers(zoomLevel)

    Log.i("checkZoomLevel", "" + zoomLevel)
    Log.i("DEBUG", "" + markerCount)

    // Calculate distances from the camera's central position to each stop
    val distances = stops.map { stop ->
        calculateDistance(cameraCentralPosition.latitude, cameraCentralPosition.longitude, stop.stopLat, stop.stopLon) to stop
    }.sortedBy { it.first }

    // Select the closest stops based on the calculated marker count
    val closestStops = distances.take(markerCount).map { it.second }

    // Clear current markers and add new ones for the closest stops
    markerStates.clear()
    closestStops.forEach { stop ->
        markerStates.add(MarkerState(position = LatLng(stop.stopLat, stop.stopLon)))
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371 // Radius of the earth in kilometers
    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)
    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(lonDistance / 2) * sin(lonDistance / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c // Convert to distance
}

fun calculateNumberOfMarkers(zoomLevel: Float): Int {
    // This is a placeholder function. Adjust the logic based on your requirements.
    val zoomLev = zoomLevel
    if (zoomLev <= 10.4) {
        return 0;
    } else if (zoomLev > 10.4 && zoomLev <= 12) {
        return 150;
    } else if (zoomLev > 12 && zoomLev <= 13) {
        return 75;
    } else if (zoomLev > 13 && zoomLev <= 14) {
        return 50;
    } else {
        return 25;
    }
}

@Composable
fun trackMapInteraction(
    cameraPositionState: CameraPositionState,
    onCameraChange: (Float, LatLng) -> Unit
) {
    // Track both the zoom level and the central position
    val currentZoomLevel by remember { mutableStateOf(cameraPositionState.position.zoom) }
    val currentPosition by remember { mutableStateOf(cameraPositionState.position.target) }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val newZoomLevel = cameraPositionState.position.zoom
            val newPosition = cameraPositionState.position.target
            // Check if either the zoom level or the position has changed
            if (newZoomLevel != currentZoomLevel || newPosition != currentPosition) {
                onCameraChange(newZoomLevel, newPosition)
            }
        }
    }
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}