package it.project.appwidget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import it.project.appwidget.LocationService.Companion.NOTIFICATION_CHANNEL_DESCRIPTION
import it.project.appwidget.LocationService.Companion.NOTIFICATION_CHANNEL_ID
import it.project.appwidget.LocationService.Companion.SERVICE_NOTIFICATION_ID
import it.project.appwidget.activities.MainActivity
import it.project.appwidget.fragments.Run
import it.project.appwidget.util.LocationParser
import it.project.appwidget.widgets.RunWidget


/**
 * Servizio foreground per localizzazione.
 * Questo servizio utilizza [LocationManager] per ricevere aggiornamenti sulla posizione
 * del dispositivo. Rimane in esecuzione anche quando l'applicazione è chiusa, tramite una notifica
 * che compare al momento dell'avvio del servizio. Il servizio può essere avviato/fermato
 * sia dal fragment [Run] che dal widget [RunWidget]
 *
 * - I parametri per la gestione delle notifiche sono:
 * [SERVICE_NOTIFICATION_ID], [NOTIFICATION_CHANNEL_ID], [NOTIFICATION_CHANNEL_DESCRIPTION]
 *
 * - I parametri per la ricezione degli aggiornamenti sulla posizione sono:
 * [minLocationUpdateIntervalMs], [minLocationUpdateDistanceM], [minAccuracy], [minSumDistance]
 *
 * - Le variabili di stato della sessione in corso di registrazione sono:
 * [sumDistance], [locationList], [sumCalories]
 *
 * - I parametri per il conteggio calorico sono:
 * [kcal_to_m_to_kg_factor], [weight]
 */
class LocationService : Service() {

    // Variabili per gestione della notifica del serizio
    /** Riferimento al [NotificationManager] per l'invio delle notifiche. */
    private lateinit var notificationManager: NotificationManager
    /** Riferimento al costruttore di notifiche [NotificationCompat.Builder]. */
    private lateinit var notificationBuilder: NotificationCompat.Builder
    /** Parametri delle notifiche. */
    companion object {
        /** Id notifica. */
        const val SERVICE_NOTIFICATION_ID: Int = 1
        /** Id del [NotificationChannel]. */
        const val NOTIFICATION_CHANNEL_ID: String = "LocationServiceChannel"
        /** Descrizione del [NotificationChannel]. */
        const val NOTIFICATION_CHANNEL_DESCRIPTION: String = "Canale per notifiche servizio localizzazione"
    }

    // Variabili per la localizzazione
    /** Riferimento al [LocationManager] per l'invio delle richieste di localizzazione. */
    private lateinit var locationManager: LocationManager
    /** Riferimento al [LocationListener] per la ricezione degli aggiornamenti sulla posizione. */
    private lateinit var locationListener: LocationListener

    // Parametri per la localizzazione
    /** Intervallo minimo di attesa tra ogni aggiornamento della posizione. */
    private val minLocationUpdateIntervalMs: Long = 0
    /** Distanza minima per la ricezione di un aggiornamento della posizione. */
    private var minLocationUpdateDistanceM: Float = 0F
    /** Accuratezza minima per il salvataggio della posizione. */
    private var minAccuracy: Float = 20F
    /** Distanza minima tra le posizioni ricevute per considerare un incremento della distanza percorsa.*/
    private var minSumDistance: Float = 10F

    // Parametri sessione
    /** Rapporto di conversione per ogni metro di distanza percorsa a chilocalorie bruciate. */
    private val kcal_to_m_to_kg_factor: Float = 0.001f
    /** Peso dell'utente. */
    private var weight: Int = 70
    /** Timeout prima della sospensione del servizio per inattività (default a 10 minuti). */
    private var timeout: Long = 600000

    // Stato sessione
    /** Distanza calcolata della sessione. */
    private var sumDistance: Float = 0F
    /** Lista delle posizioni salvate. */
    private var locationList: ArrayList<Location> = ArrayList()
    /** Somma delle calorie consumate. */
    private var sumCalories: Float = 0F
    /** Rate (minuti a chilometro) di movimento. */
    private var rate: Float = 0F

