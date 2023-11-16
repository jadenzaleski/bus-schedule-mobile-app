package edu.miamioh.csi.capstone.busapp.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.maps.MapboxExperimental

@OptIn(MapboxExperimental::class)
@Composable
fun MapView() {
    Text(text = "Hello world")
}






@Composable
@Preview
fun MapViewPreview() {
    MapView()
}