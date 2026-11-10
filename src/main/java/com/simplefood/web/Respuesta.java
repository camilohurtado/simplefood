package com.simplefood.web;

/** Respuesta HTTP simple: codigo de estado y cuerpo JSON. */
public record Respuesta(int estado, String cuerpo) {

    public static Respuesta ok(String json) {
        return new Respuesta(200, json);
    }

    public static Respuesta creado(String json) {
        return new Respuesta(201, json);
    }

    public static Respuesta sinContenido() {
        return new Respuesta(204, "");
    }
}
