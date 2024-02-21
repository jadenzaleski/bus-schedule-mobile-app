package edu.miamioh.csi.capstone.busapp.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.miamioh.csi.capstone.busapp.CSVHandler

//@Composable
//fun StopsView() {
//    val singapore = LatLng(1.35, 103.87)
//    val singaporeState = MarkerState(position = singapore)
//    val sydney = LatLng(-33.867, 151.207) // Assuming you have a Sydney LatLng defined somewhere
//    val sydneyState = MarkerState(position = sydney)
//
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(singapore, 10f)
//    }
//
//    GoogleMap(
//        modifier = Modifier.fillMaxSize(),
//        cameraPositionState = cameraPositionState
//    ) {
//        Marker(
//            state = singaporeState,
//            title = "Marker in Singapore",
//        )
//        Marker(
//            state = sydneyState,
//            title = "Marker in Sydney"
//        )
//    }
//}
@Composable
fun StopsView() {
    val stops = CSVHandler.getStops()

    // Initial camera position setup; adjust as necessary
    val initialPosition = LatLng(38.9048, 16.5952) // Center point or first stop as initial position
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 9f) // Adjust zoom level as needed
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Use a for loop to iterate over the stops, stopping at 50 or the list size, whichever is smaller
        val stopCount = minOf(stops.size, 50) // Determines the number of stops to iterate over
        for (i in 0 until stopCount) {
            val stop = stops[i]
            val position = LatLng(stop.stopLat, stop.stopLon)
            Marker(
                state = MarkerState(position = position),
                title = "Stop", // You can customize this title based on stop details
                snippet = "Latitude: ${stop.stopLat}, Longitude: ${stop.stopLon}" // Optional: additional info
            )
        }
    }
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}