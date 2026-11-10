package com.simplefood.util;

/** Error de validacion o de regla de negocio. */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
