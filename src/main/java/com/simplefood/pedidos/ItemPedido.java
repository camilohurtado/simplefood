package com.simplefood.pedidos;

import com.simplefood.util.JsonSerializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Linea de un pedido; guarda el precio vigente al momento de pedir. */
public record ItemPedido(long platoId, String nombrePlato, int cantidad, BigDecimal precioUnitario)
        implements JsonSerializable {

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("platoId", platoId);
        m.put("nombrePlato", nombrePlato);
        m.put("cantidad", cantidad);
        m.put("precioUnitario", precioUnitario);
        m.put("subtotal", subtotal());
        return m;
    }
}
