# Operación Alquimia — Protocolo Savitar

Solución del examen final de Construcción de Software 2 (2026-2): un sistema que, con la información de tres
dispositivos de vigilancia, **calcula la ubicación del laboratorio de Alchemy** (trilateración) y
**reconstruye el mensaje interceptado**.

- Java 17, Spring Boot 4.1.0, Maven.
- **DDD + Arquitectura Hexagonal (puertos y adaptadores).**
- Información **solo en memoria** (sin base de datos ni servicios externos).

## Arquitectura

```
src/main/java/alquimia
├── domain                      <- el núcleo: no depende de HTTP ni de infraestructura
│   ├── model                   Position, SurveillanceDevice, MessageFragment, Reading, IntelligenceResult
│   ├── exception               errores de negocio (InvalidReading, UnknownDevice, InsufficientData, ...)
│   ├── service                 TrilaterationService, MessageReconstructionService (lógica pura)
│   └── port
│       ├── in                  casos de uso (puertos de entrada), uno por requerimiento 2.1 a 2.6
│       └── out                 ReadingRepositoryPort (puerto de salida)
├── application                 IntelligenceService: orquesta los casos de uso
└── adapter
    ├── in/rest                 IntelligenceController, DTOs, RestExceptionHandler (HTTP)
    └── out/memory              InMemoryReadingRepository (almacenamiento temporal en memoria)
```

La regla de dependencia: `adapter` y `application` dependen de `domain`; el `domain` no importa nada de Spring.

## Dispositivos (posición fija)

| Id (en la URL) | Nombre | Posición |
|---|---|---|
| `star-labs` | STAR Labs | [-500, -200] |
| `ccpd` | CCPD | [100, -100] |
| `star-satellite` | S.T.A.R. Satellite | [500, 100] |

El nombre también se acepta como `STAR Labs`, `S.T.A.R. Satellite`, etc. (se ignoran mayúsculas, espacios, puntos y guiones).

## Cómo funciona

- **Ubicación (trilateración):** al restar las ecuaciones de las circunferencias de los tres dispositivos queda un
  sistema lineal de 2x2 que se resuelve directamente. El resultado se redondea a 2 decimales.
- **Mensaje:** cada fragmento es una lista de palabras; `""` es una palabra que no llegó. Para cada posición se toma la
  palabra que haya recibido algún dispositivo. Los vacíos se ignoran.
  - Si dos dispositivos reciben palabras **distintas** en la misma posición, o los fragmentos tienen **distinto largo**
    → error `422`.
  - Si **ningún** dispositivo recibió una palabra → no hay información suficiente.

## Endpoints (`/api/v1`)

| Requerimiento | Método y ruta | Respuesta |
|---|---|---|
| 2.1 Información completa | `POST /intelligence` | `200` posición y mensaje (no guarda nada) |
| 2.2 Registro individual | `POST /devices/{name}` | `201` el dispositivo |
| 2.3 Consulta y procesamiento | `GET /intelligence` | `200` posición y mensaje, o `404` si faltan datos |
| 2.4 Reemplazo | `PUT /devices/{name}` | `200` (requiere distancia y mensaje) |
| 2.5 Actualización parcial | `PATCH /devices/{name}` | `200` (solo cambia lo enviado) |
| 2.6 Eliminación | `DELETE /devices/{name}` | `204` |
| Apoyo | `GET /devices`, `GET /devices/{name}` | `200` lo almacenado |

Cuerpo de un dispositivo: `{ "distance": 538.52, "message": ["este", "", "mensaje"] }`.
Cuerpo de 2.1: `{ "devices": [ { "name": "star-labs", "distance": 538.52, "message": [...] }, ... ] }`.

### Errores

Todos devuelven `{ "status": ..., "error": ..., "message": ... }`.

| Código | Cuándo |
|---|---|
| `400` | Datos inválidos o incompletos (distancia ausente, negativa o no numérica; mensaje ausente, vacío o con `null`; faltan dispositivos en 2.1; JSON mal formado; PATCH vacío) |
| `404` | Dispositivo desconocido, sin información registrada, o no hay información suficiente en la consulta |
| `409` | Registrar un dispositivo que ya tiene información (usar PUT o PATCH) |
| `422` | Fragmentos que no se pueden combinar (palabras distintas o distinto largo) |

## Cómo ejecutarlo

Con Docker:

```
docker compose up --build
```

O con Maven (Java 17):

```
mvn spring-boot:run
```

La API queda en `http://localhost:8080/api/v1`.

### Pruebas

```
mvn test
```

Las pruebas cubren el dominio (trilateración, reconstrucción, validaciones) y los casos de uso 2.1 a 2.6, con casos de error.

## Ejemplo (PowerShell)

Con el laboratorio en (0, 0) las distancias son aproximadamente 538.52, 141.42 y 509.90.

```powershell
$u = "http://localhost:8080/api/v1"
$h = @{ "Content-Type" = "application/json" }

# 2.2 registrar de a uno, en cualquier orden
Invoke-RestMethod -Method Post -Uri "$u/devices/ccpd" -Headers $h -Body '{"distance":141.42,"message":["","piedra","","","alquimia"]}'
Invoke-RestMethod -Method Post -Uri "$u/devices/star-labs" -Headers $h -Body '{"distance":538.52,"message":["la","","es","","alquimia"]}'

# 2.3 todavia faltan datos (404)
Invoke-RestMethod -Uri "$u/intelligence"

Invoke-RestMethod -Method Post -Uri "$u/devices/star-satellite" -Headers $h -Body '{"distance":509.9,"message":["","","","obra",""]}'

# 2.3 ahora si: posicion (0,0) y mensaje "la piedra es obra alquimia"
Invoke-RestMethod -Uri "$u/intelligence"

# 2.5 actualizar solo la distancia, 2.4 reemplazar todo, 2.6 eliminar
Invoke-RestMethod -Method Patch -Uri "$u/devices/ccpd" -Headers $h -Body '{"distance":150}'
Invoke-RestMethod -Method Put -Uri "$u/devices/ccpd" -Headers $h -Body '{"distance":141.42,"message":["","piedra","","","alquimia"]}'
Invoke-RestMethod -Method Delete -Uri "$u/devices/ccpd"
```
