package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
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

    val initialPosition = LatLng(39.2, 16.25) // Cosenza
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 6f)
    }

    val defaultAgencyName = agencies.firstOrNull()?.agencyName ?: ""
    val selectedAgencyNames = remember { mutableStateListOf(defaultAgencyName) }
    var expanded by remember { mutableStateOf(false) }

    var maxStopsInput by remember { mutableStateOf("") }
    var maxStops by remember { mutableIntStateOf(50) }

    // Prepare the mapping of stop IDs to agency IDs
    val stopIdToAgencyIdMap = remember {
        CSVHandler.getStopIdToAgencyIdMap(stops, routes, trips, stopTimes)
    }
    val focusManager = LocalFocusManager.current

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

            OutlinedButton(
                onClick = { expanded = !expanded
                            focusManager.clearFocus()},
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


            OutlinedTextField(
                value = maxStopsInput,
                onValueChange = { maxStopsInput = it.filter { char -> char.isDigit() } },
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus()
                        maxStopsInput.toIntOrNull()?.let {
                            if (it >= 1) maxStops = it
                        }},
                ),
//                    label = { Text("Max Stops", style = TextStyle(fontSize = 10.sp)) },
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(0.65F),

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

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = isLocationPermissionGranted,
                compassEnabled = true
            ),
            properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
        ) {
            val selectedAgencyIds =
                agencies.filter { it.agencyName in selectedAgencyNames }.map { it.agencyID }.toSet()

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