package it.project.appwidget.util

import android.location.Location
import java.lang.IllegalArgumentException

/**
 * Questa classe permette di convertire locations in stringhe e viceversa
 */
class LocationParser {

    companion object {

        /**
         * Converte oggetto Location in stringa
         */
        fun toString(location: Location): String {
            // Attributi principali
            val provider = location.provider.toString()
            val latitude = location.latitude.toString()
            val longitude = location.longitude.toString()
            val accuracy = location.accuracy.toString()
            val speed = location.speed.toString()
            val speedAccuracyMetersPerSecond = location.speedAccuracyMetersPerSecond.toString()
            val bearing = location.bearing.toString()
            val bearingAccuracyDegrees = location.bearingAccuracyDegrees.toString()
            val altitude = location.altitude.toString()
            val verticalAccuracyMeters = location.verticalAccuracyMeters.toString()
            val time = location.time.toString()
            val elapsedRealtimeNanos = location.elapsedRealtimeNanos.toString()
            val isMock = location.isMock.toString()

            val result =
                provider + "," +
                latitude + "," +
                longitude + "," +
                accuracy + "," +
                speed + "," +
                speedAccuracyMetersPerSecond + "," +
                bearing + "," +
                bearingAccuracyDegrees + "," +
                altitude + "," +
                verticalAccuracyMeters + "," +
                time + "," +
                elapsedRealtimeNanos + "," +
                isMock

            return result
        }

        /**
         * Converte stringa in Location
         */
        fun toLocation(string: String): Location{
            val paramList = string.split(",")
            if (paramList.size != 13){
                throw IllegalArgumentException("Errore. Il numero di parametri nella stringa location non coincide con il numero di parametri attesi")
            }
            val provider = paramList[0]
            val location = Location(provider)
            location.apply {
                latitude = paramList[1].toDouble()
                longitude = paramList[2].toDouble()
                accuracy = paramList[3].toFloat()
                speed = paramList[4].toFloat()
                speedAccuracyMetersPerSecond = paramList[5].toFloat()
                bearing = paramList[6].toFloat()
                bearingAccuracyDegrees = paramList[7].toFloat()
                altitude = paramList[8].toDouble()
                verticalAccuracyMeters = paramList[9].toFloat()
                time = paramList[10].toLong()
                elapsedRealtimeNanos = paramList[11].toLong()
                isMock = paramList[12].toBoolean()
            }

            return location
        }

    }
}