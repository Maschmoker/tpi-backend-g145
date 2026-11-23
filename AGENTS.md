# AGENTS.md — Guardería de Mascotas

## 1. Propósito de este archivo

Este archivo proporciona contexto y reglas de trabajo a una IA que contribuya al backend del proyecto académico **Guardería de Mascotas**. Antes de modificar código, leer este documento y revisar la estructura real del repositorio. No asumir que una funcionalidad está implementada solo porque esté descrita aquí como objetivo.

## 2. Resumen del sistema

El sistema gestiona una guardería de mascotas. Permite administrar clientes, direcciones y mascotas; solicitar estadías; seleccionar servicios de cuidado; calcular cotizaciones; registrar la recepción, el seguimiento y la entrega de las mascotas; y coordinar traslados opcionales desde/hacia el domicilio del cliente.

Cuando hay traslados, el sistema planifica recorridos diarios usando distancias y tiempos de calles reales de Córdoba mediante Google Maps Platform. La integración debe permitir geocodificar direcciones escritas y consultar distancias y tiempos de recorrido por calles. Si la API no está disponible, se debe definir un comportamiento de contingencia; Haversine puede servir como estimación geométrica, pero no reemplaza una distancia real por calles.

El alcance es principalmente backend. Swagger UI y Postman se utilizan para explorar y probar las APIs; no se contempla una interfaz web propia en el MVP.

## 3. Arquitectura

La solución está organizada alrededor de un API Gateway, tres microservicios funcionales y una librería Maven común:

| Componente | Responsabilidad |
|---|---|
| `gateway-service` | Punto de entrada de las solicitudes, enrutamiento hacia los microservicios y validación de JWT. |
| `clientes-service` | Administración de clientes, direcciones georreferenciadas y mascotas. |
| `estadias-service` | Catálogo de servicios, solicitudes y cotizaciones de estadías, ciclo de vida de la estadía y seguimiento de servicios de cuidado. |
| `logistica-service` | Traslados de retiro/devolución, agrupación de paradas, generación y seguimiento de rutas diarias e integración con Google Maps Platform. |
| `guarderia-commons` | Librería Maven JAR con componentes técnicos verdaderamente compartidos. |
| Keycloak | Proveedor de identidad OIDC y emisión de JWT con roles. |
| Google Maps Platform | APIs de geocodificación y cálculo de rutas/distancias para direcciones y recorridos. |
| PostgreSQL | Persistencia de cada microservicio y base propia de Keycloak. |

### Principios de arquitectura

- Cada microservicio es dueño exclusivo de sus datos y de su modelo de persistencia.
- Ningún microservicio debe consultar directamente la base de datos de otro.
- La comunicación entre servicios se realiza mediante APIs/contratos, no mediante acceso compartido a tablas.
- Las referencias entre bases, por ejemplo `Estadia.clienteId`, `Estadia.mascotaId` o `Traslado.estadiaId`, son referencias lógicas. No crear claves foráneas físicas entre bases de distintos servicios.
- El Gateway no debe acceder directamente a las bases de datos.
- `guarderia-commons` no es un microservicio y no debe contener reglas de negocio, entidades JPA ni repositorios pertenecientes a un dominio.

## 4. Tecnologías y dependencias previstas

La línea tecnológica definida para el proyecto es Java 21, Spring Boot 3.x, Maven, Spring Data JPA, Spring Security OAuth2 Resource Server, PostgreSQL, Flyway, Keycloak, Docker Compose, Spring Cloud Gateway y Log4j2 para logging.

### Dependencias por proyecto

**`clientes-service`, `estadias-service` y `logistica-service`:**

- Spring Web (`spring-boot-starter-web`): endpoints REST.
- Spring Data JPA (`spring-boot-starter-data-jpa`): entidades y repositorios.
- PostgreSQL Driver (`org.postgresql:postgresql`): conexión a PostgreSQL.
- Validation (`spring-boot-starter-validation`): validación de DTOs y parámetros.
- OAuth2 Resource Server (`spring-boot-starter-oauth2-resource-server`): validación de JWT.
- Actuator (`spring-boot-starter-actuator`): health checks y métricas básicas.
- Log4j2 (`spring-boot-starter-log4j2`): implementación de logging de la aplicación, reemplazando Logback.
- Flyway (`org.flywaydb:flyway-core` y, cuando la versión de Flyway utilizada lo requiera, `org.flywaydb:flyway-database-postgresql`): migraciones.
- Lombok (`org.projectlombok:lombok`): opcional, para reducir código repetitivo.
- Springdoc OpenAPI UI (`org.springdoc:springdoc-openapi-starter-webmvc-ui`): documentación Swagger/OpenAPI.
- Spring Boot Test (`spring-boot-starter-test`, scope `test`): pruebas.

