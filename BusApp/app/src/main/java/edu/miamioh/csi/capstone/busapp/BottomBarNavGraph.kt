package edu.miamioh.csi.capstone.busapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.miamioh.csi.capstone.busapp.views.*

@Composable
fun BottomBarNavGraph(navController: NavHostController) {
    // Create a navigation host for handling different views within the app
    NavHost(
        navController = navController,
        startDestination = BottomBarObject.Stops.route
    ) {
        // Define composable functions for each view/screen
        composable(route = BottomBarObject.Home.route) {
            // Display the HomeView when the corresponding route is navigated to
            HomeView()
        }
        composable(route = BottomBarObject.Map.route) {
            // Display the MapView when the corresponding route is navigated to
            MapView()
        }
        composable(route = BottomBarObject.Route.route) {
            // Display the RouteView when the corresponding route is navigated to
            RouteView()
        }
        composable(route = BottomBarObject.Lines.route) {
            // Display the LinesView when the corresponding route is navigated to
            LinesView()
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
