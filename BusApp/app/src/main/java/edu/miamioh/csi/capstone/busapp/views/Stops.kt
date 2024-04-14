/**
 * Contributors: Jaden Zaleski, Daniel Tai, Ayo Obisesan
 * Last Modified: 3/27/2024
 * Description: Contains all the front-end and some back-end code for the Stops page. See individual
 *              method documentation for further details
 */

package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
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
import edu.miamioh.csi.capstone.busapp.navigation.Screens
import edu.miamioh.csi.capstone.busapp.ui.theme.Black
import edu.miamioh.csi.capstone.busapp.ui.theme.Blue
import edu.miamioh.csi.capstone.busapp.ui.theme.Gray400
import edu.miamioh.csi.capstone.busapp.ui.theme.Green
import edu.miamioh.csi.capstone.busapp.ui.theme.Light
import edu.miamioh.csi.capstone.busapp.ui.theme.Red
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// Top Level Composable
@Composable
fun StopsView(navController: NavHostController) {
    StopsWorkhorse(navController)
}

/**
 * The primary Composable function for the "Stops" page. It:
 * 1) Grabs all the data from the CORe website via the CSVHandler
 * 2) Sets up the Google Map that is displayed on the UI
 * 3) Displays all markers on the map
 * 4) Calls the "trackMapInteraction" function to detect changes via user gestures
 */
