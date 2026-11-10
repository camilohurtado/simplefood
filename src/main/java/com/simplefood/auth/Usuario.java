package com.simplefood.auth;

import com.simplefood.util.JsonSerializable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Usuario registrado en SimpleFood. */
public class Usuario implements JsonSerializable {

    private final long id;
    private final String nombre;
    private final String correo;
    private final String hashClave;
    private final Rol rol;

    public Usuario(long id, String nombre, String correo, String hashClave, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.hashClave = hashClave;
        this.rol = rol;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    String getHashClave() {
        return hashClave;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("nombre", nombre);
        m.put("correo", correo);
        m.put("rol", rol.name());
        return m;
    }
}
