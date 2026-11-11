package com.simplefood.e2e;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.simplefood.e2e.paginas.RegistroPage;
import com.simplefood.util.NegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;

/**
 * Script automatizado del formulario web de registro de clientes.
 *
 * <p>Casos cubiertos (particion de equivalencias):
 * <ol>
 *   <li>CP-WEB-01 Navegacion desde la pagina principal al formulario.</li>
 *   <li>CP-WEB-02 Registro exitoso con datos validos (verifica el efecto en el backend).</li>
 *   <li>CP-WEB-03 Correo ya registrado: el formulario muestra el error del servidor.</li>
 *   <li>CP-WEB-04 Validaciones del lado del cliente (datos invalidos, prueba parametrizada).</li>
 *   <li>CP-WEB-05 Formulario vacio: se marcan todos los campos obligatorios.</li>
 * </ol>
 */
@DisplayName("Formulario web de registro de clientes")
class RegistroClienteE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("CP-WEB-01 El enlace 'Registrarse' de la pagina principal abre el formulario")
    void navegaDesdeLaPaginaPrincipal() {
        driver.get(urlBase() + "/");
        capturar("pagina_principal");

        driver.findElement(By.linkText("Registrarse")).click();

        assertTrue(new RegistroPage(driver).estaCargada(), "Debe cargar " + RegistroPage.RUTA);
    }

    @Test
    @DisplayName("CP-WEB-02 Registro exitoso muestra bienvenida, limpia el formulario y crea el usuario")
    void registroExitoso() {
        String correo = correoUnico("laura.gomez");
        RegistroPage pagina = new RegistroPage(driver).abrir(urlBase())
                .llenar("Laura Gomez", correo, "secreta123");
        capturar("formulario_diligenciado");

        String mensaje = pagina.enviar().mensajeAlerta();
        capturar("mensaje_exito");

        assertAll(
                () -> assertTrue(pagina.alertaEsDeExito(), "La alerta debe ser de exito"),
                () -> assertEquals("Bienvenido, Laura Gomez. Tu cuenta de cliente fue creada.", mensaje),
                () -> assertEquals("", pagina.valorDe("correo"), "El formulario debe limpiarse"),
                // Verificacion en el backend: el usuario puede iniciar sesion con la clave registrada
                () -> assertDoesNotThrow(() -> contexto.auth().iniciarSesion(correo, "secreta123")));
    }

    @Test
    @DisplayName("CP-WEB-03 Correo ya registrado muestra el error devuelto por la API")
    void correoDuplicado() {
        RegistroPage pagina = new RegistroPage(driver).abrir(urlBase())
                .llenar("Otro Cliente", "cliente@simplefood.com", "clave123");

        String mensaje = pagina.enviar().mensajeAlerta();
        capturar("error_correo_duplicado");

        assertFalse(pagina.alertaEsDeExito());
        assertEquals("El correo ya esta registrado", mensaje);
    }

    @ParameterizedTest(name = "[{index}] {5}")
    @CsvSource(delimiter = '|', textBlock = """
            # nombre      | correo            | clave   | confirmacion | campo        | mensaje esperado
            ''            | ana@prueba.com    | clave12 | clave12      | nombre       | El nombre es obligatorio
            Ana Ruiz      | ana-sin-arroba    | clave12 | clave12      | correo       | Ingrese un correo valido
            Ana Ruiz      | ana@prueba.com    | 12345   | 12345        | clave        | La clave debe tener al menos 6 caracteres
            Ana Ruiz      | ana@prueba.com    | clave12 | clave99      | confirmacion | Las claves no coinciden
            """)
    @DisplayName("CP-WEB-04 Validaciones del cliente impiden enviar datos invalidos")
    void validacionesDelCliente(String nombre, String correo, String clave, String confirmacion,
                                String campo, String mensajeEsperado) {
        RegistroPage pagina = new RegistroPage(driver).abrir(urlBase())
                .conNombre(nombre).conCorreo(correo).conClave(clave).conConfirmacion(confirmacion)
                .enviar();
        capturar("validacion_" + campo);

        assertAll(
                () -> assertEquals(mensajeEsperado, pagina.errorDeCampo(campo)),
                () -> assertTrue(pagina.campoMarcadoInvalido(campo)),
                () -> assertFalse(pagina.alertaVisible(), "No debe llamarse a la API"),
                () -> assertThrows(NegocioException.class,
                        () -> contexto.auth().iniciarSesion(correo, clave),
                        "El usuario no debe haberse creado"));
    }

    @Test
    @DisplayName("CP-WEB-05 Enviar el formulario vacio marca todos los campos obligatorios")
    void formularioVacio() {
        RegistroPage pagina = new RegistroPage(driver).abrir(urlBase()).enviar();
        capturar("formulario_vacio");

        assertAll(
                () -> assertTrue(pagina.campoMarcadoInvalido("nombre")),
                () -> assertTrue(pagina.campoMarcadoInvalido("correo")),
                () -> assertTrue(pagina.campoMarcadoInvalido("clave")),
                () -> assertFalse(pagina.campoMarcadoInvalido("confirmacion"),
                        "Dos claves vacias coinciden; solo falla la longitud"));
    }
}
