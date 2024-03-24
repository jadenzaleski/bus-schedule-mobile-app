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
    var routeRecord: List<SubNode>
)

/**
 * A data class that holds information unique to each route record which will be associated with
 * a node (stop). Note that stops can be associated with multiple routes and/or trips. For this
 * reason, we create a SubNode class to handle these multiplicities.
 */
data class SubNode(
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
     * @param agencyIdList The list of active agencies selected by the user, represented by their
     *                     ID number.
     * @return a list of all StopIds that are associated with the agencies selected
     */
    fun findAllValidStopIdByAgencyId(
        agencyIdList: Set<Int>,
        agencies: List<Agency>,
        stops: List<Stop>,
        routes: List<Route>,
        trips: List<Trip>,
        stopTimes: List<StopTime>
    ): Set<Int> {
        // Find all qualifying routes based on the list of agencyIDs provided.
        val filteredRoutes = routes.filter { it.agencyID in agencyIdList }

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
}