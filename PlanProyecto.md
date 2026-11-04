# Plan: Sargazo Cancún Playa del Carmen Tulum (Android)

Nombre de la app: Sargazo Cancún Playa del Carmen Tulum. Proyecto de portafolio para GitHub. App que lee el semáforo oficial de sargazo de Quintana Roo y muestra el nivel de cada playa en un mapa.

## Cómo usar este plan en Claude Code

- Guarda este archivo como `PLAN.md` en la raíz del proyecto.
- Crea un `CLAUDE.md` con las reglas: Kotlin, Jetpack Compose, Hilt, Room, Coroutines, Flows/StateFlow, Retrofit, arquitectura MVVM.
- Por cada etapa escribe: "Lee PLAN.md y haz la etapa N".
- Al terminar cada etapa: prueba en Android Studio, haz commit y usa /clear antes de la siguiente.

## Tecnologías

- Jetpack Compose (pantallas)
- MapLibre Compose (mapa con mosaicos de OpenStreetMap, sin clave ni costo)
- Hilt (inyección de dependencias)
- Room (base de datos local)
- Coroutines + Flows/StateFlow
- Retrofit (clima con Open-Meteo)
- ML Kit Text Recognition (leer nombres de playas en la imagen)
- WorkManager (revisión periódica y notificaciones)

## Diseño y librerías

Usar siempre las versiones estables más recientes, administradas con el catálogo de versiones de Gradle (libs.versions.toml) y el Compose BOM.

- Material Design 3 (Material 3 para Compose), con tema claro y oscuro y colores dinámicos (Material You)
- Navigation Compose (navegación entre pantallas)
- Lifecycle ViewModel Compose (ViewModels y estado)
- Hilt + hilt-navigation-compose
- Room con KSP
- MapLibre Compose (maplibre-compose) + runtime OpenGL de MapLibre Native
- Retrofit + OkHttp + Kotlinx Serialization (llamadas a Open-Meteo)
- Coil (descargar la imagen del semáforo)
- ML Kit Text Recognition
- WorkManager
- DataStore (guardar los ajustes de notificaciones)
- Pruebas: JUnit, Turbine para Flows y pruebas de Compose

## Fuente de datos

- Semáforo oficial del Gobierno de Quintana Roo (SEMA / Coordinación General de Comunicación, cgc.qroo.gob.mx). No usar imágenes de periódicos.
- Colores: verde = escasa, amarillo = moderada, naranja = alta, rojo = muy alta, azul = sin dato.
- Mostrar en la app la fuente de los datos y un aviso de que no es una app oficial.

## Etapa 1: Proyecto base y arquitectura (Opus)

- Crear proyecto con Compose, Hilt, Room, Coroutines y navegación.
- Estructura por capas: data, domain, ui.
- Definir entidades de Room: Playa (nombre, latitud, longitud, posición en la imagen, favorita, notificaciones activadas), RegistroNivel (playa, nivel, fecha), Ajustes.
- Tabla inicial de coordenadas de playas conocidas.

## Etapa 2: Mapa (Sonnet)

- Pantalla principal con MapLibre Compose y mosaicos de OpenStreetMap (sin clave de API, sin costo).
- Limitar el mapa a la zona de playas (Tulum a Cancún, con Cozumel si viene en el semáforo) usando CameraConstraints (boundingBox, minZoom y maxZoom).
- Mostrar la atribución "© OpenStreetMap" y hacer un uso ligero de los mosaicos públicos. No se necesita clave.

## Etapa 3: Leer el semáforo (Opus)

- Descargar la imagen oficial del semáforo.
- Detectar que la imagen es un semáforo de sargazo (texto de la publicación y palabras clave leídas con ML Kit).
- Leer los nombres de las playas con ML Kit.
- Leer el color de cada playa en la imagen (Bitmap.getPixel) y convertirlo a nivel.
- Cruzar nombres con la tabla de coordenadas. Playas nuevas sin coordenadas: aparecen en la lista como "sin ubicación".
- Guardar resultados en Room.
- Pintar en el mapa un círculo o marcador por playa con su color.

## Etapa 4: Detalle de playa (Sonnet)

- Al tocar una playa: nivel actual, fecha de actualización e historial.
- Clima y estado del mar con Retrofit y Open-Meteo.
- Botón para agregar o quitar de favoritas.

## Etapa 5: Lista y favoritas (Sonnet)

- Pantalla de lista de playas sacada del semáforo, con buscador y filtro por color.
- Pantalla de favoritas.

## Etapa 6: Notificaciones y limpieza (Sonnet)

- WorkManager revisa cada 3 a 6 horas si hay semáforo nuevo (comparando fecha o huella de la imagen).
- Notificación 1, actualización general: avisa que hay información nueva para entrar a revisar.
- Notificación 2, cambios en favoritas: compara el nivel nuevo con el guardado y avisa si una favorita tiene más o menos sargazo. Si cambian varias, juntarlas en una sola notificación.
- Limpieza automática: borrar registros de niveles con más de dos meses. Favoritas y ajustes nunca se borran.

## Etapa 7: Ajustes (Sonnet)

- Interruptor general "Todas las notificaciones": al apagarlo se apagan todas, incluidas las de cada favorita.
- Interruptor para la notificación de actualización general.
- Interruptor para la notificación de cambios en favoritas.
- Interruptor por cada playa favorita.
- Al volver a encender el interruptor general, restaurar la configuración anterior.

## Etapa 8: Ícono de la app (Sonnet)

- Diseñar el ícono como una ola del mar, simple y reconocible, en tonos azules y turquesa que combinen con el tema Material 3.
- Hacerlo como ícono adaptativo de Android (ic\_launcher): capa de fondo (color o degradado azul) y capa frontal con la ola como vector (XML vector drawable), para que se vea bien en cualquier forma de ícono.
- Incluir el ícono monocromático para los íconos temáticos de Android 13 o superior.
- Diseño original, sin usar logos ni imágenes de otras marcas.
- Revisar cómo se ve en el celular (pantalla de inicio y lista de apps) y probar en modo claro y oscuro.
- Guardar también una versión del ícono en el README para GitHub.

## Etapa 9: Pulir para GitHub (Haiku)

- README con descripción, capturas, tecnologías y fuente de datos.
- Comentarios en el código y textos en strings.xml.
- Revisar que no haya claves ni datos personales en el repositorio.

## Pantallas

1. Mapa
2. Detalle de playa
3. Lista de playas
4. Favoritas
5. Ajustes
