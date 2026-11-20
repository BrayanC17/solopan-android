package com.castro.solopan.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.view.View
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Convierte imágenes a texto Base64 reducido para guardarlas en Firestore y viceversa.
 * Se limita el lado mayor a 480 px y se comprime a JPEG para no superar el tamaño de un documento.
 */
object ImageUtils {

    private const val MAX_SIDE = 480
    private const val JPEG_QUALITY = 70

    /** Lee una imagen de la galería, la corrige de rotación y la devuelve en Base64 (null si falla). */
    suspend fun uriToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.Default) {
        try {
            val bitmap = decodeSampled(context, uri) ?: return@withContext null
            bitmapToBase64(rotate(bitmap, exifRotation(context, uri)))
        } catch (e: Exception) {
            null
        }
    }

    /** Reduce el bitmap, lo comprime a JPEG y lo devuelve como texto Base64. */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val scale = MAX_SIDE.toFloat() / max(bitmap.width, bitmap.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * scale).roundToInt(), (bitmap.height * scale).roundToInt(), true
            )
        } else bitmap
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    /** Convierte texto Base64 de nuevo en imagen (null si está vacío o dañado). */
    fun base64ToBitmap(data: String): Bitmap? {
        if (data.isBlank()) return null
        return try {
            val bytes = Base64.decode(data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /** Muestra la imagen en el ImageView o lo oculta si no hay foto (deja ver el ícono de fondo). */
    fun show(view: ImageView, data: String) {
        val bitmap = base64ToBitmap(data)
        if (bitmap != null) {
            view.setImageBitmap(bitmap)
            view.visibility = View.VISIBLE
        } else {
            view.setImageDrawable(null)
            view.visibility = View.GONE
        }
    }

    /** Decodifica con muestreo para no cargar fotos enormes completas en memoria. */
    private fun decodeSampled(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > MAX_SIDE * 2 || bounds.outHeight / sample > MAX_SIDE * 2) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    /** Lee la orientación EXIF para que las fotos tomadas con el celular no salgan giradas. */
    private fun exifRotation(context: Context, uri: Uri): Int = try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) {
        0
    }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}