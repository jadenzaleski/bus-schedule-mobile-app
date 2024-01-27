package edu.miamioh.csi.capstone.busapp.views

import android.content.res.Resources.Theme
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings
import edu.miamioh.csi.capstone.busapp.CSVHandler
import edu.miamioh.csi.capstone.busapp.ui.theme.isDark


@OptIn(MapboxExperimental::class)
@Composable
fun StopsView() {
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapInitOptionsFactory = { context ->
            MapInitOptions(
                context = context,
                styleUri = if(isDark) Style.STANDARD else Style.STANDARD,
                //styleUri = if(isDark) "mapbox://styles/jadenzaleski/clpj08tiz00dh01qu8tv9fp25" else "mapbox://styles/jadenzaleski/clpj1cq5200dj01qmdiarhnwa",
                cameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(16.5952, 38.9048))
                    .zoom(9.0)
                    .build()

            )

        }
    ) {
        //EXAMPLE
        val stops = CSVHandler.getStops()
        if(stops.size > 100) {
            for (i in 1..50) {
                AddPointer(Point.fromLngLat(stops[i].stopLon, stops[i].stopLat))
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun AddPointer(point: Point) {
    PointAnnotation(point = point, textField = "Hello World", textSize = 24.0, iconSize = 100.0)
    CircleAnnotation(point = point, circleRadius = 5.0, circleColorInt = Color.Green.toArgb())
}

@Composable
@Preview
fun StopsViewPreview() {
    StopsView()
}