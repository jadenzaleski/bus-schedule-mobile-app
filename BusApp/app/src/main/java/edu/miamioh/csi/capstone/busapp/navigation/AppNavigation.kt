package edu.miamioh.csi.capstone.busapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import edu.miamioh.csi.capstone.busapp.views.RouteView
import edu.miamioh.csi.capstone.busapp.views.SettingsView
import edu.miamioh.csi.capstone.busapp.views.StopsView

/**
 * @author Daniel Tai
 *
 * A function to populate the NavBar and direct navigational activity based on user input
 */
@Composable
fun AppNavigation() {
    val navController : NavHostController = rememberNavController()

    Scaffold (
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Goes through each navItem in the list and adds it to the NavBar
                listOfNavItems.forEach {navItem ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true,
                        onClick = {
                              navController.navigate(navItem.route){
                                  popUpTo(navController.graph.findStartDestination().id){
                                      saveState = true
                                  }
                                  launchSingleTop = true
                                  restoreState = true
                              }
                        },
                        icon = {
                           Icon(
                               painterResource(id = navItem.icon),
                               contentDescription = null
                           )
                        },
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
        }
    ) {paddingValues ->
        NavHost(navController = navController,
            startDestination = Screens.RouteScreen.name + "?option={option}&name={name}&lat={lat}&lon={lon}",
            modifier = Modifier
                .padding(paddingValues)
        ){
            composable(
                route = Screens.RouteScreen.name + "?option={option}&name={name}&lat={lat}&lon={lon}",
                arguments = listOf(
                    navArgument("option") { type = NavType.StringType; defaultValue = "" },
                    navArgument("name") { type = NavType.StringType; defaultValue = "" },
                    navArgument("lat") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("lon") { type = NavType.FloatType; defaultValue = 0f }
                )
            ) { backStackEntry ->
                RouteView(
                    option = backStackEntry.arguments?.getString("option") ?: "",
                    name = backStackEntry.arguments?.getString("name") ?: "",
                    lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0,
                    lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
                )
            }

            composable(route = Screens.StopsScreen.name){
                StopsView(navController)     // Calls StopsView(), which can be found in Stops.kt
            }
            composable(route = Screens.SettingsScreen.name){
                SettingsView()  // Calls SettingsView(), which can be found in Settings.kt
            }
        }
    }
}