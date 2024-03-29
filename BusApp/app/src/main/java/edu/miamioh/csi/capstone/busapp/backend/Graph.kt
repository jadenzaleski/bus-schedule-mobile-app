/**
 * Contributors: Daniel Tai
 * Last Modified: 3/27/2024
 * Description: Contains the back-end code for creating a Graph based on the CORe website
 *              information and some user input taken from our UI.
 */

package edu.miamioh.csi.capstone.busapp.backend

import android.util.Log
import edu.miamioh.csi.capstone.busapp.views.Place
import edu.miamioh.csi.capstone.busapp.views.calculateSphericalDistance
import java.time.LocalTime
import java.util.PriorityQueue

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
    var routeRecords: List<RouteRecord>,
    var isActive: Boolean
)


/**
 * A data class that holds information unique to each route record which will be associated with
 * a node (stop). Note that stops can be associated with multiple routes and/or trips. For this
 * reason, we create a RouteRecord class to handle these multiplicities.
 */
data class RouteRecord(
    val stopID: Int,
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
    val origRouteRecord: RouteRecord,
    val destRouteRecord: RouteRecord,
    val weight: Long
)

/**
 *
 */
data class FinalRoutePoint(
    val stopID: Int,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    val arrivalTime: LocalTime,
    val departureTime: LocalTime
)


object Graph {
    val stops = CSVHandler.getStops()
    val routes = CSVHandler.getRoutes()
    val trips = CSVHandler.getTrips()
    val stopTimes = CSVHandler.getStopTimes()
    val agencies = CSVHandler.getAgencies()

    /*
    // Stores all nodes for the graph.
    val graphNodes: MutableSet<Node> = mutableSetOf()
    // Stores all edges and weights for the graph (essentially, an adjacency list).
    val EdgesAndWeights: HashMap<Int, List<EdgeWeightRelation>> = HashMap()
     */

    fun optimalRouteGenerator(
        startLocation: Place,
        endLocation: Place,
        selectedTime: String,
        validAgencyIDs: Set<Int>
    ): List<FinalRoutePoint> {
        val validStopIDs = findAllValidStopIdByAgencyId(validAgencyIDs, routes, trips, stopTimes)
        Log.i("Route Generation", "Valid Stop IDs Found (Stage 1/6)")

        val graphNodes = generateNodes(validStopIDs, validAgencyIDs, routes, trips,
            stops, stopTimes)
        Log.i("Route Generation", "Nodes created (Stage 2/6)")

        val adjacencyList = generateEdgesAndWeightsWithTimeFilter(graphNodes, selectedTime)
        Log.i("Route Generation", "Adjacency List created (Stage 3/6)")

        val potentialStartBusStops = getNearbyNodesByCoordinatesOptimized(startLocation, graphNodes)
        Log.i("Route Generation", "Potential starting stops identified (Stage 4/6)")
        Log.i("# of Starting Stops", "" + potentialStartBusStops.size)

        val potentialEndBusStops = getNearbyNodesByCoordinatesOptimized(endLocation, graphNodes)
        Log.i("Route Generation", "Potential ending stops identified (Stage 5/6)")
        Log.i("# of Ending Stops", "" + potentialEndBusStops.size)

        val finalRoute = generateRoute(potentialStartBusStops, potentialEndBusStops, adjacencyList,
            selectedTime)
        Log.i("Route Generation", "Final route with points generated (Stage 6/6)")

        Log.i("Route Generation", "Returning route now...")
        return finalRoute
    }

