package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.MainViewModel
import edu.miamioh.csi.capstone.busapp.R
import edu.miamioh.csi.capstone.busapp.backend.CSVHandler
import edu.miamioh.csi.capstone.busapp.backend.GeneratedRoute
import edu.miamioh.csi.capstone.busapp.backend.RouteFinder
import edu.miamioh.csi.capstone.busapp.backend.StopOnRoute
import edu.miamioh.csi.capstone.busapp.ui.theme.Black
import edu.miamioh.csi.capstone.busapp.ui.theme.Blue
import edu.miamioh.csi.capstone.busapp.ui.theme.Gray200
import edu.miamioh.csi.capstone.busapp.ui.theme.Gray300
import edu.miamioh.csi.capstone.busapp.ui.theme.Gray700
import edu.miamioh.csi.capstone.busapp.ui.theme.Green
import edu.miamioh.csi.capstone.busapp.ui.theme.Light
import edu.miamioh.csi.capstone.busapp.ui.theme.Red
import edu.miamioh.csi.capstone.busapp.ui.theme.White
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


// A Generic struct for places on the map
data class Place(
    var name: String,
    var lat: Double,
    var lon: Double,
    var address: String,
    var iconURL: String
)

// a point that is used by googles api Snap to Roads
data class SnappedPoint(val latitude: Double, val longitude: Double)

// user location
var userLon = 0.0
var userLat = 0.0

// global variables to store the route information
val blank: List<StopOnRoute> = emptyList()
var currentRoute = GeneratedRoute(-1, "", blank, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)
var listOfRoutes: List<GeneratedRoute> = emptyList()
val snappedPointsList = mutableListOf<SnappedPoint>()

/*
Main Route Composable that hold the map and modular form below it.
This also gets passed in variables from the routes page.
 */
