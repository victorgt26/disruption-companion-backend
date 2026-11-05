# Uso de IA en el desarrollo

## Herramientas utilizadas

Utilicé Codex como apoyo para analizar el desafío, generar código, preparar pruebas, mocks y documentación. También lo usé para generar los diagramas de arquitectura y de flujo.

La aplicación no utiliza IA en tiempo de ejecución. La evaluación de políticas y beneficios se realiza mediante reglas determinísticas.

## Decisiones y correcciones

Antes de comenzar definí el stack tecnológico: Java 25, Spring Boot 4 y Gradle. También decidí utilizar arquitectura hexagonal para separar la lógica de negocio de las integraciones.

Durante el desarrollo fui revisando y ajustando algunas propuestas de Codex:

- **Arquitectura hexagonal:** inicialmente se había colocado cierta lógica y servicios en `domain`. Pedí moverlos a `application`, dejando `domain` únicamente para POJOs, records y enums.
- **Convenciones de nombres:** solicité utilizar los sufijos `UseCase` y `UseCaseImpl` para identificar claramente los contratos y sus implementaciones. También unifiqué la ejecución de los casos de uso mediante el método `execute`.
- **Uso de enums:** pedí reemplazar Strings hardcodeados por enums cuando representan estados o categorías conocidas. Preferí mantener los Strings para identificadores y valores abiertos.
- **Diagramas:** solicité simplificarlos, eliminando detalles de clases que no aportaban a la explicación de la arquitectura.
- **Diseño de la API:** elegí mantener `POST /api/assessments` para recibir un contexto de evaluación estructurado, aunque la operación sea de solo lectura. GET también sería válido para una consulta más sencilla.

Estas correcciones responden principalmente a criterios de legibilidad, mantenibilidad y separación de responsabilidades.

## Validación

Durante el desarrollo se utilizaron Gradle, JUnit, Mockito, MockMvc y pruebas HTTP. También se revisaron las dependencias y la documentación Mermaid.

Las pruebas permitieron corregir problemas con el booleano opcional, el mapeo de `_meta` y algunos ejemplos de respuestas HTTP en OpenAPI.

Los resultados registrados y sus limitaciones están en [validation.md](docs/validation.md).

## Seguridad y límites

No compartiría con un modelo credenciales, secretos, documentos de identidad ni información personal de pasajeros.

Si se incorpora IA en el futuro, la utilizaría para interpretar consultas o generar explicaciones basadas en información validada, pero no para decidir automáticamente beneficios, compensaciones o cambios de reserva.
