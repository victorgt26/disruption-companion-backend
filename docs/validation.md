# Validación

Este documento recoge las comprobaciones registradas durante el desarrollo. No sustituye la ejecución de las pruebas sobre la versión que se vaya a utilizar.

| Comprobación ejecutada | Resultado observado |
|---|---|
| `./gradlew clean test` | BUILD SUCCESSFUL; 68 pruebas, 0 fallos, 0 errores, 0 omitidas |
| Suite de application, JUnit y puertos Mockito | 22 ejecuciones, incluidos los cuatro parámetros de umbral |
| Suite HTTP y contratos externos, contexto Spring y JSON reales | 7 pruebas de API original, 31 de catálogo/demo, 1 de aislamiento de perfil, 1 de fallo inesperado y 6 de enums externos desconocidos |
| `./gradlew bootRun` | Arranque correcto: Java 25.0.3, Boot 4.1.2, Tomcat, 127.0.0.1:8080, perfil demo |
| POST HTTP real con curl y ABC123 | REVIEW_REQUIRED; S1 160 min / 17:10; R5 con aprobación; ningún REBOOK_NO_FEE; tres motivos de revisión esperados |
| `./gradlew dependencyInsight --dependency spring-core --configuration runtimeClasspath` | Spring Framework 7.0.10 resuelto por Boot 4.1.2 |
| HTTP real de `/swagger-ui/index.html` y `/v3/api-docs` | HTML Swagger UI y contrato OpenAPI recibidos correctamente |
| Inspección de imports con rg y lectura del código | Domain sin dependencias de application/infrastructure/frameworks; Application sin Spring/Jackson/Jakarta ni infrastructure |
| Inspección de contratos, respuesta real y pruebas de privacidad | Sin nombres, contactos, documentos ni localizador en respuesta; errores genéricos; no-store verificado en MockMvc |
| Revisión de documentación y diagramas frente al código | Tres lecturas, evaluación determinística y cero escrituras; límites descritos |


Para repetir la suite:

```sh
./gradlew clean test
```

Los resultados JUnit se generan en `build/test-results/test/` y el informe HTML en `build/reports/tests/test/index.html`. Son archivos locales ignorados por Git.

## Cobertura de las pruebas

- Umbrales de 179, 180, 239 y 240 minutos; límite inclusivo de frescura; eventos futuros y antiguos; precedencia de fuentes y eventos contradictorios.
- Datos de reserva parciales o ausentes, pasajeros desconocidos y fallos de lectura. Los hechos verificados se conservan si falla la consulta posterior de políticas.
- Políticas futuras, reemplazadas o vacías; reglas y categorías externas desconocidas; conexión e internacionalidad sin confirmar.
- Atención a menores y compensación con aprobación humana. No se aplica una política DELAY sin retraso verificado.
- Contrato HTTP, errores genéricos, cabecera `no-store` y ausencia de datos personales en las respuestas.

## Catálogo y perfiles

Los 29 ejemplos de `POST /api/demo/assessments` se capturaron mediante HTTP y se contrastan con MockMvc. Cubren los tres estados y los 17 motivos de revisión actuales. OpenAPI incluye 29 ejemplos de solicitud, 27 de respuesta 200, dos de 400 y uno de 500. El error 500 se comprueba mediante una excepción inyectada en test.

El perfil `live` comprueba que el endpoint demo devuelve 404 y que sus ejemplos no aparecen en OpenAPI. Cada selector utiliza adaptadores propios, sin estado compartido entre peticiones. Las seis variantes de categorías externas desconocidas se prueban aparte, sin modificar los 29 snapshots.

## Arquitectura y documentación

La revisión de imports comprueba la separación entre capas: domain contiene datos; application concentra las decisiones y no depende de Spring; infrastructure adapta los contratos y compone los componentes. El puerto de entrada expone `execute` y Lombok genera los constructores en compilación.

Los tres diagramas se validaron con `mermaid.parse` 11.12.0. No se comprobó su representación visual en GitHub.

## Límites

No se han realizado pruebas de carga, integraciones con proveedores reales, auditorías de seguridad ni pruebas de reconciliación de escrituras. No hay mediciones de rendimiento ni un porcentaje de cobertura publicado.
