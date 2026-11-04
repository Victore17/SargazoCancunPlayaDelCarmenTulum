package victor.android.sargazocancnplayadelcarmentulum.domain.repository

import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.SemaforoAnalysis

/** No se pudo abrir la imagen o leer su texto. */
class ImageReadException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Lee una imagen del semáforo: texto con ML Kit y colores de sus píxeles. */
interface SemaforoImageReader {

    /**
     * @param imageUri URI de la foto elegida (content://…).
     * @param knownBeachNames nombres oficiales para cruzar con el texto leído.
     * @throws ImageReadException si la imagen no se puede abrir o leer.
     */
    suspend fun read(imageUri: String, knownBeachNames: Collection<String>): SemaforoAnalysis
}
