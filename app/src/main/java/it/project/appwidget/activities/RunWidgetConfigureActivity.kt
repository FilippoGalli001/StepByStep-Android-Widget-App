package it.project.appwidget.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import it.project.appwidget.widgets.RunWidget
import it.project.appwidget.R
import it.project.appwidget.WidgetSettingsSharedPrefsHelper


class RunWidgetConfigureActivity : AppCompatActivity() {


    private lateinit var widgetSettingsSharedPrefsHelper: WidgetSettingsSharedPrefsHelper
    private lateinit var cbSpeed: CheckBox
    private lateinit var cbDistance: CheckBox
    private lateinit var cbCalories: CheckBox
    private lateinit var cbSessionDistance: CheckBox
    private var hasPermissions: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        //Creo oggetto SharedPrefsHelper
        widgetSettingsSharedPrefsHelper = WidgetSettingsSharedPrefsHelper(this)

        cbSpeed = findViewById(R.id.cb_speed)
        cbDistance = findViewById(R.id.cb_distance)
        cbCalories = findViewById(R.id.cb_calories)
        cbSessionDistance = findViewById(R.id.cb_session_distance)

        val saveButton: Button = findViewById(R.id.btn_save)
        //setOnClickListener del saveButton
        saveButton.setOnClickListener {
            //Salvo stato CheckBox
            widgetSettingsSharedPrefsHelper.setSpeedChecked(cbSpeed.isChecked)
            widgetSettingsSharedPrefsHelper.setDistanceChecked(cbDistance.isChecked)
            widgetSettingsSharedPrefsHelper.setCaloriesChecked(cbCalories.isChecked)
            widgetSettingsSharedPrefsHelper.setSessionDistanceChecked(cbSessionDistance.isChecked)
            //Lancio intent
            val intent = Intent(this, RunWidget::class.java)
            intent.action = RunWidget.ACTION_BTN_SAVE
            sendBroadcast(intent)
            setResult(RESULT_OK)
            finish()
        }

        //Permette di avere inizialmente (prima volta in assoluto che apro le settings) tutti i check a true
        val isFirstLaunch = widgetSettingsSharedPrefsHelper.isFirstLaunch()
        println(isFirstLaunch)
        if (isFirstLaunch) {
            widgetSettingsSharedPrefsHelper.setSpeedChecked(true)
            widgetSettingsSharedPrefsHelper.setDistanceChecked(true)
            widgetSettingsSharedPrefsHelper.setCaloriesChecked(true)
            widgetSettingsSharedPrefsHelper.setSessionDistanceChecked(true)
            widgetSettingsSharedPrefsHelper.setFirstLaunch(false)

        }

        cbSpeed.isChecked = widgetSettingsSharedPrefsHelper.isSpeedChecked()
        cbDistance.isChecked = widgetSettingsSharedPrefsHelper.isDistanceChecked()
        cbCalories.isChecked = widgetSettingsSharedPrefsHelper.isCaloriesChecked()
        cbSessionDistance.isChecked = widgetSettingsSharedPrefsHelper.isSessionDistanceChecked()

        //RICHIEDO PERMESSI GPS
        // Creo lista di permessi da chiedere
        val permissionList = arrayListOf<String>()

        // Notifiche (a partire da Android 13)
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS)

        // Location approssimata
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Location esatta
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // Chiedo permessi
        if (permissionList.size > 0){
            // Mancano alcuni permessi, provo a chiederli
            hasPermissions = false
            requestPermissions(permissionList.toTypedArray(), 1)
        } // Ricevo aggiornamenti in onRequestPermissionResult()

    }


}
