package com.simplefood.reservas;

import com.simplefood.util.NegocioException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Gestion de reservas por fecha y hora con control de disponibilidad de mesas. */
public class ReservaService {

    /** Duracion asumida de cada reserva para evitar cruces. */
    public static final Duration DURACION = Duration.ofHours(2);
    public static final LocalTime APERTURA = LocalTime.of(11, 0);
    public static final LocalTime ULTIMA_RESERVA = LocalTime.of(21, 0);

    private final int totalMesas;
    private final Clock reloj;
    private final Map<Long, Reserva> reservas = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    public ReservaService(int totalMesas, Clock reloj) {
        this.totalMesas = totalMesas;
        this.reloj = reloj;
    }

    public synchronized Reserva reservar(long clienteId, LocalDateTime fechaHora, int personas) {
        if (fechaHora == null || !fechaHora.isAfter(LocalDateTime.now(reloj))) {
            throw new NegocioException("La reserva debe ser en una fecha futura");
        }
        LocalTime hora = fechaHora.toLocalTime();
        if (hora.isBefore(APERTURA) || hora.isAfter(ULTIMA_RESERVA)) {
            throw new NegocioException("Horario de reservas: " + APERTURA + " a " + ULTIMA_RESERVA);
        }
        if (personas <= 0 || personas > 12) {
            throw new NegocioException("Numero de personas invalido (1 a 12)");
        }
        int mesa = mesaLibre(fechaHora);
        Reserva r = new Reserva(secuencia.incrementAndGet(), clienteId, fechaHora, personas, mesa);
        reservas.put(r.getId(), r);
        return r;
    }

    public Reserva cancelar(long id) {
        Reserva r = obtener(id);
        r.cancelar();
        return r;
    }

    public Reserva obtener(long id) {
        Reserva r = reservas.get(id);
        if (r == null) {
            throw new NegocioException("Reserva no encontrada: " + id);
        }
        return r;
    }

    public List<Reserva> delDia(LocalDate dia) {
        List<Reserva> lista = new ArrayList<>(reservas.values().stream()
                .filter(r -> r.getFechaHora().toLocalDate().equals(dia))
                .toList());
        lista.sort(Comparator.comparing(Reserva::getFechaHora));
        return lista;
    }

    private int mesaLibre(LocalDateTime inicio) {
        LocalDateTime fin = inicio.plus(DURACION);
        for (int mesa = 1; mesa <= totalMesas; mesa++) {
            final int m = mesa;
            boolean ocupada = reservas.values().stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA && r.getMesa() == m)
                    .anyMatch(r -> r.getFechaHora().isBefore(fin)
                            && r.getFechaHora().plus(DURACION).isAfter(inicio));
            if (!ocupada) {
                return mesa;
            }
        }
        throw new NegocioException("No hay mesas disponibles para " + inicio);
    }
}
