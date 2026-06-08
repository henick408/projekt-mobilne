package pl.edu.przedmioty.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils {
    private const val MAX_DIMENSION = 640
    private const val JPEG_QUALITY = 52

    fun createPrivateCameraFile(context: Context): File {
        val cameraDirectory = File(context.cacheDir, "camera").apply { mkdirs() }
        return File.createTempFile("item_", ".jpg", cameraDirectory)
    }

    fun encodeAndDelete(file: File): Result<String> = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            ?: error("Nie udało się odczytać zdjęcia.")

        val rotated = applyExifRotation(bitmap, file)
        if (rotated !== bitmap) bitmap.recycle()

        val scaled = scaleDown(rotated)
        if (scaled !== rotated) rotated.recycle()

        val bytes = ByteArrayOutputStream().use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            stream.toByteArray()
        }
        scaled.recycle()

        Base64.encodeToString(bytes, Base64.NO_WRAP).also { encoded ->
            require(encoded.length <= CatalogValidation.MAX_IMAGE_BASE64_LENGTH) {
                "Zdjęcie jest zbyt duże po kompresji. Wykonaj zdjęcie ponownie."
            }
        }
    }.also {
        file.delete()
    }

    fun decodeBase64(encoded: String): Bitmap? {
        if (encoded.isBlank()) return null
        return runCatching {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    private fun applyExifRotation(bitmap: Bitmap, file: File): Bitmap {
        val orientation = ExifInterface(file.absolutePath)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > MAX_DIMENSION * 2 || height / sampleSize > MAX_DIMENSION * 2) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) return bitmap

        val scale = minOf(MAX_DIMENSION.toFloat() / width, MAX_DIMENSION.toFloat() / height)
        return Bitmap.createScaledBitmap(
            bitmap,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true,
        )
    }
}