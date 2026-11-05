# Disruption Companion Backend

Servicio backend para evaluar interrupciones de viajes aéreos y ayudar al pasajero a entender qué ocurrió y cuáles son sus alternativas.

La solución combina información de reservas, eventos operacionales y políticas para generar una evaluación trazable. No modifica reservas ni ejecuta compensaciones.

**Track:** Backend / Integration.

## Arquitectura y alcance

Implementé una arquitectura hexagonal con Java 25, Spring Boot 4.1.2 y Gradle 9.3.1, separando las responsabilidades en tres capas:

- **Domain:** modelos y enumeraciones, sin lógica de negocio.
- **Application:** casos de uso, puertos de entrada y salida, evaluación de políticas y lógica de negocio.
- **Infrastructure:** controladores REST, adaptadores mock, mapeos y configuración de Spring.

Esta separación permite probar las reglas sin levantar Spring Boot y reemplazar las integraciones mock por implementaciones reales sin modificar los casos de uso.

La primera versión cubre el escenario de retrasos utilizando contratos simulados en JSON. No implementé frontend ni operaciones de escritura, porque el objetivo es validar primero las decisiones de negocio y su confiabilidad.

[Arquitectura](docs/diagrams/hexagonal-architecture.md) · [Flujo de evaluación](docs/diagrams/disruption-assessment-flow.md) · [Diagrama de secuencia](docs/diagrams/assessment-sequence.md) · [Experiencia del pasajero](docs/passenger-experience.md)

## Decisiones técnicas

**POST en lugar de GET.** Utilicé `POST /api/assessments` porque la operación recibe un contexto de evaluación estructurado, que incluye la reserva y parámetros como `compensationRequested`. Aunque actualmente es de solo lectura, el contrato permite incorporar nuevos criterios sin depender de múltiples query parameters.

GET también sería válido para una consulta sencilla y facilitaría aprovechar el caché HTTP. Preferí POST por la estructura de entrada. La operación devuelve `200 OK` y no crea ningún recurso persistente.

**Evaluación determinística.** No incorporé IA en runtime porque las decisiones sobre beneficios deben ser verificables. La IA podría utilizarse posteriormente para interpretar consultas o generar explicaciones a partir de información validada, pero no para autorizar cambios o compensaciones.

**Frescura y confiabilidad.** Se considera la fuente, el timestamp y su estado de degradación. Para la demo asumí una ventana configurable de 30 minutos y OPS_CORE como fuente autoritativa. Estos criterios necesitan validación con el equipo de Operaciones antes de utilizar fuentes reales.

**Manejo de incertidumbre.** Cuando falta información crítica o existen contradicciones que no pueden resolverse, la evaluación indica que se requiere verificación. No se inventan estados, beneficios ni autorizaciones.

## Resultado del caso ABC123

La reserva presenta un retraso operacional de **160 minutos** para el vuelo LA500. La información anterior de LEGACY_MIRROR no reemplaza el evento válido de OPS_CORE.

La evaluación determina:

- **R1:** no aplica, porque requiere al menos 180 minutos.
- **R3:** no se confirma, porque faltan datos para determinar si la conexión se perderá.
- **R5:** identifica atención prioritaria por un pasajero menor, sujeta a aprobación humana.

El resultado es `REVIEW_REQUIRED` y la siguiente acción recomendada es `CONTACT_HUMAN_SUPPORT`.

Esto no significa que se haya creado un caso de atención ni que exista autorización para cambiar la reserva.

Las opciones alternativas tampoco se ofrecen automáticamente: O1 no tiene elegibilidad verificada, O2 dispone de un asiento para dos pasajeros y O3 requiere validar la política de reembolso.

## Ejecución y pruebas

Requiere JDK 25. La primera compilación necesita acceso a los repositorios de dependencias.

```bash
./gradlew clean test
./gradlew bootRun
```

Ejemplo de consulta:

```bash
curl -X POST http://127.0.0.1:8080/api/assessments \
  -H "Content-Type: application/json" \
  -d '{"bookingId":"ABC123","compensationRequested":false}'
```

También se puede utilizar [Swagger UI](http://127.0.0.1:8080/swagger-ui.html) o consultar los [ejemplos de API](docs/api-examples.md).

El perfil predeterminado `demo` utiliza un reloj fijo (`2026-09-15T11:03:00Z`) para reproducir el caso histórico. El perfil `live` utiliza el reloj real, pero **continúa trabajando con mocks**; no representa una integración productiva.

Las pruebas cubren reglas, umbrales, fuentes contradictorias, datos incompletos, políticas, errores y contratos HTTP. Los resultados están documentados en [validation.md](docs/validation.md).

## Seguridad, limitaciones y próximos pasos

La API no expone datos personales en sus respuestas. La demo no implementa autenticación y está configurada para uso local. Antes de publicarla sería necesario agregar autorización sobre la reserva, control de acceso, límites de consultas y auditoría.

Las operaciones de rebooking, creación de casos y notificaciones quedan fuera del alcance. Para incorporarlas consideraría confirmación explícita, idempotencia, aprobaciones humanas y reconciliación ante respuestas indeterminadas.

La solución se limita a retrasos y datos simulados. Queda pendiente validar las políticas y criterios de frescura con Operaciones, incorporar fuentes reales y confirmar las reglas para conexiones, inventario y compensaciones.

Para evaluar una futura versión mediría exactitud de recomendaciones, latencia p95, consultas derivadas a revisión humana y reducción de contactos evitables.

[Uso de IA y decisiones](AI_USAGE.md) · [Validaciones](docs/validation.md) · [Decisiones de ingeniería](docs/engineering-decisions.md)
