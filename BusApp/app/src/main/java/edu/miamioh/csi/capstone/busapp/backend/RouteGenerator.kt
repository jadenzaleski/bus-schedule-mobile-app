/**
 * Contributors: Daniel Tai
 * Last Modified: 4/9/2024
 * Description: Contains the back-end code for creating a route based on the CORe website
 *              information and some user input taken from our UI. Creates a Graph, and using
 *              the A* algorithm, generates an optimal route under certain criteria.
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
 * A data class meant to represent the stops which make up the final route.
 */
data class FinalRoutePoint(
    val stopID: Int,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double
)

/**
 * A custom PQ class created solely for the A* Algorithm implementation. Helps with the algorithm's
 * efficiency in dealing with potentially tens of thousand of edges in a given graph.
 */
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


object RouteGenerator {
    private val stops = CSVHandler.getStops()
    private val routes = CSVHandler.getRoutes()
    private val trips = CSVHandler.getTrips()
    private val stopTimes = CSVHandler.getStopTimes()

    /**
     * A workhorse function that calls the other functions in the Graph object to produce an optimal
     * route represented by a list of FinalRoutePoint objects in a specific order.
     *
     * @param startLocation - The starting location specified by the user
     * @param endLocation - The ending location specified by the user
     * @param selectedTime - The earliest time the user wants the route to start at, in HH:MM
     *                       format
     * @param validAgencyIDs - The set of AgencyIDs picked by the user, whose stops can be used to
     *                         generate the route
     * @return a list of FinalRoutePoint objects representing the generated optimal route
     */
    fun routeWorkhorse(
        startLocation: Place,
        endLocation: Place,
        selectedTime: String,
        validAgencyIDs: Set<Int>
    ): List<FinalRoutePoint> {
        // Gets all the stopIDs associated with the selected AgencyIDs (no duplicates).
        val validStopIDs = findAllValidStopIdByAgencyId(validAgencyIDs, routes, trips, stopTimes)
        Log.i("Route Generation", "Valid Stop IDs Found: COMPLETE (Stage 1/8)")

        // Generates a node for every unique valid stopID.
        val graphNodes = generateNodes(validStopIDs, validAgencyIDs, routes, trips,
            stops, stopTimes)
        Log.i("Route Generation", "Nodes created from stopIDs: COMPLETE (Stage 2/8)")
        Log.i("# of Nodes Generated from Valid Stops", "" + graphNodes.size)

        // Finds (up to) the 3 closest bus stops from the designated startLocation.
        val potentialStartBusStops = getNearbyNodesByLocation(startLocation, graphNodes)
        Log.i("Route Generation",
            "Potential starting stops identified: COMPLETE (Stage 3/8)")
        Log.i("# of Starting Stops", "" + potentialStartBusStops.size)

        // Finds (up to) the 3 closest bus stops from the designated endLocation.
        val potentialEndBusStops = getNearbyNodesByLocation(endLocation, graphNodes)
        Log.i("Route Generation",
            "Potential ending stops identified: COMPLETE (Stage 4/8)")
        Log.i("# of Ending Stops", "" + potentialEndBusStops.size)

        /*
         * Eliminates all nodes that are not within a certain distance either from either
         * one of the potential start or end bus stops. This reduces the number of unnecessary
         * edges the A* algorithm will need to look at in the future, reducing computational
         * overhead and increasing efficiency.
         */
        val startPt = potentialStartBusStops.first()
        val endPt = potentialEndBusStops.first()
        val distanceBoundary = calculateSphericalDistance(startPt.stopLat, startPt.stopLon,
            endPt.stopLat, endPt.stopLon)
        val tempFilteredNodes = filterNodesByDistance(startPt, endPt, graphNodes,
            distanceBoundary)
        Log.i("# of Distance-Filtered Nodes", "" + tempFilteredNodes.size)
        Log.i("Route Generation", "Nodes filtered by distance: COMPLETE (Stage 5/8)")

        /*
         * Eliminates all RouteRecord objects associated with a Node with departureTime values that
         * are before the selectedTime value, which sets a boundary for the very earliest the route
         * should start.
         */
        val finalFilteredNodes = filterRouteRecordsByTime(tempFilteredNodes, selectedTime)
        Log.i("Route Generation", "Route Records filtered by time: COMPLETE (Stage 6/8)")

        // Generates the adjacency list for the graph.
        val adjacencyList = generateEdgesAndWeights(finalFilteredNodes)
        Log.i("Route Generation", "Adjacency List created: COMPLETE (Stage 7/8)")
        Log.i("# of Edges generated", "" + adjacencyList.values.flatten().size)

        // Generates the final route to be returned.
        val finalRoute = generateOptimalRoute(potentialStartBusStops, potentialEndBusStops, adjacencyList,
            finalFilteredNodes, selectedTime)
        Log.i("Route Generation", "Final route with points generated (Stage 8/8)")
        Log.i("Route Generation", "Returning route now...")

        return finalRoute
    }

