package com.simplefood.menu;

import com.simplefood.util.JsonSerializable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Categoria del menu (entradas, platos fuertes, bebidas...). */
public class Categoria implements JsonSerializable {

    private final long id;
    private String nombre;

    public Categoria(long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("nombre", nombre);
        return m;
    }
}
