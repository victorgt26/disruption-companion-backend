# Instrucciones de desarrollo
Proyecto: Disruption Companion, vertical slice Backend/Integración de consulta de interrupciones.

- Stack: Java 25 (toolchain), Spring Boot 4 / Framework 7, Gradle Kotlin DSL y Wrapper, MVC, Jakarta Validation, Jackson, JUnit y Mockito.
- Código, campos API, comentarios y logs en inglés; documentación técnica en español.
- Arquitectura hexagonal: `com.disruptioncompanion.domain` contiene exclusivamente POJOs, records y enums en `model` y `enums`, sin lógica ni frameworks.
- `application` contiene toda la lógica de negocio, puertos (`port/in`, `port/out`), casos de uso (`usecase`) y servicios auxiliares (`service`). Solo depende de domain y Java.
- `infrastructure` contiene adaptadores, configuración y manejo HTTP de errores. Controllers llaman a puertos de entrada; casos de uso utilizan puertos de salida; adaptadores de salida implementan dichos puertos. No debe existir lógica de negocio en controllers ni adaptadores.
- Inyección por constructor; composición con beans Spring en infraestructura. No añadir abstracciones sin responsabilidad concreta.
- Probar decisiones de application con JUnit y Mockito sobre puertos de salida; reloj fijo, límites, fuentes contradictorias, datos parciales y fallos. Verificar contrato HTTP y privacidad.
- Comandos: `./gradlew clean test`, `./gradlew bootRun`. No inventar resultados de pruebas ni validaciones.
- Mantener README (máximo 2 páginas equivalentes), AI_USAGE (máximo 1), supuestos y especificación de experiencia actualizados. Mermaid debe reflejar componentes y caminos reales; actualizar los tres diagramas cuando cambien.
- No incluir datos personales reales, logs de payloads, secretos ni datos sensibles en errores. Solo lectura, mínimo privilegio, bind local para la demo.
- PDF de reference es fuente principal. Documentar decisiones ante ambigüedades; no inventar políticas ni garantizar conexiones, precios o compensaciones.
- Sin frontend, base de datos, cloud, mensajería ni IA en runtime. No implementar escrituras de alto impacto sin ampliar explícitamente el alcance y sus controles.
- No realizar commits, push, despliegues ni entrega del challenge sin autorización del usuario.
