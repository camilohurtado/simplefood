package com.simplefood.web;

import com.simplefood.auth.AuthService;
import com.simplefood.auth.Rol;
import com.simplefood.caja.CajaService;
import com.simplefood.menu.Categoria;
import com.simplefood.menu.MenuService;
import com.simplefood.pedidos.PedidoService;
import com.simplefood.reservas.ReservaService;
import java.math.BigDecimal;
import java.time.Clock;

/** Ensambla los servicios de la aplicacion (inyeccion manual de dependencias). */
public class Contexto {

    private final AuthService auth = new AuthService();
    private final MenuService menu = new MenuService();
    private final PedidoService pedidos;
    private final ReservaService reservas;
    private final CajaService caja;

    public Contexto(Clock reloj, int totalMesas) {
        this.pedidos = new PedidoService(menu, reloj);
        this.reservas = new ReservaService(totalMesas, reloj);
        this.caja = new CajaService(pedidos, reloj);
    }

    /** Carga usuarios y menu de ejemplo para pruebas y demostraciones. */
    public Contexto conDatosDemo() {
        auth.registrar("Administrador", "admin@simplefood.com", "admin123", Rol.ADMINISTRADOR);
        auth.registrar("Mesero Demo", "mesero@simplefood.com", "mesero123", Rol.MESERO);
        auth.registrar("Cliente Demo", "cliente@simplefood.com", "cliente123", Rol.CLIENTE);
        Categoria entradas = menu.crearCategoria("Entradas");
        Categoria fuertes = menu.crearCategoria("Platos fuertes");
        Categoria bebidas = menu.crearCategoria("Bebidas");
        menu.crearPlato("Empanadas", "Tres empanadas de carne", new BigDecimal("9000"), entradas.getId());
        menu.crearPlato("Bandeja paisa", "Plato tipico antioqueno", new BigDecimal("32000"), fuertes.getId());
        menu.crearPlato("Ajiaco", "Sopa bogotana con pollo", new BigDecimal("28000"), fuertes.getId());
        menu.crearPlato("Limonada de coco", "Bebida natural", new BigDecimal("8000"), bebidas.getId());
        return this;
    }

    public AuthService auth() {
        return auth;
    }

    public MenuService menu() {
        return menu;
    }

    public PedidoService pedidos() {
        return pedidos;
    }

    public ReservaService reservas() {
        return reservas;
    }

    public CajaService caja() {
        return caja;
    }
}
