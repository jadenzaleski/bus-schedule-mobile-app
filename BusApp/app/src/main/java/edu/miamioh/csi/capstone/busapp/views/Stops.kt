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
import androidx.compose.runtime.rememberCoroutineScope
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

@Composable
fun StopsView() {
    val stops = CSVHandler.getStops() // Assuming this fetches stops correctly
    val context = LocalContext.current
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Permissions
    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Initial camera position
    val initialPosition = LatLng(38.9048, -77.0342) // Updated to a more central location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
    }

    // Dropdown menu states
    val cities = listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Test3", "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Test4")
    val selectedCities = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Dropdown menu for selecting cities
        Text(
            text = "Select Cities",
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp), // Increase padding as needed
            fontSize = 18.sp // Increase font size as needed
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city) },
                    onClick = {
                        expanded = false
                        selectedCities.add(city)
                        // Update map based on selected cities, you can implement the logic to move camera or add markers
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = city in selectedCities,
                            onCheckedChange = { checked ->
                                if (checked) selectedCities.add(city) else selectedCities.remove(city)
                            }
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
            // Display markers based on selected cities or other criteria
            stops.take(300).forEach { stop ->
                Marker(
                    state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                    title = "Stop",
                    snippet = "Latitude: ${stop.stopLat}, Longitude: ${stop.stopLon}"
                )
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