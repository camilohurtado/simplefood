package com.simplefood.e2e;

import com.simplefood.web.Contexto;
import com.simplefood.web.ServidorWeb;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Infraestructura comun de las pruebas end-to-end.
 *
 * <ul>
 *   <li>Levanta el servidor real de SimpleFood en un puerto libre con datos de demostracion.</li>
 *   <li>Crea un navegador Chrome limpio por prueba (aislamiento entre casos).</li>
 *   <li>Guarda capturas de pantalla como evidencia en {@code target/evidencias/selenium}.</li>
 * </ul>
 *
 * <p>Propiedades del sistema: {@code -Dheadless=false} para ver el navegador.
 */
@Tag("e2e")
public abstract class BaseE2ETest {

    private static final Path EVIDENCIAS = Path.of("target", "evidencias", "selenium");
    private static final AtomicInteger CONSECUTIVO = new AtomicInteger();
    /** Las pruebas no usan Chrome DevTools; se silencian sus avisos de version. */
    private static final Logger LOG_CDP = silenciar("org.openqa.selenium.devtools");
    private static final Logger LOG_CHROMIUM = silenciar("org.openqa.selenium.chromium");

    protected static Contexto contexto;
    private static ServidorWeb servidor;

    protected WebDriver driver;
    private String nombrePrueba;

    @BeforeAll
    static void iniciarServidor() throws IOException {
        contexto = new Contexto(Clock.systemDefaultZone(), 5).conDatosDemo();
        servidor = new ServidorWeb(0, contexto);
        servidor.iniciar();
        Files.createDirectories(EVIDENCIAS);
    }

    @AfterAll
    static void detenerServidor() {
        servidor.detener();
    }

    @BeforeEach
    void abrirNavegador(TestInfo info) {
        nombrePrueba = info.getTestMethod().map(m -> m.getName()).orElse("prueba");
        ChromeOptions opciones = new ChromeOptions();
        if (!"false".equals(System.getProperty("headless"))) {
            opciones.addArguments("--headless=new");
        }
        opciones.addArguments("--window-size=1000,900", "--lang=es");
        driver = new ChromeDriver(opciones); // Selenium Manager resuelve el chromedriver
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            capturar("estado_final");
            driver.quit();
        }
    }

    protected String urlBase() {
        return "http://localhost:" + servidor.puerto();
    }

    /** Guarda una captura de pantalla numerada: NN_metodo_etiqueta.png */
    protected void capturar(String etiqueta) {
        Path destino = EVIDENCIAS.resolve(String.format("%02d_%s_%s.png",
                CONSECUTIVO.incrementAndGet(), nombrePrueba, etiqueta));
        try {
            Path temporal = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la captura " + destino, e);
        }
    }

    private static Logger silenciar(String nombre) {
        Logger logger = Logger.getLogger(nombre);
        logger.setLevel(Level.SEVERE);
        return logger;
    }

    /** Correo unico por ejecucion para que las pruebas sean repetibles. */
    protected static String correoUnico(String prefijo) {
        return prefijo + "." + System.nanoTime() + "@prueba.com";
    }
}
