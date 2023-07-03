package it.project.appwidget

import android.content.Context
import android.content.SharedPreferences

/**
 * Classe che rappresenta le informazioni utente descritte in uno Shared Preferences.
 * @property nome Nome utente
 * @property eta Età utente
 * @property peso Peso utente
 * @property sesso Sesso utente
 * @property kcalTarget Target calorico giornaliero
 */
class UserPreferencesHelper(context: Context) {
    companion object {
        private const val PREFS_FILE_NAME = "UserPreferences"
        private const val KEY_NOME_UTENTE = "nome_utente"
        private const val KEY_PESO = "peso"
        private const val KEY_ETA = "eta"
        private const val KEY_SESSO = "sesso"
        private const val KEY_KCAL_TARGET = "kcal"
        private const val POSITION_SPINNER = "spinner_position"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    /**
     * Nome utente
     */
    var nome: String
        get() = sharedPreferences.getString(KEY_NOME_UTENTE, "Utente") ?: "Utente"
        set(value) = sharedPreferences.edit().putString(KEY_NOME_UTENTE, value).apply()

    /**
     * Età utente
     */
    var peso: String
        get() = sharedPreferences.getString(KEY_PESO, "70") ?: "70"
        set(value) = sharedPreferences.edit().putString(KEY_PESO, value).apply()

    /**
     * Peso utente
     */
    var eta: String
        get() = sharedPreferences.getString(KEY_ETA, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_ETA, value).apply()

    /**
     * Sesso utente
     */
    var sesso: String
        get() = sharedPreferences.getString(KEY_SESSO, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_SESSO, value).apply()

    /**
     * Target calorico giornaliero
     */
    var kcalTarget: Int
        get() = sharedPreferences.getInt(KEY_KCAL_TARGET, 100)
        set(value) = sharedPreferences.edit().putInt(KEY_KCAL_TARGET, value).apply()

    /**
     * Posizione spinner
     */
    var spinnerPosition: Int
        get() = sharedPreferences.getInt(POSITION_SPINNER, 0)
        set(value) = sharedPreferences.edit().putInt(POSITION_SPINNER, value).apply()
}
