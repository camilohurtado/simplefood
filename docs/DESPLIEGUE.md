# Manual de despliegue

## 1. Requisitos del servidor

- JDK 21
- Maven 3.9+ (solo para compilar)
- Puerto TCP libre (8080 por defecto)

## 2. Compilación

```bash
git clone https://github.com/camilohurtado/simplefood.git
cd simplefood
git checkout v1.0.0-baseline   # versión de la línea base
mvn clean package
```

Se genera `target/simplefood-1.0.0.jar`. El build ejecuta todas las pruebas;
si alguna falla, el empaquetado se detiene.

## 3. Ejecución

```bash
java -jar target/simplefood-1.0.0.jar
```

Variables de entorno:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `SIMPLEFOOD_PORT` | `8080` | Puerto HTTP |

## 4. Ejecución como servicio (Linux, systemd)

```ini
[Unit]
Description=SimpleFood SIGR
After=network.target

[Service]
User=simplefood
WorkingDirectory=/opt/simplefood
Environment=SIMPLEFOOD_PORT=8080
ExecStart=/usr/bin/java -jar /opt/simplefood/simplefood-1.0.0.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable --now simplefood
```

## 5. Verificación

```bash
curl http://localhost:8080/api/menu/categorias
```

Debe responder un JSON con las categorías de demostración.

## 6. Consideraciones

- Los datos se guardan en memoria: se pierden al reiniciar (persistencia
  planificada para una versión posterior, ver `docs/schema.sql`).
- Para producción se recomienda un proxy inverso (Nginx) con HTTPS.
