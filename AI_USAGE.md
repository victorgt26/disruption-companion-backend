# Uso de IA en el desarrollo

Utilicé Codex como apoyo para analizar el problema, programar, preparar pruebas y revisar la documentación. También lo usé para generar fixtures y diagramas. La aplicación no utiliza IA en ejecución.

## Criterios de implementación

El desarrollo siguió las restricciones del proyecto: Java y Spring Boot, arquitectura hexagonal, consulta de solo lectura y lógica de negocio en application. Se mantuvieron los contratos JSON al introducir enums y se trataron los valores externos desconocidos como datos que requieren revisión.

La evaluación es determinista. La ventana de frescura de 30 minutos y la precedencia de OPS_CORE son supuestos de la demo que deben validarse con Operaciones. No se infiere una conexión perdida ni se concede un cambio gratuito con 160 minutos de retraso. Las compensaciones requieren aprobación humana.

## Comprobaciones

Durante el desarrollo se utilizaron Gradle, JUnit, Mockito y MockMvc, además de llamadas HTTP para comprobar el arranque, Swagger y los ejemplos. Se consultaron fuentes oficiales para revisar compatibilidad y se usaron google-java-format y el parser de Mermaid. Los resultados registrados y sus límites están en [validation.md](docs/validation.md).

Las pruebas permitieron corregir el tratamiento del booleano opcional, el mapeo de `_meta` y una comparación de códigos HTTP que omitía ejemplos en OpenAPI. Los snapshots se contrastan con las respuestas del backend.

## Datos y límites

Los fixtures nuevos omiten nombres y contactos. Una posible integración futura con IA tendría que trabajar con hechos mínimos, desidentificados y validados, sin credenciales, secretos ni conversaciones identificables. Las decisiones de elegibilidad y las autorizaciones seguirían sujetas a reglas y revisión humana.
