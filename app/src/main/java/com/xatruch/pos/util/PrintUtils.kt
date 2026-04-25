package com.xatruch.pos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.print.PrintHelper

object PrintUtils {
    /**
     * Imprime una Vista (View) convirtiéndola primero en un Bitmap.
     * Útil para imprimir facturas que ya están diseñadas en un layout.
     */
    fun printView(context: Context, view: View, jobName: String) {
        try {
            // Aseguramos que la vista tenga un fondo blanco si es transparente
            val bitmap = createBitmapFromView(view)
            val printHelper = PrintHelper(context)
            
            // SCALE_MODE_FIT asegura que la factura quepa en el papel (térmico o carta)
            printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
            
            printHelper.printBitmap(jobName, bitmap)
        } catch (e: Exception) {
            android.util.Log.e("PrintUtils", "Error al imprimir", e)
        }
    }

    private fun createBitmapFromView(view: View): Bitmap {
        // Aseguramos que la vista esté correctamente medida antes de dibujar
        if (view.width <= 0 || view.height <= 0) {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        // Creamos el bitmap con un formato que soporte dibujo manual
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Dibujamos un fondo blanco sólido primero
        canvas.drawColor(Color.WHITE)
        
        // Desactivamos aceleración por hardware temporalmente para capturar la vista completa
        val originalLayerType = view.layerType
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        
        // Dibujamos la vista en el lienzo del bitmap
        view.draw(canvas)
        
        // Restauramos el tipo de capa original
        view.setLayerType(originalLayerType, null)
        
        return bitmap
    }
}
