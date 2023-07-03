package it.project.appwidget.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.project.appwidget.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class TrackSessionAdapter(private val trackSessionList: ArrayList<TrackSession>, private val onClickListener: (TrackSession) -> Unit) :
    RecyclerView.Adapter<TrackSessionAdapter.TrackSessionViewHolder>() {


    // Describes an item view and its place within the RecyclerView
    class TrackSessionViewHolder(itemView: View, val onClickListener: (TrackSession) -> Unit) : RecyclerView.ViewHolder(itemView) {

        // Views associate all'item
        //eliminato l'id della textView
        //private val trackSessionIdTextView: TextView = itemView.findViewById(R.id.trackSessionIdTextView)
        private val trackSessionDateTextView: TextView = itemView.findViewById(R.id.trackSessionDateTextView)
        private val trackSessionTimeTextView: TextView = itemView.findViewById(R.id.trackSessionTimeTextView)
        private val trackSessionDistanceTextView: TextView = itemView.findViewById(R.id.trackSessionDistanceTextView)

        // Salvo trackSession corrente
        private var trackSession: TrackSession? = null

        init {
            // Imposto funzione listener sulla view associata all'item
            itemView.setOnClickListener {
                // Se l'oggetto trackSession non è null, lo passo come argomento alla funzione onClickListener passata al ViewHoder
                trackSession?.let {
                    onClickListener(it)
                }
            }
        }

        fun bind(trackSession: TrackSession) {
            this.trackSession = trackSession

            val dateFormat = SimpleDateFormat("dd/MM")
            val hourFormat = SimpleDateFormat("HH:mm")
            val distanceFormat = DecimalFormat("##.#")

            //trackSessionIdTextView.text = trackSession.id.toString()
            trackSessionDateTextView.text = dateFormat.format(trackSession.startTime)
            trackSessionTimeTextView.text = hourFormat.format(trackSession.startTime)
            trackSessionDistanceTextView.text = distanceFormat.format(trackSession.distance / 1000) + "km"
        }
    }

    // Returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackSessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tracksession_item, parent, false)

        return TrackSessionViewHolder(view, onClickListener)
    }

    // Returns size of data list
    override fun getItemCount(): Int {
        return trackSessionList.size
    }

    // Displays data at a certain position
    override fun onBindViewHolder(holder: TrackSessionViewHolder, position: Int) {
        holder.bind(trackSessionList[position])
    }
}