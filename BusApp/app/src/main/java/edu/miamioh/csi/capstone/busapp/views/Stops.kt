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

@Composable
fun StopsView() {
    val singapore = LatLng(1.35, 103.87)
    val singaporeState = MarkerState(position = singapore)
    val sydney = LatLng(-33.867, 151.207) // Assuming you have a Sydney LatLng defined somewhere
    val sydneyState = MarkerState(position = sydney)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = singaporeState,
            title = "Marker in Singapore",
        )
        Marker(
            state = sydneyState,
            title = "Marker in Sydney"
        )
    }
}
//@Composable
//fun StopsView() {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Blue),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = CSVHandler.getInfo().toString(),
//            fontWeight = FontWeight.Bold,
//            color = Color.White
//        )
//    }
//}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}