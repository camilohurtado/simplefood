package com.simplefood.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.simplefood.util.NegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MenuServiceTest {

    private MenuService menu;
    private Categoria bebidas;

    @BeforeEach
    void setUp() {
        menu = new MenuService();
        bebidas = menu.crearCategoria("Bebidas");
    }

    @Test
    void crudCompletoDePlato() {
        Plato p = menu.crearPlato("Jugo", "Natural", new BigDecimal("6000"), bebidas.getId());
        assertEquals(1, menu.listarPlatos().size());

        menu.actualizarPlato(p.getId(), "Jugo de mora", "En agua", new BigDecimal("6500"),
                bebidas.getId(), false);
        Plato actualizado = menu.obtenerPlato(p.getId());
        assertEquals("Jugo de mora", actualizado.getNombre());
        assertFalse(actualizado.isDisponible());

        menu.eliminarPlato(p.getId());
        assertEquals(0, menu.listarPlatos().size());
    }

    @Test
    void validaPrecioYCategoria() {
        assertThrows(NegocioException.class,
                () -> menu.crearPlato("Agua", "", BigDecimal.ZERO, bebidas.getId()));
        assertThrows(NegocioException.class,
                () -> menu.crearPlato("Agua", "", BigDecimal.TEN, 999));
    }

    @Test
    void noEliminaCategoriaConPlatos() {
        menu.crearPlato("Te", "", new BigDecimal("4000"), bebidas.getId());
        assertThrows(NegocioException.class, () -> menu.eliminarCategoria(bebidas.getId()));
    }

    @Test
    void actualizaYEliminaCategoria() {
        Categoria c = menu.crearCategoria("Postres");
        menu.actualizarCategoria(c.getId(), "Postres caseros");
        assertEquals("Postres caseros", menu.obtenerCategoria(c.getId()).getNombre());
        menu.eliminarCategoria(c.getId());
        assertEquals(1, menu.listarCategorias().size());
    }
}
