# Decisiones de ingeniería

Este documento reúne algunas decisiones tomadas durante la implementación del vertical slice y las alternativas que consideré. La intención es dejar claros los criterios utilizados y las limitaciones que todavía existen.

## 1. Uso de enums y Strings

Preferí utilizar enums para los valores que representan un conjunto conocido de estados o categorías y que participan en decisiones de negocio.

Por ejemplo, tipos de eventos, fuentes operacionales, estados de evaluación, tipos de pasajeros y beneficios.

Inicialmente había valores representados mediante Strings. Pedí cambiarlos por enums para evitar comparaciones con textos hardcodeados, reducir errores de escritura y facilitar el mantenimiento.

Sin embargo, no todos los valores externos deberían convertirse en enums.

| Tipo de dato | Representación | Motivo |
|---|---|---|
| Estados y categorías de negocio conocidas | Enum | Conjunto finito de valores |
| Tipos de eventos y beneficios | Enum | Participan en decisiones de negocio |
| Identificadores de reservas, segmentos y eventos | String | Valores abiertos |
| Versiones de políticas y referencias externas | String | Pueden cambiar independientemente del código |
| Condiciones de políticas externas | String | Se conservan como parte del contrato original |
| Mensajes y descripciones | String | Texto libre |

También consideré que los sistemas externos pueden incorporar valores nuevos. Por eso, el mapeo de valores desconocidos debe tratarse de forma controlada, sin asumir automáticamente que representan una condición válida.

El objetivo no es utilizar enums en todas partes, sino donde realmente aporten seguridad y claridad.

## 2. Arquitectura hexagonal

Elegí arquitectura hexagonal para separar la lógica de negocio de las tecnologías utilizadas para exponer la API y consumir los sistemas externos.

La organización quedó dividida en tres capas:

- **Domain:** contiene exclusivamente POJOs, records y enums.
- **Application:** contiene los puertos, los casos de uso y toda la lógica de negocio.
- **Infrastructure:** contiene los adaptadores REST, los mocks y la configuración de Spring.

Durante el desarrollo corregí una propuesta inicial que incluía servicios y lógica dentro de `domain`. Preferí trasladarlos a `application` para mantener el dominio como un conjunto de modelos de datos.

Esta distribución es una convención elegida para el proyecto, no una regla universal de la arquitectura hexagonal.

También pedí utilizar las convenciones `UseCase` para las interfaces, `UseCaseImpl` para sus implementaciones y `execute` como método de entrada. Esto permite identificar rápidamente los casos de uso y mejora la lectura del código.

No utilicé módulos Gradle independientes por capa. Para este alcance, la separación mediante paquetes y reglas de dependencia resulta suficiente.

## 3. POST vs GET para la evaluación

Elegí `POST /api/assessments` porque la operación recibe un contexto estructurado mediante JSON, actualmente compuesto por `bookingId` y `compensationRequested`.

Aunque la evaluación es de solo lectura, POST permite ampliar el contrato sin depender de múltiples query parameters.

GET también sería una alternativa válida y tiene ventajas: expresa directamente una consulta, es seguro e idempotente según la semántica HTTP y permite aprovechar mecanismos estándar de caché.

Preferí POST por el contrato de entrada. La operación devuelve `200 OK`, no crea recursos persistentes y no ejecuta cambios de reserva.

La respuesta utiliza `Cache-Control: no-store` para evitar el almacenamiento de información relacionada con la reserva.

Si el contrato se mantiene sencillo, reconsideraría GET. No considero que POST sea la única alternativa correcta.

## 4. Confiabilidad de eventos operacionales

La aplicación evalúa la fuente de los eventos, sus timestamps, la frescura de la información y el estado de degradación de las fuentes.

Para el escenario base se considera a `OPS_CORE` como fuente autoritativa. El evento anterior de `LEGACY_MIRROR` no debe reemplazar automáticamente información operacional más confiable.

La implementación actual selecciona el último evento válido de tipo `DELAY` o `STATUS` para determinar el estado operacional de un segmento.

Esta decisión introduce un supuesto importante: que ambos tipos de eventos pueden representar actualizaciones del mismo estado vigente.

El documento del desafío no define esa semántica con suficiente detalle.

Por ejemplo, si después de un evento `DELAY` llega un `STATUS: ON_TIME`, necesitamos saber si realmente cancela el retraso anterior o si informa un atributo diferente del vuelo.

Antes de integrar sistemas reales, confirmaría ese contrato con el equipo de Operaciones.

También habría que definir cómo tratar eventos fuera de orden, correcciones y actualizaciones parciales. Si una contradicción no puede resolverse con evidencia suficiente, la respuesta debería indicar incertidumbre en lugar de confirmar un estado incorrecto.

Los escenarios que requieren validación adicional están documentados en [validation.md](validation.md).

## 5. Evaluación determinística e IA

Decidí mantener la evaluación de políticas y beneficios mediante reglas determinísticas y versionadas.

La IA puede aportar valor al interpretar preguntas del pasajero o generar explicaciones más naturales, pero no debería decidir por sí sola una compensación o autorizar un cambio de reserva.

Para esta primera versión no incorporé un LLM en runtime.

Esto reduce dependencias, latencia, coste y riesgo de respuestas incorrectas. Además, facilita reproducir y auditar las decisiones.

## 6. Patrones y extensibilidad

Actualmente la solución utiliza Ports and Adapters, inversión de dependencias e inyección de dependencias.

Consideré incorporar Strategy para separar la evaluación de distintos tipos de interrupciones, por ejemplo retrasos, cancelaciones o modificaciones de itinerario.

Por ahora no lo implementé porque el alcance funcional desarrollado se concentra en retrasos y no existen varios algoritmos que justifiquen esa abstracción.

Si el producto incorpora nuevos escenarios, evaluaría una interfaz `DisruptionAssessmentStrategy`, con una implementación especializada para cada tipo de interrupción.

También podría considerarse Specification si las políticas crecen y requieren combinar condiciones reutilizables.

Preferí mantener la solución actual y agregar estos patrones cuando exista una necesidad concreta, evitando complejidad innecesaria.

## 7. Acciones de alto impacto

La evaluación no ejecuta rebookings, compensaciones ni notificaciones.

Las opciones recuperadas de sistemas externos son candidatas y no representan automáticamente derechos confirmados.

Para incorporar operaciones de escritura sería necesario definir autorización sobre la reserva, confirmación del pasajero, aprobaciones humanas, idempotencia y auditoría.

También habría que contemplar respuestas de resultado indeterminado. Por ejemplo, ante un timeout al crear una solicitud de rebooking no sería seguro asumir que la operación falló y repetirla sin reconciliar primero su estado.
