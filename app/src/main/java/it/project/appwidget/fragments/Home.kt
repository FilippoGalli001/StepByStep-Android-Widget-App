package it.project.appwidget.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import it.project.appwidget.Datasource
import it.project.appwidget.R
import it.project.appwidget.UserPreferencesHelper
import it.project.appwidget.util.WeekHelper
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * Fragment della schermata Home.
 * Nel file [R.navigation.main_navigation_graph] è specificato come fragment primario.
 *
 * Si occupa di mostrare una schermata di benvenuto all'utente, e mostra dei pannelli
 * con i progressi giornalieri di passi, distanza e calorie.
 */
class Home : Fragment() {

    // Views
    /** [TextView] relativa al nome utente (superiore) */
    private lateinit var usernameTextView: TextView
    /** [TextView] relativa ai passi giornalieri */
    private lateinit var passiTextView: TextView
    /** [TextView] relativa alla distanza giornaliera */
    private lateinit var distanceTextView: TextView
    /** [TextView] relativa alle calorie giornaliere */
    private lateinit var caloriesTextView: TextView
    /** [ProgressBar] relativa alle calorie giornaliere rispetto a quelle di obiettivo */
    private lateinit var progressBar: ProgressBar
    /** [TextView] relativa alla percentuale di calorie giornaliere bruciate */
    private lateinit var percentTextView: TextView


    // Stato interno
    /** Distanza giornaliera (in metri) */
    private var distance: Double = 0.0
    /** Passi giornalieri */
    private var steps: Int = 0
    /** Chilocalorie giornaliere */
    private var kcal: Int = 0
    /** Obiettivo calorico */
    private var kcalTarget: Int = 0
    /** Nome utente */
    private var username: String = "Utente"
    /** Chilocalorie della sessione in corso */
    var sessionKcal = 0f
    /** Distanza della sessione in corso */
    var sessionDistance = 0.0
    /** Passi della sessione in corso */
    var sessionSteps = 0


