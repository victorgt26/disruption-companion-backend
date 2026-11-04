# Catálogo ejecutable de requests y responses

La descripción del contrato de pasajero está en [passenger-experience.md](passenger-experience.md). Este catálogo reúne los requests reproducibles y las respuestas completas de la implementación actual. Los ejemplos no amplían las políticas del challenge: cambian datos de entrada para verificar sus decisiones y errores.

## Probar desde Swagger UI

1. Ejecutar `./gradlew bootRun`. El perfil predeterminado es `demo`, con reloj fijo `2026-09-15T11:03:00Z` y frescura de 30 minutos.
2. Abrir [Swagger UI](http://127.0.0.1:8080/swagger-ui.html).
3. Abrir **POST /api/demo/assessments**, pulsar **Try it out** y elegir un caso en el desplegable **Examples** del request. Cada ejemplo rellena tanto el selector de fixtures como la solicitud del pasajero.
4. Pulsar **Execute** y comparar **Server response** con la respuesta enlazada abajo. Los ejemplos de respuestas de Swagger son documentación, no disparan una llamada ni cambian automáticamente al elegir otro request.
5. En **POST /api/assessments** también hay ejemplos del caso base, compensación, reserva desconocida y validación. Para cambiar datos de fuentes o políticas, usar el endpoint demo.

Si el puerto 8080 está ocupado: `./gradlew bootRun --args='--server.port=8081'` y abrir Swagger en el puerto 8081. Reiniciar la aplicación que se esté usando después de actualizar el código para cargar el catálogo nuevo.

La API principal mantiene su body original. El endpoint auxiliar, disponible solo con el perfil `demo`, recibe:

```json
{
  "scenario": "DELAY_AT_THRESHOLD",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

`scenario` en este endpoint es un **selector de fixtures** (`DemoScenario`), no un escenario de negocio. `DisruptionScenario.DELAY` es el único escenario de política implementado. Cada consulta crea adaptadores independientes con referencias a fixtures inmutables y ejecuta el mismo caso de uso. No hay configuración global mutable ni modificación de archivos desde HTTP. El endpoint demo y los ejemplos de catálogo no se registran fuera de ese perfil. El perfil `live` sigue usando mocks con reloj real, no integraciones productivas.

## Casos disponibles

Las 29 consultas del catálogo incluyen los tres estados de evaluación y todos los motivos de revisión actuales. Los casos de respuesta 200 no confirman cambios de reserva. `entitlements` describe coincidencias de reglas, sujetas a revisión y aprobación cuando corresponda.

| Request de ejemplo | Condición | HTTP / estado | Respuesta completa |
|---|---|---|---|
| [BASE_CASE](#base-case) | Caso histórico: reserva parcial, menor y conexión no confirmada | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/base_case.json) |
| [NO_DISRUPTION](#no-disruption) | Sin cambio confirmado, datos completos | 200 / `NO_VERIFIED_DISRUPTION` | [JSON](../src/main/resources/api-examples/responses/no_disruption.json) |
| [DELAY_BELOW_THRESHOLD](#delay-below-threshold) | Retraso de 179 minutos, sin derecho acreditado por R1 | 200 / `DISRUPTION` | [JSON](../src/main/resources/api-examples/responses/delay_below_threshold.json) |
| [DELAY_AT_THRESHOLD](#delay-at-threshold) | Retraso de 180 minutos, coincide R1 | 200 / `DISRUPTION` | [JSON](../src/main/resources/api-examples/responses/delay_at_threshold.json) |
| [INTERNATIONAL_UNVERIFIED](#international-unverified) | 240 minutos, internacionalidad desconocida | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/international_unverified.json) |
| [MINOR_REQUIRES_APPROVAL](#minor-requires-approval) | Menor y atención prioritaria sujeta a aprobación | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/minor_requires_approval.json) |
| [PARTIAL_TRIP](#partial-trip) | Reserva con información parcial | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/partial_trip.json) |
| [STALE_TRIP](#stale-trip) | Reserva antigua, evento operacional vigente | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/stale_trip.json) |
| [MISSING_PASSENGERS](#missing-passengers) | Falta contexto de pasajeros | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/missing_passengers.json) |
| [INCOMPLETE_TRIP](#incomplete-trip) | Faltan segmentos de la reserva | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/incomplete_trip.json) |
| [CONNECTION_UNVERIFIED](#connection-unverified) | No se puede confirmar el resultado de la conexión | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/connection_unverified.json) |
| [STALE_FEED](#stale-feed) | Respuesta operacional antigua | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/stale_feed.json) |
| [STALE_EVENT](#stale-event) | Evento antiguo | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/stale_event.json) |
| [FUTURE_EVENT](#future-event) | Evento con timestamp futuro | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/future_event.json) |
| [DEGRADED_SOURCE](#degraded-source) | Fuente autoritativa degradada | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/degraded_source.json) |
| [CONFLICTING_EVENTS](#conflicting-events) | Eventos autoritativos simultáneos contradictorios | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/conflicting_events.json) |
| [INCOMPLETE_EVENT](#incomplete-event) | Faltan detalles del retraso | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/incomplete_event.json) |
| [UNSUPPORTED_STATUS](#unsupported-status) | Estado CANCELLED fuera del alcance implementado | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/unsupported_status.json) |
| [UNKNOWN_POLICY_SCENARIO](#unknown-policy-scenario) | Escenario externo no soportado | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/unknown_policy_scenario.json) |
| [POLICY_NOT_EFFECTIVE](#policy-not-effective) | Política todavía no vigente | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/policy_not_effective.json) |
| [POLICY_SUPERSEDED](#policy-superseded) | Versión de política reemplazada | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/policy_superseded.json) |
| [UNKNOWN_POLICY_RULE](#unknown-policy-rule) | Regla de política desconocida | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/unknown_policy_rule.json) |
| [SOURCE_UNAVAILABLE](#source-unavailable) | Fuente de reserva no disponible | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/source_unavailable.json) |
| [POLICY_SOURCE_UNAVAILABLE](#policy-source-unavailable) | Fallo de política conservando retraso verificado | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/policy_source_unavailable.json) |
| [COMPENSATION_REQUESTED](#compensation-requested) | Compensación solicitada, requiere aprobación | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/compensation_requested.json) |
| [COMPENSATION_WITHOUT_DELAY](#compensation-without-delay) | Compensación sin escenario DELAY verificado | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/compensation_without_delay.json) |
| [UNKNOWN_BOOKING](#unknown-booking) | Reserva inexistente en el fixture | 200 / `REVIEW_REQUIRED` | [JSON](../src/main/resources/api-examples/responses/unknown_booking.json) |
| [INVALID_BOOKING](#invalid-booking) | Formato de reserva inválido | 400 / `400` | [JSON](../src/main/resources/api-examples/responses/invalid_booking.json) |
| [UNKNOWN_DEMO_SCENARIO](#unknown-demo-scenario) | Selector demo no reconocido | 400 / `400` | [JSON](../src/main/resources/api-examples/responses/unknown_demo_scenario.json) |

## Requests completos

Copiar el body al endpoint demo. El selector corresponde a fixtures de reserva, eventos y políticas; se puede variar `bookingId` y `compensationRequested` sin alterar el servidor. Todos los datos son sintéticos/minimizados y corresponden al escenario histórico de demostración.

### BASE CASE

Caso histórico: reserva parcial, menor y conexión no confirmada.

```json
{
  "scenario": "BASE_CASE",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/base_case.json).

### NO DISRUPTION

Sin cambio confirmado, datos completos.

```json
{
  "scenario": "NO_DISRUPTION",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/no_disruption.json).

### DELAY BELOW THRESHOLD

Retraso de 179 minutos, sin derecho acreditado por R1.

```json
{
  "scenario": "DELAY_BELOW_THRESHOLD",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/delay_below_threshold.json).

### DELAY AT THRESHOLD

Retraso de 180 minutos, coincide R1.

```json
{
  "scenario": "DELAY_AT_THRESHOLD",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/delay_at_threshold.json).

### INTERNATIONAL UNVERIFIED

240 minutos, internacionalidad desconocida.

```json
{
  "scenario": "INTERNATIONAL_UNVERIFIED",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/international_unverified.json).

### MINOR REQUIRES APPROVAL

Menor y atención prioritaria sujeta a aprobación.

```json
{
  "scenario": "MINOR_REQUIRES_APPROVAL",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/minor_requires_approval.json).

### PARTIAL TRIP

Reserva con información parcial.

```json
{
  "scenario": "PARTIAL_TRIP",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/partial_trip.json).

### STALE TRIP

Reserva antigua, evento operacional vigente.

```json
{
  "scenario": "STALE_TRIP",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/stale_trip.json).

### MISSING PASSENGERS

Falta contexto de pasajeros.

```json
{
  "scenario": "MISSING_PASSENGERS",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/missing_passengers.json).

### INCOMPLETE TRIP

Faltan segmentos de la reserva.

```json
{
  "scenario": "INCOMPLETE_TRIP",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/incomplete_trip.json).

### CONNECTION UNVERIFIED

No se puede confirmar el resultado de la conexión.

```json
{
  "scenario": "CONNECTION_UNVERIFIED",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/connection_unverified.json).

### STALE FEED

Respuesta operacional antigua.

```json
{
  "scenario": "STALE_FEED",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/stale_feed.json).

### STALE EVENT

Evento antiguo.

```json
{
  "scenario": "STALE_EVENT",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/stale_event.json).

### FUTURE EVENT

Evento con timestamp futuro.

```json
{
  "scenario": "FUTURE_EVENT",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/future_event.json).

### DEGRADED SOURCE

Fuente autoritativa degradada.

```json
{
  "scenario": "DEGRADED_SOURCE",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/degraded_source.json).

### CONFLICTING EVENTS

Eventos autoritativos simultáneos contradictorios.

```json
{
  "scenario": "CONFLICTING_EVENTS",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/conflicting_events.json).

### INCOMPLETE EVENT

Faltan detalles del retraso.

```json
{
  "scenario": "INCOMPLETE_EVENT",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/incomplete_event.json).

### UNSUPPORTED STATUS

Estado CANCELLED fuera del alcance implementado.

```json
{
  "scenario": "UNSUPPORTED_STATUS",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/unsupported_status.json).

### UNKNOWN POLICY SCENARIO

Escenario externo no soportado.

```json
{
  "scenario": "UNKNOWN_POLICY_SCENARIO",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/unknown_policy_scenario.json).

### POLICY NOT EFFECTIVE

Política todavía no vigente.

```json
{
  "scenario": "POLICY_NOT_EFFECTIVE",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/policy_not_effective.json).

### POLICY SUPERSEDED

Versión de política reemplazada.

```json
{
  "scenario": "POLICY_SUPERSEDED",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/policy_superseded.json).

### UNKNOWN POLICY RULE

Regla de política desconocida.

```json
{
  "scenario": "UNKNOWN_POLICY_RULE",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/unknown_policy_rule.json).

### SOURCE UNAVAILABLE

Fuente de reserva no disponible.

```json
{
  "scenario": "SOURCE_UNAVAILABLE",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/source_unavailable.json).

### POLICY SOURCE UNAVAILABLE

Fallo de política conservando retraso verificado.

```json
{
  "scenario": "POLICY_SOURCE_UNAVAILABLE",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/policy_source_unavailable.json).

### COMPENSATION REQUESTED

Compensación solicitada, requiere aprobación.

```json
{
  "scenario": "BASE_CASE",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": true
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/compensation_requested.json).

### COMPENSATION WITHOUT DELAY

Compensación sin escenario DELAY verificado.

```json
{
  "scenario": "NO_DISRUPTION",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": true
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/compensation_without_delay.json).

### UNKNOWN BOOKING

Reserva inexistente en el fixture.

```json
{
  "scenario": "BASE_CASE",
  "request": {
    "bookingId": "XYZ999",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/unknown_booking.json).

### INVALID BOOKING

Formato de reserva inválido.

```json
{
  "scenario": "BASE_CASE",
  "request": {
    "bookingId": "INVALID",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/invalid_booking.json).

### UNKNOWN DEMO SCENARIO

Selector demo no reconocido.

```json
{
  "scenario": "NOT_SUPPORTED",
  "request": {
    "bookingId": "ABC123",
    "compensationRequested": false
  }
}
```

[Respuesta completa](../src/main/resources/api-examples/responses/unknown_demo_scenario.json).

## Otras respuestas HTTP

- **400**: body inválido, `scenario` ausente/desconocido, `request` ausente, localizador inválido o JSON malformado. Por ejemplo, sustituir el body por `{` reproduce el error de sintaxis JSON. Se devuelve ProblemDetail genérico sin reflejar el valor rechazado.
- **500**: [respuesta genérica completa](../src/main/resources/api-examples/responses/unexpected-failure.json). Representa un error inesperado, no una decisión de elegibilidad. Está verificado mediante una excepción inyectada en la prueba HTTP, no mediante un selector de negocio que fuerce errores internos. En Swagger figura como ejemplo de respuesta 500.
- **404**: `/api/demo/assessments` no existe con perfil `live`; el endpoint principal sigue disponible.

Los fallos esperados de fuentes se convierten en **200 / REVIEW_REQUIRED**, no en un 500 que sugiera un resultado definitivo. Las fuentes simuladas como no disponibles se configuran explícitamente sin recurso de lectura, y el adaptador lanza `SourceUnavailableException`. Si falla política después de leer eventos válidos, el retraso se conserva.

## Fuente de los ejemplos y mantenimiento

[El catálogo JSON](../src/main/resources/api-examples/catalog.json) contiene los nombres, bodies, códigos HTTP y recursos de respuesta que Swagger utiliza. Las respuestas del catálogo se capturaron mediante llamadas HTTP al endpoint implementado y se contrastan con MockMvc en cada ejecución de `./gradlew clean test`. El ejemplo 500 se valida mediante inyección controlada de fallo en test. Si cambia el comportamiento, revisar la decisión antes de actualizar un snapshot: no regenerar respuestas esperadas para ocultar una regresión.

Los fixtures de negocio están en `src/main/resources/mocks/demo/`; las referencias por selector están en `DemoScenario`. Los timestamps y umbrales pertenecen al reloj y configuración de demo: si se cambian, también deben revisarse ejemplos y pruebas. La validación automática comprueba cobertura de estados y motivos, esquemas OpenAPI y ausencia del endpoint fuera de demo. No se simulan cancelaciones, reembolsos, inventario, reservas nuevas ni políticas que no están implementadas.
