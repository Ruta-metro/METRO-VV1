// La geografía cambia de escala; los nombres y marcadores conservan su tamaño en pantalla.
const controles = new WeakMap();
const limita = (n, min, max) => Math.max(min, Math.min(max, n));
const cruza = (a, b, margen = 3) => a.x < b.x + b.width + margen && a.x + a.width + margen > b.x
    && a.y < b.y + b.height + margen && a.y + a.height + margen > b.y;

// Distribuye rótulos junto a su estación, sin tapar otros nombres ni otros marcadores.
export function colocarEtiquetas(etiquetas, marcadores, ancho, alto) {
    const resultado = new Map();
    const ocupadas = [];
    const ordenadas = [...etiquetas].sort((a, b) => a.prioridad - b.prioridad || a.id.localeCompare(b.id));
    for (const e of ordenadas) {
        if (e.x < 0 || e.y < 0 || e.x > ancho || e.y > alto) continue;
        const derecha = [10, -e.height / 2];
        const izquierda = [-10 - e.width, -e.height / 2];
        const opciones = e.lado === 'end' ? [izquierda, derecha] : [derecha, izquierda];
        opciones.push([-e.width / 2, -10 - e.height], [-e.width / 2, 10],
            [10, -10 - e.height], [-10 - e.width, -10 - e.height],
            [10, 10], [-10 - e.width, 10]);
        // Un transbordo puede estar entre muchas paradas: busca un poco más de espacio, con una guía corta.
        if (e.prioridad === 0) {
            for (const margen of [24, 36]) {
                opciones.push([margen, -e.height / 2], [-margen - e.width, -e.height / 2],
                    [-e.width / 2, -margen - e.height], [-e.width / 2, margen]);
            }
        }
        for (const [dx, dy] of opciones) {
            const caja = { x: e.x + dx, y: e.y + dy, width: e.width, height: e.height };
            if (caja.x < 6 || caja.y < 6 || caja.x + caja.width > ancho - 6 || caja.y + caja.height > alto - 6) continue;
            if (ocupadas.some(otra => cruza(caja, otra))) continue;
            if (marcadores.some(m => cruza(caja, m, 2))) continue;
            resultado.set(e.id, { ...caja, dx, dy });
            ocupadas.push(caja);
            break;
        }
    }
    return resultado;
}

export function vistaInicial(puntos, ancho, alto) {
    const xs = puntos.map(p => p.x), ys = puntos.map(p => p.y);
    const minX = Math.min(...xs), maxX = Math.max(...xs);
    const minY = Math.min(...ys), maxY = Math.max(...ys);
    const escala = Math.min(Math.max(1, ancho - 80) / Math.max(1, maxX - minX),
        Math.max(1, alto - 56) / Math.max(1, maxY - minY));
    return { x: (minX + maxX) / 2, y: (minY + maxY) / 2, width: ancho / escala, height: alto / escala };
}

class Mapa {
    constructor(ventana) {
        this.ventana = ventana;
        this.zoom = 100;
        this.observador = new ResizeObserver(() => this.programar());
        this.observador.observe(ventana);
        ventana.addEventListener('pointerdown', e => {
            if (this.zoom === 100 || e.button !== 0) return;
            this.arrastre = { x: e.clientX, y: e.clientY, centro: { ...this.centro } };
            ventana.setPointerCapture(e.pointerId);
            ventana.classList.add('map-dragging');
        });
        ventana.addEventListener('pointermove', e => {
            if (!this.arrastre) return;
            this.centro = { x: this.arrastre.centro.x - (e.clientX - this.arrastre.x) / this.escala,
                y: this.arrastre.centro.y - (e.clientY - this.arrastre.y) / this.escala };
            this.programar();
        });
        const terminar = () => {
            this.arrastre = null;
            ventana.classList.remove('map-dragging');
        };
        ventana.addEventListener('pointerup', terminar);
        ventana.addEventListener('pointercancel', terminar);
        ventana.addEventListener('lostpointercapture', terminar);
        ventana.addEventListener('keydown', e => {
            const movimiento = { ArrowLeft: [-50, 0], ArrowRight: [50, 0], ArrowUp: [0, -50], ArrowDown: [0, 50] }[e.key];
            if (!movimiento || this.zoom === 100 || !this.centro) return;
            e.preventDefault();
            this.centro.x += movimiento[0] / this.escala;
            this.centro.y += movimiento[1] / this.escala;
            this.programar();
        });
    }

