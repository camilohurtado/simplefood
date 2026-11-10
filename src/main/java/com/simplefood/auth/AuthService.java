package com.simplefood.auth;

import com.simplefood.util.NegocioException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Registro, inicio de sesion y control de acceso por rol.
 */
public class AuthService {

    private final Map<String, Usuario> usuariosPorCorreo = new ConcurrentHashMap<>();
    private final Map<String, Usuario> sesiones = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();
    private final SecureRandom random = new SecureRandom();

    public Usuario registrar(String nombre, String correo, String clave, Rol rol) {
        if (nombre == null || nombre.isBlank() || correo == null || !correo.contains("@")) {
            throw new NegocioException("Nombre y correo valido son obligatorios");
        }
        if (clave == null || clave.length() < 6) {
            throw new NegocioException("La clave debe tener al menos 6 caracteres");
        }
        String llave = correo.toLowerCase();
        if (usuariosPorCorreo.containsKey(llave)) {
            throw new NegocioException("El correo ya esta registrado");
        }
        Usuario u = new Usuario(secuencia.incrementAndGet(), nombre, llave, hash(clave), rol);
        usuariosPorCorreo.put(llave, u);
        return u;
    }

    /** Devuelve un token de sesion si las credenciales son correctas. */
    public String iniciarSesion(String correo, String clave) {
        Usuario u = correo == null ? null : usuariosPorCorreo.get(correo.toLowerCase());
        if (u == null || !u.getHashClave().equals(hash(clave))) {
            throw new NegocioException("Credenciales invalidas");
        }
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, u);
        return token;
    }

    public void cerrarSesion(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    public Optional<Usuario> usuarioDeToken(String token) {
        return token == null ? Optional.empty() : Optional.ofNullable(sesiones.get(token));
    }

    /** Verifica que el token pertenezca a un usuario con alguno de los roles indicados. */
    public Usuario autorizar(String token, Rol... permitidos) {
        Usuario u = usuarioDeToken(token).orElseThrow(() -> new NegocioException("Sesion no valida"));
        for (Rol r : permitidos) {
            if (u.getRol() == r) {
                return u;
            }
        }
        throw new NegocioException("Acceso denegado para el rol " + u.getRol());
    }

    private static String hash(String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(clave.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
