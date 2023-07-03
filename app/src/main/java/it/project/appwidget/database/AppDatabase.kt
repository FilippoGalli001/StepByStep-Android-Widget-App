package it.project.appwidget.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// la classe implementa il pattern Singleton
// per garantire che esista una sola istanza del database nell'applicazione

/*
Il Singleton viene implementato in modo che la classe AppDatabase abbia un unico punto di accesso globale all'istanza del database.
Il campo INSTANCE mantiene il riferimento all'istanza del database e viene inizializzato come null.
Quando viene chiamato il metodo getInstance(context: Context), il blocco synchronized viene utilizzato per
garantire che solo un thread alla volta possa accedere alla creazione dell'istanza del database. All'interno del blocco,
viene verificato se l'istanza è già stata creata. Se è stata creata, viene restituita direttamente.
Altrimenti, viene utilizzato Room.databaseBuilder per creare l'istanza del database e assegnarla sia al campo INSTANCE che alla variabile instance.
Infine, l'istanza viene restituita.
 */


@Database(entities = [TrackSession::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackSessionDao(): TrackSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Restituisce un'istanza di AppDatabase thread safe.
         */
        fun getInstance(context: Context): AppDatabase {
            Log.d("AppDatabase", "Tentativo accesso istanza database tramite getInstance().")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database").allowMainThreadQueries().build()
                INSTANCE = instance
                Log.d("AppDatabase", "Creato nuovo oggetto database $instance")
                instance
            }
        }
    }
}
