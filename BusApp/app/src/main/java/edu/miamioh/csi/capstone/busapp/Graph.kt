package edu.miamioh.csi.capstone.busapp

import java.time.LocalTime

/**
 * A data class to represent the nodes that will make up the graphs used for route creation.
 * In our situation, each node represents a different stop, which contains a variety of info.
 * stopID, stopName, stopLat, and stopLon are all constants that do not change, but they are
 * also UNIQUE. These values should not change in terms of the graph structure itself.
 */
data class Node(
    val stopID: Int,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    var routeRecords: List<RouteRecord>
)

/**
 * A data class that holds information unique to each route record which will be associated with
 * a node (stop). Note that stops can be associated with multiple routes and/or trips. For this
 * reason, we create a RouteRecord class to handle these multiplicities.
 */
data class RouteRecord(
    val agencyID: Int,
    val routeID: Int,
    val tripID: Int,
    val departureTime: LocalTime,
    val arrivalTime: LocalTime,
    val stopSequence: Int
)

object Graph {
    /**
     * Based on the list of user-selected agencies, returns the list of ALL StopIds associated
     * with those agencies. Funnels through manually created associations between required .csv
     * files in order to acquire final list of StopIds to be returned.
     *
     * @param agencyIDList - The list of active agencies selected by the user, represented by their
     *                       ID number.
     * @param routes - A list of Route objects (processed via the CSVHandler)
     * @param trips - A list of Trip objects (processed via the CSVHandler)
     * @param stopTimes - A list of StopTime objects (processed via the CSVHandler)
     * @return a list of all StopIds that are associated with the agencies selected
     */
    fun findAllValidStopIdByAgencyId(
        agencyIDList: Set<Int>,
        routes: List<Route>,
        trips: List<Trip>,
        stopTimes: List<StopTime>
    ): Set<Int> {
        // Find all qualifying routes based on the list of agencyIDs provided.
        val filteredRoutes = routes.filter { it.agencyID in agencyIDList }

        // Find all qualifying trips based on the qualifying routes previously found.
        val filteredTripIds = trips.filter { trip ->
            filteredRoutes.any { it.routeID == trip.routeID }
        }.map { it.tripID }.toSet()

        // Find all qualifying stops (represented by ID) based on the qualifying trips previously
        // found.
        val stopIds = stopTimes.filter { it.tripID in filteredTripIds }.map { it.stopID }.toSet()

        // Return the generated set of Stop IDs. Since this is a set, there are no duplicates.
        return stopIds
    }

    /**
     * Based on given list of stopIDs, creates a set of Node objects for each stopID, which contain
     * information on the stop itself, as well as information on what trips/agencies are associated
     * for each route that goes through that stop.
     *
     * @param validStopIDs - The set of stopIDs, where a unique Node will be generated for each ID
     * @param agencyIDList - The list of active agencies selected by the user, represented by their
     *                     ID number.
     * @param routes - A list of Route objects (processed via the CSVHandler)
     * @param trips - A list of Trip objects (processed via the CSVHandler)
     * @param stops - A list of Trip objects (processed via the CSVHandler)
     * @param stopTimes - A list of StopTime objects (processed via the CSVHandler)
     * @return a list of all StopIds that are associated with the agencies selected
     */
    fun generateNodes(
        validStopIDs: Set<Int>,
        agencyIDList: Set<Int>,
        routes: List<Route>,
        trips: List<Trip>,
        stops: List<Stop>,
        stopTimes: List<StopTime>
    ): Set<Node> {
        // Allows for quick lookup by the specified fields, which in this case act like keys.
        // Makes the function a lot more efficient, which will be helpful in the long-run
        // when this function may be making thousands of Node objects in some scenarios.
        val stopsMap = stops.associateBy { it.stopID }
        val tripsMap = trips.associateBy { it.tripID }
        val routesMap = routes.associateBy { it.routeID }

        /*
         * Iterate over every stopID in validStopIDs.
         *
         * The "agencyIDList" variable here holds all the agencyIDs selected in the dropdown by
         * the user, so this ensures that agencies not specified by the user won't have their
         * stops included here.
         */
        return validStopIDs.mapNotNull { stopId ->
            stopsMap[stopId]?.let { stop ->
                // Here, multiple StopTime records could be associated to the singular stop.
                val routeRecords = stopTimes.filter { it.stopID == stopId }
                    .mapNotNull { stopTime ->
                        tripsMap[stopTime.tripID]?.let { trip ->
                            routesMap[trip.routeID]?.let { route ->
                                if (agencyIDList.contains(route.agencyID)) {
                                    /*
                                     * By this point, if we're able to create a RouteRecord, then
                                     * the following checks should have occurred:
                                     * - Trip and Route match the stopID (via tripID and routeID
                                     *   fields)
                                     * - agencyID (taken from route-in-question) matches at least
                                     *   one of the agencyIDs in the list which contains user-
                                     *   selected agencies from the UI
                                     */
                                    RouteRecord(
                                        agencyID = route.agencyID,
                                        routeID = route.routeID,
                                        tripID = trip.tripID,
                                        departureTime = LocalTime.parse(stopTime.departureTime),
                                        arrivalTime = LocalTime.parse(stopTime.arrivalTime),
                                        stopSequence = stopTime.stopSequence
                                    )
                                } else null
                            }
                        }
                    }
                // Now that all the RouteRecord objects have been generated for each tripID,
                // generate the Node object for this stopID.
                Node(
                    stopID = stop.stopID,
                    stopName = stop.stopName,
                    stopLat = stop.stopLat,
                    stopLon = stop.stopLon,
                    routeRecords = routeRecords
                )
            }
        }.toSet()
    }
}