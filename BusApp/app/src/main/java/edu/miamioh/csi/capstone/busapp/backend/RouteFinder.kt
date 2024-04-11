package edu.miamioh.csi.capstone.busapp.backend

import android.util.Log
import edu.miamioh.csi.capstone.busapp.views.Place
import edu.miamioh.csi.capstone.busapp.views.calculateSphericalDistance
import java.time.LocalTime
import java.util.PriorityQueue

data class TripRecord(
    val tripID: Int,
    val routeID: Int,
    val routeShortName: String,
    val stopsList: List<StopInfo>,
    val routeStartTime: LocalTime,
    val routeEndTime: LocalTime
)

data class StopInfo(
    val stopID: Int,
    val arrivalTime: LocalTime,
    val departureTime: LocalTime,
    val stopSequence: Int
)

data class StopOnRoute(
    val stopID: Int,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    val arrivalTime: LocalTime,
    val departureTime: LocalTime
)

data class GeneratedRoute(
    val tripID: Int,
    val routeInfo: List<StopOnRoute>,
    val routeStartTime: LocalTime,
    val routeEndTime: LocalTime
)

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

    fun routeWorkhorse(
        startLocation: Place,
        endLocation: Place,
        selectedTime: String,
        validAgencyIDs: Set<Int>
    ) {
        var validTripIDs = findAllValidTripIDs(validAgencyIDs)
        Log.i("Route Generation", "Valid TripIDs found: COMPLETE (Stage 1/?)")
        Log.i("# of valid TripIDs", "" + validTripIDs.size)

        var tripRecords = generateTripRecords(validTripIDs)
        Log.i("Route Generation", "Trip Records generated: COMPLETE (Stage 2/?)")
        Log.i("# of Trip Records", "" + tripRecords.size)

        var filteredTripRecords = filterTripRecordsByTime(tripRecords, selectedTime)
        Log.i("Route Generation", "Trip records filtered by time: COMPLETE (Stage 3/?)")
        Log.i("# of Filtered Trip Records", "" + filteredTripRecords.size)

        var validStopIDs = findAllValidStopIDs(validAgencyIDs)
        Log.i("Route Generation", "Valid StopIDs found: COMPLETE (Stage 4/?)")
        Log.i("# of valid StopIDs", "" + validStopIDs.size)

        var startStopIDs = findNearbyStopIDs(startLocation, validStopIDs)
        Log.i("Route Generation", "Starting StopIDs found: COMPLETE (Stage 5/?)")
        Log.i("# of start StopIDs", "" + startStopIDs.size)

        var endStopIDs = findNearbyStopIDs(endLocation, validStopIDs)
        Log.i("Route Generation", "Ending StopIDs found: COMPLETE (Stage 6/?)")
        Log.i("# of end StopIDs", "" + startStopIDs.size)
    }

    fun findAllValidTripIDs(validAgencyIDs: Set<Int>): Set<Int> {
        // Directly access routes from the pre-computed map, eliminating the need to filter the entire routes list
        val validRoutes = routeByRouteID.values.filter { it.agencyID in validAgencyIDs }
        val validRouteIDs = validRoutes.map { it.routeID }.toSet()

        // Utilize the filtered validRouteIDs to directly access corresponding trips from the pre-computed map,
        // significantly reducing the number of operations
        return validRouteIDs.flatMap { routeID ->
            tripByTripID.values.filter { it.routeID == routeID }.map { it.tripID }
        }.toSet()
    }

    fun generateTripRecords(validTripIDs: Set<Int>): Set<TripRecord> {
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

    private fun filterTripRecordsByTime(tripRecords: Set<TripRecord>, selectedTime: String): Set<TripRecord> {
        val timeBoundary = parseStringToLocalTime("$selectedTime:00")
        return tripRecords.filter { it.routeStartTime.isAfter(timeBoundary) }.toSet()
    }

    fun findAllValidStopIDs(validAgencyIDs: Set<Int>): Set<Int> {
        // Leverage the pre-computed map to directly access routes by agencyID
        val validRouteIDs = routes.filter { it.agencyID in validAgencyIDs }.map { it.routeID }.toSet()

        // Directly access valid trips using the pre-computed tripByTripID map,
        // reducing the need to filter the trips list repeatedly
        val validTripIDs = validRouteIDs.flatMap { routeID ->
            tripByTripID.values.filter { it.routeID == routeID }.map { it.tripID }
        }.toSet()

        // Using the grouped stop times by tripID to efficiently collect valid stopIDs
        return validTripIDs.flatMap { tripID ->
            stopTimesByTripID[tripID]?.map { it.stopID } ?: emptyList()
        }.toSet()
    }

    fun findNearbyStopIDs(location: Place, validStopIDs: Set<Int>): List<Int> {
        // Pre-filter stops to include only validStopIDs to minimize distance calculations.
        val validStops = stops.filter { it.stopID in validStopIDs }

        // Priority queue to store the closest stops, ordered by distance.
        val closestStops = PriorityQueue<Pair<Int, Double>>(compareBy { it.second })

        validStops.forEach { stop ->
            val distance = calculateSphericalDistance(location.lat, location.lon, stop.stopLat, stop.stopLon)
            closestStops.add(stop.stopID to distance)

            // Keep only the three closest stops in the queue.
            if (closestStops.size > 5) {
                closestStops.poll() // Removes the farthest stop if more than 3 are stored.
            }
        }

        // Return stop IDs from the priority queue, sorted by distance.
        return closestStops.map { it.first }.sortedBy { stopId ->
            closestStops.find { it.first == stopId }?.second ?: Double.MAX_VALUE
        }
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