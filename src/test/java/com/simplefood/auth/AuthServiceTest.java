package com.simplefood.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.simplefood.util.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private AuthService auth;

    @BeforeEach
    void setUp() {
        auth = new AuthService();
        auth.registrar("Ana", "ana@mail.com", "secreto1", Rol.MESERO);
    }

    @Test
    void iniciaSesionConCredencialesValidas() {
        String token = auth.iniciarSesion("ANA@mail.com", "secreto1");
        assertNotNull(token);
        assertEquals("Ana", auth.usuarioDeToken(token).orElseThrow().getNombre());
    }

    @Test
    void rechazaClaveIncorrecta() {
        assertThrows(NegocioException.class, () -> auth.iniciarSesion("ana@mail.com", "otra"));
    }

    @Test
    void noPermiteCorreoDuplicado() {
        assertThrows(NegocioException.class,
                () -> auth.registrar("Ana 2", "ana@mail.com", "secreto2", Rol.CLIENTE));
    }

    @Test
    void controlaAccesoPorRol() {
        String token = auth.iniciarSesion("ana@mail.com", "secreto1");
        assertEquals(Rol.MESERO, auth.autorizar(token, Rol.MESERO, Rol.ADMINISTRADOR).getRol());
        NegocioException e = assertThrows(NegocioException.class,
                () -> auth.autorizar(token, Rol.ADMINISTRADOR));
        assertTrue(e.getMessage().startsWith("Acceso"));
    }

    @Test
    void cerrarSesionInvalidaToken() {
        String token = auth.iniciarSesion("ana@mail.com", "secreto1");
        auth.cerrarSesion(token);
        assertTrue(auth.usuarioDeToken(token).isEmpty());
    }
}
