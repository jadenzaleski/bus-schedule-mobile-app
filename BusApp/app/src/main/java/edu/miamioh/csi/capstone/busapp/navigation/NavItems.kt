package edu.miamioh.csi.capstone.busapp.navigation

import edu.miamioh.csi.capstone.busapp.R

/**
 * @author Daniel Tai
 *
 * A class to store the attributes of each selectable tab in the NavBar. Each "NavItem" holds a
 * label (the tab's name), icon (the image to be displayed above the label), and route
 * (what View to navigate to when the tab is clicked).
 */
data class NavItem(
    val label: String,
    val icon: Int,
    val route: String
)

/**
 * To add additional tabs to the existing NavBar, place the tab's attributes into the list below
 *
 * With the icons, they are referenced from the res/drawable folder. You will need to add a new
 * Vector Asset to that folder in order for it to be correctly referenced
 */
val listOfNavItems = listOf(
    NavItem(
        label = "Stops",
        icon = R.drawable.bus_stop,
        route = Screens.StopsScreen.name
    ),
    NavItem(
        label = "Routes",
        icon = R.drawable.route,
        route = Screens.RouteScreen.name + "?option={option}&name={name}&lat={lat}&lon={lon}"
    ),
    NavItem(
        label = "Settings",
        icon = R.drawable.settings,
        route = Screens.SettingsScreen.name
    )
)