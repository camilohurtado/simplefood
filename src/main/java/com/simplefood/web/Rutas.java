package com.simplefood.web;

import static com.simplefood.auth.Rol.ADMINISTRADOR;
import static com.simplefood.auth.Rol.CLIENTE;
import static com.simplefood.auth.Rol.MESERO;

import com.simplefood.auth.Rol;
import com.simplefood.auth.Usuario;
import com.simplefood.caja.CierreCaja;
import com.simplefood.pedidos.EstadoPedido;
import com.simplefood.reservas.Reserva;
import com.simplefood.util.Json;
import com.simplefood.util.NegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Enrutador de la API REST: /api/{modulo}/... */
public class Rutas {

    private final Contexto ctx;

    public Rutas(Contexto ctx) {
        this.ctx = ctx;
    }

    public Respuesta atender(Solicitud s) {
        List<String> seg = s.segmentos();
        if (seg.size() < 2 || !"api".equals(seg.get(0))) {
            return new Respuesta(404, error("Recurso no encontrado"));
        }
        return switch (seg.get(1)) {
            case "auth" -> auth(s);
            case "menu" -> menu(s);
            case "pedidos" -> pedidos(s);
            case "reservas" -> reservas(s);
            case "caja" -> caja(s);
            default -> new Respuesta(404, error("Recurso no encontrado"));
        };
    }

    // ---------------------------------------------------------------- auth

    private Respuesta auth(Solicitud s) {
        String accion = s.segmentos().size() > 2 ? s.segmentos().get(2) : "";
        Map<String, String> body = s.json();
        switch (s.metodo() + " " + accion) {
            case "POST registro" -> {
                Usuario u = ctx.auth().registrar(body.get("nombre"), body.get("correo"),
                        body.get("clave"), Rol.CLIENTE);
                return Respuesta.creado(Json.of(u));
            }
            case "POST login" -> {
                String token = ctx.auth().iniciarSesion(body.get("correo"), body.get("clave"));
                Usuario u = ctx.auth().usuarioDeToken(token).orElseThrow();
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("token", token);
                r.put("usuario", u);
                return Respuesta.ok(Json.of(r));
            }
            case "POST logout" -> {
                ctx.auth().cerrarSesion(s.token());
                return Respuesta.sinContenido();
            }
            case "GET yo" -> {
                return Respuesta.ok(Json.of(ctx.auth().autorizar(s.token(), Rol.values())));
            }
            default -> {
                return noEncontrado();
            }
        }
    }

    // ---------------------------------------------------------------- menu

    private Respuesta menu(Solicitud s) {
        List<String> seg = s.segmentos();
        if (seg.size() < 3) {
            return noEncontrado();
        }
        String recurso = seg.get(2);
        boolean conId = seg.size() > 3;
        if ("GET".equals(s.metodo())) {
            if ("categorias".equals(recurso)) {
                return Respuesta.ok(Json.of(conId
                        ? ctx.menu().obtenerCategoria(s.idEn(3))
                        : ctx.menu().listarCategorias()));
            }
            if ("platos".equals(recurso)) {
                if (conId) {
                    return Respuesta.ok(Json.of(ctx.menu().obtenerPlato(s.idEn(3))));
                }
                String cat = s.query().get("categoria");
                return Respuesta.ok(Json.of(cat == null
                        ? ctx.menu().listarPlatos()
                        : ctx.menu().listarPorCategoria(Long.parseLong(cat))));
            }
            return noEncontrado();
        }
        ctx.auth().autorizar(s.token(), ADMINISTRADOR);
        Map<String, String> b = s.json();
        String clave = s.metodo() + " " + recurso + (conId ? "/id" : "");
        switch (clave) {
            case "POST categorias" -> {
                return Respuesta.creado(Json.of(ctx.menu().crearCategoria(b.get("nombre"))));
            }
            case "PUT categorias/id" -> {
                return Respuesta.ok(Json.of(ctx.menu().actualizarCategoria(s.idEn(3), b.get("nombre"))));
            }
            case "DELETE categorias/id" -> {
                ctx.menu().eliminarCategoria(s.idEn(3));
                return Respuesta.sinContenido();
            }
            case "POST platos" -> {
                return Respuesta.creado(Json.of(ctx.menu().crearPlato(b.get("nombre"), b.get("descripcion"),
                        decimal(b.get("precio")), entero(b.get("categoriaId")))));
            }
            case "PUT platos/id" -> {
                return Respuesta.ok(Json.of(ctx.menu().actualizarPlato(s.idEn(3), b.get("nombre"),
                        b.get("descripcion"), decimal(b.get("precio")), entero(b.get("categoriaId")),
                        !"false".equals(b.get("disponible")))));
            }
            case "DELETE platos/id" -> {
                ctx.menu().eliminarPlato(s.idEn(3));
                return Respuesta.sinContenido();
            }
            default -> {
                return noEncontrado();
            }
        }
    }

    // ------------------------------------------------------------- pedidos

