package com.simplefood;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Utilidades comunes de prueba. */
public final class TestSupport {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private TestSupport() {
    }

    public static Clock relojEn(LocalDateTime momento) {
        return Clock.fixed(momento.atZone(ZONA).toInstant(), ZONA);
    }
}
