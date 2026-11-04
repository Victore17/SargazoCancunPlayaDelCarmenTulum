package victor.android.sargazocancnplayadelcarmentulum.domain.semaforo

/** Renglón de texto leído en la imagen (por ML Kit), con su caja en píxeles. */
data class TextLine(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val height: Int get() = bottom - top
    val centerY: Int get() = (top + bottom) / 2
}
