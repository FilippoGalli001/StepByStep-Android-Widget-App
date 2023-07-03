package it.project.appwidget.activities

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.project.appwidget.R
import it.project.appwidget.widgets.GraphWidget
import kotlinx.coroutines.launch


/**
 * Classe che gestisce l'activity di configurazione del [GraphWidget].
 *
 * Permette all'utente di scegliere un campo da graficare tra distanza, calorie e durata delle sessioni.
 */
class GraphWidgetConfigureActivity : AppCompatActivity(){

    /** Bottone salvataggio configurazione */
    private lateinit var saveButton: Button
    /** Elemento spinner da cui selezionare il campo di interesse */
    private lateinit var optionSpinner: Spinner

    // Stato
    /** Elemento correntemente selezionato */
    private lateinit var selectedItem: String
    /** Id widget associato a questa configurazione */
    private var widgetId = -1


    /** Listener per aggiornare [selectedItem] al click di un elemento sullo spinner */
    private val itemSelectedListener = ItemSelectedListener()

    /**
     * Classe che implementa interfaccia [AdapterView.OnItemSelectedListener] per la gestione dei
     * click sullo spinner [optionSpinner]
     */
    private inner class ItemSelectedListener: AdapterView.OnItemSelectedListener {

        /**
         * Chiamato al click di un elemento sullo spinner [optionSpinner].
         * Aggiorna il valore di [selectedItem] con l'item cliccato.
         * Viene chiamato anche alla rotazione della activity.
         */
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            selectedItem = parent?.getItemAtPosition(position) as String
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            return
        }

    }

    // Creazione activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph_widget_configure)

        // Imposto risultato fallito nel caso in cui l'utente esca senza salvare
        setResult(RESULT_CANCELED)

        // Recupero widgetId
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        // Ottengo riferimenti a views
        saveButton = findViewById(R.id.graphWidgetSaveButton)
        optionSpinner = findViewById(R.id.graphWidgetOption)

        // Imposto dati spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.graph_widget_configure_options, R.layout.graph_widget_spinner_item)
        spinnerAdapter.setDropDownViewResource(R.layout.graph_widget_spinner_item)
        // Inizializzo item selezionato
        selectedItem = spinnerAdapter.getItem(0) as String
        // Imposto adapter e listener sullo spinner
        optionSpinner.adapter = spinnerAdapter
        optionSpinner.onItemSelectedListener = itemSelectedListener

        // Imposto listener sul bottone
        saveButton.setOnClickListener {
            Log.d("GraphWidgetConfigureActivity", "Selezionato $selectedItem su $widgetId")
            saveAndUpdate()
            setResult(RESULT_OK)
            finish()
        }
    }


    /**
     * Salvo impostazione spinner [optionSpinner] su Shared Preferences
     * tramite [selectedItem] e aggiorno il widget con id [widgetId] in background.
     */
    private fun saveAndUpdate(){
        lifecycleScope.launch {
            // Salvo su sharedprefs
            val prefs = getSharedPreferences(GraphWidget.SHARED_PREFERENCES_FILE_PREFIX + widgetId, MODE_PRIVATE).edit()
            prefs.putString(GraphWidget.DATA_SETTINGS, selectedItem)
            prefs.apply()

            // Ottengo istanza AppWidgetMananger
            val appWidgetManager = AppWidgetManager.getInstance(this@GraphWidgetConfigureActivity)
            // Aggiorno widget
            GraphWidget.updateWidget(this@GraphWidgetConfigureActivity, appWidgetManager, widgetId)
        }
    }

}