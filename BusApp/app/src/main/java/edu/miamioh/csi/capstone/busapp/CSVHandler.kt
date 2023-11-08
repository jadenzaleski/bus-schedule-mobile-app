package edu.miamioh.csi.capstone.busapp

import android.util.Log
import com.opencsv.CSVReaderBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Represents each agency in agency.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class Agency(
    val agencyID: Int,
    val agencyName: String,
    val agencyUrl: String,
    val agencyTimeZone: String,
    val agencyPhone: String
)

/**
 * Represents each service in calendar.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class ServiceSchedule(
    val serviceID: Int,
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean,
    val startDate: Int,
    val endDate: Int
)

/**
 * Represents each date service was preformed in calendar_dates.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class ServiceDate(
    val serviceID: Int,
    val date: Int,
    val exceptionType: Int
)

/**
 * Represents the information about the publisher of the data. From in feed_info.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class Info(
    val feedPublisherName: String,
    val feedPublisherUrl: String,
    val feedLang: String,
    val feedStartDate: String,
    val feedEndDate: String,
    val feedVersion: String
)

/**
 * Represents a route from all the available routes. From in routes.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class Route(
    val agencyID: Int,
    val routeID: Int,
    val routeShortName: String,
    val routeLongName: String,
    val routeType: Int
)

/**
 * Represents a stop and where it is in the route. From in stop_times.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class StopTime(
    val tripID: Int,
    val stopID: Int,
    val arrivalTime: String,
    val departureTime: String,
    val stopSequence: Int,
    val stopHeadsign: String,
    val routeShortName: String,
    val pickup: Boolean,
    val dropoff: Boolean,
)

/**
 * Represents each stop and provides world coordinates for the stop. From in stops.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class Stop(
    val stopId: Int,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double,
    val stopTimezone: String
)

/**
 * Represents each trip and its name. From in trips.csv, which is imported from
 * [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni)
 */
data class Trip(
    val routeID: Int,
    val tripID: Int,
    val serviceID: Int,
    val tripShortName: String,
    val routeShortName: String,
)

/**
 * Provides functionality to parse and initialize data from CSV files.
 * It is designed to handle specific CSV file formats from [CORe](https://mobilita.regione.calabria.it/Informazioni/Informazioni).
 *
 * @property agencies List of agencies.
 * @property calendar List of service schedules.
 * @property serviceDates List of service dates.
 * @property info Feed information.
 * @property routes List of routes.
 * @property stopTimes List of stop times.
 * @property stops List of stops.
 * @property trips List of trips.
 */
object CSVHandler {
    private val agencies: MutableList<Agency> = mutableListOf()
    private val calendar: MutableList<ServiceSchedule> = mutableListOf()
    private val serviceDates: MutableList<ServiceDate> = mutableListOf()
    private var info: Info = Info(
        feedPublisherName = "",
        feedPublisherUrl = "",
        feedLang = "",
        feedStartDate = "",
        feedEndDate = "",
        feedVersion = ""
    )
    private val routes: MutableList<Route> = mutableListOf()
    private val stopTimes: MutableList<StopTime> = mutableListOf()
    private val stops: MutableList<Stop> = mutableListOf()
    private val trips: MutableList<Trip> = mutableListOf()

    /**
     * Parses a CSV line into a list of string values. This function takes into account quotes within
     * each value if necessary.
     *
     * @param line The input CSV line to be parsed.
     * @return A list of string values extracted from the CSV line.
     */
    private fun parseCSVLine(line: String): List<String> {
        val parts = mutableListOf<String>()
        val builder = StringBuilder()
        var insideQuotes = false

        // go through each char and see if its a quote. If true, toggle the flag so the next
        // comma does not spilt the string into a new value.
        for (char in line) {
            when {
                char == '"' -> {
                    insideQuotes = !insideQuotes
                }

                char == ',' && !insideQuotes -> {
                    parts.add(builder.toString())
                    builder.clear()
                }

                else -> {
                    builder.append(char)
                }
            }
        }
        parts.add(builder.toString())
        return parts
    }

