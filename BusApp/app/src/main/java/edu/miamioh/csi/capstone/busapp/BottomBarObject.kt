package edu.miamioh.csi.capstone.busapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// Sealed class to represent the bottom navigation views
sealed class BottomBarObject(val route: String, val title: String, val icon: ImageVector) {
    object Home:BottomBarObject(route = "home", title = "Home", icon = Icons.Outlined.Home)
    object Map:BottomBarObject(route = "map", title = "Map", icon = Icons.Outlined.Place)
    object Route:BottomBarObject(route = "route", title = "Route", icon = Icons.Outlined.Create)
    object Lines:BottomBarObject(route = "lines", title = "Lines", icon = Icons.Outlined.Menu)
    object Stops:BottomBarObject(route = "stops", title = "Stops", icon = Icons.Outlined.List)
    object Settings:BottomBarObject(route = "settings", title = "Settings", icon = Icons.Outlined.Settings)


}