@Composable
fun RouteView(viewModel: MainViewModel, option: String, name: String, lat: Double, lon: Double) {
    val snappedPointsReady = remember { mutableStateOf(false) }
    // before we can load the screen the app needs to have the updated data, so we check for that
    val isDataInitialized by viewModel.isDataInitialized.collectAsState()
    // if the data is ready to go show everything
    if (isDataInitialized) {
        val context = LocalContext.current
        // csv variables
        val stops = CSVHandler.getStops()
        val routes = CSVHandler.getRoutes()
        val trips = CSVHandler.getTrips()
        val stopTimes = CSVHandler.getStopTimes()
        val agencies = CSVHandler.getAgencies()
        // Check if location permission is granted
        var isLocationPermissionGranted by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        // A client for location services.
        val fusedLocationClient =
            remember { LocationServices.getFusedLocationProviderClient(context) }
        // if the location is granted lets run our function to set the global variables right away
        if (isLocationPermissionGranted)
            getLastLocation(fusedLocationClient)
        // run this when the page is launched,
        // we are checking again for permissions
        LaunchedEffect(key1 = context) {
            isLocationPermissionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        /*
         * Sets up initial map settings for when user first loads up the Routes page upon opening app.
         * This includes:
         * The initial starting coordinates to center upon when the map is first loaded
         * The initial zoom level of the map
         * The center of the map
         */
        val initialPosition = LatLng(39.2, 16.25) // Cosenza
        var mapCenter by remember { mutableStateOf(initialPosition) }
        val currentZoomLevel by remember { mutableStateOf(9f) }
        // define where we want the camera to be
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialPosition, currentZoomLevel)
        }
        // Agencies, which we ended up not needing
        val defaultAgencyName = agencies.firstOrNull()?.agencyName ?: ""
        val selectedAgencyNames = remember { mutableStateListOf(defaultAgencyName) }
        // Prepare the mapping of stop IDs to agency IDs
        val stopIdToAgencyIdMap = remember {
            CSVHandler.getStopIdToAgencyIdMap(stops, routes, trips, stopTimes)
        }
        // List of selected agencies
        val selectedAgencyIds = remember {
            derivedStateOf {
                agencies.filter { it.agencyName in selectedAgencyNames }.map { it.agencyID }.toSet()
            }
        }.value
        // Map interaction tracking
        trackMapInteraction(cameraPositionState) { _, center ->
            mapCenter = center
        }
        // Allow for clearing of keyboard
        val focusManager = LocalFocusManager.current
        // boolean for weather or not we show the form
        val showForm = remember { mutableStateOf(true) }
        // boolean for weather or not we show the list which is after the form
        val showList = remember { mutableStateOf(false) }

        // +++ FORM +++
        // set initial date in form to current time
        var selectedTime by remember {
            mutableStateOf(
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Calendar.getInstance().time)
            )
        }
        // has the user selected there location as the starting point?
        val startIsCurrentLocation = remember { mutableStateOf(false) }
        // has the user selected there location as the ending point?
        val endIsCurrentLocation = remember { mutableStateOf(false) }
        // start string to search for
        var startSearchString by rememberSaveable { mutableStateOf("") }
        // end string to search for
        var endSearchString by rememberSaveable { mutableStateOf("") }
        // boolean to show the dropdown for results of start
        var startSearchResultsExpanded by remember { mutableStateOf(false) }
        // boolean to show the dropdown for results of end
        var endSearchResultsExpanded by remember { mutableStateOf(false) }
        // list of places that was found by the API call
        val searchResults = remember { mutableStateListOf(Place("", 0.0, 0.0, "", "")) }
        // our place we selected to start at
        val selectedStartPlace = remember { mutableStateOf(Place("", 0.0, 0.0, "", "")) }
        // our place we selected to end at
        var selectedEndPlace = remember { mutableStateOf(Place("", 0.0, 0.0, "", "")) }
        // boolen to show an alert
        val openAlertDialog = remember { mutableStateOf(false) }
        // what type of alert are we showing, for now there is only one type, 0
        var dialogType = remember { mutableIntStateOf(-1) }
        // when openAlertDialog = true, show the custom alert
        when {
            openAlertDialog.value -> {
                CustomAlert(dialogType.intValue,
                    onConfirmation = { openAlertDialog.value = false },
                    onDismissRequest = { openAlertDialog.value = false })
            }
        }

        // this doesn't work but it does not make the app unusable
        LaunchedEffect(key1 = option) {
                // set the passed in params to the correct vars
                when (option) {
                    "start" -> {
                        startSearchString = URLDecoder.decode(name, "UTF-8")
                        selectedStartPlace.value = Place(URLDecoder.decode(name, "UTF-8"), lat, lon, "", "")
                    }
                    "end" -> {
                        endSearchString = URLDecoder.decode(name, "UTF-8")
                        selectedEndPlace.value = Place(URLDecoder.decode(name, "UTF-8"), lat, lon, "", "")
                    }
                }
        }

        // boolean to make sure the the starting and ending places arent blank
        val valid =
            (startIsCurrentLocation.value || (selectedStartPlace.value.lat > 0 && selectedStartPlace.value.lon > 0))
                    &&
                    (endIsCurrentLocation.value || (selectedEndPlace.value.lat > 0 && selectedEndPlace.value.lon > 0))
                    &&
                    selectedAgencyIds.isNotEmpty()


        // this function take in the points on the route and does its best to return a list
