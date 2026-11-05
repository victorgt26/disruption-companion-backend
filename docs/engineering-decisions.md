## POST vs GET para la evaluación

Elegí `POST /api/assessments` porque el servicio recibe un contexto de evaluación mediante un JSON, en lugar de consultar únicamente un recurso por su identificador.

Actualmente la operación recibe `bookingId` y `compensationRequested`. Aunque no realiza escrituras, POST permite ampliar los parámetros de evaluación sin depender de múltiples query parameters.

Consideré GET como alternativa. Tiene una semántica más clara para consultas y facilita el uso de mecanismos de caché HTTP. Sin embargo, para este caso preferí mantener un request estructurado.

El endpoint devuelve `200 OK`, no crea un recurso persistente y no ejecuta cambios de reserva. Se utiliza `Cache-Control: no-store` por tratarse de información relacionada con un viaje y una reserva.

Si en el futuro el contrato se mantiene simple, GET sería una opción que reconsideraría.

## Selección de eventos operacionales

Actualmente se priorizan los eventos de `OPS_CORE` y se toma el último evento válido de tipo `DELAY` o `STATUS` para cada segmento, teniendo en cuenta la frescura de los datos y las fuentes degradadas.

Esta decisión supone que los eventos de `DELAY` y `STATUS` pueden representar actualizaciones del mismo estado operacional vigente.

El PDF no garantiza expresamente esa semántica. Por ejemplo, si después de un `DELAY` recibimos un `STATUS: ON_TIME`, necesitamos saber si ese estado reemplaza el retraso anterior o si ambos eventos describen aspectos distintos.

Antes de integrar fuentes reales, confirmaría este comportamiento con el equipo de Operaciones y el responsable del contrato de eventos.

Mientras no exista una definición clara, es preferible marcar la información como incierta cuando no pueda resolverse la contradicción, en lugar de asumir que el último evento siempre tiene razón.

También sería necesario definir qué hacer con correcciones, eventos recibidos fuera de orden y actualizaciones que solo modifican un atributo del vuelo.

## Patrones y extensibilidad

La arquitectura utiliza Ports and Adapters para separar la lógica de negocio de los sistemas externos.

La implementación de los casos de uso mantiene una estructura sencilla: un puerto de entrada, una implementación que coordina la operación y puertos de salida que abstraen las lecturas.

Consideré utilizar Strategy para resolver diferentes tipos de interrupción, como retrasos, cancelaciones o cambios de itinerario. Por ahora no lo incorporé porque el alcance implementado se limita a retrasos y no existen estrategias alternativas reales.

Si se incorporan nuevos escenarios, podría definir una interfaz `DisruptionAssessmentStrategy` y una implementación para cada tipo de interrupción, seleccionada mediante un enum. Esto permitiría agregar comportamientos sin concentrar todas las condiciones en un único caso de uso.

Preferí mantener la implementación actual y evitar abstracciones que todavía no son necesarias.
