/**
 * Contributors: Daniel Tai
 * Last Modified: 3/27/2024
 * Description: A file for testing the creation of Graphs produced using functionalities outlined in
 *              the Graph.kt file.
 */

package edu.miamioh.csi.capstone.busapp.backend


import android.util.Log
import edu.miamioh.csi.capstone.busapp.views.Place

object GraphTester {
    /**
     * A tester suite that ensures the Graph being created from a designated list of agencyIDs is
     * correct. Calls upon functions from the Graph.kt file to get the output to be tested. There
     * are some manual elements you'll need to do here at this point based on how this tester file
     * is set up. This includes:
     * - Doing manual calculations for the expected number of edges
     * - Running through spreadsheet information to ensure the number of stops produced
     * - Running through different tripIDs to ensure the number of edges matches your expected
     *   number you previously calculated
     *
     * Results are returned in the Logcat for now. You can call this function by simply importing
     * the GraphTester object into a main Composable function of your choice connected to the UI
     * and then calling this function there.
     */
    fun runTests() {
        // Get all info from spreadsheets using the CSVHandler object.
        val stops = CSVHandler.getStops()
        val routes = CSVHandler.getRoutes()
        val trips = CSVHandler.getTrips()
        val stopTimes = CSVHandler.getStopTimes()
        val agencies = CSVHandler.getAgencies()

        // Place agencyIDs you want to test in this Set.
        //val selectedAgencyIds = mutableSetOf(25)
        val selectedAgencyIds = mutableSetOf(33, 34, 8, 9, 10, 11, 12, 13, 7, 27, 28, 29, 30, 35,
            32, 19, 21, 22, 24, 25, 26, 14, 15, 16, 17, 18)

        /*
        // Test # of Stops, and print list of stopIDs for manual check with .CSV files.
        var validStopIDs = Graph.findAllValidStopIdByAgencyId(selectedAgencyIds, routes, trips,
            stopTimes)
        Log.i("# of Stops from Agency", "" + validStopIDs.size)

        // Test # of Nodes generated (should match # of stops from Agency number above).
        var tempValidNodes = Graph.generateNodes(validStopIDs, selectedAgencyIds, routes, trips, stops,
            stopTimes)
        Log.i("# of Nodes Generated from Valid Stops", "" + tempValidNodes.size)

        //var filteredNodes = Graph.filterRouteRecordsByTime(validNodes, )

        // Test # of Edges generated (unless another method gets written for testing purposes, you
        // do this check manually.
        var adjacencyList = Graph.generateEdgesAndWeights(tempValidNodes)
        Log.i("# of Edges generated", "" + adjacencyList.values.flatten().size)
         */

        var start = Place("", 38.76257, 16.328503, "", "")
        var end = Place("", 39.398974, 16.262295, "", "")

        var finalRoute = Graph.optimalRouteGenerator(start, end, "00:00", selectedAgencyIds)
        Log.i("Final Route Size", "" + finalRoute.size)

        for (point in finalRoute) {
            Log.i("Point in Route", "" + point.stopID + ", " + point.stopLat + ", " + point.stopLon)
        }

    }
}