// of points that we can draw a polyline with.
        @OptIn(DelicateCoroutinesApi::class)
        fun googleSnapToRoads(places: List<StopOnRoute>) {
            // clear the list
            snappedPointsList.clear()
            snappedPointsReady.value = false

            GlobalScope.launch(Dispatchers.IO) {
                // TODO: AYO api key also here
                val apiKey = "AIzaSyArxmzr9k53luII5xTXHT98rCV2dWEZU_E"
                // max points allowed for the api
                val maxPoints = 100
                // maximum distance between each point
                val maxDistance = 0.2
                // Step 1: Pre-process places to insert midpoints where necessary
                // This adds midpoints to a list when the distance between two points is greater then the
                // defined max distance. we have to do this because google does not snap well when points are
                // farther then 0.3 km.
                val processedPlaces = mutableListOf<StopOnRoute>()
                for (i in places.indices) {
                    processedPlaces.add(places[i])
                    if (i < places.size - 1) {
                        var current = places[i]
                        val next = places[i + 1]
                        var distance = calculateSphericalDistance(
                            current.stopLat,
                            current.stopLon,
                            next.stopLat,
                            next.stopLon
                        )
                        while (distance > maxDistance) {
                            val midPoint = midpoint(
                                current.stopLat,
                                current.stopLon,
                                next.stopLat,
                                next.stopLon
                            )
                            processedPlaces.add(midPoint)
                            current = midPoint
                            distance = calculateSphericalDistance(
                                current.stopLat,
                                current.stopLon,
                                next.stopLat,
                                next.stopLon
                            )
                        }
                    }
                }

                // Step 2: Divide the processed list into batches and process each batch
                // for each batch of maxpoints we call Snap to Roads API and add all the return coords to a list
                // that list will then be used by the Polyline in the GoogleMaps Composable to create the route line.
                processedPlaces.chunked(maxPoints).forEach { batch ->
                    try {
                        val path = batch.joinToString("|") { "${it.stopLat},${it.stopLon}" }
                        val url =
                            "https://roads.googleapis.com/v1/snapToRoads?interpolate=true&path=$path&key=$apiKey"
                        URL(url).openStream().use { input ->
                            val response = input.bufferedReader().use(BufferedReader::readText)
                            val jsonObject = JSONObject(response)
                            val snappedPoints = jsonObject.getJSONArray("snappedPoints")
                            for (i in 0 until snappedPoints.length()) {
                                val location =
                                    snappedPoints.getJSONObject(i).getJSONObject("location")
                                val latitude = location.getDouble("latitude")
                                val longitude = location.getDouble("longitude")
                                snappedPointsList.add(SnappedPoint(latitude, longitude))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                withContext(Dispatchers.Main) {
                    snappedPointsReady.value = true  // Set ready state to true after loading
                }
            }
        }

        // Search function calls google API to find 20 results that match the users query.
        @OptIn(DelicateCoroutinesApi::class)
        fun googleSearch(query: String) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    //TODO: AYO we need to move the key out of this file, should be in local.properties
                    val key = "&key=AIzaSyArxmzr9k53luII5xTXHT98rCV2dWEZU_E"
                    // Search from the center of the map
                    val location = "&location=" + mapCenter.latitude + "%2C" + mapCenter.longitude
                    val url =
                        "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + location + key + "&rankby=distance"
                    // attempt to open a connection
                    val connection = URL(url).openConnection()
                    // read it all and put it into json
                    val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
                    val jsonData = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        jsonData.append(line)
                    }
                    reader.close()
                    val jsonObject = JSONObject(jsonData.toString())
                    // and them to an array and go through the json pulling out the necessary values
                    // then adding them to a list Compose can use.
                    val resultsArray = jsonObject.getJSONArray("results")
                    // before we write to the array we must clear it from the previous time
                    searchResults.clear()
                    for (i in 0 until resultsArray.length()) {
                        val resultObj = resultsArray.getJSONObject(i)
                        val name = resultObj.getString("name")
                        val formattedAddress = resultObj.getString("formatted_address")
                        val iconURL = resultObj.getString("icon")
                        val geometryObj = resultObj.getJSONObject("geometry")
                        val locationObj = geometryObj.getJSONObject("location")
                        val lat = locationObj.getDouble("lat")
                        val lon = locationObj.getDouble("lng")
                        searchResults.add(Place(name, lat, lon, formattedAddress, iconURL))
                        // sort by distance from the user
                        searchResults.sortBy { x ->
                            calculateSphericalDistance(userLat, userLon, x.lat, x.lon)
                        }
                    }
                } catch (e: IOException) {
                    Log.i("Error", "Error occurred: ${e.message}")
                } catch (e: JSONException) {
                    Log.i("Error", "Error occurred while parsing JSON: ${e.message}")
                }
            }
        }

        // Column that holds the map
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }) {
            // main google map composable
            GoogleMap(
                modifier = Modifier.fillMaxHeight(if (showForm.value) 0.72f else if (showList.value) 0.55f else 0.6f),
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
                // if the user has tapped through the form and list draw a line of points created
                // by Snap to Roads to create the line of the specific route.
                if (!showForm.value && !showList.value && snappedPointsReady.value) {
                    Polyline(
                        points = snappedPointsList.map { LatLng(it.latitude, it.longitude) },
                        width = 15f,
                        color = Blue
                    )
                    Log.i("SNAPPED", "Snapped a total of ${snappedPointsList.size} points.")
                    // for all stops plot a marker
                    for (i in currentRoute.routeInfo.indices) {
                        val stop = currentRoute.routeInfo[i]
                        if (i == 0 || (i == currentRoute.routeInfo.size - 1)) {
                            MarkerInfoWindowContent(
                                state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                                icon = if (i == 0) {
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                } else {
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                },
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    if (i == 0) {
                                        Text(
                                            text = "Start",
                                            modifier = Modifier.padding(top = 5.dp),
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Green
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "End",
                                            modifier = Modifier.padding(top = 5.dp),
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Red
                                            )
                                        )
                                    }
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
                                    Text(text = "Stop ID: ${stop.stopID}")
                                }
                            }
                        } else {
                            // TODO: Remove Marker below when done verifying routes
                            Marker(
                                state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                                title = stop.stopName
                            )
                        }
                    }
                    // next lets move the camera to the starting stop
                    LaunchedEffect(key1 = snappedPointsList) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition(
                                    LatLng(
                                        currentRoute.routeInfo.first().stopLat,
                                        currentRoute.routeInfo.first().stopLon
                                    ),
                                    14f, // Zoom level
                                    0f,  // Tilt angle
                                    0f   // Bearing
                                )
                            )
                        )
                    }
                }
            }
            // we start by showing the form for the user to enter
            if (showForm.value) {
                // Column to hold the Form.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp)
                        .background(Light), verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    // Row that holds the FROM: location button and text-field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "From:",
                            style = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        // Using the custom CurrentLocationButton
                        CurrentLocationButton(
                            isCurrentLocation = startIsCurrentLocation,
                            context = context,
                            focusManager = focusManager,
                            onLocationEnabled = {
                                if (isLocationPermissionGranted)
                                    getLastLocation(fusedLocationClient);
                            },
                            onLocationDisabled = {
                                // Define what happens when location is disabled.
                                // nothing
                            }
                        )
                        // search box for start
                        OutlinedTextField(
                            value = startSearchString,
                            enabled = startIsCurrentLocation.value.not(),
                            onValueChange = { startSearchString = it },
                            keyboardActions = KeyboardActions(
                                onDone = {


                                    startSearchResultsExpanded = startSearchResultsExpanded.not()
                                    googleSearch(startSearchString)
                                    focusManager.clearFocus()
                                },
                            ),
                            placeholder = {
                                Text(
                                    text = if (startIsCurrentLocation.value.not()) "Search Anything..." else "Current Location.",
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    color = Color.Gray
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(start = 10.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = Color.Gray,
                                focusedIndicatorColor = Color.Gray,
                                focusedTextColor = Black,
                                focusedLabelColor = Color.DarkGray,
                                unfocusedLabelColor = Color.DarkGray,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                cursorColor = Color.Gray,
                                disabledContainerColor = Gray300,
                            ),
                            shape = RoundedCornerShape(20),
                            trailingIcon = {
                                // search button
                                OutlinedButton(
                                    onClick = {
                                        startSearchResultsExpanded =
                                            !startSearchResultsExpanded
                                        googleSearch(startSearchString)
                                        focusManager.clearFocus()
                                    },
                                    enabled = startIsCurrentLocation.value.not(),
                                    colors = ButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Green,
                                        disabledContainerColor = Color.Transparent,
                                        disabledContentColor = Color.Gray
                                    ),
                                    border = BorderStroke(0.dp, Color.Transparent),
                                    contentPadding = PaddingValues(horizontal = 1.dp),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier
                                    )
                                }
                            }
                        )

                        //Dropdown menu for start search results
                        DropdownMenu(
                            expanded = startSearchResultsExpanded,
                            onDismissRequest = {
                                startSearchResultsExpanded = !startSearchResultsExpanded
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f)
                        ) {
                            // for all the search results
                            searchResults.forEach { result ->
                                DropdownMenuItem(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    text = {
                                        val dist = calculateSphericalDistance(
                                            userLat,
                                            userLon,
                                            result.lat,
                                            result.lon
                                        )
                                        Column {
                                            Text(result.name)
                                            Text(
                                                "${String.format("%.3f", dist)}km",
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                result.address,
                                                fontWeight = FontWeight.Light,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedStartPlace.value = result
                                        startSearchResultsExpanded = !startSearchResultsExpanded
                                        startSearchString = selectedStartPlace.value.name
                                    },
                                    leadingIcon = {
                                        AsyncImage(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(24.dp),
                                            model = result.iconURL,
                                            contentDescription = "Place Image",
                                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                                setToSaturation(0f) // grayscale
                                            })
                                        )
                                    }
                                )
                            }
                        }

                    }
                    // Row that holds the TO: location button and text-field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "To:  ",
                            style = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        // current location button
                        // Using the custom CurrentLocationButton
                        CurrentLocationButton(
                            isCurrentLocation = endIsCurrentLocation,
                            context = context,
                            focusManager = focusManager,
                            onLocationEnabled = {
                                if (isLocationPermissionGranted)
                                    getLastLocation(fusedLocationClient)
                            },
                            onLocationDisabled = {
                                // Define what happens when location is disabled.
                                // nothing
                            }
                        )
                        // search text box
                        OutlinedTextField(
                            value = endSearchString,
                            onValueChange = { endSearchString = it },
                            enabled = endIsCurrentLocation.value.not(),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    endSearchResultsExpanded = !endSearchResultsExpanded
                                    googleSearch(endSearchString)
                                    focusManager.clearFocus()
                                },
                            ),
                            placeholder = {
                                Text(
                                    text = if (endIsCurrentLocation.value.not()) "Search Anything..." else "Current Location.",
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    color = Color.Gray
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(start = 10.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = Color.Gray,
                                focusedIndicatorColor = Color.Gray,
                                focusedTextColor = Black,
                                focusedLabelColor = Color.DarkGray,
                                unfocusedLabelColor = Color.DarkGray,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                cursorColor = Color.Gray,
                                disabledContainerColor = Gray300
                            ),
                            shape = RoundedCornerShape(20),
                            trailingIcon = {
                                // search button
                                OutlinedButton(
                                    onClick = {
                                        endSearchResultsExpanded =
                                            !endSearchResultsExpanded
                                        googleSearch(endSearchString)
                                        focusManager.clearFocus()
                                    },
                                    enabled = endIsCurrentLocation.value.not(),
                                    colors = ButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Green,
                                        disabledContainerColor = Color.Transparent,
                                        disabledContentColor = Color.Gray
                                    ),
                                    border = BorderStroke(0.dp, Color.Transparent),
                                    contentPadding = PaddingValues(horizontal = 1.dp),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier
                                    )
                                }
                            },
                        )


                        //Dropdown menu for end search Results
                        DropdownMenu(
                            expanded = endSearchResultsExpanded,
                            onDismissRequest = {
                                endSearchResultsExpanded = !endSearchResultsExpanded
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f)
                            ) {
                            // for all the search results
                            searchResults.forEach { result ->
                                DropdownMenuItem(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    text = {
                                        val dist = calculateSphericalDistance(
                                            userLat,
                                            userLon,
                                            result.lat,
                                            result.lon
                                        )
                                        Column {
                                            Text(result.name)
                                            Text(
                                                "${String.format("%.3f", dist)}km",
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                result.address,
                                                fontWeight = FontWeight.Light,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedEndPlace.value = result
                                        endSearchResultsExpanded = !endSearchResultsExpanded
                                        endSearchString = selectedEndPlace.value.name
                                    },
                                    leadingIcon = {
                                        AsyncImage(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height(24.dp),
                                            model = result.iconURL,
                                            contentDescription = "Place Image",
                                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                                setToSaturation(0f) // grayscale
                                            })
                                        )
                                    }
                                )
                            }
                        }
                    }
                    // this row hold the time selector and GO button
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // time selector
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
                                .height(50.dp)
                                .width(120.dp),
                            shape = RoundedCornerShape(20)
                        ) {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(text = selectedTime)
                        }

                        // GO button
                        OutlinedButton(
                            enabled = if (valid) true else false,
                            onClick = {
                                // update last location if
                                if (isLocationPermissionGranted)
                                    getLastLocation(fusedLocationClient);
                                // get the start and stop place object if its current location
                                val start: Place = if (startIsCurrentLocation.value) {
                                    Place("Current Location", userLat, userLon, "", "")
                                } else {
                                    selectedStartPlace.value
                                }
                                val stop: Place = if (endIsCurrentLocation.value) {
                                    Place("Current Location", userLat, userLon, "", "")
                                } else {
                                    selectedEndPlace.value
                                }

                                // CALCULATE ROUTE!
                                calcRoute(
                                    start = start,
                                    end = stop,
                                    time = selectedTime,
                                    allowedAgencies = selectedAgencyIds
                                )
                                if (listOfRoutes.isEmpty()) {
                                    // no route
                                    Log.e(
                                        "ROUTE",
                                        "Attempt to display route failed because the routeInfo is empty or the listOfRoutes is empty."
                                    )
                                    dialogType.intValue = 0
                                    openAlertDialog.value = true
                                } else {
                                    // valid route
                                    showForm.value = false
                                    showList.value = true
                                }
                            },
                            colors = ButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Green,
                                disabledContainerColor = Gray300,
                                disabledContentColor = Color.Gray
                            ),
                            border = BorderStroke(2.dp, if (valid) Green else Color.Gray),
                            modifier = Modifier
                                .height(50.dp)
                                .fillMaxWidth()
                                .padding(start = 10.dp),
                            shape = RoundedCornerShape(20)
                        ) {
                            Text(text = "Go")
                        }
                    }
                }
            } else if (showList.value) {
                // show the list of routes in this custom composable
                RoutesListView(showForm, showList, snappedPointsReady, { info -> googleSnapToRoads(info) })
            } else {
                // if both of the booleans are false we show the detailed view of the current route
                SpecificRouteView(currentRoute, showList)
            }
        }
    }
}

