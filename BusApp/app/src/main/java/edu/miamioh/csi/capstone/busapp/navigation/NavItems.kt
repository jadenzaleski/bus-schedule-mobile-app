package edu.miamioh.csi.capstone.busapp.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import edu.miamioh.csi.capstone.busapp.R

data class NavItem(
    val label: String,
    val icon: Int,
    val route: String
)

val listOfNavItems = listOf(
    NavItem(
        label = "Stops",
        icon = R.drawable.bus_stop,
        route = Screens.StopsScreen.name
    ),
    NavItem(
        label = "Route",
        icon = R.drawable.route,
        route = Screens.RouteScreen.name
    ),
    NavItem(
        label = "Settings",
        icon = R.drawable.settings,
        route = Screens.SettingsScreen.name
    )
)