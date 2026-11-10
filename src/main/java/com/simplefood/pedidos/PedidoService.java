package com.simplefood.pedidos;

import com.simplefood.menu.MenuService;
import com.simplefood.menu.Plato;
import com.simplefood.util.NegocioException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Registro de pedidos y seguimiento de su estado. */
public class PedidoService {

    private final MenuService menu;
    private final Clock reloj;
    private final Map<Long, Pedido> pedidos = new ConcurrentHashMap<>();
    private final List<PedidoListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicLong secuencia = new AtomicLong();

    public PedidoService(MenuService menu, Clock reloj) {
        this.menu = menu;
        this.reloj = reloj;
    }

    /**
     * Registra un pedido.
     *
     * @param lineas mapa platoId -> cantidad
     */
    public Pedido registrar(int mesa, long meseroId, Map<Long, Integer> lineas) {
        if (mesa <= 0) {
            throw new NegocioException("Numero de mesa invalido");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new NegocioException("El pedido debe tener al menos un plato");
        }
        Pedido pedido = new Pedido(secuencia.incrementAndGet(), mesa, meseroId, LocalDateTime.now(reloj));
        lineas.forEach((platoId, cantidad) -> {
            if (cantidad == null || cantidad <= 0) {
                throw new NegocioException("Cantidad invalida para el plato " + platoId);
            }
            Plato plato = menu.obtenerPlato(platoId);
            if (!plato.isDisponible()) {
                throw new NegocioException("Plato no disponible: " + plato.getNombre());
            }
            pedido.agregarItem(new ItemPedido(platoId, plato.getNombre(), cantidad, plato.getPrecio()));
        });
        pedidos.put(pedido.getId(), pedido);
        notificar(pedido);
        return pedido;
    }

    public Pedido cambiarEstado(long id, EstadoPedido nuevo) {
        Pedido p = obtener(id);
        if (!p.getEstado().puedeCambiarA(nuevo)) {
            throw new NegocioException("Transicion no permitida: " + p.getEstado() + " -> " + nuevo);
        }
        p.cambiarEstado(nuevo, LocalDateTime.now(reloj));
        notificar(p);
        return p;
    }

    public Pedido obtener(long id) {
        Pedido p = pedidos.get(id);
        if (p == null) {
            throw new NegocioException("Pedido no encontrado: " + id);
        }
        return p;
    }

    public List<Pedido> listar() {
        List<Pedido> lista = new ArrayList<>(pedidos.values());
        lista.sort(Comparator.comparingLong(Pedido::getId));
        return lista;
    }

    /** Pedidos aun no cerrados, para el tablero de cocina. */
    public List<Pedido> activos() {
        return listar().stream()
                .filter(p -> p.getEstado() != EstadoPedido.PAGADO && p.getEstado() != EstadoPedido.CANCELADO)
                .toList();
    }

    public List<Pedido> pagadosDelDia(LocalDate dia) {
        return listar().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PAGADO)
                .filter(p -> p.getActualizadoEn().toLocalDate().equals(dia))
                .toList();
    }

    public void suscribir(PedidoListener listener) {
        listeners.add(listener);
    }

    private void notificar(Pedido pedido) {
        listeners.forEach(l -> l.pedidoActualizado(pedido));
    }
}
