package it.project.appwidget.fragments

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputLayout
import it.project.appwidget.R
import it.project.appwidget.UserPreferencesHelper

/**
 * Questo fragment si occupa di mostrare all'utente una schermata per la modifica della
 * configurazione utente mostrata in [Config]. Permette di modifcare parameteri quali
 * nome, peso, età, sesso e traget calorico giornaliero, salvandoli sulle [SharedPreferences].
 */
class Setup : Fragment() {

    /** Variabile per l'accesso agevole alle [SharedPreferences] dell'utente utente */
    private lateinit var preferencesHelper: UserPreferencesHelper
    /** Indice posizione riferita al genere maschile o femminile */
    private var positionSelected: Int = -1
    /** Elemento selezionato della lista*/
    private lateinit var genderItem: String

    /** Listener che si aggiorna quando seleziono un elemento diverso della lista*/
    private val itemSelectedListener = ItemSelectedListener()

    /** Classe per la gestione dei click nello Spinner */
    private inner class ItemSelectedListener: AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            genderItem = parent?.getItemAtPosition(position) as String
            positionSelected = position
            Log.d("SetupFragment", "Selezionato genere $positionSelected")
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Se non seleziono nulla prendo l'item di default (cioè M)
            return
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inizializzo preferenze utente
        preferencesHelper = UserPreferencesHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Imposto valori nei vari campi
        populateFields(view)

        // Recupero lo spinner
        val genderSpinner = view.findViewById<Spinner>(R.id.gender_Spinner)
        // Recupero bottone salvataggio
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        // Imposto dati spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.gender_configure_options, R.layout.gender_spinner_item)
        spinnerAdapter.setDropDownViewResource(R.layout.gender_spinner_item)

        // Inizializzo item selezionato
        genderItem = spinnerAdapter.getItem(0) as String
        // Imposto adapter e listener sullo spinner
        genderSpinner.adapter = spinnerAdapter
        genderSpinner.onItemSelectedListener = itemSelectedListener

        // Creo alert da utilizzare nel caso in cui tutti i campi non siano stati completati
        val alert = AlertDialog.Builder(context)
            .setTitle("Inserimento errato")
            .setMessage("Non hai compilato correttamente i campi")
            .setIcon(R.drawable.icons8_errore_24)
            .setPositiveButton("OK"){_, _ -> }

        btnSave.setOnClickListener {
            // Controllo se tutti i dati sono stati completati
            if (!areAllFieldsFilled(view)){
                alert.show()    // Mostro alert all'utente
                return@setOnClickListener
            }

            // Salvataggio dei dati
            val nomeUtente: String = (view.findViewById<TextInputLayout>(R.id.nome_utente)?.editText?.text ?: "").toString()
            val peso: String = (view.findViewById<TextInputLayout>(R.id.peso)?.editText?.text ?: "").toString()
            val eta: String = (view.findViewById<TextInputLayout>(R.id.eta)?.editText?.text ?: "").toString()
            val sesso: String = genderItem
            val kcalTarget: String = (view.findViewById<TextInputLayout>(R.id.kcalTarget)?.editText?.text ?: "").toString()

            // Salva i dati utilizzando l'helper nelle preferenze condivise
            preferencesHelper.nome = nomeUtente
            preferencesHelper.peso = peso
            preferencesHelper.eta = eta
            preferencesHelper.sesso = sesso
            preferencesHelper.kcalTarget = kcalTarget.toInt()
            preferencesHelper.spinnerPosition = positionSelected

            // Apri il fragment "config"
            navigateToSetupFragment()
        }
    }


    /**
     * Controlla che tutti i campi siano riempiti
     */
    private fun areAllFieldsFilled(view: View): Boolean {
        // Recupero testo (se esiste) dalle views
        val nomeUtente: String = (view.findViewById<TextInputLayout>(R.id.nome_utente).editText?.text ?: "").toString()
        val peso: String = (view.findViewById<TextInputLayout>(R.id.peso).editText?.text ?: "").toString()
        val eta: String = (view.findViewById<TextInputLayout>(R.id.eta).editText?.text ?: "").toString()
        val kcalTarget: String = (view.findViewById<TextInputLayout>(R.id.kcalTarget).editText?.text ?: "").toString()
        // Controllo che il testo non sia nullo
        return nomeUtente.isNotEmpty() && peso.isNotEmpty() && eta.isNotEmpty() && kcalTarget.isNotEmpty()
    }

    /**
     * Riempi TextInputLayout con i dati precedentemente inseriti
     */
    private fun populateFields(view: View) {
        val nomeUtente = preferencesHelper.nome
        val peso = preferencesHelper.peso
        val eta = preferencesHelper.eta
        val kcalTarget = preferencesHelper.kcalTarget
        val positionSelected = preferencesHelper.spinnerPosition


        view.findViewById<TextInputLayout>(R.id.nome_utente)?.editText?.setText(nomeUtente)
        view.findViewById<TextInputLayout>(R.id.peso)?.editText?.setText(peso)
        view.findViewById<TextInputLayout>(R.id.eta)?.editText?.setText(eta)
        view.findViewById<Spinner>(R.id.gender_Spinner)?.setSelection(positionSelected)
        view.findViewById<TextInputLayout>(R.id.kcalTarget)?.editText?.setText(kcalTarget.toString())
    }

    /**
     * Utilizza in [NavController] per tornare al fragment [Config]
     */
    private fun navigateToSetupFragment() {
        // Ottieni il riferimento al controllore della navigazione dall'activity
        val navController: NavController? = activity?.run {
            findNavController(R.id.navigationHostFragment)
        }

        // Effettua la navigazione verso il fragment "config" e aggiungi "Setup" al back stack
        navController?.navigate(R.id.config)
        navController?.popBackStack(R.id.setup, true)
    }

}

