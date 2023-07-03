package it.project.appwidget.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import it.project.appwidget.BarChart
import it.project.appwidget.Datasource
import it.project.appwidget.R
import it.project.appwidget.util.WeekHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Implementazione GraphWidget, che mostra un grafico rispetto alla proprietà configurata (default: distanza).
 * Sequenza di chiamate: onEnabled() -> onReceive() -> onUpdate() | onAppWidgetOptionsChanged() -> onDeleted() -> onDisabled().
 * App Widget Configuration implemented in [it.project.appwidget.activities.GraphWidgetConfigureActivity]
 */
class GraphWidget : AppWidgetProvider() {
    companion object{

        /** Costante per il prefisso del nome del file delle [SharedPreferences]. Associare questo nome all'id del widget*/
        const val SHARED_PREFERENCES_FILE_PREFIX = "it.project.appwidget.widgets.GraphWidget"
        /** Costante per la chiave dell'impostazione relativa a questo tipo di widget*/
        const val DATA_SETTINGS = "data"

        /**
         * Aggiorna in modo asincrono le views di un widget in base ai valore impostato sulle [SharedPreferences].
         *
         *
         * - [context] : Necessario per accedere alle [SharedPreferences].
         * - [appWidgetManager] : Istanza di [AppWidgetManager] che si occuperà di aggiornare effettivamente le views
         * - [appWidgetId] : Id del widget da aggiornare
         */
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            Log.d("GrapWidget", "Avvio aggiornamento grafico")

            // Aggiorno i dati in modo asincrono
            CoroutineScope(Dispatchers.Main).launch {
                // Leggo dati dalle preferenze
                val prefs = context.getSharedPreferences(SHARED_PREFERENCES_FILE_PREFIX + appWidgetId, 0)
                val settings = prefs.getString(DATA_SETTINGS, "Distanza")

                // Ottengo riferimento alle RemoteViews
                val views = RemoteViews(context.packageName, R.layout.graph_widget)

                // Ottengo dati dal database
                val weekRange = WeekHelper.getWeekRange(System.currentTimeMillis())
                val trackSessions = Datasource(context).getSessionList(weekRange.first,weekRange.second)

                // Ottengo valori e etichette dai dati
                val values: ArrayList<Double> = when (settings){
                    "Calorie" -> WeekHelper.convertTrackSessionInCaloriesArray(trackSessions).map{ it.toDouble()} as ArrayList<Double>
                    "Durata" -> WeekHelper.convertTrackSessionInDurationArray(trackSessions)
                    else -> WeekHelper.convertTrackSessionInDistanceArray(trackSessions)
                }
                val labels: ArrayList<String> = WeekHelper.getDateList(weekRange.first,weekRange.second)

                // Costruisco grafico
                val chart = BarChart(context, null)
                chart.labels = labels
                chart.values = values
                val image: Bitmap = when(settings){
                    "Calorie" -> chart.getChartImage(color = Color.RED, dataLabel = "kcal")
                    "Durata" -> chart.getChartImage(color = Color.GREEN, dataLabel = "min")
                    else -> chart.getChartImage()
                }

                // Imposto titolo
                views.setTextViewText(R.id.graphWidgetTitleTextView, settings)

                // Imposto immagine nella viewImage
                views.setImageViewBitmap(R.id.graphImageView, image)
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }


    }

    /** Il provider è stato abilitato poichè un widget è stato aggiunto allo schermo */
    override fun onEnabled(context: Context) {
        Log.d("GraphWidget", "Chiamato onEnabled")
    }

    /** Il provider ha ricevuto un intent */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GraphWidget", "Chiamato onReceive")
        super.onReceive(context, intent)
        if(intent.action == "database-updated"){
            Log.d("GraphWidget", "Ricevuto intent database-updated")
            // Ottengo istanza AppWidgetMananger
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GraphWidget::class.java))
            // Aggiorno tutti i widget di questo provider
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    /** Il provider deve aggiornare i suoi widget */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("GraphWidget", "Chiamato onUpdate")

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /** Uno dei widget è stato ridimensionato */
    override fun onAppWidgetOptionsChanged(context: Context?, appWidgetManager: AppWidgetManager?,
        appWidgetId: Int, newOptions: Bundle?) {

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.d("GraphWidget", "Chiamato onAppWidgetOptionsChanged")
    }

    /** Uno o più widget sono stati eliminati*/
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d("GraphWidget", "Chiamato onDeleted")
        // Rimuovo preferenze in modo asincrono
        CoroutineScope(Dispatchers.Main).launch{
            for (appWidgetId in appWidgetIds) {
                // Rimuovo preferenze per ciascuno
                val pref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_PREFIX + appWidgetId, 0).edit()
                pref.remove(DATA_SETTINGS)
                pref.apply()
            }
        }
    }

    /** Il provider è stato disabilitato in quanto tutti i suoi widget non sono più presenti sullo schermo */
    override fun onDisabled(context: Context) {
        Log.d("GraphWidget", "Chiamato onDisabled")
    }


}