// this function gets the last location and saves it to the two global vars
@SuppressLint("MissingPermission")
private fun getLastLocation(fusedLocationClient: FusedLocationProviderClient) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            // Got last known location. In some rare situations, this can be null.
            location?.let {
                userLat = location.latitude
                userLon = location.longitude
                Log.i("LOCATION", "LAT: $userLat, LON: $userLon")
                // Use latitude and longitude here
            }
        }
        .addOnFailureListener { e ->
            Log.e("LOCATION", "Error getting location in getLastLocation")
        }
}

// Route is calculated here
fun calcRoute(start: Place, end: Place, time: String, allowedAgencies: Set<Int>) {
    val logMessage = "Start: ${start.name}, Lat: ${start.lat}, Lon: ${start.lon}, " +
            "Stop: ${end.name}, Lat: ${end.lat}, Lon: ${end.lon}, " +
            "Time: $time, Allowed Agencies: $allowedAgencies"
    Log.i("calcRoute", logMessage)
    // create the list of routes generate by the route workhorse
    listOfRoutes = RouteFinder.routeWorkhorse(start, end, time, allowedAgencies)
}

// Function to calculate the midpoint between two points
fun midpoint(lat1: Double, lon1: Double, lat2: Double, lon2: Double): StopOnRoute {
    val dLon = Math.toRadians(lon2 - lon1)

    // Convert to radians
    val rlat1 = Math.toRadians(lat1)
    val rlat2 = Math.toRadians(lat2)
    val rlon1 = Math.toRadians(lon1)

    val bx = cos(rlat2) * cos(dLon)
    val by = cos(rlat2) * sin(dLon)
    val latMid =
        atan2(sin(rlat1) + sin(rlat2), sqrt((cos(rlat1) + bx) * (cos(rlat1) + bx) + by * by))
    val lonMid = rlon1 + atan2(by, cos(rlat1) + bx)

    // Convert back to degrees
    return StopOnRoute(
        -1,
        "DUMMY",
        Math.toDegrees(latMid),
        Math.toDegrees(lonMid),
        LocalTime.MIDNIGHT,
        LocalTime.MIDNIGHT,
        -1
    )
}

