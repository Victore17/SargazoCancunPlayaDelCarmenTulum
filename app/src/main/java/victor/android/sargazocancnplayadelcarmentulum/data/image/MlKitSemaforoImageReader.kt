package victor.android.sargazocancnplayadelcarmentulum.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import victor.android.sargazocancnplayadelcarmentulum.di.IoDispatcher
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.ImageReadException
import victor.android.sargazocancnplayadelcarmentulum.domain.repository.SemaforoImageReader
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.ArgbArrayPixelSource
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.SemaforoAnalysis
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.SemaforoParser
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.TextLine
import javax.inject.Inject
import kotlin.math.max

/**
 * Lee la imagen del semáforo:
 * 1. La abre como Bitmap (reducida si es muy grande).
 * 2. Lee los renglones de texto con ML Kit (modelo incluido en la app, sin internet).
 * 3. Copia los píxeles del Bitmap (equivalente a Bitmap.getPixel, pero en un solo paso)
 *    y se los pasa a [SemaforoParser], que busca los círculos de color.
 */
class MlKitSemaforoImageReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SemaforoImageReader {

    override suspend fun read(imageUri: String, knownBeachNames: Collection<String>): SemaforoAnalysis {
        val bitmap = withContext(ioDispatcher) { decode(Uri.parse(imageUri)) }
        val lines = recognizeText(bitmap)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val pixelSource = ArgbArrayPixelSource(bitmap.width, bitmap.height, pixels)
        return withContext(ioDispatcher) { SemaforoParser(knownBeachNames).parse(lines, pixelSource) }
    }

    /** Abre la imagen con BitmapFactory (funciona desde Android 8), reduciéndola si es muy grande. */
    private fun decode(uri: Uri): Bitmap {
        val resolver = context.contentResolver
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw ImageReadException("La imagen está vacía o dañada")

            var sampleSize = 1
            while (max(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_SIDE) sampleSize *= 2
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            return resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
                ?: throw ImageReadException("La imagen está vacía o dañada")
        } catch (e: ImageReadException) {
            throw e
        } catch (e: Exception) {
            throw ImageReadException("No se pudo abrir la imagen", e)
        }
    }

    private suspend fun recognizeText(bitmap: Bitmap): List<TextLine> {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val result = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
            result.textBlocks.flatMap { block ->
                block.lines.mapNotNull { line ->
                    val box = line.boundingBox ?: return@mapNotNull null
                    TextLine(line.text, box.left, box.top, box.right, box.bottom)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ImageReadException("No se pudo leer el texto de la imagen", e)
        } finally {
            recognizer.close()
        }
    }

    private companion object {
        /** Lado más largo con el que se analiza la imagen (suficiente para leer los nombres). */
        const val MAX_SIDE = 2400
    }
}
