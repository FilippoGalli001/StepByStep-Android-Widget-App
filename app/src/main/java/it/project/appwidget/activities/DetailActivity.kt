package it.project.appwidget.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.project.appwidget.R
import it.project.appwidget.database.AppDatabase
import it.project.appwidget.database.TrackSession
import it.project.appwidget.fragments.Stats
import it.project.appwidget.util.WeekHelper
import it.project.appwidget.widgets.ListWidget
import kotlinx.coroutines.launch
import java.text.DecimalFormat


/**
 * Questa activity mostra i dettagli della [TrackSession] selezionata, sia dal fragment [Stats] che dal
 * widget [ListWidget].
 */
class DetailActivity : AppCompatActivity() {

    /** Tag per messaggi di [Log] */
    private val mTAG = this::class.simpleName

    companion object {
        /** Chiave dell'Id della [TrackSession] da recuperare dal [Bundle] */
        const val ARG_SESSION_ID = "session:id"
    }

    /** [TextView] relativa alla data di inizio della [TrackSession] */
    private lateinit var tv_startData: TextView
    /** [TextView] relativa alla data di fine della [TrackSession] */
    private lateinit var tv_endData: TextView
    /** [TextView] relativa alla tipologia di [TrackSession] */
    private lateinit var tv_typeData: TextView
    /** [TextView] relativa alla distanza della [TrackSession] */
    private lateinit var tv_distanceData: TextView
    /** [TextView] relativa alla durata della [TrackSession] */
    private lateinit var tv_timeData: TextView
    /** [TextView] relativa alla velocità media della [TrackSession] */
    private lateinit var tv_avrSpeedData: TextView
    /** [TextView] relativa alle calorie della [TrackSession] */
    private lateinit var tv_calorieData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(mTAG, "Chiamato onCreate()")
        setContentView(R.layout.activity_session_detail)

        // Riferimenti alle Views
        tv_startData = findViewById(R.id.tv_startData)
        tv_endData = findViewById(R.id.tv_endData)
        tv_typeData = findViewById(R.id.tv_typeData)
        tv_distanceData = findViewById(R.id.tv_distanceData)
        tv_timeData = findViewById(R.id.tv_timeData)
        tv_avrSpeedData = findViewById(R.id.tv_avrSpeedData)
        tv_calorieData = findViewById(R.id.tv_caloriesData)


        // Ottengo Id della sessione cliccata
        val sessionId = intent.getIntExtra(ARG_SESSION_ID, -1)

        Log.d(mTAG, "Ricevuto id $sessionId")

        if (sessionId == -1){
            return
        }

        loadData(sessionId)
    }

    override fun onStop() {
        super.onStop()
        Log.v(mTAG, "onStop() called")
    }

    override fun onPause() {
        super.onPause()
        Log.v(mTAG, "onPause() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(mTAG, "onDestroy() called")
    }

    /** Carica i dati dal database [AppDatabase] in modo asincrono, cercando la [TrackSession] per id */
    private fun loadData(sessionId: Int){
        lifecycleScope.launch {
            // Dall'Id ottengo tutte le informazioni sulla sessione
            val trackSessionDao = AppDatabase.getInstance(this@DetailActivity).trackSessionDao()
            val trackSession = trackSessionDao.getTrackSessionById(sessionId)[0]
            val format = "HH:mm:ss"
            val noDecimal = DecimalFormat("#")
            val duration = trackSession.duration/1000

            val hours = duration / 3600
            val minutes = (duration % 3600) / 60
            val seconds = duration % 60

            // Imposto il testo per tutte le views in base ai dati che ho ricevuto dall'intent
            tv_startData.text = WeekHelper.getDate(trackSession.startTime, format)
            tv_endData.text = WeekHelper.getDate(trackSession.endTime, format)
            tv_typeData.text = trackSession.activityType
            // Aggiunta modifica per la distanza in km
            tv_distanceData.text = noDecimal.format(trackSession.distance / 1000) + "km"
            tv_timeData.text = "" + hours + "h " + minutes + "min " + seconds + "sec"
            // Aggiunta modifica per la velocità media in km/h
            tv_avrSpeedData.text = DecimalFormat("#.#").format(trackSession.averageSpeed * 3.6) + "km/h"
            tv_calorieData.text = noDecimal.format(trackSession.kcal) + "Kcal"

        }
    }

}