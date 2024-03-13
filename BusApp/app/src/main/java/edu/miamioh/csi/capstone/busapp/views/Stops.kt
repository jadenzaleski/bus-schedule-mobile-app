/**
 * Contributors: Jaden Zaleski, Daniel Tai, Ayo Obisesan
 * Last Modified: 3/13/2024
 * Description: Contains all the front-end and back-end code for the Stops page. See individual
 *              method documentation for further details
 */

package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
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

// A mutable list of marker states. This variable stores whatever stops will
// be actively displayed at a specific moment in time based on user actions
var markerStates = mutableStateListOf<MarkerState>()

@Composable
fun StopsView() {
    StopsWorkhorse()
}

/**
 * The primary Composable function for the "Stops" page. It:
 * 1) Grabs all the data from the CORe website via the CSVHandler
 * 2) Sets up the Google Map that is displayed on the UI
 * 3) Displays all markers on the map
 * 4) Calls the "trackMapInteraction" function to detect changes via user gestures
 */
@Composable
fun StopsWorkhorse() {
    val stops = CSVHandler.getStops()
    val context = LocalContext.current
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    val currentZoomLevel by remember { mutableStateOf(9f) } // Initial zoom level

    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /*
     * Sets up initial map settings for when user first loads up the Stops page upon opening app.
     * This includes:
     * The initial starting coordinates to center upon when the map is first loaded
     * The initial zoom level of the map
     */
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
        // Triggers appropriate function based on zoom or coordinate position changes.
        updateMarkersBasedOnZoomAndPosition(stops, newZoomLevel, newPosition)
    }
}

/**
 * A function that updates the list of markers that should be actively displayed on the map whenever
 * a user gesture is recorded. It finds out how many stops should be displayed, as well as which
 * stops should be displayed (based on Haversine formula's distance from a set point)
 *
 * @param stops - Fed from CSVHandler, contains all stop information taken from CORe website
 * @param zoomLevel - The current zoom level of the map
 * @param cameraCentralPosition - The coordinates corresponding to the current center of the map
 *        on the screen
 */
fun updateMarkersBasedOnZoomAndPosition(stops: List<Stop>, zoomLevel: Float, cameraCentralPosition: LatLng) {
    // Calls a function to figure out how many stops should be displayed based on the zoom level
    val markerCount = calculateNumberOfMarkers(zoomLevel)

    // Some helpful debugging code involving Logcat
    // Log.i("checkZoomLevel", "" + zoomLevel)
    // Log.i("DEBUG", "" + markerCount)

    // Calculate distances from the camera's central coordinates to each individual stop
    val distances = stops.map { stop ->
        calculateDistance(cameraCentralPosition.latitude, cameraCentralPosition.longitude, stop.stopLat, stop.stopLon) to stop
    }.sortedBy { it.first }

    // Finds the "x" closest stops, where x = markerCount
    val closestStops = distances.take(markerCount).map { it.second }

    // Clear all current markers stored and add new ones for the closest stops
    markerStates.clear()
    closestStops.forEach { stop ->
        markerStates.add(MarkerState(position = LatLng(stop.stopLat, stop.stopLon)))
    }
}

/**
 * Using the Haversine formula: calculates the distance between two sets of latitudes and longitudes
 *
 * @param lat1 - The latitude of the first set of coordinates
 * @param lon1 - The longitude of the first set of coordinates
 * @param lat2 - The latitude of the second set of coordinates
 * @param lon2 - The longitude of the second set of coordinates
 */
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

/**
 * Based on the zoom level of the map, decides how many markers (stops) should be displayed on the
 * map. Subject to change depending on client preferences, but these numbers can be easily adjusted
 *
 * @param zoomLevel - The current zoom level of the active map
 */
fun calculateNumberOfMarkers(zoomLevel: Float): Int {
    // This is a placeholder function. Adjust the logic based on your requirements.
    if (zoomLevel <= 10.4) {
        return 0
    } else if (zoomLevel > 10.4 && zoomLevel <= 12) {
        return 150
    } else if (zoomLevel > 12 && zoomLevel <= 13) {
        return 75
    } else if (zoomLevel > 13 && zoomLevel <= 14) {
        return 50
    } else {
        return 25
    }
}

/**
 * Detects when there are changes to either the zoom level or central camera position of the map.
 * Handles said changes - primarily by adjusting what markers should be displayed on the map
 *
 * @param cameraPositionState - The current state of the map's camera position
 * @param onCameraChange - A callback function that gets called when there's a change in either the
 *        zoom level or camera position
 */
@Composable
fun trackMapInteraction(
    cameraPositionState: CameraPositionState,
    onCameraChange: (Float, LatLng) -> Unit
) {
    // Tracks both the zoom level and the central position
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