**Dependencias específicas de `logistica-service`:**

- Google Maps Platform se consume mediante HTTP. Puede utilizarse `RestClient`, incluido en Spring Framework; no es obligatorio agregar un SDK de Google.
- Definir qué APIs de Google Maps Platform se habilitarán según las operaciones necesarias: Geocoding para convertir direcciones escritas en coordenadas y Routes/Route Matrix (o el producto vigente equivalente) para obtener rutas, distancias y duraciones.
- Si el proyecto adopta `WebClient`, agregar `spring-boot-starter-webflux`; no agregarlo solo por consumir Google Maps si se decide usar `RestClient`.
- Springdoc MVC, igual que los otros servicios.

**`gateway-service`:**

- Spring Cloud Gateway, variante reactiva (`org.springframework.cloud:spring-cloud-starter-gateway`, según la versión compatible de Spring Cloud).
- OAuth2 Resource Server (`spring-boot-starter-oauth2-resource-server`).
- Actuator (`spring-boot-starter-actuator`).
- Log4j2 (`spring-boot-starter-log4j2`): implementación de logging del Gateway, reemplazando Logback.
- Spring Boot Test (`spring-boot-starter-test`, scope `test`).
- Lombok es opcional.
- Si se expone Swagger UI desde el Gateway, evaluar Springdoc WebFlux UI (`org.springdoc:springdoc-openapi-starter-webflux-ui`) y la agregación de los documentos de los servicios.

No agregar Spring Web MVC (`spring-boot-starter-web`) ni JPA/PostgreSQL al Gateway salvo que exista una decisión arquitectónica explícita que lo justifique. La variante reactiva de Gateway no debe mezclarse accidentalmente con Spring MVC.

**`guarderia-commons`:**

- Proyecto Maven Java de tipo JAR, no una aplicación Spring Boot ejecutable.
- Mantener sus dependencias mínimas. Según los componentes compartidos efectivamente implementados, puede necesitar `spring-web` para tipos de filtros/errores web, `spring-security-core` y `spring-security-oauth2-resource-server` para integración JWT, y `jakarta.validation-api` para anotaciones de validación.
- Evitar agregar starters completos si alcanza con dependencias de biblioteca.
- Los microservicios deben declarar `guarderia-commons` como dependencia Maven. En desarrollo local se puede instalar con `mvn install`; para CI/CD conviene publicarla en un repositorio Maven de artefactos o construirla junto con los servicios.

### Dependencias de infraestructura, no de Spring Initializr

Keycloak y PostgreSQL se ejecutan como componentes de infraestructura (por ejemplo, mediante Docker Compose). Google Maps Platform es un servicio externo en la nube y Log4j2 es una dependencia de las aplicaciones. No se agregan como contenedores propios:
- Keycloak se configura como issuer OIDC/JWT; los servicios validan los tokens como OAuth2 Resource Servers.
- PostgreSQL es el motor de base de datos; cada servicio utiliza su propia base y credenciales.
- Google Maps Platform se consume mediante HTTP desde `logistica-service`; la clave/API key se inyecta por variable de entorno o mecanismo seguro equivalente, nunca se versiona en el repositorio.

Las versiones exactas de Spring Boot, Spring Cloud, Springdoc y Flyway deben elegirse de forma compatible. No fijar versiones arbitrarias de Spring Cloud Gateway: importar el BOM de Spring Cloud correspondiente a la versión de Spring Boot seleccionada. Spring Initializr y la documentación oficial deben usarse para confirmar los nombres y compatibilidad vigentes al generar el proyecto.

## Logging con Log4j2

Todos los módulos ejecutables Spring Boot (`clientes-service`, `estadias-service`, `logistica-service` y `gateway-service`) utilizarán `spring-boot-starter-log4j2` como implementación de logging.

- Excluir `spring-boot-starter-logging` (Logback) de las dependencias transitivas para evitar tener dos implementaciones de logging en el classpath. Revisar especialmente los starters Web, Data JPA, Actuator y Gateway.
- En el código Java utilizar SLF4J (`org.slf4j.Logger` y `LoggerFactory`) como API de logging; Log4j2 será la implementación subyacente.
- Centralizar la configuración de cada aplicación en `log4j2-spring.xml` (o una configuración común documentada), con niveles apropiados y salida a consola para Docker.
- No registrar contraseñas, tokens JWT, API keys, datos sensibles ni información personal innecesaria.
- Incluir `correlation-id` en los mensajes cuando esté disponible para relacionar solicitudes entre Gateway y microservicios.
- La librería `guarderia-commons` no debe forzar una implementación concreta de logging. Si necesita emitir logs, debe depender de la API SLF4J y dejar que cada aplicación provea Log4j2.

