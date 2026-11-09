# Ficha de estructuras de datos

Estructuras que usa MetroRuta y por qué. Las tres primeras son implementación propia (paquete `structures`).

| Estructura | Para qué la necesitamos | Dónde se integra | Caso normal | Caso límite |
|---|---|---|---|---|
| `MinHeap` (cola de prioridad) | Dijkstra necesita sacar siempre el nodo pendiente de menor costo | `service/Dijkstra` | Se insertan los vecinos con su costo y sale primero el más barato | Heap vacío: `extraerMinimo` lanza `NoSuchElementException`; costos repetidos se desempatan por nombre del nodo |
| `Pila` | Deshacer el último cierre o reapertura de una estación (el último en entrar es el primero en salir) | `service/CierreService` | Cerrar Prado y Hospital; deshacer devuelve primero Hospital y luego Prado | Pila vacía: el botón Deshacer se deshabilita y el servicio lanza `IllegalStateException` |
| `ListaEnlazada` | Guardar los vecinos de cada nodo del grafo (lista de adyacencia) | `structures/Grafo` | El nodo San Antonio de la Línea A tiene 4 vecinos (2 tramos y 2 transbordos) | Nodo sin vecinos: lista vacía; `eliminarSi` sobre una lista vacía devuelve 0 |
| `Grafo` (propio, sobre `ListaEnlazada`) | Representar la red: un nodo por pareja (estación, línea) y aristas con minutos y marca de transbordo | `service/RedService`, `service/Dijkstra` | 41 nodos y 37 tramos con 4 transbordos | Nodo cerrado: se salta; conectar con minutos menores o iguales a 0 o con un nodo que no existe lanza `IllegalArgumentException` |
| `Trie` (propio) | Autocompletado de estaciones mientras se escribe | `service/BusquedaService`, `view/CampoEstacion` | "Par" sugiere Parque Berrío | Texto vacío devuelve todas; texto que no existe devuelve lista vacía; se ignoran tildes y mayúsculas |

## Complejidad

| Operación | Costo |
|---|---|
| `MinHeap.insertar` y `extraerMinimo` | O(log n) |
| `Pila.apilar` y `desapilar` | O(1) |
| `ListaEnlazada.agregar` (con puntero al último) | O(1) |
| `ListaEnlazada.eliminarSi` y recorrer | O(n) |
| `Grafo.conectar` | O(1) |
| `Grafo.vecinos` | O(grado del nodo) |
| `Trie.insertar` | O(L), con L el largo del nombre |
| `Trie.buscarPorPrefijo` | O(L + k log k), con k resultados (se ordenan) |
| Dijkstra con heap | O((V + E) log E) |

Con la red real V = 41 y E = 37 tramos más los transbordos, por eso cada ruta se calcula en decenas de microsegundos (ver `docs/RENDIMIENTO.md`).

## Otras estructuras del lenguaje

`HashMap` guarda los nodos del grafo (nodo a lista de vecinos) y los costos de Dijkstra, y `HashSet` guarda los nodos cerrados. Las usamos como apoyo y no las contamos como estructuras del proyecto.

## Paralelismo

`RedService` y `BusquedaService` arman un grafo o un trie nuevo cuando algo cambia y reemplazan la referencia de una sola vez (campo `volatile`). Así una sesión que calcula una ruta nunca ve un grafo a medio armar. Los servicios que cambian datos (`CierreService`, `EstacionService`, `ConexionService`) son `synchronized` para que dos administradores no mezclen cambios.
