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
- Tranvía de Ayacucho: [Wikipedia](https://en.wikipedia.org/wiki/Ayacucho_Tram). Esa página da los nombres en inglés; el nombre en español "Pabellón del Agua EPM" falta confirmarlo en la página oficial del Metro.
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
- Los colores de las líneas son los del diagrama de la aplicación, no los oficiales.
- No se usan datos en vivo: la aplicación no sabe si el Metro está operando.
