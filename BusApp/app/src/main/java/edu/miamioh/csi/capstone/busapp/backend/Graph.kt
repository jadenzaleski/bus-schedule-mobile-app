/**
 * Contributors: Daniel Tai
 * Last Modified: 3/30/2024
 * Description: Contains the back-end code for creating a Graph based on the CORe website
 *              information and some user input taken from our UI.
 */

package edu.miamioh.csi.capstone.busapp.backend

import android.util.Log
import edu.miamioh.csi.capstone.busapp.views.Place
import edu.miamioh.csi.capstone.busapp.views.calculateSphericalDistance
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
    var routeRecords: List<RouteRecord>,
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
data class Edge(
    val endStopID: Int,
    val tripID: Int,
    val startDepartureTime: LocalTime,
    val endArrivalTime: LocalTime,
    val weight: Long,
    var isActive: Boolean
)

/**
 *
 */
data class FinalRoutePoint(
    val stopID: Int,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
)


object Graph {
    private val stops = CSVHandler.getStops()
    private val routes = CSVHandler.getRoutes()
    private val trips = CSVHandler.getTrips()
    private val stopTimes = CSVHandler.getStopTimes()

    fun optimalRouteGenerator(
        startLocation: Place,
        endLocation: Place,
        selectedTime: String,
        validAgencyIDs: Set<Int>
    ): List<FinalRoutePoint> {
        val validStopIDs = findAllValidStopIdByAgencyId(validAgencyIDs, routes, trips, stopTimes)
        Log.i("Route Generation", "Valid Stop IDs Found: COMPLETE (Stage 1/7)")

        val graphNodes = generateNodes(validStopIDs, validAgencyIDs, routes, trips,
            stops, stopTimes)
        Log.i("Route Generation", "Nodes created from stopIDs: COMPLETE (Stage 2/7)")
        //Log.i("# of Nodes Generated from Valid Stops", "" + graphNodes.size)

        val potentialStartBusStops = getNearbyNodesByCoordinatesOptimized(startLocation,
            graphNodes)
        Log.i("Route Generation", "Potential starting stops identified: COMPLETE (Stage 3/7)")
        Log.i("# of Starting Stops", "" + potentialStartBusStops)

        val potentialEndBusStops = getNearbyNodesByCoordinatesOptimized(endLocation,
            graphNodes)
        Log.i("Route Generation", "Potential ending stops identified: COMPLETE (Stage 4/7)")
        //Log.i("# of Ending Stops", "" + potentialEndBusStops)

        val startPt = potentialStartBusStops.first()
        val endPt = potentialEndBusStops.first()
        val distanceBoundary = calculateSphericalDistance(startPt.stopLat, startPt.stopLon,
            endPt.stopLat, endPt.stopLon)
        val tempFilteredNodes = filterNodesByDistance(startPt, endPt, graphNodes, distanceBoundary / 2)
        //Log.i("# of Distance-Filtered Nodes", "" + tempFilteredNodes.size)
        Log.i("Route Generation", "Nodes filtered by distance: COMPLETE (Stage 5/7)")

        val finalFilteredNodes = filterRouteRecordsByTime(tempFilteredNodes, selectedTime)
        Log.i("Route Generation", "Route Records filtered by time: COMPLETE (Stage 6/7)")

        val adjacencyList = generateEdgesAndWeights(finalFilteredNodes)
        Log.i("Route Generation", "Adjacency List created: COMPLETE (Stage 7/7)")
        Log.i("# of Edges generated", "" + adjacencyList.values.flatten().size)

        val finalRoute = generateRoute(potentialStartBusStops, potentialEndBusStops, adjacencyList,
            finalFilteredNodes)
        Log.i("Route Generation", "Final route with points generated (Stage 8/7)")
        Log.i("Route Generation", "Returning route now...")

        return finalRoute
    }

    // Represents a simplified version of a priority queue for demonstration purposes.
    class EfficientPriorityQueue<T>(private val comparator: Comparator<T>) {
        private val innerList = mutableListOf<T>()

        fun add(element: T) {
            innerList.add(element)
            innerList.sortWith(comparator)
        }

        fun poll(): T? = if (innerList.isNotEmpty()) innerList.removeAt(0) else null

        fun isEmpty(): Boolean = innerList.isEmpty()

        fun updatePriority(element: T, newPriority: T) {
            // This is a simplified placeholder. Implement efficient update logic here.
            innerList.remove(element)
            add(newPriority)
        }

        fun contains(element: T): Boolean = innerList.contains(element)
    }

