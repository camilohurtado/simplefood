package com.simplefood.caja;

import com.simplefood.util.JsonSerializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reporte de ventas diario agrupado por plato. */
public record ReporteVentas(LocalDate fecha, int pedidos, BigDecimal total, BigDecimal ticketPromedio,
                            Map<String, Integer> unidadesPorPlato) implements JsonSerializable {

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fecha", fecha.toString());
        m.put("pedidos", pedidos);
        m.put("total", total);
        m.put("ticketPromedio", ticketPromedio);
        m.put("unidadesPorPlato", unidadesPorPlato);
        return m;
    }
}
