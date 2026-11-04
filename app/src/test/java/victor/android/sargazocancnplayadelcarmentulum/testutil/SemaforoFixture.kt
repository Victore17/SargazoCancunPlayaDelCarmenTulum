package victor.android.sargazocancnplayadelcarmentulum.testutil

import victor.android.sargazocancnplayadelcarmentulum.domain.model.SargassumLevel
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.ArgbArrayPixelSource
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.PixelSource
import victor.android.sargazocancnplayadelcarmentulum.domain.semaforo.TextLine
import javax.imageio.ImageIO

/**
 * Datos de prueba basados en la imagen oficial real del semáforo del 03/11/2026
 * (src/test/resources/semaforo/semaforo_2026_11_03.png, 997 × 1261 px).
 *
 * ML Kit solo funciona en Android, así que los renglones de texto se midieron sobre la imagen
 * y se escribieron aquí a mano, con la caja de cada nombre en píxeles.
 */
object SemaforoFixture {

    private const val IMAGE_RESOURCE = "semaforo/semaforo_2026_11_03.png"

    /** Carga la imagen real como [PixelSource] (ImageIO entrega los píxeles en ARGB, igual que Bitmap). */
    fun loadImage(): PixelSource {
        val stream = requireNotNull(javaClass.classLoader?.getResourceAsStream(IMAGE_RESOURCE)) {
            "No se encontró $IMAGE_RESOURCE"
        }
        val image = stream.use { ImageIO.read(it) }
        val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        return ArgbArrayPixelSource(image.width, image.height, pixels)
    }

    /** Conteo de círculos por color que se ve en la imagen (incluye el bulbo rojo del termómetro). */
    val expectedDotCounts = mapOf(
        SargassumLevel.EXCESSIVE to 2, // Punta Molas + bulbo del termómetro
        SargassumLevel.ABUNDANT to 4,
        SargassumLevel.MODERATE to 9,
        SargassumLevel.LOW to 82,
        SargassumLevel.NO_SARGASSUM to 4, // Holbox Ferry, Chiquilá, Isla Contoy y Punta Sam
    )

    /** Renglones con el nivel que se ve en la imagen junto a cada nombre. */
    val beachLines: List<Pair<TextLine, SargassumLevel>> = listOf(
        TextLine("Cabo Catoche", 317, 10, 430, 24) to SargassumLevel.LOW,
        TextLine("Holbox Ferry", 231, 116, 343, 136) to SargassumLevel.NO_SARGASSUM,
        TextLine("Isla Contoy", 621, 155, 717, 174) to SargassumLevel.NO_SARGASSUM,
        TextLine("Chiquilá", 175, 174, 245, 194) to SargassumLevel.NO_SARGASSUM,
        TextLine("Punta Sam", 720, 272, 792, 296) to SargassumLevel.NO_SARGASSUM,
        TextLine("Cancún Playa Marlín", 775, 467, 908, 482) to SargassumLevel.LOW,
        TextLine("Cancún Playa Delfines", 763, 502, 906, 517) to SargassumLevel.LOW,
        TextLine("Cancún Playa Coral", 559, 514, 695, 530) to SargassumLevel.ABUNDANT,
        TextLine("Riviera Cancún", 577, 535, 682, 549) to SargassumLevel.MODERATE,
        TextLine("Punta Caracol", 563, 619, 657, 632) to SargassumLevel.MODERATE,
        TextLine("Puerto Morelos Norte", 496, 638, 654, 652) to SargassumLevel.MODERATE,
        TextLine("Punta Molas", 797, 870, 894, 896) to SargassumLevel.EXCESSIVE,
        TextLine("Playa Xhanan", 791, 911, 888, 927) to SargassumLevel.ABUNDANT,
        TextLine("Playa El Castillo", 782, 928, 895, 945) to SargassumLevel.ABUNDANT,
        TextLine("Playa Mezcalitos", 771, 963, 886, 977) to SargassumLevel.ABUNDANT,
        TextLine("Playa Chumul", 760, 985, 847, 1000) to SargassumLevel.MODERATE,
        TextLine("Playa Chen Río", 736, 1010, 838, 1024) to SargassumLevel.MODERATE,
        TextLine("Mirador San Martín", 700, 1025, 844, 1041) to SargassumLevel.MODERATE,
        TextLine("Playa Punta Chiqueros", 684, 1058, 840, 1074) to SargassumLevel.MODERATE,
        TextLine("Xcacel-Xcacelito", 105, 1107, 225, 1131) to SargassumLevel.MODERATE,
    )

    /** Textos que no son playas: leyenda, fecha y títulos. */
    val otherLines: List<TextLine> = listOf(
        TextLine("Temperatura", 814, 197, 970, 221),
        TextLine("03/Noviembre /2026", 69, 223, 280, 266),
        TextLine("Excesivo", 123, 279, 233, 298),
        TextLine("Abundante", 100, 327, 260, 350),
        TextLine("Moderado", 105, 375, 255, 398),
        TextLine("Bajo", 146, 424, 205, 452),
        TextLine("Sin Sargazo", 98, 464, 265, 500),
        TextLine("Total de Playas", 69, 512, 280, 555),
    )

    val allLines: List<TextLine> get() = beachLines.map { it.first } + otherLines
}
