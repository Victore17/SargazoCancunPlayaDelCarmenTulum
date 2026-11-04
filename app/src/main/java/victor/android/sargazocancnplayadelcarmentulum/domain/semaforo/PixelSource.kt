package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

/**
 * Acceso de solo lectura a los píxeles de una imagen, en formato ARGB (como Bitmap.getPixel).
 * Permite analizar la imagen sin depender de Android: en la app se usa un Bitmap y en las
 * pruebas una imagen cargada con ImageIO.
 */
interface PixelSource {
    val width: Int
    val height: Int

    /** Color ARGB del píxel en (x, y). */
    fun argb(x: Int, y: Int): Int
}

/** [PixelSource] sobre un arreglo de píxeles ARGB ordenado por filas. */
class ArgbArrayPixelSource(
    override val width: Int,
    override val height: Int,
    private val pixels: IntArray,
) : PixelSource {
    init {
        require(pixels.size == width * height) { "El arreglo no coincide con el tamaño de la imagen" }
    }

    override fun argb(x: Int, y: Int): Int = pixels[y * width + x]
}
