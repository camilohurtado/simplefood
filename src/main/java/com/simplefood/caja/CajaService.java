package com.simplefood.caja;

import com.simplefood.pedidos.ItemPedido;
import com.simplefood.pedidos.Pedido;
import com.simplefood.pedidos.PedidoService;
import com.simplefood.util.NegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/** Cierre de caja diario y reportes de ventas. */
public class CajaService {

    private final PedidoService pedidos;
    private final Clock reloj;
    private final Map<LocalDate, CierreCaja> cierres = new ConcurrentHashMap<>();

    public CajaService(PedidoService pedidos, Clock reloj) {
        this.pedidos = pedidos;
        this.reloj = reloj;
    }

    public ReporteVentas reporteDiario(LocalDate dia) {
        List<Pedido> pagados = pedidos.pagadosDelDia(dia);
        BigDecimal total = pagados.stream().map(Pedido::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio = pagados.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(pagados.size()), 2, RoundingMode.HALF_UP);
        Map<String, Integer> unidades = new TreeMap<>();
        for (Pedido p : pagados) {
            for (ItemPedido i : p.getItems()) {
                unidades.merge(i.nombrePlato(), i.cantidad(), Integer::sum);
            }
        }
        return new ReporteVentas(dia, pagados.size(), total, promedio, unidades);
    }

    /** Cierra la caja del dia actual comparando ventas registradas con el efectivo contado. */
    public synchronized CierreCaja cerrarCaja(BigDecimal efectivoContado, long responsableId) {
        if (efectivoContado == null || efectivoContado.signum() < 0) {
            throw new NegocioException("El efectivo contado no puede ser negativo");
        }
        LocalDate hoy = LocalDate.now(reloj);
        if (cierres.containsKey(hoy)) {
            throw new NegocioException("La caja del dia " + hoy + " ya fue cerrada");
        }
        ReporteVentas reporte = reporteDiario(hoy);
        CierreCaja cierre = new CierreCaja(hoy, reporte.pedidos(), reporte.total(), efectivoContado,
                efectivoContado.subtract(reporte.total()), responsableId, LocalDateTime.now(reloj));
        cierres.put(hoy, cierre);
        return cierre;
    }

    public Optional<CierreCaja> cierreDe(LocalDate dia) {
        return Optional.ofNullable(cierres.get(dia));
    }
}
