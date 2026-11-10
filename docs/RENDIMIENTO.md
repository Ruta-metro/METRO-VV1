# Rendimiento

Tiempos que salen en el log de la aplicación (equipo de desarrollo, red real de 41 nodos).

| Operación | Log | Tiempo típico |
|---|---|---|
| Armar el grafo desde la base de datos | `Grafo construido: ...` | entre 6 y 34 ms (la primera vez es la más lenta) |
| Calcular una ruta | `Ruta A -> B (...): ... calculada en N us` | entre 20 y 70 us; la primera consulta tras arrancar puede pasar de 400 us |
| Buscar por prefijo en el Trie | `Busqueda 'x': N resultados en N us` (nivel debug) | microsegundos |
| Generar el diagrama | `Diagrama generado: ... en N us` | del orden de cientos de microsegundos |

Para ver los tiempos de la búsqueda en el Trie se activa el nivel debug con `logging.level.com.metroruta.service.BusquedaService=DEBUG` en `application.properties`.

La primera consulta es más lenta porque la JVM todavía está cargando clases y compilando; las siguientes ya van mucho más rápido.