## 5. Bases de datos y propiedad de los datos

Se prevé una instancia PostgreSQL con bases separadas por servicio:

### `clientes_db` — `clientes-service`

- `Cliente`: datos del cliente y vínculo con identidad de Keycloak mediante `keycloakId`.
- `Direccion`: calle, número, ciudad, latitud, longitud y marca de dirección principal.
- `Mascota`: nombre, especie, raza, tamaño, peso, observaciones y condición activa.
- Relaciones: un cliente puede tener varias direcciones y varias mascotas.

### `estadias_db` — `estadias-service`

- `ServicioCatalogo`: tipo de servicio, precio según tamaño y especies a las que aplica.
- `Estadia`: referencias lógicas a cliente y mascota, modalidad, estado, fechas y costo total.
- `EstadiaServicio`: servicios seleccionados para una estadía, estado de ejecución, responsable y fecha.
- `HistorialEstado`: historial de cambios de estado de la estadía.
- Relaciones: una estadía tiene varios registros `EstadiaServicio` y varios registros de historial; un servicio del catálogo puede aparecer en muchos registros `EstadiaServicio`.
- Mantener en la estadía una instantánea mínima de los datos relevantes de la mascota (nombre, especie y tamaño) al momento de la solicitud, según el diseño, para reducir dependencias de lecturas en tiempo real al servicio de clientes.

### `logistica_db` — `logistica-service`

- `Traslado`: referencia lógica a estadía, tipo de traslado, fecha programada, instantánea de dirección/coordenadas y estado.
- `RutaDiaria`: fecha, tipo de recorrido, distancia y duración total, e indicador de ruta estimada.
- `ParadaRuta`: orden, traslados agrupados y estado.
- `Configuracion`: tarifa por kilómetro, radio de cobertura, capacidad, umbral de agrupación y coordenadas de la guardería.
- La combinación `estadiaId + tipo` de traslado debe ser única para evitar duplicados y permitir operaciones idempotentes.
- La instantánea de coordenadas permite planificar recorridos aunque `clientes-service` no esté disponible.

Keycloak mantiene su propia base de datos de infraestructura. No incluir su esquema interno en el DER de negocio.

Flyway administra la evolución del esquema y los datos iniciales necesarios. Cada servicio debe utilizar credenciales propias y conectarse únicamente a su base.

## 6. Actores y roles

Los roles definidos en Keycloak son:

- `CLIENTE`
- `RECEPCIONISTA`
- `PERSONAL_CUIDADO`
- `CHOFER`
- `ADMIN`

Responsabilidades principales:

- Cliente: administra sus datos, direcciones y mascotas; solicita estadías y consulta su estado.
- Recepcionista: registra recepción y entrega, y gestiona estadías.
- Personal de cuidado: inicia y marca como realizados los servicios asignados.
- Chofer: consulta recorridos diarios, inicia rutas y completa paradas.
- Administrador: administra catálogo, tarifas, cobertura, capacidad y usuarios.

Aplicar autorización por rol y, para el rol `CLIENTE`, control de propiedad: un cliente solo puede consultar o modificar sus propias mascotas y estadías. No confiar únicamente en que el Gateway haya validado el token; los microservicios también deben validar JWT y aplicar sus reglas de autorización.

## 7. Reglas de negocio esenciales

1. Una mascota debe pertenecer al cliente que solicita la estadía y estar activa.
2. Una mascota no puede tener dos estadías activas simultáneamente.
3. Los servicios seleccionados deben ser aplicables a la especie de la mascota.
4. El precio del servicio depende del tamaño de la mascota.
5. Todo traslado requiere una dirección con coordenadas.
6. La cobertura se valida usando distancia real por calles/ruta, no distancia en línea recta.
7. El costo de transporte se calcula a partir de la distancia de ida y vuelta por la tarifa por kilómetro, respetando el mínimo configurado.
8. La capacidad diaria de traslados es configurable; si se supera, se rechaza la operación.
9. Traslados a la misma dirección o a direcciones dentro de un umbral configurable (referencia inicial: 30–50 m) pueden agruparse en una misma parada.
10. Solo se permiten las transiciones de estado definidas. Una transición inválida debe rechazarse, normalmente con HTTP 409.
11. Una estadía solo puede cancelarse antes de la recepción; los traslados asociados deben cancelarse como compensación.
12. Cuando todos los servicios de cuidado estén `REALIZADO`, la estadía pasa automáticamente a `LISTA_PARA_RETIRO`.
13. Un cliente solo puede operar sobre sus propios recursos.
14. Toda estadía debe incluir al menos un servicio.
15. La fecha de egreso planificada no puede ser anterior a la fecha de ingreso.
16. El orden configurable de servicios de cuidado es una extensión opcional, fuera del MVP.

