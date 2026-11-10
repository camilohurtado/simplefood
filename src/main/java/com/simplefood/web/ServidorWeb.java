package com.simplefood.web;

import com.simplefood.util.Json;
import com.simplefood.util.NegocioException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servidor HTTP embebido (JDK) que expone la API REST, el flujo de eventos
 * de pedidos en tiempo real (Server-Sent Events) y la interfaz web estatica.
 */
public class ServidorWeb {

    private static final Logger LOG = Logger.getLogger(ServidorWeb.class.getName());

    private final HttpServer server;
    private final Rutas rutas;
    private final Set<OutputStream> suscriptores = new CopyOnWriteArraySet<>();

    public ServidorWeb(int puerto, Contexto ctx) throws IOException {
        this.rutas = new Rutas(ctx);
        this.server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/api/eventos/pedidos", this::eventos);
        server.createContext("/api/", this::api);
        server.createContext("/", this::estatico);
        ctx.pedidos().suscribir(p -> difundir(Json.of(p)));
    }

    public void iniciar() {
        server.start();
    }

    public void detener() {
        server.stop(0);
    }

    public int puerto() {
        return server.getAddress().getPort();
    }

    private void api(HttpExchange ex) throws IOException {
        Respuesta r;
        try {
            String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            List<String> seg = Arrays.stream(ex.getRequestURI().getPath().split("/"))
                    .filter(x -> !x.isBlank()).toList();
            String auth = ex.getRequestHeaders().getFirst("Authorization");
            String token = auth != null && auth.startsWith("Bearer ") ? auth.substring(7) : null;
            Solicitud s = new Solicitud(ex.getRequestMethod(), seg,
                    Solicitud.parsearQuery(ex.getRequestURI().getRawQuery()), token, cuerpo);
            r = rutas.atender(s);
        } catch (NegocioException e) {
            int codigo = e.getMessage().startsWith("Sesion") ? 401
                    : e.getMessage().startsWith("Acceso") ? 403 : 400;
            r = new Respuesta(codigo, Rutas.error(e.getMessage()));
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado", e);
            r = new Respuesta(500, Rutas.error("Error interno"));
        }
        enviar(ex, r.estado(), "application/json; charset=utf-8", r.cuerpo().getBytes(StandardCharsets.UTF_8));
    }

    private void eventos(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Content-Type", "text/event-stream");
        ex.getResponseHeaders().add("Cache-Control", "no-cache");
        ex.sendResponseHeaders(200, 0);
        OutputStream out = ex.getResponseBody();
        out.write(": conectado\n\n".getBytes(StandardCharsets.UTF_8));
        out.flush();
        suscriptores.add(out);
    }

    private void difundir(String json) {
        byte[] datos = ("event: pedido\ndata: " + json + "\n\n").getBytes(StandardCharsets.UTF_8);
        for (OutputStream out : suscriptores) {
            try {
                out.write(datos);
                out.flush();
            } catch (IOException e) {
                suscriptores.remove(out);
            }
        }
    }

    private void estatico(HttpExchange ex) throws IOException {
        String ruta = ex.getRequestURI().getPath();
        if ("/".equals(ruta)) {
            ruta = "/index.html";
        }
        if (ruta.contains("..")) {
            enviar(ex, 400, "text/plain", new byte[0]);
            return;
        }
        try (InputStream in = getClass().getResourceAsStream("/public" + ruta)) {
            if (in == null) {
                enviar(ex, 404, "text/plain; charset=utf-8", "No encontrado".getBytes(StandardCharsets.UTF_8));
                return;
            }
            String tipo = ruta.endsWith(".html") ? "text/html; charset=utf-8"
                    : ruta.endsWith(".js") ? "application/javascript"
                    : ruta.endsWith(".css") ? "text/css" : "application/octet-stream";
            enviar(ex, 200, tipo, in.readAllBytes());
        }
    }

    private static void enviar(HttpExchange ex, int estado, String tipo, byte[] cuerpo) throws IOException {
        ex.getResponseHeaders().set("Content-Type", tipo);
        if (estado == 204) {
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return;
        }
        ex.sendResponseHeaders(estado, cuerpo.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(cuerpo);
        }
    }
}
