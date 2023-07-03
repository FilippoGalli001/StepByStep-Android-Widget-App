package it.project.appwidget.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import it.project.appwidget.ListWidgetService
import it.project.appwidget.R
import it.project.appwidget.activities.DetailActivity


/**
 * Implementazione ListWidget, widget che fornisce una lista contenente tutte le sessioni nell'arco temporale indicato nelle impostazioni dello stesso
 * App Widget Configuration implemented in [it.project.appwidget.activities.ListWidgetConfigureActivity]
 */
class ListWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("ListWidget", "Chiamato onUpdate")

        // Per ogni widget associato a questo provider
        for (appWidgetId in appWidgetIds) {
            // Creo il layout di RemoteViews
            val remoteViews = RemoteViews(context.packageName, R.layout.list_widget)
            // Creazione dell'intent per avvio servizio di gestione caricamento dei dati nella ListView
            val remoteAdapterIntent = Intent(context, ListWidgetService::class.java)
            remoteAdapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) // Invio id widget al service

            /*
            L'intent creato per avviare il servizio e restituire la RemoteViewsFactory viene in realtà
            salvato dal servizio in una hash map (https://github.com/aosp-mirror/platform_frameworks_base/blob/540962a60ac38757123a8988c4d441848c7dbf93/core/java/android/widget/RemoteViewsService.java#L41)
            che mappa gli Intent alle RemoteViewsFactory. Se due intent sono uguali (stessa action, data, type, identity, class, e categories, https://developer.android.com/reference/android/content/Intent#filterEquals(android.content.Intent) )
            non viene creata una nuova RemoteViewsFactory, ma riutilizzata quella già presente nella hashmap (https://github.com/aosp-mirror/platform_frameworks_base/blob/540962a60ac38757123a8988c4d441848c7dbf93/core/java/android/widget/RemoteViewsService.java#L235-L251).
            Pertanto, se si vuole che ogni widget abbia una RemoteViewsFactory specifica, è necessario creare Intent che siano differenti
            (in questo caso ciascuna RemoteViewsFactory dovrà gestire il ridimensionamento di ogni degli item nella lista in uno specifico Widget).
            Per creare intent differenti, imposto l'attributo Intent.data nella forma URI 'widget:$appWidgetId', in modo che widget differenti abbiano URI differenti.
            Se ciò non venisse fatto, due Widget con id differente farebbero riferimento alla stessa RemoteViewsFactory e durante il ridimensionamento del secondo
            verrebbero utilizzate le dimensioni del primo.
             */
            remoteAdapterIntent.data = (Uri.fromParts("widget", appWidgetId.toString(), null))

            // Collega l'intent del servizio al layout della ListView del widget
            remoteViews.setRemoteAdapter(R.id.widget_listview, remoteAdapterIntent)

            // Creazione di un intent per la gestione dei click sugli elementi della ListView
            val clickIntent = Intent(context, DetailActivity::class.java)

            // Creazione di un PendingIntent che apre una Activity
            //MUTABILI E IMMUTABILI -> inviati più volte o inviato una volta (per la sicurezza)
            val clickPendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_MUTABLE)

            // Imposta il PendingIntent come template per gli elementi della ListView del widget
            // https://developer.android.com/reference/android/widget/RemoteViews#setPendingIntentTemplate(int,%20android.app.PendingIntent)
            // necessario per avere un template predefinito per ogni tipo di ListView
            remoteViews.setPendingIntentTemplate(R.id.widget_listview, clickPendingIntent)

            // Richiedo aggiornamento del widget con le nuove views
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ListWidget", "Chiamato onReceive")
        super.onReceive(context, intent)
        if(intent.action == "database-updated"){
            Log.d("ListWidget", "Ricevuto intent database-updated")
            // Ottengo istanza AppWidgetManager
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, ListWidget::class.java))
            // Notifico aggiornamento dati di tutti i widget di questo provider
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
        }
    }

    // Questo metodo viene chiamato quando il widget viene ridimensionato
    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager?, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.d("ListWidget", "Chiamato onAppWidgetOptionsChanged per Widget $appWidgetId")

        /* Quando il widget viene ridimensionato, alcune RemoteViews all'interno della
        ListView remota dovrebbero mostrarsi o nascondersi. Non è possibile mostrare o nascondere
        le TextView della ListView direttamente da questa classe, perchè è ListWidgetFactory che si
        occupa di crearle e mostrarle. Bisogna quindi forzare ListWidgetFactory a ricostruire
        tutte le righe e per ogni riga nascondere o mostrare le TextView. Tecnicamente quindi basterebbe
        dire a ListWidgetFactory di ri-generare le Views in ogni riga, ma questo non è direttamente possibile.
        Quindi chiamiamo notifyAppWidgetViewDataChanged() che a sua volta farà eseguire i metodi
        onDataSetChanged() e getViewAt() di ListWidgetFactory, e quindi i dati verranno ricaricati.
         */
        appWidgetManager?.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listview)
    }

}