## 8. Estados del dominio

### Estadía

Transiciones principales previstas:

- `SOLICITADA` → `RETIRO_PROGRAMADO` cuando corresponde retiro a domicilio.
- `SOLICITADA` → `RECIBIDA` cuando el cliente lleva la mascota.
- `SOLICITADA` → `CANCELADA`.
- `RETIRO_PROGRAMADO` → `EN_TRANSITO_IDA` o `CANCELADA`.
- `EN_TRANSITO_IDA` → `RECIBIDA`.
- `RECIBIDA` → `EN_CUIDADO`.
- `EN_CUIDADO` → `LISTA_PARA_RETIRO` cuando todos los servicios están realizados.
- `LISTA_PARA_RETIRO` → `EN_TRANSITO_VUELTA` si hay devolución a domicilio.
- `LISTA_PARA_RETIRO` → `ENTREGADA` si retira el dueño.
- `EN_TRANSITO_VUELTA` → `ENTREGADA`.
- `CANCELADA` es terminal.

### Servicio de cuidado

`PENDIENTE` → `EN_CURSO` → `REALIZADO`.

### Traslado

`PENDIENTE` → `PROGRAMADO` → `EN_RUTA` → `COMPLETADO`, o `CANCELADO` cuando corresponda.

No permitir cambios de estado arbitrarios desde controladores. Centralizar y validar las transiciones en la lógica de aplicación/dominio del servicio propietario.

## 9. Logística y rutas

- `logistica-service` obtiene los traslados programados para una fecha y tipo (retiro o devolución).
- Agrupa traslados cercanos según la configuración.
- Consulta Google Maps Platform para obtener rutas, distancias y tiempos de recorrido.
- Ordena las paradas mediante una heurística de vecino más cercano y mejora con 2-opt.
- Persiste la ruta diaria y sus paradas ordenadas.
- Si Google Maps Platform falla, aplica la política de contingencia definida por el proyecto. Haversine solo proporciona una estimación en línea recta y no debe presentarse como distancia real por calles; si se utiliza, marcar la ruta/cotización como estimada.
- El MVP contempla un solo chofer; la capacidad es configurable.
- Los recorridos de retiro y devolución se planifican por separado.

No afirmar que una ruta es óptima globalmente: el enfoque descrito es heurístico. Google Maps aporta datos de rutas/distancias; el orden de visita sigue siendo responsabilidad del algoritmo del proyecto.

## 10. API y comunicación

Los prefijos públicos previstos en el Gateway son:

- `/api/clientes/**`
- `/api/estadias/**`
- `/api/logistica/**`

Los endpoints concretos deben verificarse contra los controladores y el contrato OpenAPI vigente. No inventar rutas, métodos HTTP ni permisos como si ya estuvieran implementados.

Operaciones de logística explícitamente contempladas en el diseño incluyen:
- `POST /traslados/cotizar`
- `POST /traslados`
- `PATCH /traslados/{id}/habilitar`
- `POST /rutas/generar`
- `PUT /configuracion`

Las rutas internas pueden diferir de los prefijos públicos del Gateway. Mantener claramente diferenciadas las URLs externas y las internas entre servicios.

## 11. Librería compartida `guarderia-commons`

Puede contener componentes transversales que tengan semántica y comportamiento común, por ejemplo:

- estructura común de error de API;
- utilidades compartidas de autenticación/autorización JWT y conversión de roles;
- propagación/creación de `correlation-id`;
- estructura `Coordenada` y cálculo Haversine si realmente se reutiliza;
- DTOs de contratos entre servicios, únicamente cuando sean contratos estables y compartidos;
- constantes de roles.

No colocar en la librería:
- entidades JPA ni repositorios;
- reglas de negocio específicas;
- estados internos de estadía, servicio o traslado;
- servicios de aplicación propios de un microservicio;
- acceso a bases de datos;
- lógica que fuerce a un servicio a conocer el modelo interno de otro.

