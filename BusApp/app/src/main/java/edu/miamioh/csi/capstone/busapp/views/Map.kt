package edu.miamioh.csi.capstone.busapp.views

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.DefaultSettingsProvider
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import edu.miamioh.csi.capstone.busapp.R

@OptIn(MapboxExperimental::class)
@Composable
fun MapView() {
    MapboxOptions.accessToken = "sk.eyJ1IjoiamFkZW56YWxlc2tpIiwiYSI6ImNsb3A5a2pmdzA3N3gyaW5xMWlhdXpkankifQ.0wzY9kVrxyI3zuoBy_SxMA"
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapInitOptionsFactory = { context -> MapInitOptions(
            context = context,

            styleUri = Style.LIGHT,
            cameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(24.9384, 60.1699))
                .zoom(12.0)
                .build()

        )

        }
    ){
        AddPointer(Point.fromLngLat(24.9384, 60.1699))
    }
}

@Composable
fun AddPointer(point: Point) {
    PointAnnotation(point = point, iconImage = Icons.Outlined.Place.toString(), iconSize = 1.0)
}






@Composable
@Preview
fun MapViewPreview() {
    MapView()
}