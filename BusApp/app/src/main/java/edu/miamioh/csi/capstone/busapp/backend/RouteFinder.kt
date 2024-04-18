package edu.miamioh.csi.capstone.busapp.backend

import android.util.Log
import edu.miamioh.csi.capstone.busapp.views.Place
import edu.miamioh.csi.capstone.busapp.views.calculateSphericalDistance
import java.time.LocalTime
import java.util.PriorityQueue

/**
 * A data class that contains a TripRecord object for each tripID found in the CORe data.
 * - stopsList is a list of all the stops (each represented by a StopInfo object) in that specific
 *   trip
 * - routeStartTime is the departureTime value of the first stop in stopsList
 * - routeEndTime is the arrivalTime value of the last stop in stopsList
 */
data class TripRecord(
    val tripID: Int,
    val routeID: Int,
    val routeShortName: String,
    val stopsList: List<StopInfo>,
    val routeStartTime: LocalTime,
    val routeEndTime: LocalTime
)

/**
 * A data class that holds all the necessary information for each stop that's a part of a trip.
 */
data class StopInfo(
    val stopID: Int,
    val arrivalTime: LocalTime,
    val departureTime: LocalTime,
    val stopSequence: Int
)

/**
 * A data class representing a potential route that was generated under time constraints and a
 * specified start and end location.
 * - tripID is a unique identifier and likely will not be displayed
 * - routeShortName is meant to be the visualizer that tells the user what bus to board
 * - routeInfo is a List containing all the stops that are a part of the route
 * - routeStartTime denotes when the route begins in reference to the start location
 * - routeEndTime denotes when the route ends in reference to the end location
 * The times above do not factor in walking times.
 */
data class GeneratedRoute(
    val tripID: Int,
    val routeShortName: String,
    val routeInfo: List<StopOnRoute>,
    val routeStartTime: LocalTime,
    val routeEndTime: LocalTime
)

/**
 * A data class representing individual stops that, when put together, form a route which helps get
 * the user from a designated start to end location. This was designed with what information needed
 * to be displayed on the UI in mind. You can easily add more fields to this class depending on what
 * you need to be able to access and/or display. Consult the CSVHandler Class Diagram in the wiki
 * to see the full available list of fields.
 */
data class StopOnRoute(
    val stopID: Int,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    val arrivalTime: LocalTime,
    val departureTime: LocalTime,
    val stopSequence: Int
)


/**
 * RouteFinder generates potential transit routes based on user inputs for starting stops, ending stops and available data.
 * It initializes data structures, fetches transit data, and provides functions for route generation and filtering.
 *
 * Key properties include sets for agency IDs and maps for quick access to routes, trips, and stop times.
 *
 * The `routeWorkhorse` function finds potential routes based on start and end locations, selected time, and valid agency IDs.
 *
 * Additionally, helper functions handle tasks such as generating potential routes, filtering routes, and converting time strings.
 */
object RouteFinder {
    private val agencies = CSVHandler.getAgencies()
    private val routes = CSVHandler.getRoutes()
    private val trips = CSVHandler.getTrips()
    private val stopTimes = CSVHandler.getStopTimes()
    private val stops = CSVHandler.getStops()

    // Place All Valid agencyIDs in this Set.
    val selectedAgencyIds = mutableSetOf(33)

    // Allows for quicker lookups and easier data access.
    private val routeByRouteID = routes.associateBy { it.routeID }
    private val tripByTripID = trips.associateBy { it.tripID }
    private val stopTimesByTripID = stopTimes.groupBy { it.tripID }

