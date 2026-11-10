# Guion de la demo

Duración aproximada: 10 minutos. Antes de empezar: `docker compose up -d`, `./mvnw spring-boot:run` y dos ventanas del navegador (una normal y una de incógnito).

1. **Login.** En la ventana 1 entrar como `usuario` / `usuario123`. Mostrar que no aparece el enlace de Administración. En la ventana 2 entrar como `admin` / `admin123`.
2. **Autocompletado (F02).** Escribir "Par" en el origen y elegir Parque Berrío. Estructura: Trie.
3. **Ruta más rápida (F03, F05, F06).** Santo Domingo Savio a San Javier. Mostrar 44 min, 2 transbordos, el detalle por línea y la hora de llegada. Estructuras: Grafo, MinHeap, ListaEnlazada.
4. **Diagrama (F07).** Señalar la ruta resaltada en el diagrama.
5. **Cierre y tiempo real (F08, F11).** El admin cierra Prado. En la ventana del usuario, con la ruta Hospital a Parque Berrío ya calculada, ver el aviso y que la ruta se recalcula sola. Estación roja en el diagrama.
6. **Deshacer y reabrir (F09, F10).** Deshacer el cierre. Estructura: Pila.
7. **Menos transbordos (F04).** Como admin, en Administrar conexiones crear Línea B, Floresta a Loyola, 30 minutos. Consultar Cisneros a Loyola con los dos criterios (18 min con 1 transbordo contra 36 min con 0). Borrar el tramo.
8. **CRUD (F13, F14).** Crear una estación nueva, buscarla en el autocompletado y eliminarla.
9. **Historial (F12).** En la ventana del usuario abrir Historial.
10. **Logs y pruebas.** Mostrar en la consola los logs de cada ruta (minutos, transbordos, tiempo) y correr `./mvnw test`.

## Si algo falla

- La app no arranca: revisar que Docker esté abierto y que `docker compose ps` muestre la base de datos arriba.
- Un cambio quedó a medias: reiniciar la app; la red se carga desde la base de datos.
- Estaciones que quedaron cerradas: entrar como admin y reabrirlas desde Administración.