    /**
     * Given a list of potential starting and ending points, generates routes between the points in
     * this list using the A* algorithm, and returns the most optimal route (the one with the least
     * weight). Uses geographical distance as the heuristic in this scenario.
     *
     * @param startPoints - A list of potential starting points for the route
     * @param endPoints - A list of potential ending points for the route
     * @param adjacencyList - Represents the edges/weights for the specific graph being used
     * @param nodes - The nodes (bus stops) that are available for use in generating the route
     * @return a list of FinalRoutePoint objects representing the generated optimal route
     */
    private fun generateOptimalRoute(
        startPoints: List<Node>,
        endPoints: List<Node>,
        adjacencyList: HashMap<Int, MutableList<Edge>>,
        nodes: Set<Node>,
        selectedTime: String
    ): List<FinalRoutePoint> {
        // Initially filter edges based on the selected start time
        filterEdgesByTime(adjacencyList, selectedTime)

        // Allows for O(1) access to a node by referencing its associated stopID.
        val nodesMap = nodes.associateBy { it.stopID }

        val endPointIDs = endPoints.map { it.stopID }.toSet()

        // A custom-defined PQ that holds nodes, prioritized by the fScore value (which is the
        // second value in the pair).
        val openSet = EfficientPriorityQueue<Pair<Node, Long>>(compareBy { it.second })

        // A map that holds how nodes that we've visited are related to our nodes, essentially
        // recording a trail.
        val cameFrom = mutableMapOf<Int, Int>()

        // Holds the cost of the cheapest path from the start node the current node.
        val gScore = mutableMapOf<Int, Long>().withDefault { Long.MAX_VALUE }

        // Combines the cost of the cheapest path with the heuristic value to produce a "cumulative"
        // cost of sorts, which helps in determining the optimal route.
        val fScore = mutableMapOf<Int, Long>().withDefault { Long.MAX_VALUE }

        // TODO(): Fix so that every combination of startPoints and endPoints has a route generated
        startPoints.forEach { node ->
            gScore[node.stopID] = 0L
            fScore[node.stopID] = heuristic(node, endPoints.first())
            openSet.add(node to fScore[node.stopID]!!)
        }

        while (!openSet.isEmpty()) {
            val currentPair = openSet.poll()
            val current = currentPair!!.first

            // If the current node being processed is one of the defined endPoints, just return
            // the route.
            if (current.stopID in endPointIDs) {
                //Log.i("cameFrom contents", cameFrom.toString())
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
                        openSet.updatePriority(neighbor to fScore[neighbor.stopID]!!,
                            neighbor to fScore[neighbor.stopID]!!)
                    }

                    // After moving to the neighbor, adjust the time boundary for subsequent edges
                    filterEdgesByTime(adjacencyList, edge.endArrivalTime.toString())
                }
            }
        }

        return emptyList() // Return an empty list if no path reaches an end point
    }

    /**
     * A helper function that calculates the heuristic value between two nodes (the geographical
     * distance between them).
     *
     * @param node - The node presently being processed
     * @param endNode - The designated endNode of the route overall
     * @return a Long value holding the distance between the two nodes
     */
    private fun heuristic(node: Node, endNode: Node): Long {
        // Placeholder for the actual heuristic function
        // For example, using straight-line distance as the heuristic
        return calculateSphericalDistance(node.stopLat, node.stopLon, endNode.stopLat,
            endNode.stopLon).toLong()
    }

    /**
     * A helper function that goes through all the points in the cameFrom map, which shows how the
     * stops added to the route being constructed are related to one another. It starts with what
     * should be the endPoint node, and iterates all the way to the startPoint node, reversing the
     * order and storing each node it processes through in a mutable list. It then maps each node
     * and creates a FinalRoutePoint object for each node, returning that list of objects.
     *
     * @param cameFrom - the map holding the relations between all nodes that are a part of the
     *                   route to be constructed
     * @param current - the end (last) node of the route
     * @return a list of FinalRoutePoint objects representing the route requested by the user
     */
    private fun reconstructRoute(
        cameFrom: Map<Int, Int>,
        current: Node,
        nodesMap: Map<Int, Node>
    ): List<FinalRoutePoint> {
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
     * A helper function that filters through all edges, and marks an edge as either active (true)
     * or inactive (false) based on how its departureTime relates to an inputted String value
     * meant to represent a time in HH:MM format.
     *
     * @param adjacencyList - The data structure containing all the edges of the graph
     * @param selectedTime - The minimum time boundary specified
     */
    fun filterEdgesByTime(adjacencyList: HashMap<Int, MutableList<Edge>>, selectedTime: String) {
        val timeBoundary = parseStringToLocalTime("$selectedTime:00")

        // Iterate through all edges in the adjacency list.
        adjacencyList.values.forEach { edges ->
            edges.forEach { edge ->
                // Compare the startDepartureTime of each edge with the time boundary
                if (edge.startDepartureTime.isBefore(timeBoundary)) {
                    edge.isActive = false
                } else {
                    edge.isActive = true
                }
            }
        }

        val activeEdgesCount = adjacencyList.values.flatten().count { it.isActive }
        Log.i("ActiveEdges", "Number of active edges after filtering by time $selectedTime: $activeEdgesCount")
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
    fun generateNodes(
        validStopIDs: Set<Int>,
        agencyIDList: Set<Int>,
        routes: List<Route>,
        trips: List<Trip>,
        stops: List<Stop>,
        stopTimes: List<StopTime>
    ): Set<Node> {
        val filteredRoutes =
            routes.filter { it.agencyID in agencyIDList }.associateBy { it.routeID }
        val filteredTrips =
            trips.filter { it.routeID in filteredRoutes.keys }.associateBy { it.tripID }
        val stopTimesByStopID = stopTimes.groupBy { it.stopID }

        // Pre-compute time parsing if specific repetitive time strings are expected
        val parsedTimesCache =
            stopTimes.flatMap { listOf(it.departureTime, it.arrivalTime) }.distinct()
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

    /**
     * Given a Place object which contains a set of latitude and longitude coordinates, calculates
     * and finds up to the 3 closest nodes (bus stops) based on those coordinates, orders them
     * by the distance calculated, and returns that list.
     *
     * @param location - A specified location for which we want to find the closest bus stops
     * @param nodes - The set of available nodes for which to pull the list of closest bus stops
     *                from
     * @return a list of the closest nodes to the location, ordered by calculated distance
     */
    private fun getNearbyNodesByLocation(
        location: Place,
        nodes: Set<Node>
    ): List<Node> {
        /*
         * Unit for this number is in km.
         * The initial max distance set to around 1-2 City Blocks, but this can be manually adjusted
         * as needed. NOTE: Increasing this distance means that the start/end nodes can be quite far
         * from the actual intended destination. Take care to avoid making this number too high.
         */
        var maxDistance = 0.2

        val allNodesWithDistances = nodes.map { node ->
            node to calculateSphericalDistance(location.lat, location.lon, node.stopLat, node.stopLon)
        }

        // Looks for the closest nodes using the initial maxDistance value of 0.2
        var filteredNodesWithDistances =
            allNodesWithDistances.filter { (_, distance) -> distance <= maxDistance }

        // If the initial filtering with the 0.2 value did not return any results, bump up the
        // maxDistance value, and try again until you find a node that matches the constraint.
        while (filteredNodesWithDistances.isEmpty()) {
            maxDistance += 0.1  // Increase max distance and try again
            filteredNodesWithDistances =
                allNodesWithDistances.filter { (_, distance) -> distance <= maxDistance }
        }

        // Sorts the closest nodes found by their distance and takes the closest 3
        // NOTE: Can adjust the number to return more/less stops as needed here.
        return filteredNodesWithDistances.sortedBy { it.second }.map { it.first }.take(3)
    }

    /**
     * Calculates the distance of all available nodes from a "starting" and "ending" Node. If the
     * distance is above a certain previously defined value, then disregard it; otherwise, add it to
     * a new list of nodes to be returned. This function helps reduce computational overhead when
     * the A* algorithm is utilized.
     *
     * @param startPt - A node designated as a potential starting point for the route
     * @param endPt - A node designated as a potential ending point for the route
     * @param nodes - The set of nodes to be filtered by distance
     * @param distanceBoundary - The distance value which is used to filter what nodes should be
     *                           returned and which ones shouldn't be
     * @return an updated set of nodes that have been filtered by distance, as defined above
     */
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
            val distanceFromStart = calculateSphericalDistance(startPt.stopLat, startPt.stopLon,
                node.stopLat, node.stopLon)
            val distanceFromEnd = calculateSphericalDistance(endPt.stopLat, endPt.stopLon,
                node.stopLat, node.stopLon)

            // Add the node to the set if it is within the distance boundary from either point
            if (distanceFromStart <= distanceBoundary || distanceFromEnd <= distanceBoundary) {
                filteredNodes.add(node)
            }
        }

        // Return the set of filtered nodes
        return filteredNodes
    }

    /**
     * Iterates through all RouteRecords from a given set of nodes. If a RouteRecord's
     * departureTime and arrivalTime values are later than a designated time, keeps and returns them
     * as part of the Node they were previously associated with; otherwise, these RouteRecords are
     * disregarded and not kept.
     *
     * @param nodes - The set of nodes whose associated RouteRecords will be filtered through
     * @param selectedTime - The specified time value, inputted as a String in HH:MM format. which
     *                       is used to filter what RouteRecords should be accepted
     * @return an updated set of nodes with filtered RouteRecords matching the time constraint
     */
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
                !record.departureTime.isBefore(timeBoundary) &&
                        !record.arrivalTime.isBefore(timeBoundary)
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
    fun generateEdgesAndWeights(nodes: Set<Node>): HashMap<Int, MutableList<Edge>> {
        val adjacencyList = HashMap<Int, MutableList<Edge>>()

        // Allows for O(1) access to a particular node by referencing its stopID.
        val nodeMap = nodes.associateBy { it.stopID }

        // Pre-computes and maps tripIDs to their sequential stop sequences.
        val tripToSequentialStops = nodes.flatMap { it.routeRecords }
            .groupBy { it.tripID }
            .mapValues { entry ->
                entry.value.sortedBy { it.stopSequence }
                    .windowed(2, 1, false)
                    .associate { it.first().stopID to it.last() }
            }

        // Generate Edge objects based on the matching RouteRecord values found between two nodes.
        for (node in nodes) {
            for (record in node.routeRecords) {
                tripToSequentialStops[record.tripID]?.get(node.stopID)?.let { nextStopRecord ->
                    nodeMap[nextStopRecord.stopID]?.let { destNode ->
                        val weight = java.time.Duration.between(record.departureTime,
                            nextStopRecord.arrivalTime).toMinutes()
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
