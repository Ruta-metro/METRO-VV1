# Matriz de cumplimiento F01 a F14

Cada funcionalidad con la clase donde está, la prueba que la cubre y cómo mostrarla en la demo.

| Id | Funcionalidad | Dónde está | Prueba | Cómo se muestra |
|---|---|---|---|---|
| F01 | Login con roles ADMIN y USUARIO | `config/SeguridadConfig`, `service/DetallesUsuarioService`, `view/VistaLogin` | `DetallesUsuarioServiceTest`, `CargaUsuariosTest`, `VistaAdminTest` | Entrar como `usuario` (no ve Administración) y como `admin` (sí la ve) |
| F02 | Autocompletado de estaciones | `service/BusquedaService`, `structures/Trie`, `view/CampoEstacion` | `BusquedaServiceTest`, `TrieTest` | Escribir "Par" en el origen y ver las sugerencias |
| F03 | Ruta más rápida | `service/RutaService`, `service/Dijkstra`, `view/VistaInicio` | `DijkstraTest`, `RutaServiceTest` | Santo Domingo Savio a San Javier: 44 min |
| F04 | Ruta con menos transbordos | `service/Dijkstra` (criterio `MENOS_TRANSBORDOS`) | `DijkstraTest`, `ConexionServiceTest` | Ver la nota de abajo |
| F05 | Detalle de la ruta | `service/DetalleService`, `view/VistaInicio` | `DetalleServiceTest` | Tramos por línea y minutos de cada transbordo |
| F06 | Hora estimada de llegada | `service/LlegadaService` | `LlegadaServiceTest` | "Saliendo ahora llegas a las HH:mm" |
| F07 | Ruta resaltada en el diagrama | `service/DiagramaService`, `view/PanelDiagrama` | `DiagramaServiceTest` | Calcular una ruta y ver la línea negra sobre el diagrama |
| F08 | Cerrar estación | `service/CierreService`, `view/VistaAdmin` | `CierreServiceTest` | Cerrar Prado y ver que Hospital a Parque Berrío ya no tiene ruta |
| F09 | Reabrir estación | `service/CierreService` | `CierreServiceTest` | Reabrir Prado y volver a calcular |
| F10 | Deshacer cierre o reapertura | `structures/Pila`, `service/CierreService` | `CierreServiceTest`, `PilaTest` | Botón Deshacer; queda deshabilitado con la pila vacía |
| F11 | Actualización en vivo | `config/AplicacionVaadin` (@Push), `service/DifusorEventos`, `view/PanelDiagrama`, `view/VistaInicio` | `DifusorEventosTest` | Dos ventanas: el admin cierra una estación y la otra se actualiza sola |
| F12 | Historial de rutas | `service/HistorialService`, `view/VistaHistorial` | `HistorialServiceTest` | Pestaña Historial con las últimas 20 consultas del usuario |
| F13 | CRUD de estaciones | `service/EstacionService`, `view/VistaEstaciones` | `EstacionServiceTest` | Crear una estación, buscarla en el autocompletado y eliminarla |
| F14 | CRUD de conexiones | `service/ConexionService`, `view/VistaConexiones` | `ConexionServiceTest` | Crear un tramo y cambiar sus minutos |

## Nota sobre F04

La red real del Metro es un árbol (no tiene ciclos), y en un árbol solo hay un camino entre dos estaciones, así que las dos rutas salen iguales. Para mostrar que el criterio sí cambia el resultado se crea un ciclo desde Administrar conexiones: Línea B, Floresta a Loyola, 30 minutos. Con eso Cisneros a Loyola da 18 min con 1 transbordo en la más rápida y 36 min con 0 transbordos en la de menos transbordos. Esto está probado en `ConexionServiceTest.conUnCicloLasDosRutasPuedenSerDistintas`. Al terminar la demo se borra el tramo.
