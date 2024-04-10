package edu.miamioh.csi.capstone.busapp.backend

import android.util.Log

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

        var validTripIDs = RouteFinder.findAllValidTripIDs(selectedAgencyIds, routes, trips)
        Log.i("# of Trips from Agency", "" + validTripIDs.size)

        var tripRecords = RouteFinder.generateTripRecords(validTripIDs)
        Log.i("# of TripRecords", "" + tripRecords.size)
    }
}