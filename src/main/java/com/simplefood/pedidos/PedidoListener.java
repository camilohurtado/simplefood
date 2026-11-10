package com.simplefood.pedidos;

/** Observador notificado cada vez que un pedido cambia (seguimiento en tiempo real). */
@FunctionalInterface
public interface PedidoListener {

    void pedidoActualizado(Pedido pedido);
}
