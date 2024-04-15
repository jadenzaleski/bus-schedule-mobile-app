package edu.miamioh.csi.capstone.busapp.backend

object UnitConverter {
    var useMiles: Boolean = false  // This should be dynamically set from the user's settings

    fun getDistanceUnit(): String = if (useMiles) "miles" else "kilometers"

    fun convertDistance(distanceInKm: Double): Double {
        return if (useMiles) distanceInKm * 0.621371 else distanceInKm
    }

    fun formatDistance(distanceInKm: Double): String {
        val convertedDistance = convertDistance(distanceInKm)
        return String.format("%.3f %s", convertedDistance, getDistanceUnit())
    }
}
