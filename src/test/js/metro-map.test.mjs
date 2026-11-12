import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { colocarEtiquetas, vistaInicial } from '../../main/resources/META-INF/resources/mapa/metro-map.js';

const contenido = await readFile(new URL('../../main/resources/mapa/estaciones.txt', import.meta.url), 'utf8');
const estaciones = contenido.trim().split('\n').map(fila => {
    const [id, x, y, , , lado] = fila.split('|');
    return { id, x: Number(x), y: Number(y), lado, prioridad: ['Universidad de Medellín', 'Niquía', 'Hospital'].includes(id) ? 0 : 2,
        width: id.length * 7, height: 14 };
});
const seCruzan = (a, b) => a.x < b.x + b.width && a.x + a.width > b.x
    && a.y < b.y + b.height && a.y + a.height > b.y;

for (const [ancho, alto] of [[940, 650], [620, 450], [320, 400]]) {
    test(`la red completa cabe en ${ancho} × ${alto}`, () => {
        const vista = vistaInicial(estaciones, ancho, alto);
        for (const e of estaciones) {
            assert.ok(e.x >= vista.x - vista.width / 2 && e.x <= vista.x + vista.width / 2);
            assert.ok(e.y >= vista.y - vista.height / 2 && e.y <= vista.y + vista.height / 2);
        }
    });
    for (const zoom of [100, 150, 200, 250]) {
        test(`los nombres no se cruzan ni se recortan a ${zoom} % en ${ancho} × ${alto}`, () => {
            const vista = vistaInicial(estaciones, ancho, alto);
            const escala = ancho / vista.width * zoom / 100;
            const puntos = estaciones.map(e => ({ ...e,
                x: (e.x - vista.x) * escala + ancho / 2,
                y: (e.y - vista.y) * escala + alto / 2 }));
            const marcadores = puntos.map(e => ({ x: e.x - 5, y: e.y - 5, width: 10, height: 10 }));
            const posiciones = colocarEtiquetas(puntos, marcadores, ancho, alto);
            assert.ok(posiciones.size > 5, 'debe mostrar nombres útiles, no ocultarlos todos');
            const hospital = puntos.find(e => e.id === 'Hospital');
            if (hospital.x > 40 && hospital.x < ancho - 40 && hospital.y > 40 && hospital.y < alto - 40) {
                assert.ok(posiciones.has('Hospital'), 'el transbordo debe conservar su nombre en la zona densa');
            }
            const cajas = [...posiciones.values()];
            for (let i = 0; i < cajas.length; i++) {
                const caja = cajas[i];
                assert.ok(caja.x >= 0 && caja.y >= 0 && caja.x + caja.width <= ancho && caja.y + caja.height <= alto);
                assert.ok(marcadores.every(m => !seCruzan(caja, m)), 'un nombre no debe tapar una estación');
                for (let j = i + 1; j < cajas.length; j++) assert.ok(!seCruzan(caja, cajas[j]));
            }
            // El orden de consulta de la base no debe cambiar la composición visual.
            assert.deepEqual(colocarEtiquetas([...puntos].reverse(), marcadores, ancho, alto), posiciones);
        });
    }
}

test('los puntos de origen, destino y transbordo tienen prioridad sin forzar solapamientos', () => {
    const etiquetas = [
        { id: 'Origen', x: 150, y: 100, width: 70, height: 14, prioridad: 0, lado: 'start' },
        { id: 'Transbordo', x: 155, y: 100, width: 80, height: 14, prioridad: 0, lado: 'start' },
        { id: 'Secundaria', x: 150, y: 102, width: 100, height: 14, prioridad: 4, lado: 'start' }
    ];
    const resultado = colocarEtiquetas(etiquetas, [], 320, 200);
    assert.ok(resultado.has('Origen'));
    assert.ok(resultado.has('Transbordo'));
    const cajas = [...resultado.values()];
    for (const [i, caja] of cajas.entries()) for (const otra of cajas.slice(i + 1)) assert.ok(!seCruzan(caja, otra));
});
