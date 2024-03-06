package edu.miamioh.csi.capstone.busapp.views

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
    val stops = CSVHandler.getStops()
    val context = LocalContext.current
//    val uiSettings = remember {
//        MapUiSettings(myLocationButtonEnabled = true)
//    }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
//    var showPermissionRationale by remember { mutableStateOf(false) }

    // Prepare the permission launcher
//    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
//        if (isGranted) {
//            isLocationPermissionGranted = true
//        } else {
//            isLocationPermissionGranted = false
//        }
//    }

    // This block of code checks to see if the locationPermission has been granted beforehand
    LaunchedEffect(key1 = context) {
        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val initialPosition = LatLng(38.9048, 16.5952)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 9f)
    }

//    if (showPermissionRationale) {
//        // Show a dialog explaining why the permission is needed
//        AlertDialog(
//            onDismissRequest = { showPermissionRationale = false },
//            title = { Text("Location Permission Required") },
//            text = { Text("This feature requires location permissions to function. Please grant them.") },
//            confirmButton = {
//                Button(onClick = {
//                    showPermissionRationale = false
//                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//                }) {
//                    Text("OK")
//                }
//            },
//            dismissButton = {
//                Button(onClick = { showPermissionRationale = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(myLocationButtonEnabled = isLocationPermissionGranted, compassEnabled = true),
        properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted)
    ) {
        val stopCount = minOf(stops.size, 50)
        for (i in 0 until stopCount) {
            val stop = stops[i]
            Marker(
                state = MarkerState(position = LatLng(stop.stopLat, stop.stopLon)),
                title = "Stop",
                snippet = "Latitude: ${stop.stopLat}, Longitude: ${stop.stopLon}"
            )
        }
    }
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}