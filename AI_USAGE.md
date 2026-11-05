# Uso de IA y decisiones de desarrollo

## Herramientas utilizadas

Utilicé Codex como apoyo para analizar el desafío, generar parte del código, preparar pruebas, mocks, documentación y diagramas.

Antes de comenzar definí el stack tecnológico (Java 25, Spring Boot 4 y Gradle) y la arquitectura hexagonal que quería utilizar.

La aplicación no incorpora IA en tiempo de ejecución. Preferí mantener las decisiones sobre políticas y beneficios mediante reglas determinísticas, que puedan verificarse y auditarse.

## Decisiones y correcciones

Durante el desarrollo revisé las propuestas de Codex y fui realizando ajustes, principalmente para mejorar la arquitectura, la legibilidad y la mantenibilidad.

Estas fueron algunas de mis intervenciones:

- **Arquitectura:** inicialmente Codex había colocado servicios y cierta lógica dentro de `domain`. Pedí moverlos a `application`, dejando el dominio únicamente para POJOs, records y enums.
- **Casos de uso:** solicité utilizar los sufijos `UseCase` y `UseCaseImpl` para diferenciar interfaces e implementaciones. También pedí estandarizar el método `execute` para hacer más clara su utilización.
- **Enums:** pedí reemplazar Strings hardcodeados por enums cuando representan estados o categorías conocidas. Los identificadores y valores abiertos se mantienen como Strings.
- **Diagramas:** solicité simplificarlos porque inicialmente incluían demasiados nombres de clases y detalles que no aportaban a la explicación general.
- **API:** decidí mantener `POST /api/assessments` para recibir los parámetros de evaluación mediante un JSON. Aunque GET sería válido para una consulta simple, preferí POST por la estructura del request y su posible evolución.

Acepté el uso de adaptadores mock, pruebas automatizadas y diagramas generados con ayuda de Codex, revisando su coherencia con el alcance del proyecto.

No incorporé Strategy ni un LLM en runtime. Para este primer alcance consideré que no aportaban suficiente valor como para justificar la complejidad adicional.

## Validación

Durante el desarrollo se utilizaron Gradle, JUnit, Mockito, MockMvc y pruebas HTTP.

También se corrigieron problemas relacionados con un booleano opcional, el mapeo de `_meta` y la documentación de respuestas HTTP en OpenAPI.

Los resultados registrados y las validaciones pendientes están documentados en [validation.md](docs/validation.md).

## Seguridad

No compartiría con un modelo credenciales, tokens, documentos de identidad ni información personal de pasajeros.

Si posteriormente se incorpora IA, la utilizaría para interpretar consultas o generar explicaciones basadas en información previamente validada. Las decisiones de elegibilidad y las acciones de alto impacto deberían continuar bajo reglas y controles humanos.
