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

## Usuarios de prueba
- Administrador: `admin` con la clave `admin123`
- Usuario normal: `usuario` con la clave `usuario123`

## Cómo trabajamos
Tablero y tareas en GitHub Projects. Cada tarea tiene su rama y su Pull Request.

## Funcionalidades
Calcula la mejor ruta entre dos estaciones del Metro de Medellín (Línea A, Línea B, Tranvía de Ayacucho y Metrocable Línea K), por tiempo o por menos transbordos. Incluye detalle de la ruta, hora de llegada, diagrama con la ruta resaltada, cierre y reapertura de estaciones con deshacer, actualización en vivo entre sesiones, historial de rutas y administración de estaciones y conexiones.

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
`./mvnw test` (necesita la base de datos arriba). Las pruebas cubren casos límite: sin camino, origen igual a destino, empates de tiempo, estación cerrada y datos inválidos.

## Red y cartografía

La carga inicial incluye 38 estaciones y 37 tramos en A, B, T y K. Los tiempos son estimados y excluyen espera y frecuencia. La penalización de transbordo es de 4 minutos por defecto, configurable entre 3 y 5 mediante `METRORUTA_TRANSBORDO_MINUTOS`.

El mapa carga con toda la red a la vista. Al calcular una ruta, el primer tramo se resalta en rosa y el color alterna con lila en cada transbordo. El zoom permite inspeccionar estaciones cercanas.

- [Trazados y estaciones del Metro de Medellín](https://services3.arcgis.com/ARUXxWVTK25ENUfB/ArcGIS/rest/services/Sistema_Metro2/FeatureServer), simplificados para el mapa.
- [Río Medellín de GeoMedellín](https://www.medellin.gov.co/servidormapas/rest/services/ServiciosCiudad/CartografiaBase/MapServer/6) y [vías principales del Valle de Aburrá](https://services7.arcgis.com/e2M5JRjf6HcmygSb/ArcGIS/rest/services/VIAS_PRINCIPALES_LOTERO/FeatureServer/1).
- [Relieve de Mapzen Terrain Tiles](https://registry.opendata.aws/terrain-tiles/), con curvas cada 150 m.

La red inicial tiene un solo camino entre cada par de estaciones. La prueba `ConexionServiceTest.conUnCicloLasDosRutasPuedenSerDistintas` comprueba que los criterios de tiempo y transbordos pueden elegir recorridos distintos en una red con alternativas.
