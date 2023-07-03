package it.project.appwidget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.text.DecimalFormat

/**
 * Classe che implementa un basilare grafico a barre. Viene utilizzato un [Canvas] per disegnare
 * rettangoli sullo schermo, scalati rispetto ad un certo valore massimo.
 * @property labels Lista di etichette da applicare alla barra orizzontale del grafico.
 * @property values Lista di valori da graficare tramite barre.
 */
class BarChart(context: Context, attrs: AttributeSet?): View(context, attrs) {

    /** Array di etichette da applicare lungo l'asse x */
    var labels: ArrayList<String> = arrayListOf("LUN", "MAR", "MER", "GIO", "VEN", "SAB", "DOM")
        set(newArray) {
            field = newArray
            invalidate()
            requestLayout()
        }

    /**
     * Array di valori da rappresentare tramite barre.
     */
    var values: ArrayList<Double> = arrayListOf(10.0, 20.0, 70.0, 30.0, 60.0, 40.0, 50.0)
        set(newArray) {
            field = newArray
            invalidate()
            requestLayout()
        }

    // Oggetti paint per definire le proprietà del disegno
    /** Configurazione [Paint] per le barre verticali del grafico */
    private val barPaint: Paint
    /** Configurazione [Paint] per il testo del grafico */
    private val textPaint: Paint

    init {
        // Ottengo configurazione tema scuro dispositivo
        val nightModeFlags = getContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        // Impostazioni painter per le barre
        barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        barPaint.apply {
            color = Color.CYAN
        }

        // Impostazioni painter per il testo
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.apply {
            // Colore testo in base a tema dispositivo
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> color = Color.WHITE
                Configuration.UI_MODE_NIGHT_NO -> color =Color.BLACK
                Configuration.UI_MODE_NIGHT_UNDEFINED -> color = Color.BLACK
            }
            // Allineamento testo
            textAlign = Paint.Align.LEFT
            // Dimensione testo
            textSize = 35f
        }
    }

    /**
     * Disegno tutte le barre in ordine, scalandole rispetto alla barra più alta (con il valore massimo)
     * Ogni barra è disegnata utilizzando i riferimenti top, bottom left e right.
     * Bottom è costante, definito da height - margine inferiore
     * (nel canvas il sistema di riferimento parte dall'angolo superiore sinistro)
     * Top parte da bottom e viene spostato in alto fino a (height - margine superiore) con un fattore di scalamento (valore/massimo)
     * Left e Right sono rispettivamente a sinistra e a destra del centro di un quarto dello spazio a disposizione
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Margini inferiore e superiore
        val lowerMargin = 50f
        val upperMargin = 100f

        // Numero di barre
        val numbars = values.size
        // Spazio a disposizione per disegnare ogni barra (comprende la spaziatura)
        val space = width / numbars
        // Posizione del punto centrale della barra nel suo spazio relativo
        val relativeCenter = space / 2

        // Bordo inferiore con margine
        val bottom = height - lowerMargin

        Log.d("BarCharView", "Disegno in uno spazio di $width x $height")

        // Cerco l'elemento più grande
        val maxValue: Float = values.max().toFloat()

        // Per ogni barra
        for ((position, value) in values.withIndex()){
            // Calcolo la posizione assoluta del i-esimo centro della barra rispetto alla width della view
            val absCenter = relativeCenter + (position * space)
            // Coordinata del bordo sinistro (un quarto a sinistra del centro)
            val left = absCenter - space/4
            //Coordinata del bordo destro della barra (un quarto a destra del centro)
            val right = absCenter + space/4

            // Calcolo lo scalamento del dato rispetto al massimo
            var scale = 0.0
            if(maxValue != 0f) {
                //Calcolo il rapporto tra il valore e l'elemento più grande del vettore
                scale = value / maxValue
            }

            // Per calcolare l'altezza, parto da bottom e sottraggo height*scale
            val top = bottom - (height - upperMargin)*scale

            Log.d("BarChartView", "Disegno barra $position con coordinate (left, top, right, bottom): $left, $top, $right, $bottom")


            // Evito di graficare il testo se il valore è inferiore a 100mt
            if (value > 0.1) {
                //Disegno rettangolo della barra tramite bordi sinistro, superiore, destro, inferiore
                canvas?.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom, barPaint)
                canvas?.drawText(DecimalFormat("#.#km").format(value), left.toFloat(), top.toFloat(), textPaint)

            }
            // Applico le etichette
            canvas?.drawText(labels[position], left.toFloat(), bottom + 50, textPaint)
        }

    }

    /**
     * Metodo che restituisce l'immagine del grafico a barre.
     * @return Bitmap rappresentante il grafico a barre.
     */
    fun getChartImage(width: Int = 900, height: Int = 850, color: Int = Color.CYAN, dataLabel: String = "km"): Bitmap {
        // Aggiorno painter della classe
        barPaint.color = color
        // Creo una bitmap vuota
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Creo un canvas sulla bitmap
        val canvas = Canvas(bitmap)
        // Disegno la view sul canvas
        onDrawBit(canvas, width, height, dataLabel)
        // Restituisco la bitmap
        return bitmap
    }


    private fun onDrawBit(canvas: Canvas, w: Int, h: Int, label: String) {
        val lowerMargin = 50f
        val upperMargin = -700f
        //Numero di barre
        val numbars = values.size
        //Spazio a disposizione per disegnare ogni barra (comprende lo spazio vuoto attorno a sè)
        val space = w / numbars
        //Distanza del centro della barra dall'inizio dello spazio
        val relativeCenter = space / 2

        // Bordo inferiore con margine
        val bottom = h - lowerMargin

        // Formattazione
        val singleDecimal = DecimalFormat("#.#$label")

        //Cerco l'elemento più grande
        val maxValue: Float = values.max().toFloat()

        //Per ogni barra
        for ((position, value) in values.withIndex()){
            //Calcolo la posizione assoluta del i-esimo centro della barra rispetto alla width della view
            val absCenter = relativeCenter + (position * space)
            //Coordinata del bordo sinistro (metà della distanza tra centro e inizio spazio)
            val left = absCenter - space/4
            //Coordinata del bordo destro della barra (a metà distanza tra centro e fine spazio)
            val right = absCenter + space/4

            var scale = 0.0
            if(maxValue != 0f) {
                //Calcolo il rapporto tra il valore e l'elemento più grande del vettore
                scale = value / maxValue
            }

            // Per calcolare l'altezza, parto da bottom e sottraggo height*scale
            val top = bottom - (height - upperMargin)*scale

            //Disegno rettangolo della barra tramite bordi sinistro, superiore, destro, inferiore
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom, barPaint)
            if (value != 0.0) {
                canvas.drawText(singleDecimal.format(value), left.toFloat(), top.toFloat(), textPaint)

            }
            canvas.drawText(labels[position], left.toFloat(), bottom + 50, textPaint)
        }

    }


}