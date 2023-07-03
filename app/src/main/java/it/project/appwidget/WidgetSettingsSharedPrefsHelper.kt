package it.project.appwidget

import android.content.Context
import android.content.SharedPreferences

/**
 * Classe helper separata per gestire le operazioni relative alle preferenze condivise.
 * Ha lo scopo di poter aver accesso alle preferences sia in RunWidgetConfigureActivity che RunWidget in modo più semplice e centralizzato
 */
class WidgetSettingsSharedPrefsHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("isFirstLaunch", true)
    }

    fun setFirstLaunch(isFirstLaunch: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstLaunch", isFirstLaunch)
        editor.apply()
    }

    fun isSpeedChecked(): Boolean {
        return sharedPreferences.getBoolean("speed", true)
    }

    fun setSpeedChecked(isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("speed", isChecked)
        editor.apply()
    }

    fun isDistanceChecked(): Boolean {
        return sharedPreferences.getBoolean("distance", true)
    }

    fun setDistanceChecked(isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("distance", isChecked)
        editor.apply()
    }

    fun isCaloriesChecked(): Boolean {
        return sharedPreferences.getBoolean("calories", true)
    }

    fun setCaloriesChecked(isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("calories", isChecked)
        editor.apply()
    }

    fun isSessionDistanceChecked(): Boolean {
        return sharedPreferences.getBoolean("sessionDistance", true)
    }

    fun setSessionDistanceChecked(isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("sessionDistance", isChecked)
        editor.apply()
    }

}

