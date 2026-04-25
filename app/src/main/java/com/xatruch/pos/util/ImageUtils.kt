package com.xatruch.pos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Comprime y convierte una imagen de una Uri a un String Base64.
     * Limita el tamaño a un máximo (ej. 400x400) para no exceder el límite de Firestore (1MB).
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                Log.e(TAG, "No se pudo decodificar el Bitmap desde la URI")
                return null
            }

            // Redimensionar para ahorrar espacio
            val maxSize = 400
            val width = originalBitmap.width
            val height = originalBitmap.height
            
            val finalWidth: Int
            val finalHeight: Int
            
            if (width > height) {
                val ratio = width.toFloat() / height.toFloat()
                finalWidth = maxSize
                finalHeight = (maxSize / ratio).toInt()
            } else {
                val ratio = height.toFloat() / width.toFloat()
                finalHeight = maxSize
                finalWidth = (maxSize / ratio).toInt()
            }

            Log.d(TAG, "Redimensionando de ${width}x${height} a ${finalWidth}x${finalHeight}")
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)
            
            val outputStream = ByteArrayOutputStream()
            // Usar JPEG con calidad media para comprimir
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            
            val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            Log.d(TAG, "Base64 generado exitosamente (Tamaño: ${base64.length} caracteres)")
            
            // Retornamos con el prefijo para que sepamos que es Base64
            "data:image/jpeg;base64,$base64"
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir URI a Base64", e)
            null
        }
    }

    /**
     * Decodifica una cadena Base64 (con o sin prefijo data:image) a ByteArray.
     */
    fun decodeBase64(base64String: String): ByteArray? {
        return try {
            val pureBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            Base64.decode(pureBase64, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Error al decodificar Base64", e)
            null
        }
    }
}
