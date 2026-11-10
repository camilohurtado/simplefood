package com.simplefood.menu;

import com.simplefood.util.NegocioException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** CRUD de categorias y platos del menu digital. */
public class MenuService {

    private final Map<Long, Categoria> categorias = new ConcurrentHashMap<>();
    private final Map<Long, Plato> platos = new ConcurrentHashMap<>();
    private final AtomicLong secCategoria = new AtomicLong();
    private final AtomicLong secPlato = new AtomicLong();

    // ---- Categorias

    public Categoria crearCategoria(String nombre) {
        validarTexto(nombre, "nombre de categoria");
        Categoria c = new Categoria(secCategoria.incrementAndGet(), nombre.trim());
        categorias.put(c.getId(), c);
        return c;
    }

    public List<Categoria> listarCategorias() {
        List<Categoria> lista = new ArrayList<>(categorias.values());
        lista.sort(Comparator.comparingLong(Categoria::getId));
        return lista;
    }

    public Categoria obtenerCategoria(long id) {
        Categoria c = categorias.get(id);
        if (c == null) {
            throw new NegocioException("Categoria no encontrada: " + id);
        }
        return c;
    }

    public Categoria actualizarCategoria(long id, String nombre) {
        validarTexto(nombre, "nombre de categoria");
        Categoria c = obtenerCategoria(id);
        c.setNombre(nombre.trim());
        return c;
    }

    public void eliminarCategoria(long id) {
        obtenerCategoria(id);
        boolean enUso = platos.values().stream().anyMatch(p -> p.getCategoriaId() == id);
        if (enUso) {
            throw new NegocioException("La categoria tiene platos asociados");
        }
        categorias.remove(id);
    }

    // ---- Platos

    public Plato crearPlato(String nombre, String descripcion, BigDecimal precio, long categoriaId) {
        validarPlato(nombre, precio, categoriaId);
        Plato p = new Plato(secPlato.incrementAndGet(), nombre.trim(), descripcion, precio, categoriaId);
        platos.put(p.getId(), p);
        return p;
    }

    public List<Plato> listarPlatos() {
        List<Plato> lista = new ArrayList<>(platos.values());
        lista.sort(Comparator.comparingLong(Plato::getId));
        return lista;
    }

    public List<Plato> listarPorCategoria(long categoriaId) {
        return listarPlatos().stream().filter(p -> p.getCategoriaId() == categoriaId).toList();
    }

    public Plato obtenerPlato(long id) {
        Plato p = platos.get(id);
        if (p == null) {
            throw new NegocioException("Plato no encontrado: " + id);
        }
        return p;
    }

    public Plato actualizarPlato(long id, String nombre, String descripcion, BigDecimal precio,
                                 long categoriaId, boolean disponible) {
        validarPlato(nombre, precio, categoriaId);
        Plato p = obtenerPlato(id);
        p.setNombre(nombre.trim());
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setCategoriaId(categoriaId);
        p.setDisponible(disponible);
        return p;
    }

    public void eliminarPlato(long id) {
        obtenerPlato(id);
        platos.remove(id);
    }

    private void validarPlato(String nombre, BigDecimal precio, long categoriaId) {
        validarTexto(nombre, "nombre del plato");
        if (precio == null || precio.signum() <= 0) {
            throw new NegocioException("El precio debe ser mayor que cero");
        }
        obtenerCategoria(categoriaId);
    }

    private static void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("El " + campo + " es obligatorio");
        }
    }
}