@Composable
fun StopsWorkhorse(navController: NavHostController) {
    // Pull specific lists and store them into variables from the CSVHandler
    val stops = CSVHandler.getStops()
    val routes = CSVHandler.getRoutes()
    val trips = CSVHandler.getTrips()
    val stopTimes = CSVHandler.getStopTimes()
    val agencies = CSVHandler.getAgencies()
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    // Check if location permission is granted
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // variable that stores zoom level
    val currentZoomLevel by remember { mutableStateOf(9f) } // Initial zoom level

    // Permission dialog states
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showLocationActivatedDialog by remember { mutableStateOf(false) }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                isLocationPermissionGranted = true
                permissionDenied = false
            } else {
                permissionDenied = true
                isLocationPermissionGranted = false
                showPermissionDeniedDialog = true // Show dialog if permission is denied
            }
        }
    )

    // Preliminary check to see if location permissions are granted upon launching the Stops page
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
            agencyIdsForStop != null && agencyIdsForStop.any { it in selectedAgencyIds } &&
                    calculateSphericalDistance(mapCenter.latitude, mapCenter.longitude, stop.stopLat, stop.stopLon) <= 60
        }.sortedBy { calculateSphericalDistance(mapCenter.latitude, mapCenter.longitude, it.stopLat, it.stopLon) }
            .take(min(maxStops, 150))
    }

    // Retrieve the current focus manager from the local composition
    val focusManager = LocalFocusManager.current

    // Define a mutable state variable to control the visibility of the dialog
    var showDialog by remember { mutableStateOf(false) }

    // Define a mutable state variable to store the currently selected stop
    var selectedStop by remember { mutableStateOf<Place?>(null) }

    // called when info marker content is tapped
    fun navigateToRoutes(option: String, stop: Place?, navController: NavHostController) {
        if (stop != null) {
            val route = "${Screens.RouteScreen.name}?option=${option}&name=${URLEncoder.encode(stop.name, "UTF-8")}&lat=${stop.lat}&lon=${stop.lon}"
            navController.navigate(route)
        }
    }
    /*
    * If the user has tapped to plan a route this dialog comes up
    * It has two options for either selecting to start from a specific stop or end at a particular stop
    * Upon selection, the showDialog goes false and the user is redirected to the Routes page
    */
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Start or End?") },
            text = { Text("Would you like to set this stop as your starting or ending point?:") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        navigateToRoutes("start", selectedStop, navController)
                    }
                ) { Text("Start Here", color = Blue) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        navigateToRoutes("end", selectedStop, navController) // "from" for start point
                    }
                ) { Text("End Here", color = Blue) }
            },
            icon = { Icon(Icons.Default.Place, "") }
        )
    }


    /*
     * Code for the top bar of the Stops page. Contains various fields that the user can manipulate
     * (including what agencies are active, and the max number of stops to display).
     */
    Column(modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = {
        focusManager.clearFocus()
    }) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .background(Light)
                .padding(8.dp)
                .fillMaxWidth(),
        ) {

            // Code for the Activate Location Button
            ActivateLocationButton(
                isLocationPermissionGranted = isLocationPermissionGranted,
                permissionDenied = permissionDenied,
                onPermissionRequest = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                onShowPermissionsDeniedDialog = {
                    // Logic to show permissions denied dialog
                    showPermissionDeniedDialog = true
                },
                onShowLocationActivatedDialog = { showLocationActivatedDialog = true }
            )

            // This Dialog Appears when a user denies to access current location. It allows them to go
            // directly to their in app device settings to change location
            if (showPermissionDeniedDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDeniedDialog = false },
                    title = { Text("Location Permission Denied") },
                    text = { Text("You need to enable location permissions in app settings.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showPermissionDeniedDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    }
                )
            }

            // If location is already activated, this dialog informs the user that location is on
            if (showLocationActivatedDialog) {
                AlertDialog(
                    onDismissRequest = { showLocationActivatedDialog = false },
                    title = { Text("Location Activated") },
                    text = { Text("Your location is already activated.") },
                    confirmButton = {
                        TextButton(onClick = { showLocationActivatedDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

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
                    .width(100.dp)
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
                val nextDepartureTime = CSVHandler.getNextDepartureTimeForStop(stop.stopID) ?: "Unavailable"
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) // Get current time in 24-hour format
                // Using the custom MarkerInfoWindowContent instead of the standard Marker
                // Additionally the MarkerInfoWindowContent is a giant buttton (has an onClick)
                MarkerInfoWindowContent(
                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                    onInfoWindowClick = {
                        selectedStop = Place(stop.stopName, stop.stopLat, stop.stopLon, "", "")
                        showDialog = true
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        // Text for the name of a Stop
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
                        // Text for next Departure Time
                        Row {
                            Text(text = "Next departure: ", style = TextStyle(fontSize = 16.sp))
                            Text(text = nextDepartureTime, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                        // Text for Current Time
                        Row {
                            Text(text = "Current Time: ", style = TextStyle(fontSize = 16.sp)) // Added text for current time
                            Text(text = currentTime, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(top = 5.dp)) {
                            Text(text = "Lat: ${stop.stopLat}", style = TextStyle(fontSize = 16.sp))
                            // Replaced VerticalDivider with Spacer for simplicity
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Lon: ${stop.stopLon}", style = TextStyle(fontSize = 16.sp))
                        }
                        // Text for StopID
                        Text(text = "Stop ID: ${stop.stopID}", style = TextStyle(fontSize = 16.sp))
                        // Text for Tap to Plan:
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
 * Composable to handle user interactions corresponding with activating/displaying location permissions
 *
 * @param isLocationPermissionGranted, a boolean value for if location permissions are granted
 * @param permissionDenied, a boolean value for if location permissions are denied
 * @param onPermissionRequest, submit request to activate location settings
 * @param onShowPermissionsDeniedDialog, show dialog corresponding to denied location permissions
 * @param onShowLocationActivatedDialog, show dialog corresponding to activated location
 *
 * @return A composable button that allows a user to handle their permission settings on the Stops page
 */
@Composable
fun ActivateLocationButton(
    isLocationPermissionGranted: Boolean,
    permissionDenied: Boolean,
    onPermissionRequest: () -> Unit,
    onShowPermissionsDeniedDialog: () -> Unit,
    onShowLocationActivatedDialog: () -> Unit
) {
    // Button text that changes based on if the location permissions are granted
    val buttonText = if (isLocationPermissionGranted) "Location Activated" else "Activate Location"

    // Button color that changes based on if the location permissions are granted
    val buttonColor = if (isLocationPermissionGranted) Green else Red

    // Value that stores the action that takes place based on the state of parameters
    // Either:
    // A. Show the locationActivated Dialog
    // B. Show the locationDenied Dialog
    // C. Submit a request to get current location from the user in the app
    val onClickAction = {
        when {
            isLocationPermissionGranted -> {
                // Show dialog indicating location is already activated
                onShowLocationActivatedDialog()
            }
            permissionDenied -> {
                // Instead of opening app settings directly, show dialog to explain the need to enable permissions in settings
                onShowPermissionsDeniedDialog()
            }
            else -> {
                // Request permission if not previously denied and not granted
                onPermissionRequest()
            }
        }
    }

    // Format of the button
    OutlinedButton(
        onClick = onClickAction,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = buttonColor),
        border = BorderStroke(2.dp, buttonColor),
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .height(50.dp)
            .width(100.dp),
        shape = RoundedCornerShape(20),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(text = buttonText, color = buttonColor, textAlign = TextAlign.Center)
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