fun getTimeUntil(startTime: LocalTime): String {
    val now = LocalTime.now()

    // Check if the startTime has already passed today
    if (startTime.isBefore(now)) {
        // Calculate duration to midnight plus duration from midnight to startTime
        val durationToMidnight = Duration.between(now, LocalTime.MAX)
        val durationFromMidnightToStartTime = Duration.between(LocalTime.MIN, startTime)
        val totalDuration = durationToMidnight.plus(durationFromMidnightToStartTime)

        val hours = totalDuration.toHours()
        val minutes = totalDuration.toMinutes() % 60
        return "${hours}h ${minutes}m"
    } else {
        // StartTime is still upcoming today
        val duration = Duration.between(now, startTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return "${hours}h ${minutes}m"
    }
}

// a custom alert dialog
@Composable
fun CustomAlert(size: Int, onDismissRequest: () -> Unit, onConfirmation: () -> Unit) {
    // Could add more conditions to this composable to make it easier to call more dialogs
    // but for now we only have one.
    if (size == 0) {
        // no Route
        AlertDialog(
            confirmButton = {
                TextButton(onClick = { onConfirmation() }) {
                    Text(text = "Okay", color = Blue)
                }
            },
            onDismissRequest = { onDismissRequest() },
            dismissButton = {},
            icon = { Icon(Icons.Default.Clear, "", tint = Red) },
            title = { Text(text = "No Available Route") },
            text = {
                Text(
                    text = "A route can not be generated due to one of the following:\n\n" +
                            "- Your start and end points are the same.\n" +
                            "- There is no upcoming route for your desired starting and ending point.\n" +
                            "- It is faster to walk to your destination."
                )
            },
        )
    }
}

// custom button for the current location buttons on the form
@Composable
fun CurrentLocationButton(
    isCurrentLocation: MutableState<Boolean>,
    context: Context,
    focusManager: FocusManager,
    onLocationEnabled: () -> Unit,
    onLocationDisabled: () -> Unit  // Callback when location is deselected
) {
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            isCurrentLocation.value = true
            onLocationEnabled()  // Call when permission is granted and location is enabled
        } else {
            // Show permission denied dialog only if permission was previously requested
            showPermissionDeniedDialog = true
        }
    }
    // Check permission status
    val isLocationPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Button UI and click logic
    OutlinedButton(
        onClick = {
            if (isCurrentLocation.value) {
                // If currently selected, deselect and run the onLocationDisabled callback
                isCurrentLocation.value = false
                onLocationDisabled()
            } else if (!isLocationPermissionGranted) {
                // If permission not granted, request it
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                // If permission is granted but location not currently selected, select it
                isCurrentLocation.value = true
                onLocationEnabled()
            }
            focusManager.clearFocus()  // Dismiss keyboard if open
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCurrentLocation.value) Green else Color.Transparent,
            contentColor = if (isCurrentLocation.value) Color.White else Color.Gray
        ),
        border = BorderStroke(1.dp, if (isCurrentLocation.value) Green else Color.Gray),
        modifier = Modifier
            .height(50.dp)
            .width(50.dp),
        shape = RoundedCornerShape(20),
        contentPadding = PaddingValues(horizontal = 1.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_my_location_24),
            contentDescription = "Toggle Current Location",
            tint = if (isCurrentLocation.value) Color.White else Color.Gray
        )
    }

    // Permission Denied Dialog
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
}

