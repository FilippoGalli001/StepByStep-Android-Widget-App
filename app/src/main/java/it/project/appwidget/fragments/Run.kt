package it.project.appwidget.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import it.project.appwidget.LocationService
import it.project.appwidget.R
import it.project.appwidget.database.TrackSession
import it.project.appwidget.widgets.RunWidget
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * Fragment che mostra all'utente i pulsanti per iniziare/arrestare la registrazione della [TrackSession]
 * Il fragment riceve aggiornamenti dal [LocationService] e mostra all'utente informazioni utili quali
 * tempo di attività, velocità (in minuti al kilometro), distanza percorsa, calorie bruciate.
 * Il [LocationService] invia una notifica che gli consente di funzionare anche se il fragment non è attivo.
 * Grazie ai messaggi broadcast lanciati dal servizio, il fragment è in grado di recuperare le informazioni
 * sulla sessione in corso anche se è stato distrutto e ricreato. A questo fragment corrisponde il widget [RunWidget].
 */
class Run : Fragment() {

    /** Variabile per controllo dei permessi */
    private var hasPermissions: Boolean = false

    // Views
    /** [TextView] relativa alla distanza (in km) percorsa nella sessione in corso */
    private lateinit var distanceTextView: TextView
    /** [TextView] relativa al ritmo (minuti a kilometro) di movimento sessione in corso */
    private lateinit var rateTextView: TextView
    /** [TextView] relativa alle calorie bruciate nella sessione in corso */
    private lateinit var kcalTextView: TextView
    /** [Chronometer] che mostra la durata della sessione in corso (non corrisponde alla durata effettiva salvata della sessione) */
    private lateinit var sessionChronometer: Chronometer
    /** [Button] che fa partire [LocationService] per la registrazione delle posizioni */
    private lateinit var startServiceButton: Button
    /** [Button] che fa fermare [LocationService] e interrompe la sessione in corso */
    private lateinit var stopServiceButton: Button

    /** [TextView] di debug per accuratezza */
    private lateinit var accuracy_debug_textview: TextView
    /** [TextView] di debug per velocità */
    private lateinit var speed_debug_textview: TextView
    /** [TextView] di debug per distanza */
    private lateinit var distance_debug_textview: TextView

    // Stato
    /** Salva se [sessionChronometer] è attivo o meno */
    private var runningChronometer = false

    /** [BroadcastReceiver] che riceve aggiornamenti sulla sessione in corso */
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver

    /** Questa classe implementa un [BroadcastReceiver] per la ricezione dei messaggi broadcast lanciati da [LocationService]*/
    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Log.d("Run.LocationBroadcastReceiver", "Chiamato onReceive con intent " + intent.action)