    /**
     * A workhorse function that calls the other functions in the RouteFinder object to find
     * and return a list of optimized potential routes to use. The list of routes is sorted under
     * certain criterion (which can be adjusted in the individual  themselves).
     *
     * @param startLocation - The starting location specified by the user
     * @param endLocation - The ending location specified by the user
     * @param selectedTime - The earliest time the user wants the route to start at, in HH:MM
     *                       format
     * @param validAgencyIDs - The set of AgencyIDs picked by the user, whose stops can be used to
     *                         generate the route
     * @return a list of GeneratedRoute objects representing potential routes the user can choose
     *         from
     */
    fun routeWorkhorse(
        startLocation: Place,
        endLocation: Place,
        selectedTime: String,
        validAgencyIDs: Set<Int>
    ): List<GeneratedRoute> {
        // If the starting and ending locations are the same don't even bother calculating
        // the route and just return a blank list.
        if (startLocation.lat == endLocation.lat && startLocation.lon == endLocation.lon) {
            Log.e("Route Generation", "Error: start and end location have the same coords.")
            return emptyList()
        }

        val validTripIDs = findAllValidTripIDs(validAgencyIDs)
        Log.i("Route Generation", "Valid TripIDs found: COMPLETE (Stage 1/8)")
        Log.i("# of valid TripIDs", "" + validTripIDs.size)

        val tripRecords = generateTripRecords(validTripIDs)
        Log.i("Route Generation", "Trip Records generated: COMPLETE (Stage 2/8)")
        Log.i("# of Trip Records", "" + tripRecords.size)

        val filteredTripRecords = filterTripRecordsByTime(tripRecords, selectedTime)
        Log.i("Route Generation", "Trip records filtered by time: COMPLETE (Stage 3/8)")
        Log.i("# of Filtered Trip Records", "" + filteredTripRecords.size)

        val validStopIDs = findAllValidStopIDs(validAgencyIDs)
        Log.i("Route Generation", "Valid StopIDs found: COMPLETE (Stage 4/8)")
        Log.i("# of valid StopIDs", "" + validStopIDs.size)

        val startStopIDs = findNearbyStopIDs(startLocation, validStopIDs)
        Log.i("Route Generation", "Starting StopIDs found: COMPLETE (Stage 5/8)")
        Log.i("# of start StopIDs", "" + startStopIDs.size)

        val endStopIDs = findNearbyStopIDs(endLocation, validStopIDs)
        Log.i("Route Generation", "Ending StopIDs found: COMPLETE (Stage 6/8)")
        Log.i("# of end StopIDs", "" + startStopIDs.size)

        val potentialRoutes = generatePotentialRoutes(filteredTripRecords, startStopIDs, endStopIDs, selectedTime)
        Log.i("Route Generation", "Generate potential routes: COMPLETE (Stage 7/8)")
        Log.i("# of generated routes", "" + potentialRoutes.size)

        val filteredRoutes = filterRoutes(potentialRoutes, startLocation, endLocation)
        Log.i("Route Generation", "Filter potential routes: COMPLETE (Stage 8/8)")
        Log.i("# of filtered routes", "" + filteredRoutes.size)

        return filteredRoutes
    }

    /**
     * Generates potential transit routes based on filtered trip records, start and end stop IDs,
     * and a selected time.
     *
     * @param filteredTripRecords - Set of trip records filtered based on user criteria
     * @param startStopIDs - Set of start stop IDs
     * @param endStopIDs - Set of end stop IDs
     * @param selectedTime - Selected time in HH:MM format
     * @return List of GeneratedRoute objects representing potential transit routes
     */
    private fun generatePotentialRoutes(
        filteredTripRecords: Set<TripRecord>,
        startStopIDs: Set<Int>,
        endStopIDs: Set<Int>,
        selectedTime: String
    ): List<GeneratedRoute> {
        val generatedRoutes = mutableListOf<GeneratedRoute>()
        val stopsMap = stops.associateBy { it.stopID }
        val timeBoundary = parseStringToLocalTime("$selectedTime:00")
        val timeBoundaryPlus30Minutes = timeBoundary.plusHours(1)

        filteredTripRecords.forEach { tripRecord ->
            val stopsList = tripRecord.stopsList

            // Iterate through stopsList to find all valid start-end combinations
            stopsList.forEachIndexed { startIndex, startStop ->
                if (startStop.stopID in startStopIDs) {
                    // For each valid start, look for a valid end that comes after it
                    stopsList.subList(startIndex + 1, stopsList.size).forEach { endStop ->
                        if (endStop.stopID in endStopIDs && endStop.stopSequence > startStop.stopSequence) {
                            // Generate route info from startIndex to the index of endStop
                            val routeInfo = stopsList.subList(startIndex, stopsList.indexOf(endStop) + 1).mapNotNull { stopInfo ->
                                stopsMap[stopInfo.stopID]?.let {
                                    StopOnRoute(
                                        stopID = it.stopID,
                                        stopName = it.stopName,
                                        stopLat = it.stopLat,
                                        stopLon = it.stopLon,
                                        arrivalTime = stopInfo.arrivalTime,
                                        departureTime = stopInfo.departureTime,
                                        stopSequence = stopInfo.stopSequence
                                    )
                                }
                            }
                            /*
                             * Create the route only if the routeStartTime is within the specified
                             * time window
                             */
                            if (routeInfo.isNotEmpty() && routeInfo.first().arrivalTime.isAfter(timeBoundary) &&
                                routeInfo.first().arrivalTime.isBefore(timeBoundaryPlus30Minutes)) {
                                val route = GeneratedRoute(
                                    tripID = tripRecord.tripID,
                                    routeShortName = tripRecord.routeShortName,
                                    routeInfo = routeInfo,
                                    routeStartTime = routeInfo.first().arrivalTime,
                                    routeEndTime = routeInfo.last().departureTime
                                )
                                generatedRoutes.add(route)
                            }
                        }
                    }
                }
            }
        }
        return generatedRoutes
    }

