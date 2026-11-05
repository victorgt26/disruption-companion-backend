# Uso de IA durante el desarrollo

## Herramientas utilizadas

Para este desafío utilicé principalmente Codex. Me apoyé en la herramienta para generar código, pruebas, mocks, documentación y diagramas, además de revisar algunos aspectos de la implementación.

Antes de comenzar ya tenía definido el stack que quería utilizar: Java 25, Spring Boot 4, Gradle y arquitectura hexagonal.

Decidí no incorporar IA en tiempo de ejecución. Para este caso me pareció más adecuado resolver las políticas y beneficios mediante reglas determinísticas, donde pueda verificar exactamente cómo se llegó a cada resultado.

## Decisiones y ajustes

Durante el desarrollo fui revisando lo que generaba Codex y le pedí varios cambios. Los principales fueron:

- **Arquitectura:** inicialmente había colocado algunos servicios y lógica de negocio dentro de `domain`. Le pedí mover todo eso a `application`, dejando el dominio únicamente para modelos, records y enums. Es la forma en que prefiero organizar este tipo de proyectos.
- **Casos de uso:** pedí utilizar los sufijos `UseCase` para las interfaces y `UseCaseImpl` para las implementaciones. También unifiqué el método de entrada como `execute`. Me parece que así el código es más fácil de seguir.
- **Enums:** encontré varios valores manejados como Strings hardcodeados y pedí reemplazarlos por enums cuando se trataba de estados o categorías conocidas. Los identificadores y valores que pueden variar externamente los mantuve como Strings.
- **Diagramas:** las primeras versiones tenían demasiados detalles de clases e implementaciones. Pedí simplificarlos para mostrar mejor la arquitectura y el flujo general.
- **API:** revisé la elección de `POST /api/assessments`, aunque sea una operación de consulta. Decidí mantenerlo porque recibe un JSON con los parámetros de evaluación y permite ampliar el contrato. Reconozco que GET también sería válido para una consulta más sencilla.

Mantuve las propuestas de utilizar adaptadores mock y pruebas automatizadas. También preferí no incorporar patrones adicionales, como Strategy, porque por ahora solo estamos resolviendo un tipo de interrupción.

## Validación

Se utilizaron Gradle, JUnit, Mockito, MockMvc y pruebas HTTP para comprobar el comportamiento del servicio.

Durante las validaciones también se corrigieron algunos detalles relacionados con un booleano opcional, el mapeo de `_meta` y las respuestas documentadas en OpenAPI.

Los resultados de las pruebas y los escenarios pendientes están detallados en [validation.md](docs/validation.md).

## Seguridad y límites

No compartiría con un modelo credenciales, tokens, documentos de identidad ni información personal de pasajeros.

Si más adelante se incorpora IA al producto, la utilizaría principalmente para interpretar consultas o generar explicaciones a partir de información previamente validada. Las decisiones sobre beneficios, compensaciones o cambios de reserva deberían seguir teniendo reglas claras y controles humanos.
