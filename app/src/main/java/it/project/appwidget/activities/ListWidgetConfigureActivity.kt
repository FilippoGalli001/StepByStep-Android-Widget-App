package it.project.appwidget.activities

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.project.appwidget.R
import kotlinx.coroutines.launch

/**
 * Gestire la configurazione di ListView
 * Possibilità di filtrare le sessioni di corsa per:
 * - Giorno --> vengono visualizzate le sessioni del giorno corrente
 * - Settimana --> vengon visualizzate le sessioni della settimana corrente
 * - Mese --> vengono visualizzate le sessioni del mese corrente
 * - Anno --> vengono visualizzate le sessioni dell'anno corrente
 */
class ListWidgetConfigureActivity: AppCompatActivity() {

    //Bottone per salvare
    private lateinit var saveBtn: Button

    //Spinner per decidere tra i tre possibili filtri
    private lateinit var filterSpinner: Spinner

    //id del widget associato a questa configurazione - imposto valore di default a -1
    private var widgetId = -1

    /** elemento selezionato della lista **/
    private lateinit var  configurationItem: String

    //Listener che aggiorna quando cambio elemento selezionato
    private val itemSelectedListener = ItemSelectedListener()

    /**
     * Classe per la gestione del click sullo Spinner
     */
    private inner class ItemSelectedListener: AdapterView.OnItemSelectedListener{

        //metodo chiamato quando viene cliccato un elemento nello Spinner
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            configurationItem = parent?.getItemAtPosition(position) as String
        }

        //metodo chiamato se non viene cliccato nessun elemento
        override fun onNothingSelected(parent: AdapterView<*>?) {
            return
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_view_widget_configure)

        //recupero id del widget
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        //imposto il risultato nel caso l'utente esca senza salvare
        setResult(RESULT_CANCELED)

        //Ottengo i riferimenti alle views presenti nel layout
        saveBtn = findViewById(R.id.ListViewButton)
        filterSpinner = findViewById(R.id.ListViewSelection)

        // Imposto dati spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.list_widget_configure_options, R.layout.graph_widget_spinner_item)
        spinnerAdapter.setDropDownViewResource(R.layout.graph_widget_spinner_item)
        // Inizializzo item selezionato
        configurationItem = spinnerAdapter.getItem(0) as String
        // Imposto adapter e listener sullo spinner
        filterSpinner.adapter = spinnerAdapter
        filterSpinner.onItemSelectedListener = itemSelectedListener

        // Imposto listener sul bottone
        saveBtn.setOnClickListener {
            saveAndUpdate()
            setResult(RESULT_OK)
            finish()
        }

    }


    private fun saveAndUpdate() {
        lifecycleScope.launch {
            // Salvo su sharedprefs
            val preferencesFileName = "it.project.appwidget.listwidget.$widgetId"
            val preferences = getSharedPreferences(preferencesFileName, MODE_PRIVATE).edit()
            preferences.putString("range.length", configurationItem)
            preferences.apply()
            println(configurationItem)

            // Ottengo istanza AppWidgetMananger
            val appWidgetManager = AppWidgetManager.getInstance(this@ListWidgetConfigureActivity)
            // Notifico ListWidgetFactory delle modifiche
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_listview)


        }
    }

}




