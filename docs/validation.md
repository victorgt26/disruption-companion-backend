# Validación técnica

Este documento resume las validaciones del vertical slice Backend / Integración y los escenarios que todavía requieren mayor definición.

## 1. Herramientas y alcance

La solución utiliza:

- Gradle para compilación y ejecución de pruebas.
- JUnit y Mockito para pruebas unitarias.
- MockMvc para validar contratos HTTP.
- Fixtures JSON para simular los sistemas externos.
- Un reloj fijo en el perfil `demo` para reproducir los datos históricos.
- Mermaid para documentar arquitectura, flujo y secuencia.

El objetivo de estas comprobaciones es validar las decisiones de negocio, el manejo de errores, los contratos y la respuesta del sistema ante información incompleta o contradictoria.

## 2. Ejecución de pruebas

Comando principal:

```bash
./gradlew clean test
```

El registro anterior del proyecto documentaba **68 pruebas exitosas y 0 fallos**.

Este resultado corresponde a la validación previamente registrada. Debe confirmarse nuevamente después de los últimos cambios para considerarlo el resultado final de la versión entregada.

| Verificación | Resultado |
|---|---|
| Pruebas automatizadas anteriormente registradas | 68 |
| Fallos anteriormente registrados | 0 |
| Última ejecución después de cambios de documentación | Pendiente de confirmar |
| Compilación final | Pendiente de confirmar |

Las pruebas de negocio se concentran principalmente en `application`, utilizando mocks de los puertos de salida.

## 3. Escenarios funcionales

El proyecto incluye un catálogo de 29 escenarios en [api-examples.md](api-examples.md). Estos escenarios permiten revisar diferentes decisiones y respuestas del sistema.

Entre los casos documentados están:

| Escenario | Comportamiento que se comprueba |
|---|---|
| `BASE_CASE` | Retraso de 160 minutos y conexión no confirmada |
| `NO_DISRUPTION` | Ausencia de interrupción verificada |
| `DELAY_BELOW_THRESHOLD` | Retraso de 179 minutos, sin activar R1 |
| `DELAY_AT_THRESHOLD` | Retraso de 180 minutos, activando R1 |
| `MINOR_REQUIRES_APPROVAL` | Atención prioritaria sujeta a aprobación |
| `CONNECTION_UNVERIFIED` | No confirmar una conexión perdida sin evidencia |
| `STALE_EVENT` | Tratamiento de eventos antiguos |
| `FUTURE_EVENT` | Tratamiento de timestamps futuros |
| `DEGRADED_SOURCE` | Evaluación conservadora de fuentes degradadas |
| `CONFLICTING_EVENTS` | Manejo de evidencia operacional contradictoria |
| `UNKNOWN_POLICY_RULE` | Manejo de reglas desconocidas |
| `POLICY_NOT_EFFECTIVE` | Validación de vigencia de políticas |
| `SOURCE_UNAVAILABLE` | Manejo de dependencia no disponible |
| `COMPENSATION_REQUESTED` | Solicitud de compensación con revisión humana |
| `INVALID_BOOKING` | Validación del identificador de reserva |

El catálogo también incluye casos de contexto parcial, datos faltantes, políticas reemplazadas y errores de dependencias.

La existencia de un escenario en el catálogo no equivale por sí sola a una prueba automatizada exitosa. Los resultados deben comprobarse mediante la suite de tests y las respuestas HTTP correspondientes.

## 4. Contratos HTTP y pruebas manuales

Para iniciar la aplicación:

```bash
./gradlew bootRun
```

Consulta principal:

```bash
curl -X POST http://127.0.0.1:8080/api/assessments \
  -H "Content-Type: application/json" \
  -d '{"bookingId":"ABC123","compensationRequested":false}'
```

El escenario base debe devolver una evaluación que refleje:

- El retraso operacional de 160 minutos.
- La conexión en riesgo, sin declarar que se ha perdido.
- La no activación de R1 por estar debajo de 180 minutos.
- La necesidad de revisión humana por la presencia de un menor.
- La ausencia de cambios de reserva ejecutados.

También se dispone de Swagger UI y de un endpoint auxiliar de escenarios, exclusivo del perfil `demo`.

El perfil `live` utiliza el reloj real, pero continúa trabajando con adaptadores mock.

## 5. Seguridad y privacidad

Los contratos públicos deben evitar exponer datos personales de los pasajeros, como documentos de identidad, correo electrónico o teléfono.

También deben evitarse detalles internos y payloads sensibles en los logs y errores HTTP.

Estas comprobaciones forman parte de las validaciones de privacidad del proyecto.

La implementación es una demo local sin autenticación. No debe considerarse preparada para exponer información de reservas reales.

## 6. Validaciones pendientes sobre eventos operacionales

Se identificó un caso que requiere mayor definición del contrato de eventos: la relación entre las actualizaciones `DELAY` y `STATUS` de un mismo segmento.

Antes de modificar la lógica de selección, corresponde revisar los siguientes escenarios:

| Escenario | Validación esperada |
|---|---|
| `DELAY` seguido de `STATUS: ON_TIME` | Confirmar si el segundo evento reemplaza el retraso o si existe ambigüedad |
| `STATUS: ON_TIME` seguido de `DELAY` | Reconocer el retraso cuando representa el estado vigente |
| Eventos contradictorios con igual timestamp | No seleccionar arbitrariamente una versión |
| `DELAY` seguido de `GATE_CHANGE` | Mantener la evidencia del retraso |
| Eventos recibidos fuera de orden | Evaluar según semántica, fuente y timestamp, no según orden de llegada |

La implementación actual selecciona el último evento válido `DELAY` o `STATUS` de OPS_CORE.

Este comportamiento depende de un supuesto sobre la semántica del contrato externo. Debe confirmarse con Operaciones antes de utilizar integraciones productivas.

**Estado:** validación adicional pendiente. Estos escenarios no deben registrarse como pruebas exitosas hasta que se implementen y ejecuten.

## 7. Limitaciones

Las validaciones utilizan fuentes simuladas y un escenario histórico.

No comprueban la integración con sistemas reales de reservas, inventario o políticas, ni la ejecución de operaciones de escritura.

Tampoco validan la autenticación, la autorización sobre reservas reales o el comportamiento bajo carga de producción.

La cobertura está enfocada en demostrar decisiones de negocio, seguridad básica de la respuesta y manejo de incertidumbre dentro del alcance del desafío.
