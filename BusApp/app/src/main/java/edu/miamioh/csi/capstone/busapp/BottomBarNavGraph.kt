package edu.miamioh.csi.capstone.busapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.miamioh.csi.capstone.busapp.views.RouteView
import edu.miamioh.csi.capstone.busapp.views.SettingsView
import edu.miamioh.csi.capstone.busapp.views.StopsView

@Composable
fun BottomBarNavGraph(navController: NavHostController) {
    // Create a navigation host for handling different views within the app
    NavHost(
        navController = navController,
        startDestination = BottomBarObject.Stops.route
    ) {
        composable(route = BottomBarObject.Route.route) {
            // Display the RouteView when the corresponding route is navigated to
            RouteView()
        }
        composable(route = BottomBarObject.Stops.route) {
            // Display the StopsView when the corresponding route is navigated to
            StopsView()
        }
        composable(route = BottomBarObject.Settings.route) {
            // Display the SettingsView when the corresponding route is navigated to
            SettingsView()
        }
    }
}
