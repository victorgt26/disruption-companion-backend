# Disruption Companion Backend

**Track Backend/Integración.** Consulta de solo lectura que combina reserva, eventos y reglas verificables para reducir incertidumbre sin prometer beneficios ni modificar reservas sin control. Se prioriza este track para demostrar decisiones reproducibles y trazabilidad. Fuente: [challenge](reference/Disruption%20Companion.pdf), leído completo (7 páginas). Alcance acotado al retraso del caso base; experiencia complementaria [especificada](docs/passenger-experience.md), sin frontend.

## Ejecución y validación

Requiere JDK 25 y acceso a Maven Central/Gradle en la primera ejecución; no requiere servicios externos.

```sh
./gradlew clean test
./gradlew bootRun
curl -sS http://127.0.0.1:8080/api/assessments \
  -H 'Content-Type: application/json' \
  -d '{"bookingId":"ABC123","compensationRequested":false}'
```

El perfil predeterminado `demo` fija `Clock` en `2026-09-15T11:03:00Z`. Así, la demostración histórica no caduca según el día de ejecución. `./gradlew bootRun --args='--spring.profiles.active=live'` usa el reloj real: los mismos mocks históricos producirán revisión por datos antiguos. Los tests fijan el tiempo. Reporte: `build/reports/tests/test/index.html`.

Java 25 con toolchains; Spring Boot 4.1.2 gestiona Framework 7, Jackson 3, JUnit y Mockito. Gradle Wrapper 9.3.1. Compatibilidad: [Spring](https://docs.spring.io/spring-boot/system-requirements.html), [Gradle](https://docs.gradle.org/current/userguide/compatibility.html). MVC, Jakarta Validation, sin DB ni dependencias de servicios remotos.

## Decisiones y resultado

- Domain: records y enum, sin lógica. Application: puerto `AssessDisruption`, caso de uso, evaluador y tres puertos de lectura. Infrastructure: REST, mapper, JSON y composición Spring. [Arquitectura](docs/diagrams/hexagonal-architecture.md), [flujo](docs/diagrams/disruption-assessment-flow.md), [secuencia](docs/diagrams/assessment-sequence.md).
- ABC123: retraso verificado de **160 min**, salida informada 17:10 en S1. Se ignora el estado antiguo/degradado de LEGACY_MIRROR. R1 exige ≥180: **no se ofrece cambio gratuito por retraso**. No se afirma conexión perdida: faltan llegada actualizada, zonas horarias y tiempo mínimo de conexión. R3 queda sin acreditar. R5 identifica atención prioritaria **sujeta a aprobación humana** por el menor.
- Resultado: `REVIEW_REQUIRED`, motivos de reserva parcial, conexión desconocida y aprobación; siguiente paso `CONTACT_HUMAN_SUPPORT`. No significa que se haya creado un caso. `entitlements` son coincidencias de reglas, sujetas a revisión cuando el estado lo indica; no son autorizaciones de ejecución. Cada coincidencia incluye versión y regla; cada evento incluye fuente, timestamp y tratamiento.
- Supuestos deliberados: cualquier retraso positivo es relevante; solo OPS_CORE confirma retraso/estado; se toma el último evento válido por segmento, empate contradictorio exige revisión. Otros eventos son contexto y no se comunican como estado. Frescura configurable `assessment.max-source-age=PT30M`, límite inclusivo, sin timestamps futuros ni posteriores a la recuperación. La ventana y jerarquía son decisiones de demo para validar con Operaciones, no reglas del PDF.
- La reserva parcial/antigua y los fallos de lectura requieren revisión; no impiden comunicar hechos operacionales independientemente verificados. Políticas: fecha efectiva, versión no declarada reemplazada y recuperación no futura; no se inventa TTL de política. Solo condiciones conocidas, sin evaluar código o expresiones remotas. No se deduce internacionalidad de códigos de aeropuertos: R2 permanece sin acreditar a ≥240 min. Compensación R4 requiere aprobación; nunca se aplica una política DELAY sin retraso confirmado.

## Controles, alternativas y próximos pasos

Todo el runtime es **determinístico**. La construcción fue **AI-assisted**; no hay ejecución agentic en producto. IA podría reformular preguntas ambiguas, pero no aporta suficiente valor aquí frente a costo, latencia y riesgo de alucinación; ninguna llamada a modelos ni costo de inferencia por consulta. Operaciones decide hechos desconocidos y autorizaciones (**human-controlled**). El diseño de consulta compleja está en la especificación complementaria.

Se descartaron escrituras y opciones candidatas: O1 no demuestra elegibilidad con 160 min; O2 tiene una plaza para dos personas; O3 tiene baja confianza y política de reembolso no recuperada. No se promete ninguna. Rebooking, casos y notificaciones quedan fuera; sus controles futuros se describen en la especificación. Sin autenticación en esta demo local, bind 127.0.0.1; antes de exponer: identidad, autorización sobre reserva, rate limiting y auditoría. No se incluyen nombres, contactos ni documentos en fixtures nuevos, respuestas o logs. El PDF original sí contiene ejemplos personales: no distribuirlo como payload ni cargarlo en un modelo de runtime.

Las pruebas cubren umbrales, menor, compensación, contradicciones, fuentes antiguas/futuras, fallos, reglas desconocidas, vigencia y contrato HTTP con JSON reales. Validación y decisiones humanas pendientes: [AI_USAGE](AI_USAGE.md). Medir en piloto exactitud de beneficios, respuestas bloqueadas por calidad de datos, latencia p95 y contactos evitados; aún no hay mediciones de producción. Límites: solo retraso, una reserva mock, sin confirmación de conexión, internacionalidad, inventario, acciones o autenticación. Siguiente paso: validar supuestos con Operaciones y contratos con fuentes reales.
