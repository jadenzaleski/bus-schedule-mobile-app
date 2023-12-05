package edu.miamioh.csi.capstone.busapp.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import edu.miamioh.csi.capstone.busapp.CSVHandler

@OptIn(MapboxExperimental::class)
@Composable
fun StopsView() {
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapInitOptionsFactory = { context ->
            MapInitOptions(
                context = context,

                styleUri = Style.LIGHT,
                cameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(16.5952, 38.9048))
                    .zoom(9.0)
                    .build()
            )
        }
    ) {
        val stops = CSVHandler.getStops()
        if (stops.size > 100) {
            for (i in 1..100) {
                AddPointer(Point.fromLngLat(stops[i].stopLon, stops[i].stopLat))
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun AddPointer(point: Point) {
    //PointAnnotation(point = point, textField = "Hello World", textSize = 24.0, d, iconSize = 100.0)
    CircleAnnotation(point = point, circleRadius = 5.0, circleColorInt = Color.Red.toArgb())
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}