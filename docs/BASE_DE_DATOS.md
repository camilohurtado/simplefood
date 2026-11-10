# Estructura de base de datos

En la línea base (v1.0.0) los datos se almacenan **en memoria** dentro de cada
servicio. El esquema relacional objetivo está en [`schema.sql`](schema.sql) y
corresponde uno a uno con las entidades del dominio.

## Diagrama entidad-relación

```
USUARIO 1───* PEDIDO 1───* ITEM_PEDIDO *───1 PLATO *───1 CATEGORIA
   │
   ├──1───* RESERVA
   └──1───* CIERRE_CAJA (responsable)
```

## Tablas

| Tabla | Entidad Java | Descripción |
|---|---|---|
| `usuario` | `auth.Usuario` | Usuarios y su rol |
| `categoria` | `menu.Categoria` | Categorías del menú |
| `plato` | `menu.Plato` | Platos con precio y disponibilidad |
| `pedido` | `pedidos.Pedido` | Pedido por mesa y su estado |
| `item_pedido` | `pedidos.ItemPedido` | Líneas del pedido con precio histórico |
| `reserva` | `reservas.Reserva` | Reservas por fecha/hora y mesa asignada |
| `cierre_caja` | `caja.CierreCaja` | Un cierre por día |

## Estados de pedido

`REGISTRADO → EN_PREPARACION → LISTO → ENTREGADO → PAGADO`
(se permite `CANCELADO` desde `REGISTRADO` o `EN_PREPARACION`).
