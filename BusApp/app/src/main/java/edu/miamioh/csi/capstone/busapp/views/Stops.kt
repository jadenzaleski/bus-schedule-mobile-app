package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
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
//    // Assuming these fetches are correctly implemented in CSVHandler
//    val stops = CSVHandler.getStops()
//    val routes = CSVHandler.getRoutes()
//    val trips = CSVHandler.getTrips()
//    val stopTimes = CSVHandler.getStopTimes()
//    val agencies = CSVHandler.getAgencies()
//    val context = LocalContext.current
//
//    // Call getAgencyIdToStopsMap from CSVHandler
//    val agencyIdToStopsMap = remember { CSVHandler.getAgencyIdToStopsMap(stops, routes, trips, stopTimes) }
//
//    var isLocationPermissionGranted by remember { mutableStateOf(false) }
//    LaunchedEffect(key1 = context) {
//        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
//            context, Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    val initialPosition = LatLng(38.9048, -77.0342) // A central location
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
//    }
//
//    // Default agency selected
//    val defaultAgencyName = agencies.firstOrNull()?.agencyName ?: ""
//    val selectedAgencyNames = remember { mutableStateListOf(defaultAgencyName) }
//    var expanded by remember { mutableStateOf(false) }
//
//    // State for user-specified max stops
//    var maxStopsInput by remember { mutableStateOf("") }
//    var maxStops by remember { mutableStateOf(50) }
//
//    Column {
//        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
//            Text(
//                text = "Select Agencies",
//                modifier = Modifier
//                    .clickable { expanded = true }
//                    .padding(end = 8.dp),
//                fontSize = 18.sp
//            )
//
//            TextField(
//                value = maxStopsInput,
//                onValueChange = { maxStopsInput = it.filter { char -> char.isDigit() } },
//                label = { Text("Max Stops") },
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier.width(100.dp)
//            )
//
//            Button(
//                onClick = {
//                    maxStopsInput.toIntOrNull()?.let {
//                        if (it >= 1) maxStops = it
//                    }
//                },
//                modifier = Modifier.padding(start = 8.dp)
//            ) {
//                Text("Set")
//            }
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            agencies.forEach { agency ->
//                val isSelected = agency.agencyName in selectedAgencyNames
//                DropdownMenuItem(
//                    text = { Text(agency.agencyName) },
//                    onClick = {
//                        if (isSelected) {
//                            selectedAgencyNames.remove(agency.agencyName)
//                        } else {
//                            selectedAgencyNames.add(agency.agencyName)
//                        }
//                        expanded = false
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
//        // We need a state to keep track of the first stop's position to move the camera to.
//        val firstStopLatLng = remember { mutableStateOf<LatLng?>(null) }
//
//        GoogleMap(
//            modifier = Modifier.fillMaxSize(),
//            cameraPositionState = cameraPositionState,
//            uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted, compassEnabled = true),
//            properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
//        ) {
//            var stopsDisplayed = 0
//            agencies.filter { it.agencyName in selectedAgencyNames }.forEachIndexed { index, agency ->
//                agencyIdToStopsMap[agency.agencyID]?.take(maxStops)?.forEachIndexed { stopIndex, stop ->
//                    if (stopsDisplayed < maxStops) {
//                        if (index == 0 && stopIndex == 0) {
//                            // This is the first stop of the first selected agency, capture its position.
//                            firstStopLatLng.value = LatLng(stop.stopLat, stop.stopLon)
//                        }
//                        Marker(
//                            state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
//                            title = "Stop",
//                            snippet = "Agency: ${agency.agencyName}, Stop ID: ${stop.stopId}"
//                        )
//                        stopsDisplayed++
//                    }
//                }
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

    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val initialPosition = LatLng(41.9028, 12.4964) // Rome, Italy as fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 5f)
    }

    val defaultAgencyName = agencies.firstOrNull()?.agencyName ?: ""
    val selectedAgencyNames = remember { mutableStateListOf(defaultAgencyName) }
    var expanded by remember { mutableStateOf(false) }

    var maxStopsInput by remember { mutableStateOf("") }
    var maxStops by remember { mutableStateOf(50) }

    // Prepare the mapping of stop IDs to agency IDs
    val stopIdToAgencyIdMap = remember {
        CSVHandler.getStopIdToAgencyIdMap(stops, routes, trips, stopTimes)
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Select Agencies",
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(end = 8.dp),
                fontSize = 18.sp
            )

            TextField(
                value = maxStopsInput,
                onValueChange = { maxStopsInput = it.filter { char -> char.isDigit() } },
                label = { Text("Max Stops") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(100.dp)
            )

            Button(
                onClick = {
                    maxStopsInput.toIntOrNull()?.let {
                        if (it >= 1) maxStops = it
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Set")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            agencies.forEach { agency ->
                val isSelected = agency.agencyName in selectedAgencyNames
                DropdownMenuItem(
                    text = { Text(agency.agencyName) },
                    onClick = {
                        if (isSelected) {
                            selectedAgencyNames.remove(agency.agencyName)
                        } else {
                            selectedAgencyNames.add(agency.agencyName)
                        }
                        expanded = false
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
            val selectedAgencyIds = agencies.filter { it.agencyName in selectedAgencyNames }.map { it.agencyID }.toSet()

            // Filter stops based on the selected agency IDs and limit to maxStops
            val filteredStops = stops.filter { stop ->
                stopIdToAgencyIdMap[stop.stopId] in selectedAgencyIds
            }.take(maxStops)

            filteredStops.forEachIndexed { index, stop ->
                Marker(
                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                    title = "Stop",
                    snippet = "Stop ID: ${stop.stopId}, Agency ID: ${stopIdToAgencyIdMap[stop.stopId]}"
                )
                // Move the camera to the first stop
                if (index == 0) {
                    cameraPositionState.move(CameraUpdateFactory.newLatLng(LatLng(stop.stopLat, stop.stopLon)))
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