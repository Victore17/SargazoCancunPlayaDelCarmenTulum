package victor.android.sargazocancnplayadelcarmentulum.data.local.seed

import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity

/**
 * Tabla inicial de playas conocidas de la zona Cancún – Playa del Carmen – Tulum (más Isla Mujeres
 * y Cozumel), con coordenadas aproximadas.
 *
 * En la etapa de lectura del semáforo los nombres leídos con ML Kit se cruzan con esta tabla.
 * Las playas nuevas que no estén aquí se guardan sin ubicación.
 */
object KnownBeaches {

    val all: List<BeachEntity> = listOf(
        // Isla Mujeres
        beach("Playa Norte", 21.2607, -86.7520),
        beach("Costa Mujeres", 21.2260, -86.8060),

        // Cancún (Benito Juárez)
        beach("Playa del Niño", 21.2025, -86.8072),
        beach("Playa Las Perlas", 21.1465, -86.8090),
        beach("Playa Langosta", 21.1424, -86.7895),
        beach("Playa Tortugas", 21.1410, -86.7786),
        beach("Playa Caracol", 21.1376, -86.7480),
        beach("Playa Gaviota Azul", 21.1340, -86.7441),
        beach("Playa Chac Mool", 21.1275, -86.7480),
        beach("Playa Marlín", 21.1110, -86.7580),
        beach("Playa Ballenas", 21.0975, -86.7660),
        beach("Playa Delfines", 21.0607, -86.7800),

        // Puerto Morelos
        beach("Puerto Morelos", 20.8475, -86.8735),

        // Playa del Carmen (Solidaridad)
        beach("Xcalacoco", 20.6610, -87.0400),
        beach("Punta Esmeralda", 20.6385, -87.0590),
        beach("Playa Mamitas", 20.6304, -87.0645),
        beach("Playa 88", 20.6218, -87.0735),
        beach("Playacar", 20.6125, -87.0820),
        beach("Puerto Aventuras", 20.4980, -87.2265),
        beach("Xpu-Há", 20.4705, -87.2590),

        // Tulum
        beach("Akumal", 20.3955, -87.3140),
        beach("Xcacel", 20.3360, -87.3510),
        beach("Playa Ruinas", 20.2140, -87.4285),
        beach("Playa Pescadores", 20.2125, -87.4300),
        beach("Playa Santa Fe", 20.2105, -87.4315),
        beach("Playa Paraíso", 20.2000, -87.4350),
        beach("Playa Las Palmas", 20.1820, -87.4490),

        // Cozumel
        beach("Punta Morena", 20.4060, -86.8320),
        beach("Playa San Martín", 20.3860, -86.8370),
        beach("Chen Río", 20.3620, -86.8380),
        beach("Playa Palancar", 20.3000, -87.0300),
    )

    private fun beach(name: String, latitude: Double, longitude: Double) =
        BeachEntity(name = name, latitude = latitude, longitude = longitude)
}
