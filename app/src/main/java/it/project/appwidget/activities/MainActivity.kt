package it.project.appwidget.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import it.project.appwidget.R

/**
 * Entry point dell'applicazione.
 * Il layout di questa activity si compone semplicemente di un [NavHostFragment], per ospitare i
 * vari fragment che verranno caricati, e di una [BottomNavigationView] che permette la
 * navigazione tra i fragment.
 * L'activity inoltre prova a chiedere i permessi all'utente.
 */
class MainActivity : AppCompatActivity() {

    /** Variabile per controllo permessi */
    private var hasPermissions: Boolean = false

    /** Riferimento al [NavHostFragment] che ospiterà i vari fragment */
    private lateinit var navigationHostFragment: NavHostFragment

    /** Riferiemento al [NavController] che si occupa della navigazione in questa activity */
    private lateinit var navigationController: NavController

    /** Riferimento alla [BottomNavigationView] che riceve i click della navigazione */
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Chiamato onCreate")
        setContentView(R.layout.activity_main)

        // Ottengo riferimento al navigationHostFragment tramite il supportFragmentManager
        navigationHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHostFragment) as NavHostFragment
        // Dal navigationHostFragment ottengo il controllore della navigation
        navigationController = navigationHostFragment.navController

        // Ottengo riferimento alla bottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        /* Aggangio le action della bottom navigation view al controller che si occupa della navigation.
         Poichè gli id del menù sono gli stessi id dei vari fragments nel grafo di navigazione, il controller
         collega autmaticamente i click sugli elementi della barra (definiti nel menù) al fragment corrispondente */
        bottomNavigationView.setupWithNavController(navigationController)


        // Controllo e chiedo permessi
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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("MainActivity", "Chiamato onSaveInstanceState")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("MainActivity", "Chiamato onRequestPermissionsResult")
        // Controllo ogni permesso
        for ((index, result) in grantResults.withIndex()){
            if (result == PackageManager.PERMISSION_DENIED){
                Log.d("MainActivity", "C'è almeno un permesso negato: ${permissions[index]}")
                hasPermissions = false
                return
            }
        }
        // Se non sono stati negati permessi, sono a posto
        hasPermissions = true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "Chiamato onDestroy")
    }

}
