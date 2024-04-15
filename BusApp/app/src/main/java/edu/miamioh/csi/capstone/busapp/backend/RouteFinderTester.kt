package edu.miamioh.csi.capstone.busapp.backend

import edu.miamioh.csi.capstone.busapp.views.Place

object RouteFinderTester {
    fun runTests() {
        // Get all info from spreadsheets using the CSVHandler object.
        val stops = CSVHandler.getStops()
        val routes = CSVHandler.getRoutes()
        val trips = CSVHandler.getTrips()
        val stopTimes = CSVHandler.getStopTimes()
        val agencies = CSVHandler.getAgencies()

        // Place agencyIDs you want to test in this Set.
        val selectedAgencyIds = mutableSetOf(33)

        var start = Place("Domus Hotel", 39.352986978817455, 16.240970535302434, "", "")
        var end = Place("University", 39.36197761102033, 16.226076505252237, "", "")
        var selectedTime = "13:45"

        var potentialRoutes = RouteFinder.routeWorkhorse(start, end, selectedTime, selectedAgencyIds)

    }
}