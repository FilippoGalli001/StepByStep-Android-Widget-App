package it.project.appwidget.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackSessionDao {

    /**
     * Inserisce nuovo oggetto TrackSession nel database
     */
    @Insert
    fun insertSession(trackSession: TrackSession)

    /**
     * Rimuove oggetto TrackSession dal database
     */
    @Delete
    fun delete(trackSession: TrackSession)

    @Query("DELETE FROM track_sessions")
    fun deleteAllTracks()

    /**
     * Aggiorna oggetto TrackSession nel database
     */
    @Query("UPDATE track_sessions SET activityType = :activityType WHERE id = :sessionId")
    fun updateActivityType(sessionId: Int, activityType: String)

    /**
     * Restituisce tutti gli oggetti TrackSession nel database
     */
    @Query("SELECT * FROM track_sessions")
    fun getAllTrackSessions(): LiveData<List<TrackSession>> //TODO: dove usiamo il livedata?

    /**
     * Restituisce tutti gli oggetti TrackSession nel database tramite id
     */
    @Query("SELECT * FROM track_sessions WHERE id = :sessionId")
    fun getTrackSessionById(sessionId: Int): List<TrackSession>

    /**
     * Restituisce una lista di oggetti [TrackSession] nel database in cui lo Unix time della
     * prima location salvata è compreso tra [startTime] <= data <= [endTime].
     * @param startTime limite inferiore inclusivo alla data in Unix time
     * @param endTime limite superiore inclusivo alla data in Unix time
     * @return restituisce una lista di [TrackSession] che rispettano la condizione, o una lista vuota
     * se nessun elemento è stato trovato.
     */
    @Query("SELECT * FROM track_sessions WHERE startTime >= :startTime AND startTime <= :endTime")
    fun getTrackSessionsBetweenDates(startTime: Long, endTime: Long): List<TrackSession>


}
