package edu.miamioh.csi.capstone.busapp.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val listOfNavItems = listOf(
    NavItem(
        label = "Stops",
        icon = Icons.Default.Place,
        route = Screens.StopsScreen.name
    ),
    NavItem(
        label = "Route",
        icon = Icons.Default.Info,
        route = Screens.RouteScreen.name
    ),
    NavItem(
        label = "Settings",
        icon = Icons.Default.Settings,
        route = Screens.SettingsScreen.name
    )
)