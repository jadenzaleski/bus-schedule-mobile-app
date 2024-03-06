package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.CSVHandler

//@Composable
//fun StopsView() {
//    val stops = CSVHandler.getStops() // Assuming this fetches stops correctly
//    val agencies = CSVHandler.getAgencies() // Fetch all the listed agencies, List<Agency>
//    val context = LocalContext.current
//    var isLocationPermissionGranted by remember { mutableStateOf(false) }
//
//    // Permissions
//    LaunchedEffect(key1 = context) {
//        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
//            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//    }
//
//    // Initial camera position
//    val initialPosition = LatLng(38.9048, -77.0342) // A central location
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
//    }
//
//    // Dropdown menu states for agency names
//    val selectedAgencyNames = remember { mutableStateListOf<String>() }
//    var expanded by remember { mutableStateOf(false) }
//
//    Column {
//        // Dropdown menu for selecting agencies
//        Text(
//            text = "Select Agencies",
//            modifier = Modifier
//                .clickable { expanded = true }
//                .padding(16.dp),
//            fontSize = 18.sp
//        )
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            agencies.forEach { agency ->
//                val isSelected = agency.agencyName in selectedAgencyNames
//                DropdownMenuItem(
//                    text = { Text(agency.agencyName) },
//                    onClick = {
//                        // Toggle selection state of this agency name
//                        if (isSelected) {
//                            selectedAgencyNames.remove(agency.agencyName)
//                        } else {
//                            selectedAgencyNames.add(agency.agencyName)
//                        }
//                        // Optionally keep the menu open for multiple selections
//                        expanded = !isSelected // or set to true for keeping the menu always open
//                    },
//                    leadingIcon = {
//                        Checkbox(
//                            checked = isSelected,
//                            onCheckedChange = null // Interaction handled by the item's onClick
//                        )
//                    }
//                )
//            }
//        }
//
//        GoogleMap(
//            modifier = Modifier.fillMaxSize(),
//            cameraPositionState = cameraPositionState,
//            uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted, compassEnabled = true),
//            properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
//        ) {
//            // Display markers based on selected agencies or other criteria
//            stops.take(300).forEach { stop ->
//                Marker(
//                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
//                    title = "Stop",
//                    snippet = "Latitude: ${stop.stopLat}, Longitude: ${stop.stopLon}"
//                )
//            }
//        }
//    }
//}

@Composable
fun StopsView() {
    // Assuming these fetches are correctly implemented in CSVHandler
    val stops = CSVHandler.getStops()
    val routes = CSVHandler.getRoutes()
    val trips = CSVHandler.getTrips()
    val stopTimes = CSVHandler.getStopTimes()
    val agencies = CSVHandler.getAgencies()
    val context = LocalContext.current

    // Call getAgencyIdToStopsMap from CSVHandler
    val agencyIdToStopsMap = remember { CSVHandler.getAgencyIdToStopsMap(stops, routes, trips, stopTimes) }

    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    // Permission check simplified for brevity
    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val initialPosition = LatLng(38.9048, -77.0342) // A central location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
    }

    val selectedAgencyNames = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Dropdown menu for selecting agencies
        Text(
            text = "Select Agencies",
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp),
            fontSize = 18.sp
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            agencies.forEach { agency ->
                val isSelected = agency.agencyName in selectedAgencyNames
                DropdownMenuItem(
                    text = { Text(agency.agencyName) },
                    onClick = {
                        // Toggle selection state of this agency name
                        if (isSelected) {
                            selectedAgencyNames.remove(agency.agencyName)
                        } else {
                            selectedAgencyNames.add(agency.agencyName)
                        }
                        // Optionally keep the menu open for multiple selections
                        expanded = !isSelected // or set to true for keeping the menu always open
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null // Interaction handled by the item's onClick
                        )
                    }
                )
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted, compassEnabled = true),
            properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
        ) {
            // Initialize a counter to track the number of stops displayed
            var stopsDisplayed = 0
            val maxStops = 50 // Maximum number of stops to display

            // Filter and display stops for selected agencies, up to 50
            agencies.filter { it.agencyName in selectedAgencyNames }.forEach { agency ->
                agencyIdToStopsMap[agency.agencyID]?.let { stopsForAgency ->
                    stopsForAgency.takeWhile {
                        // Only process stops until the maximum count is reached
                        stopsDisplayed < maxStops
                    }.forEach { stop ->
                        if (stopsDisplayed >= maxStops) {
                            // If the maximum number of stops has been reached, stop processing further
                            return@forEach
                        }
                        // Add a marker for the current stop
                        Marker(
                            state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                            title = "Stop",
                            snippet = "Agency: ${agency.agencyName}, Stop ID: ${stop.stopId}"
                        )
                        // Increment the counter after adding each stop
                        stopsDisplayed++
                    }
                }
                if (stopsDisplayed >= maxStops) {
                    // Once the limit is reached, no need to process further agencies
                    return@GoogleMap
                }
            }
        }
    }
}



@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}

// Scrap Code for adding a specified location button:

//val coroutineScope = rememberCoroutineScope()
//val sydney = LatLng(-33.852, 151.211)
//Button(
//onClick = {
//    coroutineScope.launch {
//        cameraPositionState.animate(
//            CameraUpdateFactory.newCameraPosition(
//                CameraPosition.fromLatLngZoom(
//                    sydney, 10f)), 500)
//    }
//}
//)