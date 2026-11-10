# SimpleFood — Sistema Integral de Gestión de Restaurante (SIGR)

Aplicación web en Java para gestionar **pedidos**, **reservas**, **menú digital**,
**cierre de caja** y **reportes de ventas** de un restaurante.

Proyecto académico — Gestión del Software.
Autor: Camilo Andrés Hurtado Carvajal.

## Módulos de la línea base

| Módulo | Paquete | Descripción |
|---|---|---|
| Autenticación | `com.simplefood.auth` | Registro, inicio de sesión y roles (cliente, mesero, administrador) |
| Menú digital | `com.simplefood.menu` | CRUD de platos y categorías |
| Pedidos | `com.simplefood.pedidos` | Registro y seguimiento de pedidos en tiempo real |
| Reservas | `com.simplefood.reservas` | Reservas por fecha y hora con asignación de mesa |
| Caja | `com.simplefood.caja` | Cierre de caja y reporte de ventas diario |
| Web | `com.simplefood.web` | Servidor HTTP, API REST y eventos (SSE) |

## Requisitos

- Java 21 (JDK)
- Maven 3.9+
- Git

## Clonar y ejecutar

```bash
git clone https://github.com/camilohurtado/simplefood.git
cd simplefood
mvn clean package
java -jar target/simplefood-1.0.0.jar
```

Abrir <http://localhost:8080>. El puerto se cambia con la variable `SIMPLEFOOD_PORT`.

### Usuarios de demostración

| Rol | Correo | Clave |
|---|---|---|
| Administrador | admin@simplefood.com | admin123 |
| Mesero | mesero@simplefood.com | mesero123 |
| Cliente | cliente@simplefood.com | cliente123 |

## Pruebas

```bash
mvn test
```

Incluye pruebas unitarias por módulo y pruebas de integración de la API
(`ApiIntegracionTest`) que levantan el servidor real.

## API principal

| Método | Ruta | Rol |
|---|---|---|
| POST | `/api/auth/registro` | Público |
| POST | `/api/auth/login` | Público |
| GET | `/api/menu/platos`, `/api/menu/categorias` | Público |
| POST/PUT/DELETE | `/api/menu/platos[/id]`, `/api/menu/categorias[/id]` | Administrador |
| POST | `/api/pedidos` (`{"mesa":3,"items":"1:2,4:1"}`) | Mesero |
| PUT | `/api/pedidos/{id}/estado` | Mesero |
| GET | `/api/pedidos/activos` | Mesero |
| GET | `/api/eventos/pedidos` (Server-Sent Events) | Público |
| POST | `/api/reservas` (`{"fechaHora":"2026-11-20T19:30","personas":4}`) | Cliente |
| GET | `/api/reservas?fecha=aaaa-mm-dd` | Mesero |
| GET | `/api/caja/reporte?fecha=aaaa-mm-dd` | Administrador |
| POST | `/api/caja/cierre` (`{"efectivo":"150000"}`) | Administrador |

Enviar el token de sesión en la cabecera `Authorization: Bearer <token>`.

## Estructura de carpetas

```
simplefood/
├── docs/                    Documentación técnica
├── src/main/java/com/simplefood/
│   ├── auth/  menu/  pedidos/  reservas/  caja/
│   ├── util/                Utilidades comunes
│   └── web/                 Servidor y API
├── src/main/resources/public/  Interfaz web
└── src/test/java/           Pruebas
```

## Convenciones

- Código, clases y mensajes en español; paquetes en minúscula por módulo.
- Indentación de 4 espacios, llaves en la misma línea.
- Commits cortos en imperativo (`Agrega ...`, `Corrige ...`).
- Ramas: `main` estable; funcionalidades nuevas en `feature/<nombre>`.

## Documentación

- [Estructura de base de datos](docs/BASE_DE_DATOS.md)
- [Manual de despliegue](docs/DESPLIEGUE.md)
- [Historial de cambios](CHANGELOG.md)

## Licencia

MIT — ver [LICENSE.txt](LICENSE.txt).
