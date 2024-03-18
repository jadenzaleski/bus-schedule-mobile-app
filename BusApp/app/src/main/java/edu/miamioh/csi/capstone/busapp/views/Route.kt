package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.CSVHandler
import edu.miamioh.csi.capstone.busapp.R
import edu.miamioh.csi.capstone.busapp.navigation.Screens
import edu.miamioh.csi.capstone.busapp.ui.theme.Black
import edu.miamioh.csi.capstone.busapp.ui.theme.Gray400
import edu.miamioh.csi.capstone.busapp.ui.theme.Green
import edu.miamioh.csi.capstone.busapp.ui.theme.Light
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

@Composable
fun RouteView() {
    val stops = CSVHandler.getStops()
    val routes = CSVHandler.getRoutes()
    val trips = CSVHandler.getTrips()
    val stopTimes = CSVHandler.getStopTimes()
    val agencies = CSVHandler.getAgencies()
    val context = LocalContext.current
    val navController = rememberNavController()

    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    val currentZoomLevel by remember { mutableStateOf(9f) } // Initial zoom level

    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*
     * Sets up initial map settings for when user first loads up the Stops page upon opening app.
     * This includes:
     * The initial starting coordinates to center upon when the map is first loaded
     * The initial zoom level of the map
     */
    val initialPosition = LatLng(39.2, 16.25) // Cosenza
    var mapCenter by remember { mutableStateOf(initialPosition) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, currentZoomLevel)
    }

    val defaultAgencyName = agencies.firstOrNull()?.agencyName ?: ""
    val selectedAgencyNames = remember { mutableStateListOf(defaultAgencyName) }
    var expanded by remember { mutableStateOf(false) }

    // Prepare the mapping of stop IDs to agency IDs
    val stopIdToAgencyIdMap = remember {
        CSVHandler.getStopIdToAgencyIdMap(stops, routes, trips, stopTimes)
    }

    var selectedAgencyIds = remember {
        derivedStateOf {
            agencies.filter { it.agencyName in selectedAgencyNames }.map { it.agencyID }.toSet()
        }
    }.value

    var maxStopsInput by remember { mutableStateOf("50") }
    var maxStops by remember { mutableIntStateOf(100) }
    //Log.i("maxStopsBefore", "" + maxStops)

    // Map interaction tracking
    trackMapInteraction(cameraPositionState) { zoomLevel, center ->
        mapCenter = center
        //Log.i("maxStops", "" + maxStops)
        //maxStops = minOf(maxStops, calculateNumberOfMarkers(zoomLevel))
        //Log.i("# of Stops based on Zoom", "" + calculateNumberOfMarkers(zoomLevel))
        //Log.i("# of Stops Displayed", "" + maxStops)
    }

    // Dynamically calculate filtered stops based on current criteria
    val filteredStops = remember(mapCenter, selectedAgencyIds, maxStops) {
        Log.i("Agency Names", "" + selectedAgencyIds)
        Log.i("# of Stops Displayed", min(maxStops, 150).toString())
        maxStopsInput = min(maxStops, 150).toString()
        stops.filter { stop ->
            stopIdToAgencyIdMap[stop.stopId] in selectedAgencyIds &&
                    calculateDistance(
                        mapCenter.latitude,
                        mapCenter.longitude,
                        stop.stopLat,
                        stop.stopLon
                    ) <= 60
        }.sortedBy {
            calculateDistance(
                mapCenter.latitude,
                mapCenter.longitude,
                it.stopLat,
                it.stopLon
            )
        }
            .take(min(maxStops, 150))
    }

    val focusManager = LocalFocusManager.current
    // set initial date to current time
    var selectedTime by remember {
        mutableStateOf(
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Calendar.getInstance().time)
        )
    }
    var startID by remember { mutableStateOf("") }
    var stopID by remember { mutableStateOf("") }

    Column(modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }) {

        GoogleMap(
            modifier = Modifier.fillMaxHeight(0.7f),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = isLocationPermissionGranted,
                compassEnabled = true
            ),
            properties = MapProperties(
                isMyLocationEnabled = isLocationPermissionGranted,
                minZoomPreference = 5.0f
            )
        ) {
            filteredStops.forEach { stop ->
                // Using the custom MarkerInfoWindowContent instead of the standard Marker
                MarkerInfoWindowContent(
                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                    onInfoWindowClick = {
                        // put into start or stop
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
                            Spacer(modifier = Modifier.width(5.dp)) // Replaced VerticalDivider with Spacer for simplicity
                            Text(text = "Lon: ${stop.stopLon}")
                        }
                        Text(text = "Stop ID: ${stop.stopId}")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp)
                .background(Light), verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                OutlinedButton(
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Green,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.Gray
                    ),
                    onClick = {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context, { _, hour, minute ->
                                val time = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                }
                                selectedTime = SimpleDateFormat(
                                    "HH:mm",
                                    Locale.getDefault()
                                ).format(time.time)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    border = BorderStroke(2.dp, Green),
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(20)
                ) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(text = selectedTime)
                }

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = startID,
                    onValueChange = { startID = it.filter { char -> char.isDigit() } },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        },
                    ),
                    label = { Text("Start ID", style = TextStyle(fontSize = 15.sp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(150.dp),
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
                OutlinedTextField(
                    value = stopID,
                    onValueChange = { stopID = it.filter { char -> char.isDigit() } },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        },
                    ),
                    label = { Text("Stop ID", style = TextStyle(fontSize = 15.sp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(150.dp),
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
            }
            OutlinedButton(
                onClick = {
                    if (stopID != "" && startID != "")
                    // Calculate Route and show
                        calcAndDisplayRoute(
                            startID.toInt(),
                            stopID.toInt(),
                            selectedTime,
                            selectedAgencyIds.toList()
                        )
                },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Green,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.Gray
                ),
                border = BorderStroke(2.dp, Green),
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20)
            ) {
                Text(text = "Go")
            }

        }
    }

}

fun calcAndDisplayRoute(
    startID: Int,
    stopID: Int,
    arrivalTime: String,
    allowedAgencyIDs: List<Int>
) {
    Log.d(
        "calcAndDisplayRoute",
        "StartID: $startID, StopID: $stopID, Arrival Time: $arrivalTime, Allowed Agency IDs: ${allowedAgencyIDs}."
    )
    // find best route and call snap to roads which needs a list of lat an lon coords.
}

@Composable
@Preview
fun RouteViewPreview() {
    RouteView()
}