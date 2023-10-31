package edu.miamioh.csi.capstone.busapp

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.miamioh.csi.capstone.busapp.ui.theme.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainView() {
    // Create a NavController for navigating between different views
    val navController = rememberNavController()
    // Create a Scaffold with a bottom bar
    Scaffold( bottomBar = { BottomBar(navController = navController) })
    { innerPadding ->
        // make sure nothing is drawn behind the tabs
        Box(modifier = Modifier.padding(innerPadding)) {
            // Inside the Scaffold, display the content of the selected view
            BottomBarNavGraph(navController = navController)
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    // Define a list of views for the bottom navigation
    val views = listOf(
        BottomBarObject.Home,
        BottomBarObject.Map,
        BottomBarObject.Route,
        BottomBarObject.Lines,
        BottomBarObject.Stops,
        BottomBarObject.Settings
    )

    // Get the current navigation stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Create a BottomNavigation composable to display the bottom navigation bar
    BottomNavigation(backgroundColor = if (isSystemInDarkTheme()) Dark else Light) {
        // Loop through the list of views and add them to the bottom navigation
        views.forEach { view ->
            AddItem(
                view = view,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    view: BottomBarObject,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    // Create a BottomNavigationItem for each view
    BottomNavigationItem(
        label = {
            // Display the view's title with a font size of 10sp
            Text(
                text = view.title,
                fontSize = 10.sp,
            )
        },
        icon = {
            // Display an icon for the view with a content description
            Icon(
                imageVector = view.icon,
                contentDescription = "Navigation Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == view.route
        } == true,
        // set the selected content color
        selectedContentColor = Primary,
        // Set unselected content color with reduced alpha
        unselectedContentColor = Secondary,
        onClick = {
            // When clicked, navigate to the selected view
            navController.navigate(view.route) {
                // Configure the navigation behavior
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        },
        modifier = Modifier.padding(bottom = 18.dp, top = 3.dp)
    )
}
