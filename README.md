# Disruption Companion Backend

Backend de consulta de interrupciones de viaje. Combina la reserva, los eventos operacionales y las políticas disponibles para informar de retrasos y beneficios aplicables. La consulta es de solo lectura: no modifica reservas ni ejecuta compensaciones.

El alcance actual cubre retrasos con fuentes simuladas en JSON. La [experiencia del pasajero](docs/passenger-experience.md) está documentada, pero no hay frontend implementado.

## Ejecución y validación

Requiere JDK 25 y acceso a Maven Central/Gradle en la primera ejecución; no requiere servicios externos.

```sh
./gradlew clean test
./gradlew bootRun
curl -sS http://127.0.0.1:8080/api/assessments \
  -H 'Content-Type: application/json' \
  -d '{"bookingId":"ABC123","compensationRequested":false}'
```

Con la aplicación iniciada: [Swagger UI](http://127.0.0.1:8080/swagger-ui.html) permite probar la consulta con `ABC123`; [OpenAPI JSON](http://127.0.0.1:8080/v3/api-docs) expone contrato, ejemplos y enums. [Catálogo de 29 requests y responses](docs/api-examples.md): en Swagger, usar `POST /api/demo/assessments` y elegir un ejemplo del request. Este endpoint solo existe en el perfil `demo`.

El perfil predeterminado `demo` fija `Clock` en `2026-09-15T11:03:00Z`. Así, la demostración histórica no caduca según el día de ejecución. `./gradlew bootRun --args='--spring.profiles.active=live'` usa el reloj real: los mismos mocks históricos producirán revisión por datos antiguos. Los tests fijan el tiempo. Reporte: `build/reports/tests/test/index.html`.

Java 25 con toolchains; Spring Boot 4.1.2 gestiona Framework 7, Jackson 3, JUnit y Mockito. Gradle Wrapper 9.3.1. Lombok 1.18.48 genera constructores (`@RequiredArgsConstructor`), sin añadirse al runtime; [springdoc 3.2.0](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot) incorpora Swagger UI. Compatibilidad: [Spring](https://docs.spring.io/spring-boot/system-requirements.html), [Gradle](https://docs.gradle.org/current/userguide/compatibility.html). MVC, Jakarta Validation, sin DB ni dependencias de servicios remotos.

## Decisiones y resultado

- Domain: records y enums, sin lógica. Application: puerto `AssessDisruptionUseCase`, implementación `AssessDisruptionUseCaseImpl.execute`, evaluación de eventos (`OperationalEventAssessor`), frescura (`SourceFreshness`), políticas y tres puertos de lectura. Infrastructure: REST, mapper, JSON y composición Spring. [Arquitectura](docs/diagrams/hexagonal-architecture.md), [flujo](docs/diagrams/disruption-assessment-flow.md), [secuencia](docs/diagrams/assessment-sequence.md).
- ABC123: retraso verificado de **160 min**, salida informada 17:10 en S1. Se ignora el estado antiguo/degradado de LEGACY_MIRROR. R1 exige ≥180: **no se ofrece cambio gratuito por retraso**. No se afirma conexión perdida: faltan llegada actualizada, zonas horarias y tiempo mínimo de conexión. R3 queda sin acreditar. R5 identifica atención prioritaria **sujeta a aprobación humana** por el menor.
- Resultado: `REVIEW_REQUIRED`, motivos de reserva parcial, conexión desconocida y aprobación; siguiente paso `CONTACT_HUMAN_SUPPORT`. No significa que se haya creado un caso. `entitlements` son coincidencias de reglas, sujetas a revisión cuando el estado lo indica; no son autorizaciones de ejecución. Cada coincidencia incluye versión y regla; cada evento incluye fuente, timestamp y tratamiento.
- Supuestos deliberados: cualquier retraso positivo es relevante; solo OPS_CORE confirma retraso/estado; se toma el último evento válido por segmento, empate contradictorio exige revisión. Otros eventos son contexto y no se comunican como estado. Frescura configurable `assessment.max-source-age=PT30M`, límite inclusivo, sin timestamps futuros ni posteriores a la recuperación.
- La reserva parcial/antigua y los fallos de lectura requieren revisión; no impiden comunicar hechos operacionales independientemente verificados. Políticas: fecha efectiva, versión no declarada reemplazada y recuperación no futura; no se inventa TTL de política. Solo condiciones conocidas, sin evaluar código o expresiones remotas. Códigos propios tipados como enums; JSON conserva sus nombres anteriores. Escenario de política tipado como `DisruptionScenario.DELAY`; escenarios externos desconocidos se leen como null y requieren revisión. Completitud, pasajeros, tipos/estados/fuentes operacionales y beneficios también son enums; los desconocidos requieren revisión. Identificadores y condiciones permanecen como strings. [Criterios de tipado](docs/engineering-decisions.md). No se deduce internacionalidad de códigos de aeropuertos: R2 permanece sin acreditar a ≥240 min. Compensación R4 requiere aprobación; nunca se aplica una política DELAY sin retraso confirmado.

## Controles, alternativas y próximos pasos

La evaluación es determinista y no utiliza modelos de IA. Los datos que no se pueden confirmar y las autorizaciones pendientes se derivan a atención humana.

Los cambios de reserva, la creación de casos y las notificaciones quedan fuera del alcance actual. Las alternativas de viaje del ejemplo tampoco se ofrecen: O1 no acredita elegibilidad con 160 minutos de retraso; O2 tiene una plaza para dos personas; O3 tiene baja confianza y no dispone de una política de reembolso verificada. Los requisitos para incorporar estas operaciones se describen en la especificación de experiencia.

La demo no tiene autenticación y escucha en `127.0.0.1`. Para exponerla sería necesario añadir identidad, autorización sobre la reserva, límites de consultas y auditoría. Los fixtures nuevos, las respuestas y los logs omiten nombres, contactos y documentos.

Las pruebas cubren umbrales, menor, compensación, contradicciones, fuentes antiguas/futuras, fallos, reglas desconocidas, vigencia y contrato HTTP con JSON reales. Uso de IA y decisiones registradas: [AI_USAGE](AI_USAGE.md). Medir en piloto exactitud de beneficios, respuestas bloqueadas por calidad de datos, latencia p95 y contactos evitados; aún no hay mediciones de producción. Límites: solo retraso, una reserva mock, sin confirmación de conexión, internacionalidad, inventario, acciones o autenticación. Siguiente paso: validar supuestos con Operaciones y contratos con fuentes reales.
