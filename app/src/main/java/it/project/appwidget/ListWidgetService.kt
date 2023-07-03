package it.project.appwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import it.project.appwidget.database.TrackSession
import it.project.appwidget.util.WeekHelper
import java.text.DecimalFormat

/**
 * La classe [ListWidgetService] è responsabile di fornire un di adapter per il widget.
 * Si noti che [RemoteViewsService] mantiene una [HashMap] tra [Intent] e adapter.
 * Affinchè vengano effettivamente creati adapter differenti per widget differenti, è necessario creare
 * [Intent] differenti, ad esempio impostando il parametro [Intent.setData] in modo univoco per ciascun widget.
 * (vedi implementazione Android: [link](https://github.com/aosp-mirror/platform_frameworks_base/blob/540962a60ac38757123a8988c4d441848c7dbf93/core/java/android/widget/RemoteViewsService.java#L240C1-L249))
 */
class ListWidgetService : RemoteViewsService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("ListWidgetService", "Chiamato onCreate()")
    }

    // Metodo che viene chiamato quando viene richiesto l'adapter per il widget,
    // ma solo se l'intent è diverso da quelli salvati nella hash map del servizio
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ListWidgetFactory(applicationContext, intent)
    }



    /** Classe interna ListWidgetFactory implementa l'interfaccia [RemoteViewsService.RemoteViewsFactory]
        e gestisce il caricamento dei dati per la ListView del widget. */
    class ListWidgetFactory(private val context: Context, private val intent: Intent) : RemoteViewsFactory {


        private val dateFormat = "dd-MM-yyyy"
        private val hourFormat = "HH:mm"

        // Lista degli elementi da visualizzare nella ListView del widget
        private lateinit var trackSessionList: ArrayList<TrackSession>
        // Id widget di riferimento
        private var appWidgetId: Int = -1
        // Range della settimana di riferimento
        private lateinit var range: Pair<Long, Long>


        override fun onCreate() {
            Log.d("ListWidgetFatory", "Chiamato onCreate()")
            // Inizializza l'elenco items come vuoto
            trackSessionList = arrayListOf()
            // Recupero widgetID dall' intent
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            Log.d("ListWidgetFatory", "Creato nuovo ListWidgetFactory per Widget $appWidgetId")
        }

        /* Una volta che ListWidgetFactory viene creato, o quando viene aggiornato, viene chiamato
        il seguente metodo per popolare la RemoteView. Non è necessario avviare una coroutine perchè
         il sistema continuerà a mostrare il dataset precedente finchè il nuovo dato non è pronto.*/
        override fun onDataSetChanged() {
            Log.d("ListWidgetFactory", "Chiamato onDatasetChanged()")
            // Recupero sharedPreferences
            val preferencesFileName = "it.project.appwidget.listwidget.$appWidgetId"
            val sharedPreferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE)
            // Cerco il primo valore di weekRange eventualmente salvato
            val rangeLength = sharedPreferences.getString("range.length", "Settimana")
            val startRange = System.currentTimeMillis()
            println(rangeLength)

            if (rangeLength.equals("Giorno")) {
                // Aggiorno valore weekRange
                println("Invocato getDayRange")
                range = WeekHelper.getDayRange(startRange)
            }
            else if (rangeLength.equals("Settimana")) {
                // Aggiorno valore weekRange
                println("Invocato getWeekRange")
                range = WeekHelper.getWeekRange(startRange)
            }
            else if (rangeLength.equals("Mese")) {
                // Aggiorno valore weekRange
                println("Invocato getMonthRange")
                range = WeekHelper.getMonthRange(startRange)
            }

            // Leggo entries dal database in base alla settimana selezionata e aggiorno dati
            trackSessionList = Datasource(context).getSessionList(range.first, range.second)
        }

        // Restituisce il numero di elementi nella ListView del widget
        override fun getCount(): Int {
            Log.d("ListWidgetFactory", "Chiamato getCount()")
            return trackSessionList.size
        }

        // Restituisce il numero di tipi di visualizzatori diversi
        override fun getViewTypeCount(): Int {
            return 1
        }

        // Volendo, restituisce una view di caricamento personalizzata. In questo caso restituisce null e viene utilizzata la view di default
        override fun getLoadingView(): RemoteViews? {
            return null
        }

        // Ottieni la view per un determinato elemento della ListView del widget
        // Serve per riempire con gli elementi della trackSession corrispondente
        override fun getViewAt(position: Int): RemoteViews {
            //Log.d("ListWidgetFactory", "Chiamato getViewAt()")

            val remoteViews = RemoteViews(context.packageName, R.layout.list_item_widget)
            val trackSession: TrackSession = trackSessionList[position]


            //Imposto il valore di tutti gli elementi
            val data_text = WeekHelper.getDate(trackSession.startTime, dateFormat)
            val time_text = WeekHelper.getDate(trackSession.startTime, hourFormat)

            val distance_text = DecimalFormat("#.##").format(trackSession.distance/1000) + "km"

            val duration = trackSession.duration/1000
            val hours = duration / 3600
            val minutes = (duration % 3600) / 60
            val duration_text = "${hours}:${minutes}h"



            // Imposta il testo degli elementi correnti nella TextView all'interno dell'elemento della ListView
            remoteViews.setTextViewText(R.id.item_date, data_text)
            remoteViews.setTextViewText(R.id.item_time, time_text)
            remoteViews.setTextViewText(R.id.item_distance, distance_text)
            remoteViews.setTextViewText(R.id.item_duration, duration_text)


            // RIEMPIO I TEMPLATE CON I PARAMETRI CHE MI SERVONO
            // clickIntent + fillIntent = in una riga
            // Imposta intent sul singolo item della lista
            // (da clickIntent prende component e da fillIntent prende il parametro specifico)
            val fillInIntent = Intent()
            // Inserisco Id sessione nell'intent
            fillInIntent.putExtra("session:id", trackSession.id)
            //https://developer.android.com/reference/android/widget/RemoteViews#setOnClickFillInIntent(int,%20android.content.Intent)
            remoteViews.setOnClickFillInIntent(R.id.list_item_widget, fillInIntent)

            /*
            Impostare la visibilità delle varie TextViews nella ListView è possibile solo tramite questo adapter.
            Pertanto dobbiamo recuperare qui le dimensioni del Widget e mostrare in modo ottimale solo quello che ci stà.
             */

            // Ottengo la larghezza minima del widget e imposto visibilità elementi
            val options: Bundle = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            Log.d("ListWidgetFactory", "Chiamato getViewAt() di id $appWidgetId, posizione $position, largezza $minWidth")
            // Nascondo elementi che potrebbero non starci
            /**
             * Schema di dimensionamento
             * Widget piccolo [minWidth 0 - 150] -> mostro solo la data
             * Widget medio [minWidth 150 - 200] -> mostro data e ora
             * Widget medio-grande [minWidth 200-250] -> mostro data e distanza
             * Widget grande [minWidth 250-300] -> mostro data, distanza e durata
             * Widget più grande [minWidth 250-300] -> mostro data, ora, distanza e durata
             */
            //Log.d("ListWidgetFactory", "Dimensione è $minWidth")
            when (minWidth) {
                in 0..150 -> {
                    remoteViews.setViewVisibility(R.id.item_time, View.GONE)
                    remoteViews.setViewVisibility(R.id.item_distance, View.GONE)
                    remoteViews.setViewVisibility(R.id.item_duration, View.GONE)
                }
                in 150..200 -> {
                    remoteViews.setViewVisibility(R.id.item_time, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.item_distance, View.GONE)
                    remoteViews.setViewVisibility(R.id.item_duration, View.GONE)
                }
                in 200 .. 250 -> {
                    remoteViews.setViewVisibility(R.id.item_time, View.GONE)
                    remoteViews.setViewVisibility(R.id.item_distance, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.item_duration, View.GONE)
                }
                in 250..300 -> {
                    remoteViews.setViewVisibility(R.id.item_time, View.GONE)
                    remoteViews.setViewVisibility(R.id.item_distance, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.item_duration, View.VISIBLE)
                }
                in 300..1000 -> {
                    remoteViews.setViewVisibility(R.id.item_time, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.item_distance, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.item_duration, View.VISIBLE)
                }
            }

            return remoteViews
        }

        // Restituisce l'ID dell'elemento nella posizione specificata
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        // Restituisce true se gli ID degli elementi sono stabili
        override fun hasStableIds(): Boolean {
            return true
        }

        override fun onDestroy() {
            Log.d("ListWidgetFactory", "Chiamato onDestroy()")
        }

    }

}

