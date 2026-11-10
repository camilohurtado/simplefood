package com.simplefood.menu;

import com.simplefood.util.JsonSerializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Plato disponible en el menu digital. */
public class Plato implements JsonSerializable {

    private final long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private long categoriaId;
    private boolean disponible = true;

    public Plato(long id, String nombre, String descripcion, BigDecimal precio, long categoriaId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoriaId = categoriaId;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("nombre", nombre);
        m.put("descripcion", descripcion);
        m.put("precio", precio);
        m.put("categoriaId", categoriaId);
        m.put("disponible", disponible);
        return m;
    }
}