    /**
     * Generates the optimal route between start and end points using the A* algorithm with a
     * geographical distance-based heuristic.
     *
     * @param startPoints - A list of potential starting nodes.
     * @param endPoints - A list of potential ending nodes.
     * @param adjacencyList - A map where each key is a node ID and each value is a list of EdgeWeightRelations to neighboring nodes.
     * @param selectedTime - The selected time for starting the route, used for filtering.
     * @return A list of FinalRoutePoint objects representing the optimal route.
     */
    fun generateRoute(
        startPoints: List<Node>,
        endPoints: List<Node>,
        adjacencyList: HashMap<Int, List<EdgeWeightRelation>>,
        selectedTime: String
    ): List<FinalRoutePoint> {
        // Heuristic function: Estimate distance based on the spherical distance between two geographical points.
        fun heuristic(node: Node, endNode: Node): Double {
            return calculateSphericalDistance(node.stopLat, node.stopLon, endNode.stopLat, endNode.stopLon)
        }

        // Initialize open and closed set for A* algorithm
        val openSet = PriorityQueue<Pair<Node, Double>>(compareBy { it.second })
        startPoints.forEach { openSet.add(it to 0.0) }

        val cameFrom = mutableMapOf<Int, Node>()

        // For node n, gScore[n] is the cost of the cheapest path from start to n currently known, initialized to infinity for all nodes except the start node.
        val gScore = mutableMapOf<Int, Double>().withDefault { Double.MAX_VALUE }
        startPoints.forEach { gScore[it.stopID] = 0.0 }

        // For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to how short a path from start to finish can be if it goes through n.
        val fScore = mutableMapOf<Int, Double>().withDefault { Double.MAX_VALUE }
        startPoints.forEach { fScore[it.stopID] = heuristic(it, endPoints.first()) }

        while (openSet.isNotEmpty()) {
            val current = openSet.poll().first

            // Check if current is in the endPoints list
            if (endPoints.any { it.stopID == current.stopID }) {
                return reconstructPath(cameFrom, current)
            }

            adjacencyList[current.stopID]?.forEach { neighborRelation ->
                val tentativeGScore = gScore.getValue(current.stopID) + neighborRelation.weight.toDouble()
                val neighbor = neighborRelation.endNode
                if (tentativeGScore < gScore.getValue(neighbor.stopID)) {
                    // This path to neighbor is better than any previous one. Record it!
                    cameFrom[neighbor.stopID] = current
                    gScore[neighbor.stopID] = tentativeGScore
                    fScore[neighbor.stopID] = gScore.getValue(neighbor.stopID) + heuristic(neighbor, endPoints.first())

                    if (!openSet.any { it.first.stopID == neighbor.stopID }) {
                        openSet.add(neighbor to fScore.getValue(neighbor.stopID))
                    }
                }
            }
        }

        // If we get here, then no path exists
        return emptyList()
    }

