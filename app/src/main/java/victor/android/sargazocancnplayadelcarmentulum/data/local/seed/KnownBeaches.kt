package victor.android.sargazocancnplayadelcarmentulum.data.local.seed

import victor.android.sargazocancnplayadelcarmentulum.data.local.entity.BeachEntity

/**
 * Catálogo de las playas del semáforo oficial (100 playas, de Holbox a Tulum y Cozumel),
 * con el nombre exacto que usa la imagen y sus coordenadas.
 *
 * Las coordenadas se verificaron contra la línea de costa de OpenStreetMap (natural=coastline):
 * cada punto queda sobre la costa, unos metros hacia el mar, como en el semáforo oficial.
 * Si se cambian, hay que agregar una migración de Room que las actualice (ver Migrations.kt).
 *
 * Los nombres leídos con ML Kit se cruzan con este catálogo. Las playas nuevas que no estén
 * aquí se guardan sin ubicación ("sin ubicación") y no aparecen en el mapa.
 */
object KnownBeaches {

    val all: List<BeachEntity> = listOf(
        // Holbox, Chiquilá e Isla Contoy
        beach("Cabo Catoche", 21.60500, -87.10500),
        beach("Punta Mosquito", 21.54310, -87.35310),
        beach("Holbox Centro", 21.52496, -87.38047),
        beach("Punta Cocos", 21.50980, -87.39470),
        beach("Holbox Ferry", 21.51924, -87.37509),
        beach("Chiquilá", 21.43341, -87.33641),
        beach("Isla Contoy", 21.47998, -86.79039),

        // Isla Blanca y Costa Mujeres
        beach("Isla Blanca", 21.37898, -86.80057),
        beach("Costa Mujeres", 21.26337, -86.81136),
        beach("Playa Mujeres", 21.24658, -86.80480),
        beach("Punta Sam", 21.23296, -86.80123),
        beach("El Palmar", 21.21133, -86.80226),

        // Cancún centro
        beach("Playa del Niño", 21.20068, -86.80508),
        beach("Puerto Juárez", 21.18369, -86.80687),
        beach("Puerta del Mar", 21.16840, -86.80332),
        beach("Puerto Cancún", 21.15501, -86.79803),
        beach("Cancún Playa Las Perlas", 21.15121, -86.79391),
        beach("Cancún Playa Langosta", 21.14525, -86.78259),
        beach("Cancún Playa Tortugas", 21.13970, -86.76880),

        // Isla Mujeres
        beach("Isla Mujeres Playa Norte", 21.26200, -86.75100),
        beach("Isla Mujeres Playa Mia", 21.24410, -86.74674),
        beach("Isla Mujeres Playa Centro", 21.25090, -86.74410),
        beach("Isla Mujeres Playa Lancheros", 21.22469, -86.73483),
        beach("Isla Mujeres Punta Sur", 21.20285, -86.71149),

        // Cancún zona hotelera
        beach("Cancún Playa Caracol", 21.13727, -86.74574),
        beach("Punta Cancún", 21.13674, -86.74431),
        beach("Cancún Playa Gaviota Azul", 21.13202, -86.74601),
        beach("Cancún Playa Chac-Mool", 21.12842, -86.74888),
        beach("Cancún Playa Marlín", 21.10376, -86.76241),
        beach("Cancún Playa Ballenas", 21.08971, -86.76805),
        beach("Cancún Playa Delfines", 21.06187, -86.77813),
        beach("Cancún Playa San Miguel", 21.04496, -86.78189),
        beach("Cancún Punta Nizuc", 21.03169, -86.78009),

        // De Cancún sur a Puerto Morelos
        beach("Cancún Playa Coral", 20.99837, -86.82267),
        beach("Riviera Cancún", 20.98211, -86.82826),
        beach("Moon Palace", 20.97178, -86.83036),
        beach("Royalton Riviera", 20.94488, -86.83718),
        beach("Bahía Petempich", 20.90079, -86.84990),
        beach("Punta Caracol", 20.88101, -86.86434),
        beach("Puerto Morelos Norte", 20.86116, -86.86984),
        beach("Puerto Morelos Centro", 20.84741, -86.87503),

        // De Puerto Morelos a Playa del Carmen
        beach("Punta Brava", 20.82759, -86.89073),
        beach("Playa El Dorado", 20.78505, -86.93731),
        beach("Playa del Secreto", 20.77786, -86.94303),
        beach("Valentín", 20.77159, -86.94555),
        beach("Vidanta", 20.76101, -86.95486),
        beach("Playa Paraíso", 20.75157, -86.96045),
        beach("Punta Maroma", 20.73755, -86.96556),
        beach("Montecarlo", 20.71429, -86.98858),
        beach("Xcalacoco", 20.66239, -87.03185),
        beach("Punta Esmeralda", 20.64797, -87.05056),

        // Playa del Carmen
        beach("Playa Colosio (Calle 72)", 20.63430, -87.06167),
        beach("Playa Mahekal (Calle 38)", 20.62900, -87.06600),
        beach("Playa Mamitas (Calle 28)", 20.62768, -87.06768),
        beach("Playa Ave. Constituyentes", 20.62545, -87.07136),
        beach("Playa El Recodo (Calle 16)", 20.62200, -87.07396),
        beach("Playa Caribe (Portal Maya)", 20.61861, -87.07670),
        beach("Playacar Terminal del Ferry", 20.61600, -87.07800),
        beach("Playacar ZH Fase 1", 20.60687, -87.08631),
        beach("Playacar ZH Fase 2", 20.59941, -87.09351),

        // Riviera Maya sur y Tulum
        beach("Paamul", 20.52208, -87.19223),
        beach("Puerto Aventuras", 20.49851, -87.22601),
        beach("Barceló Maya", 20.48264, -87.24644),
        beach("Xpu-Ha", 20.47527, -87.25544),
        beach("Kantenah", 20.45159, -87.27529),
        beach("Akumal Media Luna", 20.40230, -87.30911),
        beach("Bahía de Akumal", 20.39575, -87.31419),
        beach("Bahía Príncipe", 20.38739, -87.32163),
        beach("Xcacel-Xcacelito", 20.33588, -87.34845),
        beach("Bahía Soliman", 20.28196, -87.37980),
        beach("Tankah", 20.23149, -87.41785),
        beach("Tulum ZA Ruinas", 20.21400, -87.42850),
        beach("Tulum ZH Norte", 20.19349, -87.43951),
        beach("Punta Piedra", 20.16418, -87.45226),
        beach("Tulum ZH Sur", 20.13896, -87.46092),
        beach("Arco Maya", 20.10289, -87.47244),

        // Cozumel, costa oeste
        beach("Isla de la Pasión", 20.54693, -86.86559),
        beach("Playa Punta Norte", 20.54106, -86.93396),
        beach("Playa Las Rocas", 20.52511, -86.94219),
        beach("Playa San Juan", 20.51699, -86.94588),
        beach("Cozumel Muelle", 20.50950, -86.95187),
        beach("Playa Las Uvas", 20.46019, -86.98588),
        beach("Playa Chankanaab", 20.44279, -86.99505),
        beach("El Cielo", 20.32536, -87.01320),
        beach("San Francisco", 20.40139, -87.01794),
        beach("Playa Mia", 20.37198, -87.02422),
        beach("Palancar", 20.34540, -87.02040),
        beach("Punta Sur", 20.28612, -86.99651),

        // Cozumel, costa este
        beach("Punta Molas", 20.58972, -86.72318),
        beach("Playa Xhanan", 20.54425, -86.74423),
        beach("Playa El Castillo", 20.49847, -86.76949),
        beach("Playa Mezcalitos", 20.42480, -86.84160),
        beach("Playa Chumul", 20.40171, -86.85975),
        beach("Mirador San Martín", 20.36384, -86.88768),
        beach("Playa Chen Río", 20.38257, -86.87553),
        beach("Playa Punta Chiqueros", 20.34457, -86.90035),
        beach("Playa Bonita", 20.34696, -86.90328),
        beach("Playa Encantada", 20.32226, -86.92493),
        beach("Playa El Mirador", 20.29624, -86.95475),
        beach("Punta Celarain", 20.27232, -86.99007),
    )

    /** Nombres del catálogo, para cruzarlos con lo que lee ML Kit. */
    val names: List<String> get() = all.map { it.name }

    private fun beach(name: String, latitude: Double, longitude: Double) =
        BeachEntity(name = name, latitude = latitude, longitude = longitude)
}