    /** Classe [LocationListener] per la gestione degli aggiornamenti della posizione. */
    private inner class CustomLocationListener: LocationListener {
        override fun onLocationChanged(currentLocation: Location) {
            Log.d("CustomLocationListener", "latitudine: ${currentLocation.latitude}, longitudine: ${currentLocation.longitude}, " +
                    "velocità: ${currentLocation.speed}(+- ${currentLocation.speedAccuracyMetersPerSecond}), " +
                    "accuratezza: ${currentLocation.accuracy}")

           // Controllo che la notifica sia già impostata, e la aggiorno con le nuove coordinate
            if (this@LocationService::notificationBuilder.isInitialized && this@LocationService::notificationManager.isInitialized){
                // Aggiorno valori sulla notifica
                notificationBuilder.setContentText("Latitudine: ${currentLocation.latitude}, Longitudine: ${currentLocation.longitude}")
                // Visualizzo aggiornamenti notifica
                notificationManager.notify(SERVICE_NOTIFICATION_ID, notificationBuilder.build())
            }

            // Filtro locations inaccurate - dopo l'aggiornamento della notifica per mostrare che comunque il servizio è attivo
            if (currentLocation.accuracy >= minAccuracy){
                return
            }

            // Salvo posizione se non è mai stata salvata
            if (locationList.size == 0){
                locationList.add(currentLocation)
            }
            // Altrimenti, se la distanza di questa rispetto all'ultima posizione salvata è maggiore di <minSum> metri, aggiorniamo la somma delle distanze
            else if (currentLocation.distanceTo(locationList.last()) >= minSumDistance){
                sumDistance += currentLocation.distanceTo(locationList.last())
                locationList.add(currentLocation)
            }

            // Calcolo il rate: tempo (in minuti) necessario a percorrere 1 km
            rate = 0.00f
            // Considero solo velocità superiori a 0.5 m/s
            if (currentLocation.speed > 0.5){
                rate = (1000 / currentLocation.speed) / 60
            }

            // Aggiorno somma calorie
            if (sumDistance > 0){
                sumCalories = kcal_to_m_to_kg_factor * sumDistance * weight
            }

            /* Invio broadcasts.
            Affinchè il widget riceva il broadcast, è necessario inviare un intent ESPLICITO. Tuttavia
            è necessario inviare il broadcast anche al fragment. Creiamo quindi due intent.*/

            // Creo intent implicito generico
            val implicitIntent = Intent("location-update")
            // Valori recuperati
            implicitIntent.putExtra("latitude", currentLocation.latitude)
            implicitIntent.putExtra("longitude", currentLocation.longitude)
            implicitIntent.putExtra("accuracy", currentLocation.accuracy)
            implicitIntent.putExtra("speed", currentLocation.speed)
            // Valori calcolati
            implicitIntent.putExtra("distance", sumDistance)
            implicitIntent.putExtra("rate", rate)
            implicitIntent.putExtra("calories", sumCalories)
            // Valori costanti
            implicitIntent.putExtra("startTime", locationList[0].time) // E' il tempo rispetto alla Unix Epoch
            implicitIntent.putExtra("startTime_elapsedRealtimeNanos", locationList[0].elapsedRealtimeNanos) // E' il tempo trascorso rispetto al boot di sistema

            // Copio intent generico e creo intent esplicito
            val explicitIntent = Intent(implicitIntent)
            explicitIntent.component = ComponentName(this@LocationService, RunWidget::class.java)
            // Invio intents
            sendBroadcast(implicitIntent)
            sendBroadcast(explicitIntent)


            Log.d("LocationService","Inviato messaggio broadcast con: " +
                    "[long: ${currentLocation.longitude}, lat: ${currentLocation.latitude}, acc: ${currentLocation.accuracy}, " +
                    "speed: ${currentLocation.speed}, dist: ${sumDistance},]")
            Log.d("LocationService","Tempo trascorso: ${System.currentTimeMillis() - locationList.last().time}\"")

            // Controllo se sono trascorsi più di 10min senza aver aggiornato la posizione
            if(System.currentTimeMillis() - locationList.last().time >= timeout) {
                // Arresto sessione
                Log.d("LocationService", "Sessione arrestata.")
                stopSelf()
            }

        }
    }

