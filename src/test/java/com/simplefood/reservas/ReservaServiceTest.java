package com.simplefood.reservas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.simplefood.TestSupport;
import com.simplefood.util.NegocioException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservaServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 9, 0);
    private ReservaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ReservaService(2, TestSupport.relojEn(AHORA));
    }

    @Test
    void asignaMesasSinCruces() {
        LocalDateTime cena = AHORA.withHour(19);
        assertEquals(1, servicio.reservar(1, cena, 2).getMesa());
        assertEquals(2, servicio.reservar(2, cena.plusMinutes(30), 4).getMesa());
        assertThrows(NegocioException.class, () -> servicio.reservar(3, cena.plusHours(1), 2));
        assertEquals(1, servicio.reservar(3, cena.plusHours(2), 2).getMesa());
    }

    @Test
    void cancelarLiberaLaMesa() {
        LocalDateTime almuerzo = AHORA.withHour(13);
        Reserva r1 = servicio.reservar(1, almuerzo, 2);
        servicio.reservar(2, almuerzo, 2);
        servicio.cancelar(r1.getId());
        assertEquals(1, servicio.reservar(3, almuerzo, 2).getMesa());
    }

    @Test
    void validaFechaYHorario() {
        assertThrows(NegocioException.class, () -> servicio.reservar(1, AHORA.minusDays(1), 2));
        assertThrows(NegocioException.class, () -> servicio.reservar(1, AHORA.plusDays(1).withHour(23), 2));
        assertThrows(NegocioException.class, () -> servicio.reservar(1, AHORA.withHour(12), 0));
    }

    @Test
    void listaReservasDelDia() {
        servicio.reservar(1, AHORA.withHour(20), 2);
        servicio.reservar(1, AHORA.withHour(12), 2);
        servicio.reservar(1, AHORA.plusDays(1).withHour(12), 2);
        assertEquals(2, servicio.delDia(AHORA.toLocalDate()).size());
        assertEquals(12, servicio.delDia(AHORA.toLocalDate()).get(0).getFechaHora().getHour());
    }
}
