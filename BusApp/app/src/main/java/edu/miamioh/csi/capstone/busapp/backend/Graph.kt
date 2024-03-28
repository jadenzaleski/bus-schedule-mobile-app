/**
 * Contributors: Daniel Tai
 * Last Modified: 3/27/2024
 * Description: Contains the back-end code for creating a Graph based on the CORe website
 *              information and some user input taken from our UI.
 */

package edu.miamioh.csi.capstone.busapp.backend

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

/**
 * A data class meant to represent an edge between two nodes. Contains the endNode that the edge
 * points toward, and also stores the weight which will be referenced in the future based on
 * whatever algorithm we choose to work with to generate routes optimally.
 */
data class EdgeWeightRelation(
    val endNode: Node,
    val weight: Long
)


object Graph {
    // Stores all nodes for the graph.
    val graphNodes: MutableSet<Node> = mutableSetOf()
    // Stores all edges and weights for the graph (essentially, an adjacency list).
    val EdgeWeightRelations: HashMap<Int, List<EdgeWeightRelation>> = HashMap()

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
                                        departureTime = parseStringToLocalTime(stopTime.departureTime),
                                        arrivalTime = parseStringToLocalTime(stopTime.arrivalTime),
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


    /**
     * Given a set of Nodes which have the ability to have connection (or edges) between one
     * another, creates said edges and assign weights under pre-described conditions.
     *
     * @param nodes - A set of nodes to be processed and potentially connected via edges
     * @return a HashMap with the stopID of each node as the key, and a list of EdgeWeightRelation
     *         objects containing edge and weight details as the value
     */
    /*
    fun generateEdgesAndWeights(
        nodes: Set<Node>
    ): HashMap<Int, List<EdgeWeightRelation>> {
        // Initializes the adjacency list that will hold all edge and weight information.
        val adjacencyList = HashMap<Int, MutableList<EdgeWeightRelation>>()

        // Iterate over every node provided to the function.
        // Here, you can think of originNode as the potential node from which the edge points from.
        nodes.forEach { originNode ->
            // Iterate through all RouteRecords from the originNode.
            originNode.routeRecords.forEach { originRecord ->
                // Doesn't make sense to check the originNode against itself, so filter that out.
                nodes.filter { it.stopID != originNode.stopID }.forEach { potentialDestNode ->
                    potentialDestNode.routeRecords.firstOrNull { destRecord ->
                        /*
                         * Checks:
                         * 1) If the RouteRecords have the same routeID and tripID. We check both of
                         *    these, although technically tripID is the really important field here.
                         *    Having the same tripID means that the bus runs through both stops in
                         *    question on one singular route run.
                         * 2) The stopSequence value tells us whether the stops are in consecutive
                         *    order (and thus if we should establish an edge or not here). Note that
                         *    the originRecord's stopSequence value has to be exactly one less than
                         *    the destRecord's stopSequence value because the bus should logically
                         *    stop at the originNode first and having an exact difference of one
                         *    means the stops come in the correct, consecutive order.
                         */
                        originRecord.routeID == destRecord.routeID &&
                                originRecord.tripID == destRecord.tripID &&
                                originRecord.stopSequence == destRecord.stopSequence - 1
                    }?.also { matchingDestRecord ->
                        // Calculates the weight. Using some Java code imports here to do this.
                        val weight = java.time.Duration.between(
                            originRecord.departureTime, matchingDestRecord.arrivalTime
                        ).toMinutes()


                        // Creates the EdgeWeightRelation object to be stored.
                        val edge = EdgeWeightRelation(endNode = potentialDestNode, weight = weight)
                        /*
                         * Adds the edge to the adjacencyList. If the key doesn't exist in the
                         * HashMap, adds it before adding the associated value to the key.
                         * Otherwise, just adds the value to the pre-existing key's list.
                         */
                        adjacencyList.computeIfAbsent(originNode.stopID) { mutableListOf() }.add(edge)
                    }
                }
            }
        }

        // Convert mutable lists to immutable before returning
        return adjacencyList.mapValues { (_, v) -> v.toList() } as HashMap<Int, List<EdgeWeightRelation>>
    }
     */

    fun generateEdgesAndWeights(nodes: Set<Node>): HashMap<Int, List<EdgeWeightRelation>> {
        // Convert nodes list to a map for O(1) access time
        val nodesMap = nodes.associateBy { it.stopID }

        // Initialize the adjacency list
        val adjacencyList = HashMap<Int, MutableList<EdgeWeightRelation>>()

        nodes.forEach { originNode ->
            originNode.routeRecords.forEach { originRecord ->
                // Directly access potential destination node, avoiding unnecessary filtering
                val nextStopSequence = originRecord.stopSequence + 1

                // Attempt to find a single destination node that matches criteria
                val destinationNode = nodesMap.values.firstOrNull { destinationNode ->
                    destinationNode.routeRecords.any { destRecord ->
                        destRecord.routeID == originRecord.routeID &&
                                destRecord.tripID == originRecord.tripID &&
                                destRecord.stopSequence == nextStopSequence
                    }
                }

                // If a matching destination node is found, calculate the weight and add the edge
                destinationNode?.routeRecords?.find { destRecord ->
                    destRecord.routeID == originRecord.routeID &&
                            destRecord.tripID == originRecord.tripID &&
                            destRecord.stopSequence == nextStopSequence
                }?.let { matchingDestRecord ->
                    val weight = java.time.Duration.between(
                        originRecord.departureTime, matchingDestRecord.arrivalTime
                    ).toMinutes()

                    val edge = EdgeWeightRelation(destinationNode, weight)
                    adjacencyList.computeIfAbsent(originNode.stopID) { mutableListOf() }.add(edge)
                }
            }
        }

        // Return the adjacency list with immutable lists
        return adjacencyList.mapValues { (_, v) -> v.toList() } as HashMap<Int, List<EdgeWeightRelation>>
    }


    /**
     * A helper function to manually adjust time (HH:MM:SS) being expressed in a String that will
     * be converted and potentially manipulated into a Java LocalTime variable. Functions very
     * similarly to LocalTime.Parse, along with some adjustments.
     * Can be helpful in ensuring a DateTimeParseException doesn't occur.
     *
     * @param timeAsString - A string representing a time (HH:MM:SS)
     * @return a LocalTime variable with the same "time value" as the string, adjusted to ensure
     *         invalid values cannot be generated
     */
    private fun parseStringToLocalTime(timeAsString: String): LocalTime {
        val parts = timeAsString.split(":").map { it.toInt() }
        val normalizedHour = parts[0] % 24
        val minute = parts[1]
        val second = parts[2]
        return LocalTime.of(normalizedHour, minute, second)
    }
}