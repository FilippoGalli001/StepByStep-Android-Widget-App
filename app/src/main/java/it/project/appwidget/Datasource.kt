package it.project.appwidget

import android.content.Context
import it.project.appwidget.database.AppDatabase
import it.project.appwidget.database.TrackSession
import it.project.appwidget.util.WeekHelper
import java.util.ArrayList

//Crea e restituisce l'array contentente le varie sessioni visualizzabili dall'utente
class Datasource(private val context: Context) {



    // Crea e ritorna array contenente il risultato della query getSessionIdsAndStartTimes
    fun getSessionListIdString(from: Long, to: Long): Array<Pair<Int, String>> {
        val trackSessionDao = AppDatabase.getInstance(context).trackSessionDao()
        val pairArray = mutableListOf<Pair<Int, String>>()
        val sessionIdStartTimes: List<TrackSession> = trackSessionDao.getTrackSessionsBetweenDates(from, to)

        for (sessionIdStartTime in sessionIdStartTimes) {
            val sessionId: Int = sessionIdStartTime.id
            val startTime: Long = sessionIdStartTime.startTime
            val dayStr = WeekHelper.getStringDayOfWeek(startTime)
            val format = "HH:mm"
            val date = WeekHelper.getDate(startTime, format)

            val pair = Pair(sessionId, "$dayStr: $date")
            pairArray.add(pair)

            println("sessionId: $sessionId, startTime: $startTime")
        }

        return pairArray.toTypedArray()
    }

    /**
     * Restituisce una lista di [TrackSession] comprese tra le date [from] e [to]
     * @param from Limite inferiore inclusivo alla data di [TrackSession]
     * @param to Limite superiore inclusivo alla data di [TrackSession]
     * @return Una lista non nulla di [TrackSession]
     */
    fun getSessionList(from: Long, to: Long): ArrayList<TrackSession> {
        val trackSessionDao = AppDatabase.getInstance(context).trackSessionDao()
        return ArrayList(trackSessionDao.getTrackSessionsBetweenDates(from, to))
    }


}
