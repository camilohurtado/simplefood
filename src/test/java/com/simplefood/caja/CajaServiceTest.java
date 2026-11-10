package com.simplefood.caja;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.simplefood.TestSupport;
import com.simplefood.menu.MenuService;
import com.simplefood.pedidos.EstadoPedido;
import com.simplefood.pedidos.Pedido;
import com.simplefood.pedidos.PedidoService;
import com.simplefood.util.NegocioException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CajaServiceTest {

    private static final LocalDateTime HOY = LocalDateTime.of(2026, 11, 10, 22, 0);
    private PedidoService pedidos;
    private CajaService caja;
    private long sopa;

    @BeforeEach
    void setUp() {
        Clock reloj = TestSupport.relojEn(HOY);
        MenuService menu = new MenuService();
        long cat = menu.crearCategoria("General").getId();
        sopa = menu.crearPlato("Sopa", "", new BigDecimal("10000"), cat).getId();
        pedidos = new PedidoService(menu, reloj);
        caja = new CajaService(pedidos, reloj);
    }

    private void pedidoPagado(int cantidad) {
        Pedido p = pedidos.registrar(1, 1, Map.of(sopa, cantidad));
        for (EstadoPedido e : new EstadoPedido[] {EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO,
                EstadoPedido.ENTREGADO, EstadoPedido.PAGADO}) {
            pedidos.cambiarEstado(p.getId(), e);
        }
    }

    @Test
    void reporteSoloIncluyePedidosPagados() {
        pedidoPagado(2);
        pedidoPagado(1);
        pedidos.registrar(2, 1, Map.of(sopa, 5));
        ReporteVentas r = caja.reporteDiario(HOY.toLocalDate());
        assertEquals(2, r.pedidos());
        assertEquals(new BigDecimal("30000"), r.total());
        assertEquals(new BigDecimal("15000.00"), r.ticketPromedio());
        assertEquals(3, r.unidadesPorPlato().get("Sopa"));
    }

    @Test
    void cierreCalculaDiferenciaYEsUnico() {
        pedidoPagado(3);
        CierreCaja c = caja.cerrarCaja(new BigDecimal("29000"), 1);
        assertEquals(new BigDecimal("-1000"), c.diferencia());
        assertThrows(NegocioException.class, () -> caja.cerrarCaja(BigDecimal.ZERO, 1));
    }
}
