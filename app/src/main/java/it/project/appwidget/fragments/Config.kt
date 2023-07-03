package it.project.appwidget.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import it.project.appwidget.R
import it.project.appwidget.UserPreferencesHelper

/**
 * Questo fragment mostra la configurazione per l'utente impostato nell'app.
 * In particolare visualizza parametri quali nome, peso, età,
 * sesso e target calorico giornaliero dell'utente,
 * recuperandoli dalle [SharedPreferences].
 */
class Config : Fragment() {

    /** Pulsante per passare al fragment di modifca dati */
    private lateinit var modifyDataButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ConfigFragment", "Chiamato onViewCreated()")

        /** Inizializza [UserPreferencesHelper] per la lettura delle preferenze */
        val preferencesHelper = UserPreferencesHelper(requireContext())

        modifyDataButton = view.findViewById(R.id.modified_data_btn)

        // Ottiengo i dati di setup
        val nomeUtente = preferencesHelper.nome
        val peso = preferencesHelper.peso
        val eta = preferencesHelper.eta
        val kcalTarget = preferencesHelper.kcalTarget
        val genderId = preferencesHelper.spinnerPosition

        // Imposta listener sul bottone
        modifyDataButton.setOnClickListener {
            // Apri il fragment "setup"
            navigateToSetupFragment()
        }


        // Seleziono etichetta da mostrare
        when(genderId){
            0 -> view.findViewById<TextView>(R.id.valore_sesso).text = "M"
            1 -> view.findViewById<TextView>(R.id.valore_sesso).text = "F"
        }

        // Imposta i valori nei TextView corrispondenti
        view.findViewById<TextView>(R.id.valore_nome).text = nomeUtente
        view.findViewById<TextView>(R.id.valore_peso).text = peso
        view.findViewById<TextView>(R.id.valore_eta).text = eta
        view.findViewById<TextView>(R.id.valore_kcal).text = kcalTarget.toString()
    }


    /**
     * Cerca il [NavController] e naviga al fragment [Setup]
     */
    private fun navigateToSetupFragment() {
        // Ottieni il riferimento al controllore della navigazione dall'Activity
        val navController = activity?.findNavController(R.id.navigationHostFragment)

        // Effettua la navigazione verso il fragment "setup"
        navController?.navigate(R.id.setup)
    }


}