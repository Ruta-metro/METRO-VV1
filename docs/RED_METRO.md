# Red del Metro de Medellín: datos, fuentes y supuestos

Datos que carga `CargaInicial` la primera vez que arranca la aplicación con la base de datos vacía.

## Líneas y estaciones (en orden)

**Línea A** (21 estaciones): Niquía, Bello, Madera, Acevedo, Tricentenario, Caribe, Universidad, Hospital, Prado, Parque Berrío, San Antonio, Alpujarra, Exposiciones, Industriales, Poblado, Aguacatala, Ayurá, Envigado, Itagüí, Sabaneta, La Estrella.

**Línea B** (7): San Antonio, Cisneros, Suramericana, Estadio, Floresta, Santa Lucía, San Javier.

**Tranvía de Ayacucho** (9): San Antonio, San José, Pabellón del Agua EPM, Bicentenario, Buenos Aires, Miraflores, Loyola, Alejandro Echavarría, Oriente.

**Metrocable Línea K** (4): Acevedo, Andalucía, Popular, Santo Domingo Savio.

**Transbordos:** San Antonio (Línea A, Línea B y Tranvía) y Acevedo (Línea A y Metrocable K). En total son 38 estaciones distintas y 37 tramos.

## Fuentes consultadas

- Estaciones de las líneas A y B, con el mismo resultado en dos fuentes: [mapa-metro.com](https://mapa-metro.com/es/Colombia/Medellin/Medellin-Metro-mapa.htm) y [The Metro Rail Guy](https://themetrorailguy.com/metro-rail-systems/medellin-metro-information-stations-route-map-fare-prices-civica-pass-hours-timings/).
- Tranvía de Ayacucho: [Wikipedia](https://en.wikipedia.org/wiki/Ayacucho_Tram). El [Metro de Medellín](https://www.metrodemedellin.gov.co/usuarios/sistema-integrado/linea-t) publica el nombre «Pabellón del Agua EPM».
- Metrocable Línea K y su orden: [Wikipedia, Metrocable](https://en.wikipedia.org/wiki/Metrocable_(Medell%C3%ADn)) y [Wikipedia, estación Andalucía](https://en.wikipedia.org/wiki/Andaluc%C3%ADa_station).
- Tiempos de recorrido completo: [UrbanRail.Net](https://urbanrail.net/am/mede/medellin.htm) (Línea A 36 min y Línea B 11 min) y [ColombiaMove](https://colombiamove.com/blog/es/getting-around-medellin-metro-metrocable-guide/) (Metrocable K cerca de 10 min).

## Supuestos (los minutos son estimados, no oficiales)

| Línea | Minutos por tramo | Total en el modelo | Referencia |
|---|---|---|---|
| Línea A | 2 | 40 min | 36 min según UrbanRail |
| Línea B | 2 | 12 min | 11 min según UrbanRail |
| Tranvía de Ayacucho | 2 | 16 min | sin fuente, estimado |
| Metrocable Línea K | 3, 3 y 4 | 10 min | cerca de 10 min según ColombiaMove |

- Los tiempos son enteros y no incluyen espera ni frecuencia del tren.
- El transbordo suma entre 3 y 5 minutos de caminata (se usa 4, configurable).
- El mapa usa tonos de azul, naranja, verde y verde claro, siguiendo la identificación visual de A, B, T y K en el mapa oficial.
- No se usan datos en vivo: la aplicación no sabe si el Metro está operando.

## Cartografía del mapa 2D

- Trazados y estaciones: [capa Sistema Metro](https://services3.arcgis.com/ARUXxWVTK25ENUfB/ArcGIS/rest/services/Sistema_Metro2/FeatureServer), Empresa de Transporte Masivo del Valle de Aburrá. Se simplificaron los trazados y se dividieron por tramos del grafo, aproximando los enlaces en Acevedo y San Antonio a un solo marcador por transbordo.
- Río: [GeoMedellín, capa Ríos](https://www.medellin.gov.co/servidormapas/rest/services/ServiciosCiudad/CartografiaBase/MapServer/6). Vías de referencia: [Vías principales del Valle de Aburrá](https://services7.arcgis.com/e2M5JRjf6HcmygSb/ArcGIS/rest/services/VIAS_PRINCIPALES_LOTERO/FeatureServer/1).
- Relieve: [Terrain Tiles de Mapzen](https://registry.opendata.aws/terrain-tiles/), consultado el 11 de noviembre de 2026. Curvas de nivel simplificadas cada 150 m.
- El recorte abarca de 6,127° a 6,362° N y de 75,690° a 75,455° O. Los recursos se sirven con la aplicación y no requieren conectarse a un proveedor de mapas durante la demo.
