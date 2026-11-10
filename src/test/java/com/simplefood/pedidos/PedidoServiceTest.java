package com.simplefood.pedidos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.simplefood.TestSupport;
import com.simplefood.menu.MenuService;
import com.simplefood.util.NegocioException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PedidoServiceTest {

    private PedidoService servicio;
    private long sopa;
    private long jugo;

    @BeforeEach
    void setUp() {
        MenuService menu = new MenuService();
        long cat = menu.crearCategoria("General").getId();
        sopa = menu.crearPlato("Sopa", "", new BigDecimal("12000"), cat).getId();
        jugo = menu.crearPlato("Jugo", "", new BigDecimal("5000"), cat).getId();
        servicio = new PedidoService(menu, TestSupport.relojEn(LocalDateTime.of(2026, 11, 10, 13, 0)));
    }

    @Test
    void calculaTotalDelPedido() {
        Pedido p = servicio.registrar(4, 1, Map.of(sopa, 2, jugo, 1));
        assertEquals(new BigDecimal("29000"), p.total());
        assertEquals(EstadoPedido.REGISTRADO, p.getEstado());
    }

    @Test
    void notificaCambiosEnTiempoReal() {
        List<EstadoPedido> eventos = new ArrayList<>();
        servicio.suscribir(p -> eventos.add(p.getEstado()));
        Pedido p = servicio.registrar(1, 1, Map.of(sopa, 1));
        servicio.cambiarEstado(p.getId(), EstadoPedido.EN_PREPARACION);
        servicio.cambiarEstado(p.getId(), EstadoPedido.LISTO);
        assertEquals(List.of(EstadoPedido.REGISTRADO, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO), eventos);
    }

    @Test
    void rechazaTransicionInvalida() {
        Pedido p = servicio.registrar(1, 1, Map.of(sopa, 1));
        assertThrows(NegocioException.class, () -> servicio.cambiarEstado(p.getId(), EstadoPedido.PAGADO));
    }

    @Test
    void rechazaPedidoVacio() {
        assertThrows(NegocioException.class, () -> servicio.registrar(1, 1, Map.of()));
    }
}
