package edu.miamioh.csi.capstone.busapp

object Graph {

    /**
     * Based on the list of user-selected agencies, returns the list
     * of ALL StopIds associated with those agencies.
     *
     * @param agencyIdList The list of active agencies selected by the user
     */
    fun findAllValidStopIdByAgencyId(
        agencyIdList: Set<Int>,
        agencies: List<Agency>,
        stops: List<Stop>,
        routes: List<Route>,
        trips: List<Trip>,
        stopTimes: List<StopTime>
    ): Set<Int> {
        // Filter routes by agency_id
        val filteredRoutes = routes.filter { it.agencyID in agencyIdList }

        // Use filtered routes to find corresponding trips
        val filteredTripIds = trips.filter { trip ->
            filteredRoutes.any { it.routeID == trip.routeID }
        }.map { it.tripID }.toSet()

        // Use those trips to find stop_ids in StopTimes
        val stopIds = stopTimes.filter { it.tripID in filteredTripIds }.map { it.stopID }.toSet()

        // Return the set of stop_ids without duplicates
        return stopIds
    }
}