    enlazar(zoom) {
        this.svg = this.ventana.querySelector('.mapa-red');
        this.marcadores = [...this.svg.querySelectorAll('.marcador-mapa')].map(el => ({
            el, x: Number(el.dataset.x), y: Number(el.dataset.y)
        }));
        this.etiquetas = [...this.svg.querySelectorAll('.etiqueta-mapa')].map(el => {
            const texto = el.querySelector('text');
            const caja = texto.getBBox();
            const guia = document.createElementNS('http://www.w3.org/2000/svg', 'path');
            guia.setAttribute('stroke', '#957488');
            guia.setAttribute('stroke-width', '1');
            guia.setAttribute('fill', 'none');
            el.prepend(guia);
            return { el, texto, guia, id: texto.textContent, x: Number(el.dataset.x), y: Number(el.dataset.y),
                prioridad: Number(el.dataset.prioridad), lado: el.dataset.lado,
                width: caja.width, height: caja.height, ascenso: -caja.y };
        });
        this.cambiarZoom(zoom);
        // Recalcula medidas si la fuente aún estaba cargando.
        document.fonts.ready.then(() => {
            if (!this.ventana.isConnected) return;
            this.etiquetas.forEach(e => {
                const caja = e.texto.getBBox();
                e.width = caja.width;
                e.height = caja.height;
            });
            this.programar();
        });
    }

    cambiarZoom(zoom) {
        this.zoom = limita(zoom, 100, 250);
        if (this.zoom === 100) this.centro = null;
        this.ventana.classList.toggle('map-zoomed', this.zoom > 100);
        this.programar();
    }

    programar() {
        cancelAnimationFrame(this.frame);
        this.frame = requestAnimationFrame(() => this.dibujar());
    }

    dibujar() {
        if (!this.svg || !this.ventana.isConnected || !this.marcadores.length) return;
        const ancho = this.ventana.clientWidth, alto = this.ventana.clientHeight;
        if (!ancho || !alto) return;
        const base = vistaInicial(this.marcadores, ancho, alto);
        const width = base.width * 100 / this.zoom, height = base.height * 100 / this.zoom;
        const centro = this.centro || base;
        this.centro = { x: limita(centro.x, base.x - (base.width - width) / 2, base.x + (base.width - width) / 2),
            y: limita(centro.y, base.y - (base.height - height) / 2, base.y + (base.height - height) / 2) };
        const x = this.centro.x - width / 2, y = this.centro.y - height / 2;
        this.escala = ancho / width;
        this.svg.setAttribute('viewBox', `${x} ${y} ${width} ${height}`);
        const transformar = p => `translate(${p.x} ${p.y}) scale(${1 / this.escala})`;
        const pantalla = p => ({ x: (p.x - x) * this.escala, y: (p.y - y) * this.escala });
        this.marcadores.forEach(p => p.el.setAttribute('transform', transformar(p)));
        const cajasMarcadores = this.marcadores.map(p => {
            const punto = pantalla(p);
            return { x: punto.x - 5, y: punto.y - 5, width: 10, height: 10 };
        });
        // También reserva el espacio de la brújula.
        cajasMarcadores.push({ x: ancho - 56, y: 8, width: 48, height: 36 });
        const visibles = this.etiquetas.filter(e => this.zoom >= 150 || e.prioridad <= 2)
            .map(e => ({ ...e, ...pantalla(e) }));
        const colocadas = colocarEtiquetas(visibles, cajasMarcadores, ancho, alto);
        this.etiquetas.forEach(e => {
            const posicion = colocadas.get(e.id);
            e.el.style.visibility = posicion ? 'visible' : 'hidden';
            if (!posicion) return;
            e.el.setAttribute('transform', transformar(e));
            e.texto.setAttribute('x', posicion.dx);
            e.texto.setAttribute('y', posicion.dy + e.ascenso);
            const finX = limita(0, posicion.dx, posicion.dx + e.width);
            const finY = limita(0, posicion.dy, posicion.dy + e.height);
            e.guia.setAttribute('d', Math.hypot(finX, finY) > 18 ? `M0 0 L${finX} ${finY}` : '');
        });
        this.svg.querySelectorAll('.fondo-cartografico text').forEach(e => {
            e.style.fontSize = `${12 / this.escala}px`;
        });
    }
}

export function iniciar(ventana, zoom = 100) {
    let mapa = controles.get(ventana);
    if (!mapa) {
        mapa = new Mapa(ventana);
        controles.set(ventana, mapa);
    }
    mapa.enlazar(zoom);
}

export function ajustarZoom(ventana, zoom) {
    const mapa = controles.get(ventana);
    if (mapa) mapa.cambiarZoom(zoom);
    else iniciar(ventana, zoom);
}
