package it.project.appwidget.util

// Classe per elaborare i dati della sessione

class SessionDataProcessor {
    companion object {
        fun calculateActivityType(distance: Float, duration: Long): String {
            // Calcolo del tipo di attività in base alla distanza e alla durata
            return when {
                calculateAverageSpeed(distance,duration)  > 10 -> "Running"
                else -> "Walking"
            }
        }

        fun calculateAverageSpeed(distance: Float, duration: Long): Double {
            // Calcola la velocità media in base alla distanza e alla durata
            return if (duration > 0) {
                (distance / duration)*1000.toDouble()
            } else {
                0.0
            }
        }

    }
}
