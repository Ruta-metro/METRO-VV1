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
Calcula la mejor ruta entre dos estaciones del Metro de Medellín (Línea A, Línea B, Tranvía de Ayacucho y Metrocable Línea K), por tiempo o por menos transbordos. Incluye detalle de la ruta, hora de llegada, diagrama con la ruta resaltada, cierre y reapertura de estaciones con deshacer, actualización en vivo entre sesiones, historial de rutas y administración de estaciones y conexiones. La lista completa F01 a F14 con su prueba y cómo mostrarla está en `docs/MATRIZ_CUMPLIMIENTO.md`.

## Estructuras de datos
`MinHeap`, `Pila`, `ListaEnlazada`, `Grafo` y `Trie` propios en el paquete `structures`. Cada una con su necesidad, dónde se usa y sus casos normal y límite en `docs/FICHA_ESTRUCTURAS.md`.

## Arquitectura
- `model`: entidades JPA (estaciones, líneas, conexiones, usuarios, cierres, historial).
- `repository`: acceso a la base de datos con Spring Data.
- `service`: lógica (rutas, cierres, administración, búsqueda).
- `view`: pantallas con Vaadin.
- `structures`: nuestras estructuras de datos.
- `config` y `dto`: configuración y objetos de resultado.

## Pruebas
`./mvnw test` (necesita la base de datos arriba). Las pruebas cubren casos límite: sin camino, origen igual a destino, empates de tiempo, estación cerrada y datos inválidos.

## Logs
Cada ruta, cierre, cambio de la red y búsqueda deja un log con lo que pasó y cuánto tardó. Resumen en `docs/RENDIMIENTO.md`.

## Documentos
- `docs/RED_METRO.md`: datos de la red, fuentes y supuestos.
- `docs/FICHA_ESTRUCTURAS.md`, `docs/MATRIZ_CUMPLIMIENTO.md`, `docs/GUION_DEMO.md`, `docs/RENDIMIENTO.md`.