    /**
     * Filters generated transit routes based on start and end locations.
     *
     * @param generatedRoutes - List of generated transit routes
     * @param startLocation - Starting location
     * @param endLocation - Ending location
     * @return Filtered list of transit routes
     */
    private fun filterRoutes(
        generatedRoutes: List<GeneratedRoute>,
        startLocation: Place,
        endLocation: Place
    ): List<GeneratedRoute> {
        // Edge case where no routes get generated. Should lead to a dialog getting displayed.
        if (generatedRoutes.isEmpty()) return emptyList()

        // Step 1: Sort by route start time, then by duration
        val sortedRoutes = generatedRoutes
            .sortedWith(compareBy<GeneratedRoute> { it.routeStartTime }
                .thenBy { java.time.Duration.between(it.routeStartTime, it.routeEndTime).toMinutes() })

        // Step 2: Filter for unique tripIDs with preference to least sum distance for start and
        // end stops
        val uniqueRoutes = sortedRoutes
            .groupBy { it.tripID }
            .mapValues { (_, routes) ->
                if (routes.size > 1) {
                    routes.minByOrNull { route ->
                        val startStop = route.routeInfo.first()
                        val endStop = route.routeInfo.last()
                        calculateSphericalDistance(startLocation.lat, startLocation.lon, startStop.stopLat, startStop.stopLon) +
                                calculateSphericalDistance(endLocation.lat, endLocation.lon, endStop.stopLat, endStop.stopLon)
                    }
                } else {
                    routes.first()
                }
            }.values.toList()
        return uniqueRoutes.filterNotNull()
    }

    /**
     * Finds all valid trip IDs based on a set of valid agency IDs.
     *
     * @param validAgencyIDs - Set of valid agency IDs
     * @return Set of valid trip IDs
     */
    private fun findAllValidTripIDs(validAgencyIDs: Set<Int>): Set<Int> {
        val validRoutes = routeByRouteID.values.filter { it.agencyID in validAgencyIDs }
        val validRouteIDs = validRoutes.map { it.routeID }.toSet()

        return validRouteIDs.flatMap { routeID ->
            tripByTripID.values.filter { it.routeID == routeID }.map { it.tripID }
        }.toSet()
    }

    /**
     * Generates trip records based on a set of valid trip IDs.
     *
     * @param validTripIDs - Set of valid trip IDs
     * @return Set of generated trip records
     */
    private fun generateTripRecords(validTripIDs: Set<Int>): Set<TripRecord> {
        return validTripIDs.mapNotNull { tripID ->
            tripByTripID[tripID]?.let { trip ->
                routeByRouteID[trip.routeID]?.let { route ->
                    stopTimesByTripID[tripID]?.sortedBy { it.stopSequence }?.let { sortedStops ->
                        if (sortedStops.isNotEmpty()) {
                            val stopsList = sortedStops.map { stop ->
                                StopInfo(
                                    stopID = stop.stopID,
                                    arrivalTime = parseStringToLocalTime(stop.arrivalTime),
                                    departureTime = parseStringToLocalTime(stop.departureTime),
                                    stopSequence = stop.stopSequence
                                )
                            }

                            val routeStartTime = parseStringToLocalTime(sortedStops.first().departureTime)
                            val routeEndTime = parseStringToLocalTime(sortedStops.last().arrivalTime)

                            TripRecord(
                                tripID = trip.tripID,
                                routeID = route.routeID,
                                routeShortName = route.routeShortName,
                                stopsList = stopsList,
                                routeStartTime = routeStartTime,
                                routeEndTime = routeEndTime
                            )
                        } else null
                    }
                }
            }
        }.toSet()
    }

    /**
     * Filters trip records based on a selected time.
     *
     * @param tripRecords - Set of trip records to filter
     * @param selectedTime - Selected time in HH:MM format
     * @return Set of filtered trip records
     */
    private fun filterTripRecordsByTime(
        tripRecords: Set<TripRecord>,
        selectedTime: String
    ): Set<TripRecord> {
        val timeBoundary = parseStringToLocalTime("$selectedTime:00")
        return tripRecords.filter { it.routeStartTime.isAfter(timeBoundary) }.toSet()
    }

    /**
     * Finds all valid stop IDs based on a set of valid agency IDs.
     *
     * @param validAgencyIDs - Set of valid agency IDs
     * @return Set of valid stop IDs
     */
    private fun findAllValidStopIDs(validAgencyIDs: Set<Int>): Set<Int> {
        val validRouteIDs = routes.filter { it.agencyID in validAgencyIDs }.map { it.routeID }.toSet()

        val validTripIDs = validRouteIDs.flatMap { routeID ->
            tripByTripID.values.filter { it.routeID == routeID }.map { it.tripID }
        }.toSet()

        return validTripIDs.flatMap { tripID ->
            stopTimesByTripID[tripID]?.map { it.stopID } ?: emptyList()
        }.toSet()
    }

    /**
     * Finds nearby stop IDs based on a location and a set of valid stop IDs.
     *
     * @param location - Location to find nearby stops around
     * @param validStopIDs - Set of valid stop IDs
     * @return Set of nearby stop IDs
     */
    private fun findNearbyStopIDs(location: Place, validStopIDs: Set<Int>): Set<Int> {
        val validStops = stops.filter { it.stopID in validStopIDs }
        val closestStops = PriorityQueue<Pair<Int, Double>>(compareByDescending { it.second })

        validStops.forEach { stop ->
            val distance = calculateSphericalDistance(location.lat, location.lon, stop.stopLat, stop.stopLon)
            closestStops.add(stop.stopID to distance)

            // Ensure the priority queue never holds more than 3 elements.
            if (closestStops.size > 5) {
                closestStops.poll()
            }
        }
        return closestStops.map { it.first }.toSet()
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