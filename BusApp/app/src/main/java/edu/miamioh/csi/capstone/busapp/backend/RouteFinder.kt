package edu.miamioh.csi.capstone.busapp.backend

import java.time.LocalTime

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

    fun findAllValidTripIDs(
        validAgencyIDs: Set<Int>,
        routes: List<Route>,
        trips: List<Trip>
    ): Set<Int> {
        // First, find all routes that belong to the provided agency IDs
        val validRouteIDs = routes.filter { it.agencyID in validAgencyIDs }.map { it.routeID }.toSet()

        // Then, find all trips that are associated with these valid route IDs
        return trips.filter { it.routeID in validRouteIDs }.map { it.tripID }.toSet()
    }

    fun generateTripRecords(
        validTripIDs: Set<Int>
    ): Set<TripRecord> {
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