    override fun onCreate() {
        super.onCreate()
        // Istanziazione variabili
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = CustomLocationListener()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Lettura peso da preferenze
        weight = UserPreferencesHelper(applicationContext).peso.toInt()

        // Creo canale per le notifiche
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_DESCRIPTION, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        Log.d("LocationService", "Servizio creato (onCreate)")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Controllo se l'intent è stato lanciato per fermare il servizio
        if (intent != null) {
            if (intent.action == "STOP-LOCATION-SERVICE") {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Creo intent per l'apertura dell'applicazione
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply{
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fromService", true)
        }
        // Creo pending intent sull'apertura dell'applicazione
        val pendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)
        // Impostazioni notifica
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.runner)
            setContentTitle("Servizio di localizzazione")   // Titolo notifica
            setContentText("Servizio di localizzazione in esecuzione")  // Descrizione notifica
            priority = NotificationCompat.PRIORITY_DEFAULT    // Priorità notifica standard
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE   // La notifica viene impostata immediatamente
            setOnlyAlertOnce(true) //Se la notifica viene aggiornata, solo la prima volta emette suono
            setContentIntent(pendingIntent) // Imposto apertura app al click sulla notifica
        }

        // Controllo permessi
        if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
            || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            Log.d("LocationService", "Attenzione. Non sono stati concessi tutti i permessi.")
            stopSelf()
            return START_NOT_STICKY
        }

        // RICEVO PARAMETRI DI DEBUG
        minLocationUpdateDistanceM = intent?.getFloatExtra("minDistance", 0F)!!
        minAccuracy = intent.getFloatExtra("minAccuracy", 20F)
        minSumDistance = intent.getFloatExtra("minSum", 10F)

        // Creo richiesta aggiornamenti posizione
        val locationRequest:LocationRequest = LocationRequest.Builder(minLocationUpdateIntervalMs).apply {
            setQuality(LocationRequest.QUALITY_HIGH_ACCURACY) // Richiedo alta accuratezza
            setMinUpdateDistanceMeters(minLocationUpdateDistanceM) // Richiedo distanza minima aggiornamenti
        }.build()
        // Imposto listener per ricezione aggiornamenti posizione
        locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, locationRequest, mainExecutor, locationListener)

        // Invio notifica e avvio del servizio in foreground
        startForeground(SERVICE_NOTIFICATION_ID, notificationBuilder.build())

        Log.d("LocationService", "Servizio avviato (onStartCommand) con parametri: $minLocationUpdateDistanceM, $minAccuracy, $minSumDistance")

        // Imposto servizio come NON_STICKY (non si riavvia allo stop)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("LocationService", "Servizio non supporta onBind()")
        return null
    }

    // Viene chiamato da Context.stopService() o Service.stopSelf()
    override fun onDestroy() {
        Log.d("LocationService", "Chiamato onDestroy()")

        // Fermo aggiornamenti
        locationManager.removeUpdates(locationListener)

        // Converto locations in stringhe
        val locationListString = Array(locationList.size) { "" }
        for ((position, location) in locationList.withIndex()){
            // Effettuo parsing della location
            val stringLocation = LocationParser.toString(location)
            // Aggiungo stringa alla lista stringhe
            locationListString.set(position, stringLocation)
        }

        // Creo oggetto Data da inviare al worker
        val data = Data.Builder().putStringArray("locationListString", locationListString).build()

        // Avvio work per elaborazione passando dati in input
        val sessionWorkerRequest: WorkRequest = OneTimeWorkRequestBuilder<TrackSessionWorker>().setInputData(data).build()
        WorkManager.getInstance(applicationContext).enqueue(sessionWorkerRequest)

        // Creo intent implicito generico
        val implicitIntent = Intent("stop-service")
        // Copio intent generico e creo intent esplicito
        val explicitWidgetIntent = Intent(implicitIntent)
        explicitWidgetIntent.component = ComponentName(this@LocationService, RunWidget::class.java)
        // Invio intents
        sendBroadcast(implicitIntent)
        sendBroadcast(explicitWidgetIntent)

        Log.d("LocationService", "Servizio distrutto")
        super.onDestroy()
    }

}