    private fun generateRoute(
        startPoints: List<Node>,
        endPoints: List<Node>,
        adjacencyList: HashMap<Int, MutableList<Edge>>,
        nodes: Set<Node>
    ): List<FinalRoutePoint> {
        val nodesMap = nodes.associateBy { it.stopID }
        val endPointIDs = endPoints.map { it.stopID }.toSet() // Cache end point IDs for quick lookup
        val openSet = EfficientPriorityQueue<Pair<Node, Long>>(compareBy { it.second })
        val cameFrom = mutableMapOf<Int, Int>()
        val gScore = mutableMapOf<Int, Long>().withDefault { Long.MAX_VALUE }
        val fScore = mutableMapOf<Int, Long>().withDefault { Long.MAX_VALUE }

        startPoints.forEach { node ->
            gScore[node.stopID] = 0L
            fScore[node.stopID] = heuristic(node, endPoints.first())
            openSet.add(node to fScore[node.stopID]!!)
        }

        while (!openSet.isEmpty()) {
            val currentPair = openSet.poll()
            val current = currentPair!!.first

            // Check if we've reached any of the end points
            if (current.stopID in endPointIDs) {
                //Log.i("cameFrom contents", cameFrom.toString())
                // Construct and return the route from start to the current end point
                return reconstructRoute(cameFrom, current, nodesMap)
            }

            adjacencyList[current.stopID]?.forEach { edge ->
                val neighbor = nodesMap[edge.endStopID]!!
                val tentativeGScore = gScore[current.stopID] ?: (Long.MAX_VALUE + edge.weight)

                if (tentativeGScore < (gScore[neighbor.stopID] ?: Long.MAX_VALUE)) {
                    cameFrom[neighbor.stopID] = current.stopID
                    gScore[neighbor.stopID] = tentativeGScore
                    fScore[neighbor.stopID] = tentativeGScore + heuristic(
                        neighbor,
                        endPoints.first()
                    )

                    if (!openSet.contains(neighbor to fScore[neighbor.stopID]!!)) {
                        openSet.add(neighbor to fScore[neighbor.stopID]!!)
                    } else {
                        // Efficiently update the priority if the neighbor is already in openSet
                        openSet.updatePriority(neighbor to fScore[neighbor.stopID]!!, neighbor to fScore[neighbor.stopID]!!)
                    }
                }
            }
        }

        return emptyList() // Return an empty list if no path reaches an end point
    }


    private fun heuristic(node: Node, endNode: Node): Long {
        // Placeholder for the actual heuristic function
        // For example, using straight-line distance as the heuristic
        return calculateSphericalDistance(node.stopLat, node.stopLon, endNode.stopLat, endNode.stopLon).toLong()
    }