    /**
     * Reconstructs the path from start to goal node as discovered by the A* algorithm.
     *
     * @param cameFrom - A map indicating for each node n, which node it can be most efficiently reached from.
     * @param current - The current goal node from which to backtrack to the start node.
     * @return A list of FinalRoutePoint objects representing the path from start to goal.
     */
    private fun reconstructPath(cameFrom: Map<Int, Node>, current: Node): List<FinalRoutePoint> {
        var currentNode = current
        val totalPath = mutableListOf<Node>(currentNode)

        while (cameFrom.containsKey(currentNode.stopID)) {
            currentNode = cameFrom[currentNode.stopID]!!
            totalPath.add(0, currentNode) // Insert at the beginning
        }

        // Convert the Node path to FinalRoutePoint path
        return totalPath.map { node ->
            FinalRoutePoint(
                stopID = node.stopID,
                stopName = node.stopName,
                stopLat = node.stopLat,
                stopLon = node.stopLon,
                arrivalTime = node.routeRecords.first().arrivalTime,
                departureTime = node.routeRecords.first().departureTime
            )
        }
    }


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
                                    RouteRecord(
                                        stopID = stop.stopID,
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
                    routeRecords = routeRecords,
                    isActive = true
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

    /*
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
     */

    fun generateEdgesAndWeightsWithTimeFilter(
        nodes: Set<Node>,
        selectedTime: String
    ): HashMap<Int, List<EdgeWeightRelation>> {
        // Convert nodes list to a map for O(1) access time
        val nodesMap = nodes.associateBy { it.stopID }

        // Initialize the adjacency list
        val adjacencyList = HashMap<Int, MutableList<EdgeWeightRelation>>()

        // Convert the selected time to LocalTime
        val timeBoundary = parseStringToLocalTime("$selectedTime:00")

        nodes.forEach { originNode ->
            originNode.routeRecords.forEach { originRecord ->
                // Filter based on the time condition
                if (!originRecord.departureTime.isBefore(timeBoundary) && !originRecord.arrivalTime.isBefore(timeBoundary)) {
                    val nextStopSequence = originRecord.stopSequence + 1

                    // Search for a destination node that matches the criteria
                    nodesMap.values.firstOrNull { destinationNode ->
                        destinationNode.routeRecords.any { destRecord ->
                            destRecord.routeID == originRecord.routeID &&
                                    destRecord.tripID == originRecord.tripID &&
                                    destRecord.stopSequence == nextStopSequence &&
                                    !destRecord.departureTime.isBefore(timeBoundary) &&
                                    !destRecord.arrivalTime.isBefore(timeBoundary)
                        }
                    }?.let { matchingDestNode ->
                        // Find the matching destination RouteRecord to get the exact record
                        matchingDestNode.routeRecords.find { destRecord ->
                            destRecord.routeID == originRecord.routeID &&
                                    destRecord.tripID == originRecord.tripID &&
                                    destRecord.stopSequence == nextStopSequence
                        }?.let { matchingDestRecord ->
                            // Calculate the weight based on the departure and arrival times
                            val weight = java.time.Duration.between(
                                originRecord.departureTime, matchingDestRecord.arrivalTime
                            ).toMinutes()

                            // Create an EdgeWeightRelation object with both RouteRecords
                            val edge = EdgeWeightRelation(
                                endNode = matchingDestNode,
                                origRouteRecord = originRecord, // Include origin RouteRecord
                                destRouteRecord = matchingDestRecord, // Include destination RouteRecord
                                weight = weight
                            )
                            adjacencyList.computeIfAbsent(originNode.stopID) { mutableListOf() }.add(edge)
                        }
                    }
                }
            }
        }

        // Return the adjacency list with immutable lists
        return adjacencyList.mapValues { (_, v) -> v.toList() } as HashMap<Int, List<EdgeWeightRelation>>
    }

    /**
     * Optimized function for finding the closest bus stops for a given Place object, which contains
     * an associated latitude and longitude location.
     *
     * @param location - The location from which to find the closest bus stops (nodes)
     * @param nodes - The set of available nodes in the Graph, generated from the list of valid
     *                AgencyIDs the user specified
     */
    fun getNearbyNodesByCoordinatesOptimized(
        location: Place,
        nodes: Set<Node>
    ): List<Node> {
        val maxDistance = 3.21869  // Expressed in km. Equals about two miles.

        /*
         * Using a PriorityQueue really helps the efficiency of this algorithm. Previously, I was
         * keeping a list containing the distances between the location and ALL nodes in the set.
         * But if the set is large, this can make this method very slow computationally.
         *
         * The PQ gets sorted based on the distance between an individual node and the location.
         * For association purposes, we place the Node and the corresponding distance value together
         * in a pair.
         */
        val closestNodes = PriorityQueue<Pair<Node, Double>>(compareBy { it.second })

        nodes.forEach { node ->
            val distance = calculateSphericalDistance(location.lat, location.lon, node.stopLat, node.stopLon)
            if (distance < maxDistance) {
                /*
                 * For now, I want the list of nodes returned to have a size no greater than 3 (can
                 * be adjusted as needed).
                 *
                 * If the size of the list is not greater than 3 already, simply add the node if it
                 * fits the distance condition. Otherwise, if the incoming distance value is less
                 * than the "farthest" node in the PQ, remove that node and add the incoming node.
                 */
                if (closestNodes.size < 3) {
                    closestNodes.add(node to distance)
                } else if (distance < closestNodes.peek().second) {
                    closestNodes.poll() // Remove the farthest node
                    closestNodes.add(node to distance)
                }
            }
        }

        return closestNodes.map { it.first }
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