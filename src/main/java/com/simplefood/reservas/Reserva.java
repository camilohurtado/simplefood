package com.simplefood.reservas;

import com.simplefood.util.JsonSerializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reserva de mesa para una fecha y hora. */
public class Reserva implements JsonSerializable {

    private final long id;
    private final long clienteId;
    private final LocalDateTime fechaHora;
    private final int personas;
    private final int mesa;
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    public Reserva(long id, long clienteId, LocalDateTime fechaHora, int personas, int mesa) {
        this.id = id;
        this.clienteId = clienteId;
        this.fechaHora = fechaHora;
        this.personas = personas;
        this.mesa = mesa;
    }

    public long getId() {
        return id;
    }

    public long getClienteId() {
        return clienteId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getPersonas() {
        return personas;
    }

    public int getMesa() {
        return mesa;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("clienteId", clienteId);
        m.put("fechaHora", fechaHora.toString());
        m.put("personas", personas);
        m.put("mesa", mesa);
        m.put("estado", estado.name());
        return m;
    }
}
