# CLAUDE.md

Reglas del proyecto para Claude Code. Nombre de la app: Sargazo Cancún Playa del Carmen Tulum (úsalo en app\_name de strings.xml y en el README). App Android de portafolio que lee el semáforo oficial de sargazo de Quintana Roo y muestra el nivel de cada playa en un mapa. El plan completo está en PLAN.md.

## Idioma

- Responde siempre en español, sin excepción: explicaciones, resúmenes, preguntas y mensajes de error que me expliques.
- El README.md del proyecto, con la información para GitHub, se escribe siempre en inglés.

## Cómo trabajar

- Trabaja solo la etapa de PLAN.md que se te pida. No avances a la siguiente sin que te lo pidan.
- Antes de escribir código, explica brevemente qué vas a hacer en esa etapa.
- Al terminar, compila el proyecto (./gradlew assembleDebug). Si hay un dispositivo físico conectado (revísalo con adb devices), instala y abre la app en él (./gradlew installDebug y adb shell am start) para que vea los cambios. Las pruebas se corren solo en dos momentos: al terminar de construir una etapa y cuando yo te lo pida. En ningún otro momento las corras, ni en correcciones, ajustes o cambios después de terminar una etapa. Al terminar cada etapa: 1) crea las pruebas unitarias de esa etapa, 2) corre todas las pruebas unitarias del proyecto (./gradlew test), las de esa etapa y las de todas las anteriores, para confirmar que nada se rompió. Si alguna falla, dime cuál y por qué antes de seguir; nunca cambies una prueba solo para que pase. Las pruebas de interfaz (con emulador o celular) solo cuando yo las pida.
- Resume qué archivos creaste o cambiaste y qué debo probar en el emulador.
- No hagas commit sin que te lo pida. Los mensajes de commit van siempre en inglés, cortos y claros.
- Si algo del plan no está claro, pregunta antes de suponer.

## Tecnologías obligatorias

- Kotlin, sin Java.
- Jetpack Compose con Material Design 3. Nada de XML para pantallas.
- Arquitectura MVVM con capas data, domain y ui.
- Hilt para inyección de dependencias.
- Room con KSP para la base de datos local.
- Coroutines y Flows. Usar StateFlow en los ViewModels, no LiveData.
- Retrofit, OkHttp y Kotlinx Serialization para red.
- MapLibre Compose (maplibre-compose) con mosaicos de OpenStreetMap para el mapa, sin clave de API ni cuenta de pago. No usar Google Maps. ML Kit Text Recognition para leer la imagen, WorkManager para tareas en segundo plano, DataStore para ajustes, Coil para imágenes.
- Firebase Authentication (correo y contraseña, solo para el administrador) y Cloud Firestore (niveles publicados), usando el BOM de Firebase.
- Versiones estables más recientes, declaradas en gradle/libs.versions.toml y usando el Compose BOM.

## Estilo de código

- Nombres de clases, funciones y variables en inglés. Textos visibles para el usuario en español, siempre en strings.xml.
- Un ViewModel por pantalla, con un estado de UI inmutable (data class) expuesto como StateFlow.
- Composables pequeños y con Preview.
- Manejar errores de red y de lectura de imagen sin que la app se cierre; mostrar un mensaje al usuario.
- Nada de lógica de negocio dentro de los Composables.

## Seguridad y GitHub

- El mapa no usa claves de API (MapLibre + OpenStreetMap). Si en el futuro se agrega algún servicio con clave, va en local.properties y nunca se sube al repositorio.
- Mostrar siempre la atribución "© OpenStreetMap" en el mapa y hacer un uso ligero de los mosaicos públicos.
- Revisa que .gitignore incluya local.properties, google-services.json, archivos de build y claves.
- La pantalla oculta de administración no es una medida de seguridad. La escritura en Firestore se protege con Firebase Authentication y reglas de seguridad que solo permiten escribir al UID del administrador. Nunca pongas correos ni contraseñas en el código.
- No pongas datos personales ni claves en el código.

## Datos

- Fuente: semáforo oficial del Gobierno de Quintana Roo (SEMA). No usar imágenes de periódicos.
- El administrador sube la imagen oficial desde la pantalla oculta; la app publica en Firestore solo los niveles, no la imagen. Las apps leen Firestore y guardan una copia en Room.
- Niveles: verde escasa, amarillo moderada, naranja alta, rojo muy alta, azul sin dato.
- Historial de niveles: guardar solo los últimos dos meses. Favoritas y ajustes nunca se borran.
- Mostrar en la app la fuente de los datos y que no es una app oficial.

## Pruebas

- Pruebas unitarias para ViewModels, repositorios y la lectura de colores del semáforo.
- Usar Turbine para probar Flows.
