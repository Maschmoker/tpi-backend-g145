# tpi-backend-g145

Guardería de Mascotas — backend (Spring Boot microservicios).

## Requisitos

- Docker Desktop con Docker Compose v2.
- PowerShell 7+ (o Windows PowerShell 5.1) para el smoke test.

## Levantar todo el proyecto

```powershell
# 1. Variables de entorno (el archivo .env no se versiona)
Copy-Item .env.example .env

# 2. Construir y levantar los 6 contenedores
docker compose up -d --build

# 3. Verificar que los 6 contenedores queden healthy
docker compose ps

# 4. Smoke test end-to-end (a través del Gateway)
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1

# 5. Logs de un servicio
docker compose logs -f estadias-service

# 6. Detener (conservando datos)
docker compose down

# 7. Detener y borrar volúmenes (reset completo)
docker compose down -v
```

El smoke test espera a que los servicios estén saludables, obtiene tokens reales de Keycloak
(`cliente1`, `recepcionista1`, `cuidado1`, `chofer1`, `admin1`), propaga `X-Correlation-ID` y
recorre el ciclo completo de una estadía con retiro y devolución hasta el estado `ENTREGADA`.
Finaliza con `SMOKE TEST OK`.

Si el usuario de prueba de Keycloak tiene otra contraseña, definir antes de ejecutarlo:

```powershell
$env:KEYCLOAK_TEST_USER_PASSWORD = 'Test1234!'
```

## Servicios

| Servicio | URL local | Health |
|---|---|---|
| Gateway | http://localhost:8080 | http://localhost:8080/actuator/health |
| Clientes | http://localhost:8081 | http://localhost:8081/actuator/health |
| Estadías | http://localhost:8082 | http://localhost:8082/actuator/health |
| Logística | http://localhost:8083 | http://localhost:8083/actuator/health |
| Keycloak | http://localhost:8088 | — |
| PostgreSQL | localhost:5432 | — |

Swagger UI: `http://localhost:<puerto>/swagger-ui.html` en cada microservicio.
