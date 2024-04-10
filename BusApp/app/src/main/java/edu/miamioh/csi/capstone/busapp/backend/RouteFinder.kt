package edu.miamioh.csi.capstone.busapp.backend

import java.time.LocalTime

data class TripRecord(
    val routeID: Int,
    val routeShortName: String,
    val tripID: Int,
    val stopsList: List<StopInfo>,
)

data class StopInfo(
    val stopID: Int,
    val arrivalTime: LocalTime,
    val departureTime: LocalTime,
    val stopSequence: Int
)