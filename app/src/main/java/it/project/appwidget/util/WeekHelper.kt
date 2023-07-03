package it.project.appwidget.util


import it.project.appwidget.database.TrackSession
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Classe statica che offre metodi utili per calcolare archi temporali a partire dal tempo fornito in millisecondi
 */
class WeekHelper {
    companion object {

        /**
         * Dato un time in millisecondi fornisce l'intervallo giornaliero in cui esso si trova, ovvero dalle 00:00 alle 23:59 del giorno selezionato
         * @param timestamp in millisecondi
         * @return Una coppia di valori startOfDay e endOfDay che indicano il range corrispondente
         */
        fun getDayRange(timestamp: Long): Pair<Long, Long> {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)

            val endOfDay = calendar.timeInMillis

            return Pair(startOfDay, endOfDay)
        }

        /**
         * Dato un time in millisecondi fornisce l'intervallo settimanale in cui esso si trova, ovvero da Lunedì 00:00 a Domenica 23:59 di quella settimana
         * @param timestamp in millisecondi
         * @return Una coppia di valori startOfWeek e endOfWeek che indicano il range corrispondente
         */
        fun getWeekRange(timestamp: Long): Pair<Long, Long> {
            val calendar = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                timeInMillis = timestamp
            }

            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startOfWeek = calendar.timeInMillis

            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)

            val endOfWeek = calendar.timeInMillis

            return Pair(startOfWeek, endOfWeek)
        }

        /**
         * Dato un time in millisecondi fornisce l'intervallo mensile in cui esso si trova, ovvero dal primo giorno del mese
         * alle 00:00 all'ultimo giorno del mese alle 23:59
         * @param timestamp in millisecondi
         * @return Una coppia di valori startOfMonth e endOfMonth che indicano il range corrispondente
         */
        fun getMonthRange(timestamp: Long): Pair<Long, Long> {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }

            // Imposta la data al primo giorno del mese corrispondente al timestamp
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startOfMonth = calendar.timeInMillis

            // Imposta la data all'ultimo giorno del mese corrispondente al timestamp
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)

            val endOfMonth = calendar.timeInMillis

            return Pair(startOfMonth, endOfMonth)
        }


        /**
         * Dato un range settimanale in millisecondi ritorna l'intervallo della settimana precedente
         * @param weekRange: coppia di Long in millisecondi che indicano il range
         * @return Una coppia di valori startOfPreviousWeek e endOfPreviousWeek che indicano la settimana precedente
         */
        fun getPreviousWeekRange(weekRange: Pair<Long, Long>): Pair<Long, Long> {
            val (startOfWeek, endOfWeek) = weekRange

            val startOfPreviousWeek =
                startOfWeek - 7 * 24 * 60 * 60 * 1000 // Sottrai una settimana in millisecondi
            val endOfPreviousWeek =
                endOfWeek - 7 * 24 * 60 * 60 * 1000 // Sottrai una settimana in millisecondi

            return Pair(startOfPreviousWeek, endOfPreviousWeek)
        }

        /**
         * Dato un range settimanale in millisecondi ritorna l'intervallo della settimana successiva
         * @param weekRange: coppia di Long in millisecondi che indicano il range
         * @return Una coppia di valori long startOfNextWeek e endOfNextWeek che indicano la settimana successiva
         */
        fun getNextWeekRange(weekRange: Pair<Long, Long>): Pair<Long, Long> {
            val (startOfWeek, endOfWeek) = weekRange

            val startOfNextWeek =
                startOfWeek + 7 * 24 * 60 * 60 * 1000 // Somma una settimana in millisecondi
            val endOfNextWeek =
                endOfWeek + 7 * 24 * 60 * 60 * 1000 // Somma una settimana in millisecondi

            return Pair(startOfNextWeek, endOfNextWeek)
        }

        /**
         * Ritorna intero corrispondente al giorno della settimana (es: 0 = lunedì, 1 = martedì)
         * @param timestamp: Unix time che si vuole convertire in intero 0-6
         * @return: un intero tra 0 (Lunedì) e 6 (Domenica)
         */
        fun getNumberDayOfWeek(timestamp: Long): Int {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            // Calendar.DAY_OF_WEEK ritorna un intero da 1 a 7 prendendo come giorno di partenza Domenica.
            // Per l'utilizzo che ne facciamo è meglio una rappresentazione da 0 a 6 partendo da Lunedì
            var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2
            if (dayOfWeek < 0) {
                dayOfWeek += 7
            }

            return dayOfWeek
        }

        fun getStringDayOfWeek(time: Long): String {
            return when (getNumberDayOfWeek(time)) {
                0 -> "Lunedì"
                1 -> "Martedì"
                2 -> "Mercoledì"
                3 -> "Giovedì"
                4 -> "Venerdì"
                5 -> "Sabato"
                6 -> "Domenica"
                else -> throw IllegalArgumentException("Numero non valido. Deve essere compreso tra 0 e 6.")
            }
        }

        /**
         * Dato un time in millisecondi e un formato ritorna la data sotto forma di stringa
         * @param milliSeconds: indica il time da convertire in data.
         * @param dateFormat: indica il formato da utilizzare nella conversione
         * @return: un intero tra 0 (Lunedì) e 6 (Domenica)
         */
        fun getDate(milliSeconds: Long, dateFormat: String?): String {
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat(dateFormat)

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = milliSeconds
            return formatter.format(calendar.time)
        }

        /**
         * Restituisce una lista di stringhe contenente la rappresentazione dei giorni compresi tra i valori
         * from e to nel formato gg/mm
         * @param from: Unix time di partenza
         * @param to: Unix time limite
         * @return: Un ArrayList di Stringhe con i valori dei giorni compresi nel formato gg/mm
         */
        fun getDateList(from: Long, to: Long): ArrayList<String> {
            val dateList = ArrayList<String>()
            val calendar = Calendar.getInstance()

            // Imposto partenza del calendario da from
            calendar.timeInMillis = from

            /* A questo punto il calendario punta al giorno from. Incrementiamo il tempo del calendario
        di giorno in giorno e convertiamo il tempo in formato giorno/mese */

            // Creo formattazione data
            val dateFormatting = SimpleDateFormat("dd/MM")
            while (calendar.timeInMillis <= to) {    // Finchè l'attuale tempo del calendario è minore del limite massimo
                val date =
                    dateFormatting.format(calendar.time) // Converto data attuale del calendario in stringa formattata
                dateList.add(date) // Aggiungo data a lista date
                calendar.add(Calendar.DAY_OF_WEEK, 1) // Incremento il tempo di un giorno
            }
            return dateList
        }

        /**
         * Converte lista di [TrackSession] in lista di distanze (in km) sommate giorno per giorno.
         * @param weekSession Lista di sessioni in una settimana
         * @return Una lista di Double contenente la somma delle distanze sommate in base al giorno in km. Restituisce
         * sempre una lista di dimensione 7.
         */
        fun convertTrackSessionInDistanceArray(weekSession: ArrayList<TrackSession>): ArrayList<Double> {
            // Inizializzo lista di dimensione 7 con valori azzerati
            val distanceList: ArrayList<Double> = arrayListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            for (session in weekSession) {
                // Aggiorno la distanza totale percorsa giorno per giorno (in km)
                distanceList[getNumberDayOfWeek(session.startTime)] += session.distance / 1000
            }
            return distanceList
        }

        /**
         * Converte lista di [TrackSession] in lista di chilocalorie sommate giorno per giorno.
         * @param weekSession Lista di sessioni in una settimana
         * @return Una lista di Int contenente la somma delle calorie sommate in base al giorno in kcal. Restituisce
         * sempre una lista di dimensione 7.
         */
        fun convertTrackSessionInCaloriesArray(weekSession: ArrayList<TrackSession>): ArrayList<Int> {
            // Inizializzo lista di dimensione 7 con valori azzerati
            val kcalList: ArrayList<Int> = arrayListOf(0, 0, 0, 0, 0, 0, 0)
            for (session in weekSession) {
                // Aggiorno le calorie totali giorno per giorno (in kcal)
                kcalList[getNumberDayOfWeek(session.startTime)] += session.kcal
            }
            return kcalList
        }

        /**
         * Converte lista di [TrackSession] in lista di minuti sommate giorno per giorno.
         * @param weekSession Lista di sessioni in una settimana
         * @return Una lista di Int contenente la somma dei minuti sommate in base al giorno in min. Restituisce
         * sempre una lista di dimensione 7.
         */
        fun convertTrackSessionInDurationArray(weekSession: ArrayList<TrackSession>): ArrayList<Double> {
            // Inizializzo lista di dimensione 7 con valori azzerati
            val durationList: ArrayList<Double> = arrayListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            for (session in weekSession) {
                // Aggiorno la durata totali giorno per giorno (in min)
                durationList[getNumberDayOfWeek(session.startTime)] += (session.duration.toDouble() / (1000.0 * 60.0))
            }
            return durationList
        }
    }

}