            // Filtro l'action associata al broadcast
            if (intent.action == "location-update") {

                // Ricevo i valori dal broadcast
                val speedloc = intent.getFloatExtra("speed", 0f)
                val accloc = intent.getFloatExtra("accuracy", 0f)
                val distloc = intent.getFloatExtra("distance", 0f)
                val rate = intent.getFloatExtra("rate", 0f)
                val calories = intent.getFloatExtra("calories", 0f)

                // Creo diversi formati numerici
                val noDecimalFormat = DecimalFormat("#")
                val singleDecimal = DecimalFormat("#.#")
                val doubleDecimal = DecimalFormat("#.##")

                /*
                Se il cronometro non è attivo ma ho ricevuto un broadcast significa che il serizio è attivo.
                Imposto il cronometro e lo avvio in modo che l'utente si renda conto che sta già registrando.
                 */
                if (!runningChronometer) {
                    // Ottengo lo start time della prima location rispetto al boot di sistema
                    var elapsedloc = intent.getLongExtra("startTime_elapsedRealtimeNanos",0)
                    // Converto nanosecondi in millisecondi e imposto base cronometro
                    elapsedloc = TimeUnit.NANOSECONDS.toMillis(elapsedloc)
                    sessionChronometer.base = elapsedloc
                    // Avvio cronometro e aggiorno stato
                    sessionChronometer.start()
                    runningChronometer = true

                    // Inoltre devo anche scambiare lo stato dei bottoni
                    startServiceButton.isEnabled = false
                    stopServiceButton.isEnabled = true
                }

                // Aggiorno il testo presente sulle TextViews
                rateTextView.text = singleDecimal.format(rate)
                distanceTextView.text = doubleDecimal.format(distloc / 1000)
                kcalTextView.text = noDecimalFormat.format(calories)

                // Debug
                speed_debug_textview.text = "speed: " + (doubleDecimal.format(speedloc * 3.6)) + "km/h"
                accuracy_debug_textview.text = "accuracy: " + accloc.toString() + "m"
                distance_debug_textview.text = "distance: " + distloc.toString() + "m"

            } // Altrimenti, se l'action ricevuta corrisponde ad uno stop del servizio, fermo il cronometro e aggiorno i bottoni
            else if (intent.action == "stop-service") {
                // Fermo cronometro e salvo stato
                sessionChronometer.stop()
                runningChronometer = false

                // Disattiva il bottone stopServiceButton e attiva il bottone startServiceButton
                stopServiceButton.isEnabled = false
                startServiceButton.isEnabled = true
            }
        }

    } // Fine classe LocationBroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Creo BroadcastReceiver
        locationBroadcastReceiver = LocationBroadcastReceiver()
        Log.d("RunFragment", "Chiamato onCreate")
    }

    // La documentazione di Android suggerisce di usare questo metodo solo per caricare il layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("RunFragment", "Chiamato onCreateView")
        // Inflate del layout
        return inflater.inflate(R.layout.fragment_run, container, false)
    }

    // E' consigliato implementare qua la logica del fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("RunFragment", "Chiamato onViewCreated")

        // Inizializzazione Views
        distanceTextView = view.findViewById(R.id.distanceTextView)
        rateTextView = view.findViewById(R.id.rateTextView)
        kcalTextView = view.findViewById(R.id.kcalTextView)
        sessionChronometer = view.findViewById(R.id.sessionChronometer)
        startServiceButton = view.findViewById(R.id.startServiceButton)
        stopServiceButton = view.findViewById(R.id.stopServiceButton)
        // Views di DEBUG
        accuracy_debug_textview = view.findViewById(R.id.debug_accuracy_textview)
        speed_debug_textview = view.findViewById(R.id.debug_speed_textview)
        distance_debug_textview = view.findViewById(R.id.debug_distance_textview)
        //Rendiamo invisibili le VIews di DEBUG
        accuracy_debug_textview.visibility = INVISIBLE
        speed_debug_textview.visibility = INVISIBLE
        distance_debug_textview.visibility = INVISIBLE

        // Disabilito bottone di STOP
        stopServiceButton.isEnabled = false

        // Creo intent filter per i broadcast lanciati dal service
        val serviceIntentFilter = IntentFilter("location-update")
        serviceIntentFilter.addAction("stop-service") // Aggiungo una seconda action
        // Registro receiver
        requireActivity().registerReceiver(locationBroadcastReceiver, serviceIntentFilter)


        // Recupero stato del fragment, ma solo se onSaveInstanceState non è null
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        }


        // Controllo i permessi. La main activity li ha richiesti all'utente
        if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_DENIED)
            || checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_DENIED
            || checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_DENIED){

            // Se uno di questi permessi manca, non è possibile registrare la posizione in modo consistente
            hasPermissions = false
            // Disabilito bottone avvio servizio
            startServiceButton.isEnabled = false
            Log.d("RunFragment", "Alcuni permessi sono mancanti.")
        }


        // Creo intent per il LocationService
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)

        startServiceButton.setOnClickListener {
            // Richiedo avvio servizio in foreground
            requireActivity().startForegroundService(serviceIntent)
            // Imposto base timer e avvio
            sessionChronometer.base = SystemClock.elapsedRealtime()
            sessionChronometer.start()
            runningChronometer = true

            // Disattiva il bottone startServiceButton e attiva il bottone stopServiceButton
            startServiceButton.isEnabled = false
            stopServiceButton.isEnabled = true
        }

        stopServiceButton.setOnClickListener {
            // Ferma il servizio
            requireActivity().stopService(serviceIntent)
            sessionChronometer.stop()
            runningChronometer = false

            // Disattiva il bottone stopServiceButton e attiva il bottone startServiceButton
            stopServiceButton.isEnabled = false
            startServiceButton.isEnabled = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("RunFragment", "Chiamato onSaveInstanceState")

        // Salvo cronometro, ma solo se attivo
        if (runningChronometer){
            outState.putBoolean("runningChronometer", runningChronometer)
            outState.putLong("sessionChronometer_base", sessionChronometer.base)
        }

        // Salvo lo stato di tutte le Views
        outState.putCharSequence("distanceTextView_text", distanceTextView.text)
        outState.putCharSequence("rateTextView_text", rateTextView.text)
        outState.putCharSequence("kcalTextView_text", kcalTextView.text)
        outState.putBoolean("stopServiceButton", stopServiceButton.isEnabled)
        outState.putBoolean("startServiceButton", startServiceButton.isEnabled)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        Log.d("RunFragment", "Chiamato onDestroyView")
        // Tolgo registrazione receiver
        requireActivity().unregisterReceiver(locationBroadcastReceiver)
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d("RunFragment", "Chiamato onDestroy")
        super.onDestroy()
    }

    /** Recupera lo stato salvato del fragment. In particolare,
     * recupera lo stato del [sessionChronometer], i testi di [distanceTextView], [rateTextView],
     * [kcalTextView] e lo stato dei bottoni [startServiceButton] e [stopServiceButton]*/
    private fun restoreState(inState: Bundle) {
        Log.d("RunFragment", "Chiamato restoreState")

        // Ottengo stato cronometro e lo faccio ripartire, ma solo se era attivo
        runningChronometer = inState.getBoolean("runningChronometer", false)
        if (runningChronometer){
            sessionChronometer.base = inState.getLong("sessionChronometer_base")
            sessionChronometer.start()
        }

        // Ripristino stato delle textviews
        distanceTextView.text = inState.getCharSequence("distanceTextView_text")
        rateTextView.text = inState.getCharSequence("rateTextView_text")
        kcalTextView.text = inState.getCharSequence("kcalTextView_text")
        stopServiceButton.isEnabled =  inState.getBoolean("stopServiceButton" )
        startServiceButton.isEnabled =  inState.getBoolean("startServiceButton" )

    }
}
