package victor.android.sargazocancnplayadelcarmentulum.domain.model

/**
 * Nivel de sargazo según el semáforo oficial de Quintana Roo.
 * El color de cada nivel se define en la capa de UI.
 */
enum class SargassumLevel {
    /** Verde: escasa. */
    LOW,

    /** Amarillo: moderada. */
    MODERATE,

    /** Naranja: alta. */
    HIGH,

    /** Rojo: muy alta. */
    VERY_HIGH,

    /** Azul: sin dato. */
    NO_DATA,
}