// The list view of routes after you tap go
@Composable
fun RoutesListView(
    showForm: MutableState<Boolean>,
    showList: MutableState<Boolean>,
    snappedPointsReady: MutableState<Boolean>,
    googleSnapToRoads: (List<StopOnRoute>) -> Unit
) {
    Log.i("LV", "${listOfRoutes.size}" + " routes to display.")
    // Scaffold is an easy way to add a header and floating button
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // header
            Column(verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier
                            .height(28.dp)
                            .fillMaxWidth()
                            .background(Gray200),
                        textAlign = TextAlign.Center,
                        text = " ${listOfRoutes.size} possible route(s)",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // close button
            FloatingActionButton(
                modifier = Modifier.padding(0.dp),
                shape = CircleShape,
                contentColor = Red,
                containerColor = Gray200,
                onClick = {
                    showForm.value = true
                    showList.value = false
                }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
        },
        // Change this to move the X around
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        // our list of possible routes to tap on
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxHeight(0.8f)
                .padding(innerPadding)
        ) {
            listOfRoutes.forEach { route ->
                ListItem(
                    route = route,
                    showList = showList,
                    snappedPointsReady = snappedPointsReady,
                    googleSnapToRoads = { routeInfo -> googleSnapToRoads(routeInfo) }
                )
                HorizontalDivider(
                    thickness = 3.dp,
                    modifier = Modifier.padding(vertical = 3.dp),
                    color = Gray200
                )
            }
        }
    }
}

