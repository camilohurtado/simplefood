package com.simplefood.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.simplefood.util.Json;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Pruebas de integracion: levantan el servidor real y recorren los flujos criticos. */
class ApiIntegracionTest {

    private static ServidorWeb servidor;
    private static final HttpClient CLIENTE = HttpClient.newHttpClient();

    @BeforeAll
    static void iniciar() throws Exception {
        servidor = new ServidorWeb(0, new Contexto(Clock.systemDefaultZone(), 5).conDatosDemo());
        servidor.iniciar();
    }

    @AfterAll
    static void detener() {
        servidor.detener();
    }

    private HttpResponse<String> llamar(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + servidor.puerto() + ruta))
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo));
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return CLIENTE.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String correo, String clave) throws Exception {
        HttpResponse<String> r = llamar("POST", "/api/auth/login", null,
                "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}");
        assertEquals(200, r.statusCode());
        return Json.parse(r.body()).get("token");
    }

    @Test
    void menuPublicoYCrudRestringidoAlAdministrador() throws Exception {
        assertEquals(200, llamar("GET", "/api/menu/platos", null, null).statusCode());
        String mesero = login("mesero@simplefood.com", "mesero123");
        assertEquals(403, llamar("POST", "/api/menu/categorias", mesero, "{\"nombre\":\"X\"}").statusCode());
        String admin = login("admin@simplefood.com", "admin123");
        HttpResponse<String> r = llamar("POST", "/api/menu/categorias", admin, "{\"nombre\":\"Postres\"}");
        assertEquals(201, r.statusCode());
        assertTrue(r.body().contains("Postres"));
    }

    @Test
    void flujoCompletoPedidoHastaCierreDeCaja() throws Exception {
        String mesero = login("mesero@simplefood.com", "mesero123");
        HttpResponse<String> creado = llamar("POST", "/api/pedidos", mesero,
                "{\"mesa\":3,\"items\":\"1:2,4:2\"}");
        assertEquals(201, creado.statusCode());
        String id = Json.parse(creado.body()).get("id");

        for (String estado : new String[] {"EN_PREPARACION", "LISTO", "ENTREGADO", "PAGADO"}) {
            HttpResponse<String> r = llamar("PUT", "/api/pedidos/" + id + "/estado", mesero,
                    "{\"estado\":\"" + estado + "\"}");
            assertEquals(200, r.statusCode(), r.body());
        }

        String admin = login("admin@simplefood.com", "admin123");
        HttpResponse<String> reporte = llamar("GET", "/api/caja/reporte?fecha=" + LocalDate.now(), admin, null);
        assertEquals(200, reporte.statusCode());
        assertTrue(reporte.body().contains("\"total\":34000"), reporte.body());
    }

    @Test
    void clienteReservaMesa() throws Exception {
        String cliente = login("cliente@simplefood.com", "cliente123");
        String fecha = LocalDate.now().plusDays(2) + "T19:30";
        HttpResponse<String> r = llamar("POST", "/api/reservas", cliente,
                "{\"fechaHora\":\"" + fecha + "\",\"personas\":4}");
        assertEquals(201, r.statusCode(), r.body());
        assertEquals("CONFIRMADA", Json.parse(r.body()).get("estado"));
    }

    @Test
    void rechazaSolicitudesSinSesion() throws Exception {
        assertEquals(401, llamar("POST", "/api/pedidos", null, "{\"mesa\":1,\"items\":\"1:1\"}").statusCode());
    }
}
