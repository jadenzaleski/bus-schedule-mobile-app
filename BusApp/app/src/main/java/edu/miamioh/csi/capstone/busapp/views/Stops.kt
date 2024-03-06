package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    val stops = CSVHandler.getStops()
    val context = LocalContext.current
    val navController = rememberNavController()
    var isLocationPermissionGranted by remember { mutableStateOf(false) }

    // This block of code checks to see if the locationPermission has been granted beforehand
    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val initialPosition = LatLng(38.9048, 16.5952)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted,
            compassEnabled = true,
            mapToolbarEnabled = true),
        properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
    ) {
        val stopCount = minOf(stops.size, 50)
        for (i in 0 until stopCount) {
            val stop = stops[i]
            val lat = String.format("%.4f", stop.stopLat)
            val lon = String.format("%.4f", stop.stopLon)

            MarkerInfoWindowContent(
                state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                onInfoWindowClick = {
                    // for some reason this is crashing. copied from AppNavigation.kt
                    navController.navigate(Screens.RouteScreen.name){
                        popUpTo(navController.graph.findStartDestination().id){
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
                    ))
                    HorizontalDivider( modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Lat: $lat")
                        VerticalDivider( modifier = Modifier.height(14.dp).padding(horizontal = 5.dp))
                        Text(text = "Lon: $lon")
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

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}