// Each of the Routes in the List has a ListItem
// this is all just simple text formatting and composable layouts.
@Composable
fun ListItem(
    route: GeneratedRoute,
    showList: MutableState<Boolean>,
    snappedPointsReady: MutableState<Boolean>,
    googleSnapToRoads: (List<StopOnRoute>) -> Unit  // passing the function as a parameter
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(3.dp)
                .background(Light),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_directions_bus_24),
                    contentDescription = "Bus icon"
                )
                Text(
                    text = route.routeShortName + ", ${route.routeStartTime}" + ", ${route.routeInfo.size} stop(s)",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,

                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 3.dp)
            ) {
                // leaves in
                Text(text = "Leaves in: ", fontSize = 16.sp)
                Text(
                    text = getTimeUntil(route.routeStartTime),
                    fontSize = 16.sp,
                    color = Blue,
                    fontWeight = FontWeight.Bold
                )
                // TODO: DAN verify is this the correct calculation? or close enough?
                val distance = calculateSphericalDistance(
                    route.routeInfo.first().stopLat,
                    route.routeInfo.first().stopLon,
                    userLat,
                    userLon
                )
                Text(text = ", (${String.format("%.3f", distance)}km away)", fontSize = 16.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 3.dp)
            ) {
                val duration = Duration.between(route.routeStartTime, route.routeEndTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutes() % 60
                val formattedTime = "${hours}h ${minutes}m"
                Text(
                    text = formattedTime,
                    fontSize = 16.sp,
                    color = Blue,
                    fontWeight = FontWeight.Bold
                )
                Text(" ride, ${route.routeStartTime} - ${route.routeEndTime}", fontSize = 16.sp)
            }
        }
        // green go button
        OutlinedButton(
            onClick = {
                currentRoute = route
                showList.value = false
                snappedPointsReady.value = false
                googleSnapToRoads(route.routeInfo)

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Green,
                contentColor = White
            ),
            border = BorderStroke(1.dp, Green),
            modifier = Modifier
                .height(60.dp),
            shape = RoundedCornerShape(20)
        ) {
            Text(text = "GO")
        }
    }
}

