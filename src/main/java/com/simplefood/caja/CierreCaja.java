package com.simplefood.caja;

import com.simplefood.util.JsonSerializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Resultado del cierre de caja de un dia. */
public record CierreCaja(LocalDate fecha, int pedidos, BigDecimal totalVentas, BigDecimal efectivoContado,
                         BigDecimal diferencia, long responsableId, LocalDateTime cerradoEn)
        implements JsonSerializable {

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fecha", fecha.toString());
        m.put("pedidos", pedidos);
        m.put("totalVentas", totalVentas);
        m.put("efectivoContado", efectivoContado);
        m.put("diferencia", diferencia);
        m.put("responsableId", responsableId);
        m.put("cerradoEn", cerradoEn.toString());
        return m;
    }
}