    /** [BroadcastReceiver] che riceve aggiornamenti alla fine delle registrazioni delle sessioni */
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver
    private inner class LocationBroadcastReceiver(): BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {

            Log.d("Run.LocationBroadcastReceiver", "Chiamato onReceive con intent " + intent.action)

            // Se la sessione è in corso somma ai valori giornalieri quelli della sessione attuale
            if (intent.action == "location-update") {

                distanceTextView = view!!.findViewById<TextView>(R.id.counterDistance)
                passiTextView = view!!.findViewById<TextView>(R.id.counterPassi)
                caloriesTextView = view!!.findViewById<TextView>(R.id.counterCalories)
                progressBar = view!!.findViewById<ProgressBar>(R.id.progress_bar)

                val distloc = intent.getFloatExtra("distance", 0f)
                val kcalloc = intent.getFloatExtra("calories", 0f)


                if (!distloc.toDouble().equals(sessionDistance))
                {
                    sessionDistance = distloc.toDouble()
                    sessionSteps = (distloc*3/2).roundToInt()

                    val totalDistance = distloc + distance
                    val totalSessionSteps = (distloc*3/2).roundToInt() + steps
                    distanceTextView.text = totalDistance.toInt().toString()
                    passiTextView.text = totalSessionSteps.toString()
                }

                if (!kcalloc.equals(sessionKcal))
                {
                    sessionKcal = kcalloc
                    val totalKcal = kcalloc + kcal
                    caloriesTextView.text = totalKcal.toInt().toString()
                    progressBar.max = 100
                    progressBar.progress = totalKcal.toInt()
                    updateProgressBar(kcalTarget, totalKcal.toInt())
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Creo BroadcastReceiver
        locationBroadcastReceiver = LocationBroadcastReceiver()
        Log.d("HomeFragment", "Chiamato onCreate")
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("HomeFragment", "Chiamato onCreateView")
        // Inflate del layout
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "Chiamato onViewCreated")

        sessionKcal = 0f
        sessionDistance = 0.0
        sessionSteps = 0

        // Ottengo riferimenti alle Views
        distanceTextView = view.findViewById<TextView>(R.id.counterDistance)
        passiTextView = view.findViewById<TextView>(R.id.counterPassi)
        caloriesTextView = view.findViewById<TextView>(R.id.counterCalories)
        progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        usernameTextView = view.findViewById<TextView>(R.id.nome_utente)
        percentTextView = view.findViewById<TextView>(R.id.percent)


        // Imposto valori default alle Views
        progressBar.max = 100
        progressBar.progress = kcal
        usernameTextView.text = username

        // Registro receiver
        requireActivity().registerReceiver(locationBroadcastReceiver, IntentFilter("location-update"))

        // Avvio coroutine impostazione valori
        lifecycleScope.launch {
            // Leggo da sharedpreferences
            val userPreferencesHelper = UserPreferencesHelper(requireActivity())
            // Recupero nome utente e obiettivo giornaliero calorie
            username = userPreferencesHelper.nome
            kcalTarget = userPreferencesHelper.kcalTarget

            // Calcolo range tempo giornaliero
            val dayRange = WeekHelper.getDayRange(System.currentTimeMillis())
            val from = dayRange.first
            val to = dayRange.second

            // Ottengo lista di tracksessions
            val trackSessionList = Datasource(requireActivity()).getSessionList(from, to)

            //Imposto dati a zero
            distance = 0.0
            kcal = 0
            steps = 0

            // Calcolo la somma delle distanze, delle calorie e dei passi totali
            for (trackSession in trackSessionList){
                distance += trackSession.distance
                kcal += trackSession.kcal
                steps += (trackSession.distance * 3/2).roundToInt()
            }


            // Aggiorno le Views
            distanceTextView.text = distance.toInt().toString()
            passiTextView.text = steps.toString()
            caloriesTextView.text = kcal.toString()
            updateProgressBar(kcalTarget, kcal)
            usernameTextView.text = username
        }

        // Recupero stato del fragment, ma solo se onSaveInstanceState non è null
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()
        // Leggo da sharedpreferences
        val userPreferencesHelper = UserPreferencesHelper(requireActivity())
        kcalTarget = userPreferencesHelper.kcalTarget
        updateProgressBar(kcalTarget, kcal)
    }

    override fun onDestroyView() {
        Log.d("HomeFragment", "Chiamato onDestroyView")
        // Disabilito receiver
        requireActivity().unregisterReceiver(locationBroadcastReceiver)
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("HomeFragment", "Chiamato onSaveInstanceState")

        // Salvo lo stato di tutte le Views
        outState.putCharSequence("distanceTextView_text", distanceTextView.text)
        outState.putCharSequence("passiTextView_text", passiTextView.text)
        outState.putCharSequence("kcalTextView_text", caloriesTextView.text)

        super.onSaveInstanceState(outState)
    }

    // Recupero stato del fragment
    private fun restoreState(inState: Bundle) {
        Log.d("HomeFragment", "Chiamato restoreState")
        // Ripristino stato delle textviews
        distanceTextView.text = inState.getCharSequence("distanceTextView_text")
        passiTextView.text = inState.getCharSequence("passiTextView_text")
        caloriesTextView.text = inState.getCharSequence("kcalTextView_text")
    }

    override fun onDestroy() {
        Log.d("HomeFragment", "Chiamato onDestroy")
        super.onDestroy()
    }

    /**
     * Questo metodo aggiorna il progresso della [progressBar] e la percentuale [percentTextView]
     * inserita al suo interno in base al rapporto sul nuovo obiettivo calorico [kcalTarget]
     */
    private fun updateProgressBar(newKcalTarget: Int, kcal: Int) {
        kcalTarget = newKcalTarget
        progressBar.max = 100
        val progress = if (kcalTarget == 0) 100.0 else kcal.toDouble() / kcalTarget.toDouble() * 100
        progressBar.progress = progress.roundToInt()
        percentTextView.text = "${progress.roundToInt()}%"
        Log.d("HomeFragment", "Nuova percentuale = " + kcal + "/" + kcalTarget + "*100 = " + progressBar.progress + "%")
    }
}