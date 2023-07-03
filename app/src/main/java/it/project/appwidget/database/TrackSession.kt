package it.project.appwidget.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entità TrackSession. Memorizza informazioni sul percorso quali ora inizio e fine, distanza, velocità media,
 * velocità massima, tipologia attività
 */
@Entity(tableName = "track_sessions")
data class TrackSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /**
     * Ora di inizio attività registrata. Corrisponde alla Unix epoch time della prima location registrata.
     */
    val startTime: Long,

    /**
     * Ora di fine attività registrata. Corrisponde alla Unix epoch time dell'ultima location registrata.
     */
    var endTime: Long,

    /**
     * Durata attività registrata. Corrisponde alla differenza di tempo tra la prima e l'ultima location registrata.
     */
    var duration: Long,

    /**
     * Distanza totale percorsa in metri. Non è la differenza delle distanze tra la prima e l'ultima
     * location, ma piuttosto la somma delle distanze tra ogni location (in genere distanti circa 10m)
     */
    var distance: Double,

    /**
     * Velocità media della sessione della sessione ottenuta sommando la velocità media tra le varie location
     * e dividendo poi per il numero di location totali
     * NOTA: Il valore della speed non viene calcolato dall'emulatore e funziona soltanto su dispositivo fisico
     */
    var averageSpeed: Double,

    /**
     * Velocità più elevata tra tutte le location della sessione
     * NOTA: Il valore della speed non viene calcolato dall'emulatore e funziona soltanto su dispositivo fisico
     */
    var maxSpeed: Double,

    /**
     * Tipologia di attività (camminata o corsa) che dipende dalla velocità media totale della sessione
     */
    var activityType: String,

    /**
     * Kcalorie bruciate al termine della sessione. Il valore dipende dalla distanza percorsa e dal peso dell'utente
     */
    var kcal: Int
)
