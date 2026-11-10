package com.simplefood;

import com.simplefood.web.Contexto;
import com.simplefood.web.ServidorWeb;
import java.io.IOException;
import java.time.Clock;

/**
 * Punto de entrada de SimpleFood (SIGR).
 */
public class App {

    public static void main(String[] args) throws IOException {
        int puerto = Integer.parseInt(System.getenv().getOrDefault("SIMPLEFOOD_PORT", "8080"));
        Contexto ctx = new Contexto(Clock.systemDefaultZone(), 15).conDatosDemo();
        ServidorWeb servidor = new ServidorWeb(puerto, ctx);
        servidor.iniciar();
        System.out.println("SimpleFood - Sistema Integral de Gestion de Restaurante");
        System.out.println("Servidor escuchando en http://localhost:" + servidor.puerto());
    }
}