    private Respuesta pedidos(Solicitud s) {
        List<String> seg = s.segmentos();
        if ("POST".equals(s.metodo()) && seg.size() == 2) {
            Usuario mesero = ctx.auth().autorizar(s.token(), MESERO, ADMINISTRADOR);
            Map<String, String> b = s.json();
            return Respuesta.creado(Json.of(ctx.pedidos().registrar((int) entero(b.get("mesa")),
                    mesero.getId(), lineas(b.get("items")))));
        }
        if ("GET".equals(s.metodo()) && seg.size() == 3 && "activos".equals(seg.get(2))) {
            ctx.auth().autorizar(s.token(), MESERO, ADMINISTRADOR);
            return Respuesta.ok(Json.of(ctx.pedidos().activos()));
        }
        if ("GET".equals(s.metodo()) && seg.size() == 3) {
            ctx.auth().autorizar(s.token(), Rol.values());
            return Respuesta.ok(Json.of(ctx.pedidos().obtener(s.idEn(2))));
        }
        if ("PUT".equals(s.metodo()) && seg.size() == 4 && "estado".equals(seg.get(3))) {
            ctx.auth().autorizar(s.token(), MESERO, ADMINISTRADOR);
            EstadoPedido nuevo;
            try {
                nuevo = EstadoPedido.valueOf(String.valueOf(s.json().get("estado")));
            } catch (IllegalArgumentException e) {
                throw new NegocioException("Estado de pedido invalido");
            }
            return Respuesta.ok(Json.of(ctx.pedidos().cambiarEstado(s.idEn(2), nuevo)));
        }
        return noEncontrado();
    }

    // ------------------------------------------------------------ reservas

    private Respuesta reservas(Solicitud s) {
        List<String> seg = s.segmentos();
        if ("POST".equals(s.metodo()) && seg.size() == 2) {
            Usuario cliente = ctx.auth().autorizar(s.token(), CLIENTE, ADMINISTRADOR);
            Map<String, String> b = s.json();
            Reserva r = ctx.reservas().reservar(cliente.getId(), fechaHora(b.get("fechaHora")),
                    (int) entero(b.get("personas")));
            return Respuesta.creado(Json.of(r));
        }
        if ("GET".equals(s.metodo()) && seg.size() == 2) {
            ctx.auth().autorizar(s.token(), MESERO, ADMINISTRADOR);
            return Respuesta.ok(Json.of(ctx.reservas().delDia(fecha(s.query().get("fecha")))));
        }
        if ("DELETE".equals(s.metodo()) && seg.size() == 3) {
            Usuario u = ctx.auth().autorizar(s.token(), Rol.values());
            Reserva r = ctx.reservas().obtener(s.idEn(2));
            if (u.getRol() == CLIENTE && r.getClienteId() != u.getId()) {
                throw new NegocioException("Acceso denegado a la reserva");
            }
            return Respuesta.ok(Json.of(ctx.reservas().cancelar(r.getId())));
        }
        return noEncontrado();
    }

    // ---------------------------------------------------------------- caja

    private Respuesta caja(Solicitud s) {
        Usuario admin = ctx.auth().autorizar(s.token(), ADMINISTRADOR);
        String accion = s.segmentos().size() > 2 ? s.segmentos().get(2) : "";
        if ("GET".equals(s.metodo()) && "reporte".equals(accion)) {
            return Respuesta.ok(Json.of(ctx.caja().reporteDiario(fecha(s.query().get("fecha")))));
        }
        if ("POST".equals(s.metodo()) && "cierre".equals(accion)) {
            CierreCaja c = ctx.caja().cerrarCaja(decimal(s.json().get("efectivo")), admin.getId());
            return Respuesta.creado(Json.of(c));
        }
        return noEncontrado();
    }

    // ------------------------------------------------------------- helpers

    static String error(String mensaje) {
        return Json.of(Map.of("error", mensaje));
    }

    private static Respuesta noEncontrado() {
        return new Respuesta(404, error("Recurso no encontrado"));
    }

    private static BigDecimal decimal(String v) {
        try {
            return new BigDecimal(v);
        } catch (RuntimeException e) {
            throw new NegocioException("Valor numerico invalido: " + v);
        }
    }

    private static long entero(String v) {
        try {
            return Long.parseLong(v);
        } catch (RuntimeException e) {
            throw new NegocioException("Valor entero invalido: " + v);
        }
    }

    private static LocalDate fecha(String v) {
        try {
            return v == null || v.isBlank() ? LocalDate.now() : LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new NegocioException("Fecha invalida, use aaaa-mm-dd");
        }
    }

    private static LocalDateTime fechaHora(String v) {
        try {
            return LocalDateTime.parse(v);
        } catch (RuntimeException e) {
            throw new NegocioException("Fecha y hora invalidas, use aaaa-mm-ddThh:mm");
        }
    }

    /** Convierte "1:2,3:1" en {1=2, 3=1}. */
    private static Map<Long, Integer> lineas(String items) {
        if (items == null || items.isBlank()) {
            throw new NegocioException("El pedido debe tener al menos un plato");
        }
        Map<Long, Integer> m = new LinkedHashMap<>();
        for (String par : items.split(",")) {
            String[] kv = par.trim().split(":");
            if (kv.length != 2) {
                throw new NegocioException("Formato de items invalido, use platoId:cantidad");
            }
            m.merge(entero(kv[0]), (int) entero(kv[1]), Integer::sum);
        }
        return m;
    }
}
