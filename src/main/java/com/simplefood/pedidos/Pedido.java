package com.simplefood.pedidos;

import com.simplefood.util.JsonSerializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pedido realizado en una mesa. */
public class Pedido implements JsonSerializable {

    private final long id;
    private final int mesa;
    private final long meseroId;
    private final List<ItemPedido> items = new ArrayList<>();
    private final LocalDateTime creadoEn;
    private EstadoPedido estado = EstadoPedido.REGISTRADO;
    private LocalDateTime actualizadoEn;

    public Pedido(long id, int mesa, long meseroId, LocalDateTime creadoEn) {
        this.id = id;
        this.mesa = mesa;
        this.meseroId = meseroId;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }

    void agregarItem(ItemPedido item) {
        items.add(item);
    }

    void cambiarEstado(EstadoPedido nuevo, LocalDateTime momento) {
        this.estado = nuevo;
        this.actualizadoEn = momento;
    }

    public BigDecimal total() {
        return items.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getId() {
        return id;
    }

    public int getMesa() {
        return mesa;
    }

    public long getMeseroId() {
        return meseroId;
    }

    public List<ItemPedido> getItems() {
        return Collections.unmodifiableList(items);
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("mesa", mesa);
        m.put("meseroId", meseroId);
        m.put("estado", estado.name());
        m.put("items", items);
        m.put("total", total());
        m.put("creadoEn", creadoEn.toString());
        m.put("actualizadoEn", actualizadoEn.toString());
        return m;
    }
}