Evitar convertir la librería común en un módulo que concentre demasiadas responsabilidades. Si un DTO solo lo usa un servicio, debe permanecer en ese servicio.

## 12. Alcance del MVP y exclusiones

Incluido:
- gestión de clientes, direcciones y mascotas;
- catálogo y precios de servicios;
- solicitud y cotización de estadías;
- seguimiento de estadías y servicios de cuidado;
- recepción, entrega y cancelación;
- coordinación de retiros/devoluciones;
- rutas diarias y consulta a Google Maps Platform con alternativa Haversine;
- seguridad con Keycloak y los cinco roles definidos;
- ejecución local con Docker Compose.

Fuera del MVP:
- facturación y cobro/pagos (solo se calcula el costo);
- múltiples choferes o vehículos;
- interfaz web propia;
- notificaciones;
- orden configurable de servicios de cuidado.

No implementar funcionalidades fuera del alcance sin que el usuario lo solicite explícitamente.

## 13. Convenciones para contribuir al código

- Respetar la separación de capas y responsabilidades que ya exista en cada servicio. No reorganizar paquetes masivamente sin necesidad.
- Mantener una arquitectura por microservicio: controlador/API, aplicación/casos de uso, dominio y persistencia/infraestructura según la estructura real del repositorio.
- Los controladores deben ocuparse de HTTP y delegar la lógica de negocio.
- Validar entradas en los límites de la API y aplicar también invariantes de negocio en la capa correspondiente.
- Usar DTOs para entrada/salida; no exponer entidades JPA directamente.
- No compartir entidades ni repositorios entre microservicios.
- Usar transacciones dentro del límite de cada base de datos. Para operaciones que afectan a más de un servicio, contemplar compensaciones/idempotencia; no asumir transacciones distribuidas.
- Mantener nombres, estados y contratos coherentes con el dominio en español o con la convención que ya use el código. No renombrar masivamente por preferencia personal.
- No agregar dependencias si la funcionalidad puede resolverse con las ya disponibles.
- No incluir secretos, contraseñas ni tokens en el repositorio. Usar variables de entorno y configuración externa.
- No cambiar versiones de Java, Spring Boot o Spring Cloud sin comprobar compatibilidad entre ellas.
- Acompañar cambios de reglas de negocio con pruebas unitarias; cambios de endpoints o persistencia con pruebas apropiadas.
- Antes de modificar un endpoint, entidad o migración, inspeccionar su implementación y usos existentes.
- No sobrescribir migraciones Flyway ya aplicadas; crear una migración nueva.
- No declarar una tarea terminada sin ejecutar o indicar las pruebas/build que se pudieron verificar.

## 14. Cómo debe trabajar una IA en este repositorio

1. Inspeccionar la estructura y los archivos relevantes antes de proponer cambios.
2. Identificar qué microservicio es dueño de la funcionalidad y de los datos afectados.
3. Verificar si la funcionalidad ya existe y reutilizar los patrones del código.
4. Separar claramente hechos del proyecto, decisiones propuestas y aspectos todavía no definidos.
5. No inventar nombres de campos, claves primarias, endpoints, permisos, variables de entorno ni estados. Si falta una definición, señalarlo y proponer una opción explícitamente como propuesta.
6. Realizar cambios acotados y consistentes con la arquitectura.
7. Ejecutar pruebas y compilación del módulo afectado cuando sea posible.
8. Resumir archivos modificados, decisiones tomadas, pruebas ejecutadas y limitaciones pendientes.

## 15. Decisiones pendientes que deben verificarse en el código o acordarse

- Nombres definitivos de claves primarias y atributos no especificados en el documento de diseño.
- Contratos completos y rutas exactas de todos los endpoints.
- Estructura física final del repositorio y configuración de los módulos Maven.
- Versiones concretas compatibles de Spring Boot, Spring Cloud Gateway, Springdoc y Flyway.
- Estrategia definitiva de publicación/consumo de `guarderia-commons` en Docker y CI/CD.
- Modelo relacional exacto entre `ParadaRuta` y `Traslado`, especialmente si se conserva historial de asignaciones de traslados a distintas rutas.
- Detalles de manejo de fallas y compensaciones entre `estadias-service` y `logistica-service`.

Cuando exista una diferencia entre este contexto y el código real, inspeccionar el código y consultar al usuario antes de realizar cambios que alteren decisiones de arquitectura o reglas de negocio.
