# MetroRuta Medellín

Planificador de rutas para la red del Metro de Medellín. Proyecto final de Estructuras de Datos.

Integrantes: Samuel Rodríguez Montoya e Ian Isaza Bermúdez.

## Tecnologías
Java 21, Spring Boot, Vaadin, PostgreSQL (con Docker) y Maven.

## Cómo correrlo
1. Tener instalados Java 21 y Docker Desktop (abierto).
2. Levantar la base de datos: `docker compose up -d`
3. Arrancar la aplicación: `./mvnw spring-boot:run`
4. Abrir http://localhost:8080 (la primera vez tarda unos minutos).

PostgreSQL de este proyecto usa el puerto local `5433`, para no confundirse con otra instalación de PostgreSQL en `5432`. El volumen de Docker conserva las estaciones, los cierres y el historial al reiniciar.

## Usuarios de prueba
- Administrador: `admin` con la clave `admin123`
- Usuario normal: `usuario` con la clave `usuario123`

## Cómo trabajamos
Tablero y tareas en GitHub Projects. Cada tarea tiene su rama y su Pull Request.

## Funcionalidades
Calcula la mejor ruta entre dos estaciones del Metro de Medellín (líneas A y B, tranvía T, cables H, J, K, L, M y P, y buses 1, 2 y O), por tiempo o por menos transbordos. Incluye detalle de la ruta, hora de llegada, diagrama con la ruta resaltada, cierre y reapertura de estaciones con deshacer, actualización en vivo entre sesiones, historial de rutas y administración de estaciones y conexiones.

## Estructuras de datos
`MinHeap`, `Pila`, `ListaEnlazada`, `Grafo` y `Trie` propios en el paquete `structures`.

## Arquitectura
- `model`: entidades JPA (estaciones, líneas, conexiones, usuarios, cierres, historial).
- `repository`: acceso a la base de datos con Spring Data.
- `service`: lógica (rutas, cierres, administración, búsqueda).
- `view`: pantallas con Vaadin.
- `structures`: nuestras estructuras de datos.
- `config` y `dto`: configuración y objetos de resultado.

## Pruebas
Las pruebas usan una base separada para conservar los cierres y el historial de la aplicación. Créala una vez con `docker exec metroruta-db createdb -U metroruta metroruta_test` y ejecuta `./mvnw verify`. Se puede configurar otra base de pruebas con `METRORUTA_TEST_DATABASE_URL`.

Cubren casos límite: sin camino, origen igual a destino, empates de tiempo, estación cerrada y datos inválidos. También comprueban Universidad de Medellín → Niquía con Estadio, San Antonio y Cisneros cerradas, ambos sentidos y criterios, y el desvío por la línea O si además cierra Palos Verdes.

Con Node.js instalado, `node --test src/test/js/metro-map.test.mjs` comprueba que los nombres no se encimen ni se recorten en los cuatro niveles de zoom y en tamaños de escritorio y móvil.

## Red y cartografía

La carga inicial incluye 84 estaciones y paradas y 100 tramos en las 12 líneas principales del [Sistema Integrado](https://www.metrodemedellin.gov.co/usuarios/sistema-integrado/). Las bases existentes reciben las líneas faltantes al arrancar. Se agrupan las paradas por sentido cuando representan la misma parada, y algunos segmentos de bus se dibujan de forma aproximada. Los tiempos son estimados y excluyen espera y frecuencia. La penalización de transbordo es de 4 minutos por defecto, configurable entre 3 y 5 mediante `METRORUTA_TRANSBORDO_MINUTOS`.

El mapa carga con toda la red a la vista y el norte arriba. Al calcular una ruta, el primer tramo se resalta en rosa y el color alterna con lila en cada transbordo. Los nombres y marcadores mantienen su tamaño al acercar. Se muestran primero el origen, destino y transbordos, y los demás nombres aparecen donde hay espacio; cada estación conserva su nombre al pasar el puntero. Con zoom se puede arrastrar el mapa o moverlo con las flechas del teclado. «Ver todo» restaura la vista completa. El itinerario resume cada línea y permite desplegar sus paradas.

- [Trazados y estaciones del Metro de Medellín](https://services3.arcgis.com/ARUXxWVTK25ENUfB/ArcGIS/rest/services/Sistema_Metro2/FeatureServer), simplificados para el mapa.
- [Río Medellín de GeoMedellín](https://www.medellin.gov.co/servidormapas/rest/services/ServiciosCiudad/CartografiaBase/MapServer/6) y [vías principales del Valle de Aburrá](https://services7.arcgis.com/e2M5JRjf6HcmygSb/ArcGIS/rest/services/VIAS_PRINCIPALES_LOTERO/FeatureServer/1).
- [Relieve de Mapzen Terrain Tiles](https://registry.opendata.aws/terrain-tiles/), con curvas cada 150 m.

Entre Floresta y Caribe, compara la ruta más rápida con el viaje directo por la línea O. El mapa y el itinerario indican dónde cambiar de línea.