    /**
     * Initializes the list of agencies from a CSV file.
     *
     * @param inputStream An input stream for the agency.csv file.
     * @return `true` if the agencies are successfully loaded, `false` otherwise.
     */
    private fun agenciesInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            var line: String? = reader.readLine()
            // go through the entire file as long as the line exists
            while (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 5) {
                    // make object
                    val agency = Agency(
                        agencyID = parts[0].toInt(),
                        agencyName = parts[1],
                        agencyUrl = parts[2],
                        agencyTimeZone = parts[3],
                        agencyPhone = parts[4]
                    )
                    // add to main list
                    agencies.add(agency)
                }
                // get next line
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the calender list from a CSV file.
     *
     * @param inputStream An input stream for the calendar.csv file.
     * @return `true` if the service schedules are successfully loaded, `false` otherwise.
     */
    private fun calendarInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            var line: String? = reader.readLine()
            // go through the entire file as long as the line exists
            while (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 10) {
                    // make object
                    val ss = ServiceSchedule(
                        serviceID = parts[0].toInt(),
                        monday = parts[1] == "1",
                        tuesday = parts[2] == "1",
                        wednesday = parts[3] == "1",
                        thursday = parts[4] == "1",
                        friday = parts[5] == "1",
                        saturday = parts[6] == "1",
                        sunday = parts[7] == "1",
                        startDate = parts[8].toInt(),
                        endDate = parts[9].toInt()
                    )
                    // add to main list
                    calendar.add(ss)
                }
                // get next line
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the list of service dates from a CSV file.
     *
     * @param inputStream An input stream for the calendar_dates.csv file.
     * @return `true` if the dates are successfully loaded, `false` otherwise.
     */
    private fun serviceDatesInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            var line: String? = reader.readLine()
            // go through the entire file as long as the line exists
            while (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 3) {
                    // make object
                    val sd = ServiceDate(
                        serviceID = parts[0].toInt(),
                        date = parts[1].toInt(),
                        exceptionType = parts[2].toInt()
                    )
                    // add to main list
                    serviceDates.add(sd)
                }
                // get next line
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the publisher info from a CSV file.
     *
     * @param inputStream An input stream for the feed_info.csv file.
     * @return `true` if info is successfully loaded, `false` otherwise.
     */
    private fun infoInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            val line: String? = reader.readLine()
            // just get the next line
            if (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 6) {
                    // make object
                    info = Info(
                        feedPublisherName = parts[0],
                        feedPublisherUrl = parts[1],
                        feedLang = parts[2],
                        feedStartDate = parts[3],
                        feedEndDate = parts[4],
                        feedVersion = parts[5]
                    )
                }
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the routes from a CSV file.
     *
     * @param inputStream An input stream for the routes.csv file.
     * @return `true` if the routes successfully loaded, `false` otherwise.
     */
    private fun routesInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            var line: String? = reader.readLine()
            // go through the entire file as long as the line exists
            while (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 5) {
                    // make object
                    val r = Route(
                        agencyID = parts[0].toInt(),
                        routeID = parts[1].toInt(),
                        routeShortName = parts[2],
                        routeLongName = parts[3],
                        routeType = parts[4].toInt()
                    )
                    // add to main list
                    routes.add(r)
                }
                // get next line
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the stop times from a CSV file.
     *
     * @param inputStream An input stream for the stop_times.csv file.
     * @return `true` stop times are successfully loaded, `false` otherwise.
     */
    private fun stopTimesInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            var line: String? = reader.readLine()
            // go through the entire file as long as the line exists
            while (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 9) {
                    // make object
                    val st = StopTime(
                        tripID = parts[0].toInt(),
                        stopID = parts[1].toInt(),
                        arrivalTime = parts[2],
                        departureTime = parts[3],
                        stopSequence = parts[4].toInt(),
                        stopHeadsign = parts[5],
                        routeShortName = parts[6],
                        pickup = parts[7] == "1",
                        dropoff = parts[8] == "1",
                    )
                    // add to main list
                    stopTimes.add(st)
                }
                // get next line
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the stops info from a CSV file.
     *
     * @param inputStream An input stream for the stops.csv file.
     * @return `true` if stop data is successfully loaded, `false` otherwise.
     */
    private fun stopsInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            var line: String? = reader.readLine()
            // go through the entire file as long as the line exists
            while (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 5) {
                    // make object
                    val s = Stop(
                        stopId = parts[0].toInt(),
                        stopName = parts[1],
                        stopLat = parts[2].toDouble(),
                        stopLon = parts[3].toDouble(),
                        stopTimezone = parts[4]
                    )
                    // add to main list
                    stops.add(s)
                }
                // get next line
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the publisher trips from a CSV file.
     *
     * @param inputStream An input stream for the trips.csv file.
     * @return `true` if the trips are successfully loaded, `false` otherwise.
     */
    private fun tripsInit(inputStream: InputStream): Boolean {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip the header line
            reader.readLine()
            // get the next line
            var line: String? = reader.readLine()
            // go through the entire file as long as the line exists
            while (line != null) {
                // array of parts of the line
                val parts = parseCSVLine(line)
                // make sure the line is valid
                if (parts.size == 5) {
                    // make object
                    val t = Trip(
                        routeID = parts[0].toInt(),
                        tripID = parts[1].toInt(),
                        serviceID = parts[2].toInt(),
                        tripShortName = parts[3],
                        routeShortName = parts[4]
                    )
                    // add to main list
                    trips.add(t)
                }
                // get next line
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("CSVHandler", "An error occurred: ${e.message}")
            return false
        } catch (e: FileNotFoundException) {
            Log.e("CSVHandler", "A FileNotFoundException error occurred: ${e.message}")
            return false
        }
        // if all ends well return true
        return true
    }

