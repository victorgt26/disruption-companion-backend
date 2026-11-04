# Instrucciones de desarrollo
Proyecto: Disruption Companion, vertical slice Backend/Integración de consulta de interrupciones.

- Stack: Java 25 (toolchain), Spring Boot 4 / Framework 7, Gradle Kotlin DSL y Wrapper, MVC, Jakarta Validation, Jackson, Lombok (solo compilación), springdoc/Swagger UI, JUnit y Mockito.
- Código, campos API, comentarios y logs en inglés; documentación técnica en español.
- Arquitectura hexagonal: `com.disruptioncompanion.domain` contiene exclusivamente POJOs, records y enums en `model` y `enums`, sin lógica ni frameworks.
- `application` contiene toda la lógica de negocio, puertos (`port/in`, `port/out`), casos de uso (`usecase`) y servicios auxiliares (`service`). Solo depende de domain y Java en ejecución; Lombok se permite como procesador de anotaciones de compilación, sin dependencia de Spring.
- `infrastructure` contiene adaptadores, configuración y manejo HTTP de errores. Controllers llaman a puertos de entrada; casos de uso utilizan puertos de salida; adaptadores de salida implementan dichos puertos. No debe existir lógica de negocio en controllers ni adaptadores.
- Inyección por constructor con `@RequiredArgsConstructor` de Lombok en clases con dependencias finales; composición con beans Spring en infraestructura. Mantener records sin anotaciones redundantes. No añadir abstracciones sin responsabilidad concreta.
- Puertos de entrada: `NombreUseCase`, un único método público `execute`; implementación `NombreUseCaseImpl` en `application/usecase`, con helpers privados.
- Imports explícitos, incluidos los estáticos: nunca usar asteriscos ni conservar imports sin uso. Mantener formato uniforme.
- Usar enums para códigos finitos controlados por el backend; conservar strings de contratos externos cuando sea necesario tolerar valores desconocidos. Mantener estable su representación JSON.
- Casos de uso coordinan; servicios auxiliares expresan decisiones concretas con métodos cortos y nombres claros. Usar streams para transformaciones y predicados, sin efectos laterales ocultos ni cadenas que dificulten leer el flujo.
- Probar decisiones de application con JUnit y Mockito sobre puertos de salida; reloj fijo, límites, fuentes contradictorias, datos parciales y fallos. Verificar contrato HTTP y privacidad.
- Comandos: `./gradlew clean test`, `./gradlew bootRun`. No inventar resultados de pruebas ni validaciones.
- Regla permanente para todo cambio actual y futuro: revisar y actualizar los `.md` afectados por cambios de código, contratos, configuración, comandos o decisiones antes de dar el trabajo por terminado. No inventar una actualización si el contenido sigue vigente.
- Mantener README (máximo 2 páginas equivalentes), AI_USAGE (máximo 1), supuestos y especificación de experiencia actualizados. Mermaid debe reflejar componentes y caminos reales; actualizar los tres diagramas cuando cambien.
- No incluir datos personales reales, logs de payloads, secretos ni datos sensibles en errores. Solo lectura, mínimo privilegio, bind local para la demo.
- PDF de reference es fuente principal. Documentar decisiones ante ambigüedades; no inventar políticas ni garantizar conexiones, precios o compensaciones.
- Sin frontend, base de datos, cloud, mensajería ni IA en runtime. No implementar escrituras de alto impacto sin ampliar explícitamente el alcance y sus controles.
- No realizar commits, push, despliegues ni entrega del challenge sin autorización del usuario.