// when the user taps go this compoasble shows,
// again, just simple layouts and text formatting
@Composable
fun SpecificRouteView(route: GeneratedRoute, showList: MutableState<Boolean>) {

    Column(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.scale(1.25f),
                painter = painterResource(id = R.drawable.baseline_directions_bus_24),
                contentDescription = "Bus icon"
            )
            Text(
                text = route.routeShortName + ", ${route.routeStartTime}" + ", ${route.routeInfo.size} stop(s)",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,

                    )
            )
        }
        HorizontalDivider(
            thickness = 3.dp,
            modifier = Modifier.padding(vertical = 3.dp),
            color = Gray200
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Leaves in: ", fontSize = 18.sp)
            Text(text = getTimeUntil(route.routeStartTime), fontSize = 18.sp, color = Blue, fontWeight = FontWeight.Bold)
            // TODO: DAN verify is this the correct calculation? or close enough? thanks
            val distance = calculateSphericalDistance(
                route.routeInfo.first().stopLat,
                route.routeInfo.first().stopLon,
                userLat,
                userLon
            )
            Text(text = ", (${String.format("%.3f", distance)}km away)", fontSize = 18.sp)
        }

        Row {
            Text(text = "Start: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Green)
            Text(
                text = route.routeInfo.first().stopName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row {
            val duration = Duration.between(route.routeStartTime, route.routeEndTime)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            val formattedTime = "${hours}h ${minutes}m"
            Text(text = "Ride for ", fontSize = 18.sp)
            Text(text = formattedTime, fontSize = 18.sp, color = Blue, fontWeight = FontWeight.Bold)
            Text(text = ", (${route.routeStartTime} - ${route.routeEndTime})", fontSize = 18.sp)
        }

        Row {
            Text(text = "End: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Red)
            Text(
                text = route.routeInfo.last().stopName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row {
            if (route.routeInfo.size >= 3) {
                Text(
                    text = "(After ${route.routeInfo[route.routeInfo.size - 2].stopName})",

                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = Gray700

                )
            }
        }
        // cancel button
        OutlinedButton(
            onClick = {
                showList.value = !showList.value
            },
            colors = ButtonColors(
                containerColor = Color.Transparent,
                contentColor = Red,
                disabledContainerColor = Gray300,
                disabledContentColor = Color.Gray
            ),
            border = BorderStroke(2.dp, Red),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20)
        ) {
            Text(text = "Cancel")
        }
    }
}

/*
@Composable
@Preview
fun RouteViewPreview() {
    RouteView()
}
*/