    /**
     * Initializes the CSVHandler with data from various CSV files.
     *
     * @param agenciesFilePath The input stream for the agencies CSV file.
     * @param calendarFilePath The input stream for the calendar CSV file.
     * @param calendarDatesFilePath The input stream for the service dates CSV file.
     * @param feedInfoFilePath The input stream for the feed information CSV file.
     * @param routesFilePath The input stream for the routes CSV file.
     * @param stopTimesFilePath The input stream for the stop times CSV file.
     * @param stopsFilePath The input stream for the stops CSV file.
     * @param tripsFilePath The input stream for the trips CSV file.
     * @return `true` if all data is successfully loaded, `false` otherwise.
     */
    fun initialize(
        agenciesFilePath: InputStream,
        calendarFilePath: InputStream,
        calendarDatesFilePath: InputStream,
        feedInfoFilePath: InputStream,
        routesFilePath: InputStream,
        stopTimesFilePath: InputStream,
        stopsFilePath: InputStream,
        tripsFilePath: InputStream
    ): Boolean {
        var result: Boolean = true
        if (!agenciesInit(agenciesFilePath)) {
            Log.e("CSVHandler", "Agencies failed to load properly from $agenciesFilePath")
            result = false
        } else {
            Log.i(
                "CSVHandler",
                "${getAgencies().size} Agencies successfully imported from $agenciesFilePath"
            );
        }
        if (!calendarInit(calendarFilePath)) {
            Log.e("CSVHandler", "Calendar failed to load properly from $calendarFilePath")
            result = false
        } else {
            Log.i(
                "CSVHandler",
                "${getCalendar().size} Calendar items successfully imported from $calendarFilePath"
            );
        }
        if (!serviceDatesInit(calendarDatesFilePath)) {
            Log.e("CSVHandler", "Service dates failed to load properly from $calendarDatesFilePath")
            result = false
        } else {
            Log.i(
                "CSVHandler",
                "${getServiceDates().size} Service dates successfully imported from $calendarDatesFilePath"
            );
        }
        if (!infoInit(feedInfoFilePath)) {
            Log.e("CSVHandler", "Feed info failed to load properly from $feedInfoFilePath")
            result = false
        } else {
            Log.i("CSVHandler", "Feed info successfully imported from $feedInfoFilePath");
        }
        if (!routesInit(routesFilePath)) {
            Log.e("CSVHandler", "Routes failed to load properly from $routesFilePath")
            result = false
        } else {
            Log.i(
                "CSVHandler",
                "${getRoutes().size} Routes successfully imported from $routesFilePath"
            );
        }
        if (!stopTimesInit(stopTimesFilePath)) {
            Log.e("CSVHandler", "Stop times failed to load properly from $stopTimesFilePath")
            result = false
        } else {
            Log.i(
                "CSVHandler",
                "${getStopTimes().size} Stop times successfully imported from $stopTimesFilePath"
            );
        }
        if (!stopsInit(stopsFilePath)) {
            Log.e("CSVHandler", "Stops failed to load properly from $stopsFilePath")
            result = false
        } else {
            Log.i(
                "CSVHandler",
                "${getStops().size} Stops successfully imported from $stopsFilePath"
            );
        }
        if (!tripsInit(tripsFilePath)) {
            Log.e("CSVHandler", "Trips failed to load properly from $tripsFilePath")
            result = false
        } else {
            Log.i(
                "CSVHandler",
                "${getTrips().size} Trips successfully imported from $tripsFilePath"
            );
        }

        return result
    }

    /**
     * Gets the list of agencies.
     *
     * @see Agency
     * @return A list of Agency objects.
     */
    fun getAgencies(): List<Agency> {
        return agencies;
    }

    /**
     * Gets the list of service schedules.
     *
     * @see ServiceSchedule
     * @return A list of ServiceSchedule objects.
     */
    fun getCalendar(): List<ServiceSchedule> {
        return calendar;
    }

    /**
     * Gets the list of service dates.
     *
     * @see ServiceDate
     * @return A list of ServiceDate objects.
     */
    fun getServiceDates(): List<ServiceDate> {
        return serviceDates;
    }

    /**
     * Gets the publisher info.
     *
     * @see Info
     * @return A Info object.
     */
    fun getInfo(): Info {
        return info
    }

    /**
     * Gets the list of routes.
     *
     * @see Route
     * @return A list of Route objects.
     */
    fun getRoutes(): List<Route> {
        return routes
    }

    /**
     * Gets the list of stop times.
     *
     * @see StopTime
     * @return A list of StopTime objects.
     */
    fun getStopTimes(): List<StopTime> {
        return stopTimes
    }

    /**
     * Gets the list of stops.
     *
     * @see Stop
     * @return A list of Stop objects.
     */
    fun getStops(): List<Stop> {
        return stops
    }

    /**
     * Gets the list of trips.
     *
     * @see Trip
     * @return A list of Trip objects.
     */
    fun getTrips(): List<Trip> {
        return trips
    }
}