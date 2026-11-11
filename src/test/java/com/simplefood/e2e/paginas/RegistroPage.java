package com.simplefood.e2e.paginas;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object del formulario de registro de clientes ({@code /registro.html}).
 *
 * <p>Encapsula los localizadores y las acciones de la pagina para que los casos
 * de prueba describan <em>que</em> se prueba y no <em>como</em> se manipula el DOM.
 * Si cambia el HTML, solo se ajusta esta clase.
 */
public class RegistroPage {

    public static final String RUTA = "/registro.html";

    private static final By NOMBRE = By.id("nombre");
    private static final By CORREO = By.id("correo");
    private static final By CLAVE = By.id("clave");
    private static final By CONFIRMACION = By.id("confirmacion");
    private static final By BOTON_REGISTRAR = By.id("btn-registrar");
    private static final By ALERTA = By.id("alerta");

    private final WebDriver driver;
    private final WebDriverWait espera;

    public RegistroPage(WebDriver driver) {
        this.driver = driver;
        this.espera = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public RegistroPage abrir(String urlBase) {
        driver.get(urlBase + RUTA);
        espera.until(ExpectedConditions.visibilityOfElementLocated(BOTON_REGISTRAR));
        return this;
    }

    public boolean estaCargada() {
        return driver.getCurrentUrl().endsWith(RUTA) && !driver.findElements(BOTON_REGISTRAR).isEmpty();
    }

    // ------------------------------------------------------------ acciones

    public RegistroPage conNombre(String valor) {
        escribir(NOMBRE, valor);
        return this;
    }

    public RegistroPage conCorreo(String valor) {
        escribir(CORREO, valor);
        return this;
    }

    public RegistroPage conClave(String valor) {
        escribir(CLAVE, valor);
        return this;
    }

    public RegistroPage conConfirmacion(String valor) {
        escribir(CONFIRMACION, valor);
        return this;
    }

    /** Atajo para llenar todo el formulario con la misma clave en ambos campos. */
    public RegistroPage llenar(String nombre, String correo, String clave) {
        return conNombre(nombre).conCorreo(correo).conClave(clave).conConfirmacion(clave);
    }

    public RegistroPage enviar() {
        driver.findElement(BOTON_REGISTRAR).click();
        return this;
    }

    // ------------------------------------------------------------ consultas

    /** Espera a que el servidor responda y la alerta sea visible; devuelve su texto. */
    public String mensajeAlerta() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(ALERTA)).getText();
    }

    public boolean alertaEsDeExito() {
        return driver.findElement(ALERTA).getAttribute("class").contains("exito");
    }

    public boolean alertaVisible() {
        return driver.findElement(ALERTA).isDisplayed();
    }

    /** Mensaje de validacion bajo el campo indicado (nombre, correo, clave, confirmacion). */
    public String errorDeCampo(String campo) {
        return driver.findElement(By.id("error-" + campo)).getText();
    }

    public boolean campoMarcadoInvalido(String campo) {
        return "true".equals(driver.findElement(By.id(campo)).getAttribute("aria-invalid"));
    }

    public String valorDe(String campo) {
        return driver.findElement(By.id(campo)).getAttribute("value");
    }

    private void escribir(By localizador, String valor) {
        WebElement campo = driver.findElement(localizador);
        campo.clear();
        if (valor != null) {
            campo.sendKeys(valor);
        }
    }
}
