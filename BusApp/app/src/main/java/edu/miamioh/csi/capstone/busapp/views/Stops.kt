/**
 * Contributors: Jaden Zaleski, Daniel Tai, Ayo Obisesan
 * Last Modified: 3/13/2024
 * Description: Contains all the front-end and back-end code for the Stops page. See individual
 *              method documentation for further details
 */

package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.R
import edu.miamioh.csi.capstone.busapp.backend.CSVHandler
import edu.miamioh.csi.capstone.busapp.backend.RouteFinderTester
import edu.miamioh.csi.capstone.busapp.navigation.Screens
import edu.miamioh.csi.capstone.busapp.ui.theme.Black
import edu.miamioh.csi.capstone.busapp.ui.theme.Gray400
import edu.miamioh.csi.capstone.busapp.ui.theme.Green
import edu.miamioh.csi.capstone.busapp.ui.theme.Light
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt


@Composable
fun StopsView() {
    //StopsWorkhorse()
    RouteFinderTester.runTests()
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
    val routes = CSVHandler.getRoutes()
    val trips = CSVHandler.getTrips()
    val stopTimes = CSVHandler.getStopTimes()
    val agencies = CSVHandler.getAgencies()

    Log.i("Check", "" + stops.size)
    Log.i("Check", "" + routes.size)
    Log.i("Check", "" + trips.size)
    Log.i("Check", "" + stopTimes.size)
    Log.i("Check", "" + agencies.size)

    val context = LocalContext.current
    val navController = rememberNavController()



    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    val currentZoomLevel by remember { mutableStateOf(9f) } // Initial zoom level
    val currentTime = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date()) }

    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*
     * Sets up initial map settings for when user first loads up the Stops page upon opening app.
     * This includes:
     * The initial starting coordinates to center upon when the map is first loaded.
     * The initial center coordinates of the map being actively displayed on the screen.
     * The cameraPositionState of the app after the initial boot-up sequence.
     */
    val initialPosition = LatLng(39.2, 16.25) // Cosenza
    var mapCenter by remember { mutableStateOf(initialPosition) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, currentZoomLevel)
    }

    // Upon starting the app, the first agency pulled from the "agencies" variable will be
    // auto-selected and active on the map.
    val defaultAgencyName = agencies.firstOrNull()?.agencyName ?: ""

    // Returns all agency names that are presently selected via the dropdown.
    val selectedAgencyNames = remember { mutableStateListOf(defaultAgencyName) }
    var expanded by remember { mutableStateOf(false) }

    // Prepare the mapping of stop IDs to agency IDs
    val stopIdToAgencyIdMap = remember {
        CSVHandler.getStopIdToAgencyIdMap(stops, routes, trips, stopTimes)
    }

    Log.i("Stops Page", "" + stopIdToAgencyIdMap.size)

    /*
     * selectedAgencyIds identifies which agencies have been selected by the user to be displayed
     * on the map. filterStops checks each of its elements to see if a stop's agency is found here.
     *
     * It is automatically updated whenever selectedAgencyNames gets updated (via the dropdown).
     */
    var selectedAgencyIds = remember {
        derivedStateOf {
            agencies.filter { it.agencyName in selectedAgencyNames }.map { it.agencyID }.toSet()
        }
    }.value

    Log.i("Valid Agency IDs: ", "" + selectedAgencyIds)

    // Sets the initial number of stops displayed when the app is started.
    var maxStopsInput by remember { mutableStateOf("50") }
    var maxStops by remember { mutableIntStateOf(50) }

    // Tracks whenever the center coordinates of the map changes, and updates mapCenter accordingly
    // for use in distance calculations, etc.
    trackMapInteraction(cameraPositionState) { zoomLevel, center ->
        mapCenter = center
    }

    // Dynamically calculate filtered stops based on all relevant criteria
    val filteredStops = remember(mapCenter, selectedAgencyIds, maxStops) {
        maxStopsInput = min(maxStops, 150).toString()
        stops.filter { stop ->
            val agencyIdsForStop = stopIdToAgencyIdMap[stop.stopID]
            //Log.i("All Agencies Associated with Stops", "" + stopIdToAgencyIdMap[stop.stopId])
            //Log.i("All Agencies Selected by User", "" + selectedAgencyIds)
            agencyIdsForStop != null && agencyIdsForStop.any { it in selectedAgencyIds } &&
                    calculateSphericalDistance(mapCenter.latitude, mapCenter.longitude, stop.stopLat, stop.stopLon) <= 60
        }.sortedBy { calculateSphericalDistance(mapCenter.latitude, mapCenter.longitude, it.stopLat, it.stopLon) }
            .take(min(maxStops, 150))
    }

    val focusManager = LocalFocusManager.current

    /*
     * Code for the top bar of the Stops page. Contains various fields that the user can manipulate
     * (including what agencies are active, and the max number of stops to display).
     */
    Column(modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = {
        focusManager.clearFocus()
    }) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .background(Light)
                .padding(8.dp)
                .fillMaxWidth(),
        ) {

            // Creates a button that, when pressed, activates the dropdown menu full of agencies.
            OutlinedButton(
                onClick = {
                    expanded = !expanded
                    focusManager.clearFocus()
                },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Green,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.Gray
                ),
                border = BorderStroke(2.dp, Green),
                modifier = Modifier
                    .padding(start = 5.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(20)
            ) {
                Icon(
                    painterResource(id = R.drawable.baseline_filter_list_24),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(text = "${selectedAgencyNames.size}")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Creates a text field where the user can input the max number of stops they want
            // displayed at a certain moment in time.
            OutlinedTextField(
                value = maxStopsInput,
                onValueChange = { maxStopsInput = it.filter { char -> char.isDigit() } },
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        maxStopsInput.toIntOrNull()?.let {
                            if (it >= 1) maxStops = it
                        }
                    },
                ),

                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Gray400,
                    focusedIndicatorColor = Gray400,
                    focusedTextColor = Black,
                    focusedLabelColor = Color.DarkGray,
                    unfocusedLabelColor = Color.DarkGray,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(20),
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.baseline_numbers_24),
                        contentDescription = null,
                        modifier = Modifier
                    )
                }
            )

            // Creates a button that, when pressed, formally changes the variable that stores how
            // many stops should be presently displayed.
            OutlinedButton(
                onClick = {
                    maxStopsInput.toIntOrNull()?.let {
                        if (it >= 1) maxStops = it
                    }
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .height(50.dp)
                    .padding(start = 10.dp, end = 5.dp),
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Green,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.Gray
                ),
                border = BorderStroke(2.dp, Green),
                shape = RoundedCornerShape(20)
            ) {
                Text("Set")
            }
        }

        // Specific code for the dropdown menu and how the agency names are displayed.
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(0.65F),

            ) {
            // Potentially add a "Clear All" button here.
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

                    },
                    leadingIcon = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null, // Interaction handled by the item's onClick
                            colors = CheckboxDefaults.colors(checkedColor = Green)
                        )
                    }
                )
            }
        }

        // Generates the basic Google Maps interface you see on the screen, and displays stops in
        // the form of markers as appropriate.
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted, compassEnabled = true),
            properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted, minZoomPreference = 5.0f)
        ) {
            filteredStops.forEach { stop ->
                val nextDepartureTime = CSVHandler.getNextDepartureTimeForStop(stop.stopID, currentTime) ?: "Unavailable"
                // Using the custom MarkerInfoWindowContent instead of the standard Marker
                MarkerInfoWindowContent(
                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                    onInfoWindowClick = {
                        // Should navigate to another screen on info window click
                        // TODO: FINISH
                        navController.navigate(Screens.RouteScreen.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(
                            text = stop.stopName,
                            modifier = Modifier.padding(top = 5.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Lat: ${stop.stopLat}")
                            // Replaced VerticalDivider with Spacer for simplicity
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Lon: ${stop.stopLon}")
                        }
                        Text(text = "Stop ID: ${stop.stopID}")
                        Text(text = "Next departure: $nextDepartureTime", style = TextStyle(fontSize = 16.sp))
                        Text(
                            text = "Tap to plan",
                            modifier = Modifier.padding(top = 10.dp, bottom = 5.dp),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Blue
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Using the Haversine formula: calculates the distance between two sets of latitudes and longitudes
 *
 * @param lat1 - The latitude of the first set of coordinates
 * @param lon1 - The longitude of the first set of coordinates
 * @param lat2 - The latitude of the second set of coordinates
 * @param lon2 - The longitude of the second set of coordinates
 * @return The distance between the two given coordinates
 */
fun calculateSphericalDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
 * @return How many stops should be displayed on the map presently
 */
/*
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
*/

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
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val newZoomLevel = cameraPositionState.position.zoom
            val newPosition = cameraPositionState.position.target
            onCameraChange(newZoomLevel, newPosition)
        }
    }
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}