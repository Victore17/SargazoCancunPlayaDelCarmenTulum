package victor.android.sargazocancnplayadelcarmentulum.domain.model

/**
 * Nivel de sargazo según el semáforo oficial de Quintana Roo.
 * El color de cada nivel se define en la capa de UI.
 *
 * Los nombres de las constantes se guardan en Room y en Firestore: no cambiarlos.
 */
enum class SargassumLevel {
    /** Azul: sin sargazo (playa limpia). */
    NO_SARGASSUM,

    /** Verde: bajo. */
    LOW,

    /** Amarillo: moderado. */
    MODERATE,

    /** Naranja: abundante. */
    ABUNDANT,

    /** Rojo: excesivo. */
    EXCESSIVE,

    /** Gris: la playa no aparece en el semáforo más reciente. */
    NO_DATA,
}
