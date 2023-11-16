package edu.miamioh.csi.capstone.busapp

// Sealed class to represent the bottom navigation views
sealed class BottomBarObject(val route: String, val title: String, val icon: Int) {
    object Home:BottomBarObject(route = "home", title = "Home", icon = R.drawable.route)
    object Map:BottomBarObject(route = "map", title = "Map", icon = R.drawable.ic_launcher_background)
    object Route:BottomBarObject(route = "route", title = "Route", icon = R.drawable.route)
    object Lines:BottomBarObject(route = "lines", title = "Lines", icon = R.drawable.ic_launcher_background)
    object Stops:BottomBarObject(route = "stops", title = "Stops", icon = R.drawable.map)
    object Settings:BottomBarObject(route = "settings", title = "Settings", icon = R.drawable.settings)


}
