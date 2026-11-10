package com.simplefood.pedidos;

/** Ciclo de vida de un pedido. */
public enum EstadoPedido {
    REGISTRADO,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    PAGADO,
    CANCELADO;

    /** Indica si es valido pasar de este estado al siguiente indicado. */
    public boolean puedeCambiarA(EstadoPedido destino) {
        return switch (this) {
            case REGISTRADO -> destino == EN_PREPARACION || destino == CANCELADO;
            case EN_PREPARACION -> destino == LISTO || destino == CANCELADO;
            case LISTO -> destino == ENTREGADO;
            case ENTREGADO -> destino == PAGADO;
            case PAGADO, CANCELADO -> false;
        };
    }
}