    private fun reconstructRoute(cameFrom: Map<Int, Int>, current: Node, nodesMap: Map<Int, Node>): List<FinalRoutePoint> {
        val totalPath = mutableListOf(current)
        var tempCurrent = current
        while (cameFrom.containsKey(tempCurrent.stopID)) {
            tempCurrent = nodesMap[cameFrom[tempCurrent.stopID]]!!
            totalPath.add(0, tempCurrent)
        }
        Log.i("totalPath size", "" + totalPath.size)
        Log.i("totalPath contents", totalPath.toString())
        return totalPath.map { node ->
            FinalRoutePoint(
                stopID = node.stopID,
                stopName = node.stopName,
                stopLat = node.stopLat,
                stopLon = node.stopLon,
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
    private fun findAllValidStopIdByAgencyId(
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

        // Return the generated set of Stop IDs. Since this is a set, there are no duplicates.
        return stopTimes.filter { it.tripID in filteredTripIds }.map { it.stopID }.toSet()
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
    private fun generateNodes(
        validStopIDs: Set<Int>,
        agencyIDList: Set<Int>,
        routes: List<Route>,
        trips: List<Trip>,
        stops: List<Stop>,
        stopTimes: List<StopTime>
    ): Set<Node> {
        val filteredRoutes = routes.filter { it.agencyID in agencyIDList }.associateBy { it.routeID }
        val filteredTrips = trips.filter { it.routeID in filteredRoutes.keys }.associateBy { it.tripID }
        val stopTimesByStopID = stopTimes.groupBy { it.stopID }

        // Pre-compute time parsing if specific repetitive time strings are expected
        val parsedTimesCache = stopTimes.flatMap { listOf(it.departureTime, it.arrivalTime) }.distinct()
            .associateWith { parseStringToLocalTime(it) }

        return stops.filter { it.stopID in validStopIDs }.map { stop ->
            val routeRecords = stopTimesByStopID[stop.stopID]?.mapNotNull { stopTime ->
                filteredTrips[stopTime.tripID]?.let { trip ->
                    filteredRoutes[trip.routeID]?.let {
                        RouteRecord(
                            stopID = stop.stopID,
                            agencyID = it.agencyID,
                            routeID = it.routeID,
                            tripID = trip.tripID,
                            departureTime = parsedTimesCache[stopTime.departureTime]!!,
                            arrivalTime = parsedTimesCache[stopTime.arrivalTime]!!,
                            stopSequence = stopTime.stopSequence
                        )
                    }
                }
            } ?: listOf()
            Node(
                stopID = stop.stopID,
                stopName = stop.stopName,
                stopLat = stop.stopLat,
                stopLon = stop.stopLon,
                routeRecords = routeRecords
            )
        }.toSet()
    }

    private fun filterNodesByDistance(
        startPt: Node,
        endPt: Node,
        nodes: Set<Node>,
        distanceBoundary: Double
    ): Set<Node> {
        // Initialize an empty set to store the filtered nodes
        val filteredNodes = mutableSetOf<Node>()

        // Directly add start and end points to avoid distance calculation
        filteredNodes.add(startPt)
        filteredNodes.add(endPt)

        // Iterate through each node only once to check conditions
        nodes.forEach { node ->
            // Skip the node if it is the start or end point, as they are already added
            if (node.stopID == startPt.stopID || node.stopID == endPt.stopID) return@forEach

            // Calculate the spherical distance from the node to both the start and end points
            val distanceFromStart = calculateSphericalDistance(startPt.stopLat, startPt.stopLon, node.stopLat, node.stopLon)
            val distanceFromEnd = calculateSphericalDistance(endPt.stopLat, endPt.stopLon, node.stopLat, node.stopLon)

            // Add the node to the set if it is within the distance boundary from either point
            if (distanceFromStart <= distanceBoundary || distanceFromEnd <= distanceBoundary) {
                filteredNodes.add(node)
            }
        }

        // Return the set of filtered nodes
        return filteredNodes
    }


    private fun filterRouteRecordsByTime(
        nodes: Set<Node>,
        selectedTime: String
    ): Set<Node> {
        // Convert the selected time string to a LocalTime object
        val timeBoundary = parseStringToLocalTime("$selectedTime:00")

        // Iterate through each node to filter RouteRecords
        return nodes.map { node ->
            // Filter RouteRecords based on the time condition
            val filteredRecords = node.routeRecords.filter { record ->
                !record.departureTime.isBefore(timeBoundary) && !record.arrivalTime.isBefore(timeBoundary)
            }
            // Create a new Node with the filtered RouteRecords
            node.copy(routeRecords = filteredRecords)
        }.toSet() // Return a new set with updated nodes
    }


    /**
     * Given a set of Nodes which have the ability to have connection (or edges) between one
     * another, creates said edges and assign weights under pre-described conditions.
     *
     * @param nodes - A set of nodes to be processed and potentially connected via edges
     * @return a HashMap with the stopID of each node as the key, and a list of EdgeWeightRelation
     *         objects containing edge and weight details as the value
     */
    private fun generateEdgesAndWeights(nodes: Set<Node>): HashMap<Int, MutableList<Edge>> {
        val adjacencyList = HashMap<Int, MutableList<Edge>>()

        // Map for O(1) node access
        val nodeMap = nodes.associateBy { it.stopID }

        // Pre-compute and map tripIDs to their sequential stop sequences
        val tripToSequentialStops = nodes.flatMap { it.routeRecords }
            .groupBy { it.tripID }
            .mapValues { entry ->
                entry.value.sortedBy { it.stopSequence }
                    .windowed(2, 1, false) // Create pairs of sequential stops
                    .associate { it.first().stopID to it.last() }
            }

        // Generate edges
        for (node in nodes) {
            for (record in node.routeRecords) {
                tripToSequentialStops[record.tripID]?.get(node.stopID)?.let { nextStopRecord ->
                    nodeMap[nextStopRecord.stopID]?.let { destNode ->
                        val weight = java.time.Duration.between(record.departureTime, nextStopRecord.arrivalTime).toMinutes()
                        val edge = Edge(
                            endStopID = destNode.stopID,
                            tripID = record.tripID,
                            startDepartureTime = record.departureTime,
                            endArrivalTime = nextStopRecord.arrivalTime,
                            weight = weight,
                            isActive = true
                        )
                        adjacencyList.computeIfAbsent(node.stopID) { mutableListOf() }.add(edge)
                    }
                }
            }
        }

        return adjacencyList
    }

    private fun getNearbyNodesByCoordinatesOptimized(
        location: Place,
        nodes: Set<Node>
    ): List<Node> {
        var maxDistance = 0.2  // Initial max distance set to around 2-3 City Blocks

        val allNodesWithDistances = nodes.map { node ->
            node to calculateSphericalDistance(location.lat, location.lon, node.stopLat, node.stopLon)
        }

        // Initially filter nodes within the max distance, progressively increasing the distance if no nodes found
        var filteredNodesWithDistances = allNodesWithDistances.filter { (_, distance) -> distance <= maxDistance }

        while (filteredNodesWithDistances.isEmpty()) {
            maxDistance += 0.1  // Increase max distance and try again
            filteredNodesWithDistances = allNodesWithDistances.filter { (_, distance) -> distance <= maxDistance }
        }

        // Sort the filtered nodes by their distance and take the closest 3
        return filteredNodesWithDistances.sortedBy { it.second }.map { it.first }.take(3)
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
