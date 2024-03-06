package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.CSVHandler
import edu.miamioh.csi.capstone.busapp.navigation.Screens


@Composable
fun StopsView() {
    // Assuming these fetches are correctly implemented in CSVHandler
    val stops = CSVHandler.getStops()
    val routes = CSVHandler.getRoutes()
    val trips = CSVHandler.getTrips()
    val stopTimes = CSVHandler.getStopTimes()
    val agencies = CSVHandler.getAgencies()
    val context = LocalContext.current
    val navController = rememberNavController()

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

            filteredStops.forEach { stop ->
                // Using the custom MarkerInfoWindowContent instead of the standard Marker
                MarkerInfoWindowContent(
                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                    onInfoWindowClick = {
                        // Navigate to another screen on info window click
                        navController.navigate(Screens.RouteScreen.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(0.8f)) {
                        Text(text = stop.stopName,
                            modifier = Modifier.padding(top = 5.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                        Divider(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Lat: ${stop.stopLat}")
                            Spacer(modifier = Modifier.width(5.dp)) // Replaced VerticalDivider with Spacer for simplicity
                            Text(text = "Lon: ${stop.stopLon}")
                        }
                        Text(text = "Stop ID: ${stop.stopId}")
                        Text(text = "Tap to plan",
                            modifier = Modifier.padding(top = 10.dp, bottom = 5.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Blue
                            ))
                    }
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