# Evidencia de validación

Validación realizada por el asistente el 4 de noviembre de 2026. No equivale a revisión humana del candidato.

| Comprobación ejecutada | Resultado observado |
|---|---|
| `./gradlew clean test` final | BUILD SUCCESSFUL; 62 pruebas, 0 fallos, 0 errores, 0 omitidas |
| Suite de application, JUnit y puertos Mockito | 22 ejecuciones, incluidos los cuatro parámetros de umbral |
| Suite HTTP, contexto Spring/MockMvc y JSON reales | 7 pruebas de API original, 31 de catálogo/demo, 1 de aislamiento de perfil y 1 de fallo inesperado |
| `./gradlew bootRun` | Arranque correcto: Java 25.0.3, Boot 4.1.2, Tomcat, 127.0.0.1:8080, perfil demo |
| POST HTTP real con curl y ABC123 | REVIEW_REQUIRED; S1 160 min / 17:10; R5 con aprobación; ningún REBOOK_NO_FEE; tres motivos de revisión esperados |
| `./gradlew dependencyInsight --dependency spring-core --configuration runtimeClasspath` | Spring Framework 7.0.10 resuelto por Boot 4.1.2 |
| HTTP real de `/swagger-ui/index.html` y `/v3/api-docs` | HTML Swagger UI y contrato OpenAPI recibidos correctamente después del refactor |
| Inspección de imports con rg y lectura del código | Domain sin dependencias de application/infrastructure/frameworks; Application sin Spring/Jackson/Jakarta ni infrastructure |
| Inspección de contratos, respuesta real y pruebas de privacidad | Sin nombres, contactos, documentos ni localizador en respuesta; errores genéricos; no-store verificado en MockMvc |
| Revisión de documentación y diagramas frente al código | Tres lecturas, evaluación determinística y cero escrituras; límites descritos |

Los XML JUnit están en `build/test-results/test/`; reporte navegable en `build/reports/tests/test/index.html`. Son artefactos locales ignorados por Git. La compilación preliminar sin tests no se contó como evidencia. Hubo fallos HTTP iniciales que permitieron corregir el booleano opcional y el mapeo de `_meta`; la suite final quedó verde. Tras el refactor se repitieron `clean test`, `bootRun` y las consultas HTTP reales de Swagger UI, OpenAPI y ABC123 con el código actualizado. El servidor de verificación se detuvo con Ctrl-C tras la consulta.

## Alcance de la revisión

- Umbrales 179/180/239/240; datos históricos independientes del reloj actual; frontera inclusiva de frescura; eventos futuros y antiguos; precedencia y contradicción autoritativa; pérdida de conexión no inferida.
- Fallos en contexto/política, conservación de hechos ya verificados, fuente faltante, política futura/reemplazada/vacía y regla desconocida sin ejecutar expresiones.
- Menor y compensación con aprobación; compensación sin escenario DELAY verificado bloqueada; reserva parcial y desconocida sin promesas.
- Revisión de código: controllers solo adaptan entrada/salida; adaptador JSON carga y filtra según contratos; evaluación y decisiones en application; records/enums puros en domain.

No se ejecutaron pruebas de carga, integraciones reales, auditoría de seguridad, reconciliación de escrituras ni revisión humana. No se afirma cobertura exhaustiva ni porcentajes de cobertura. No hay `mmdc` instalado: se entregan fuentes Mermaid sin SVG; su visualización final en GitHub queda pendiente de revisión. README y AI_USAGE se mantienen compactos para los límites de dos y una páginas equivalentes en Markdown; no se generó un PDF paginado de esos documentos.


## Revisión de convenciones solicitada

Lombok 1.18.48 (`compileOnly` y `annotationProcessor`), sin dependencia en runtime; springdoc 3.2.0 según su matriz oficial para Boot 4.1. El caso de uso es un puerto `AssessDisruptionUseCase` con un único `execute`; `AssessDisruptionUseCaseImpl` coordina servicios de frescura, eventos y políticas. Se sustituyeron imports con asteriscos por explícitos y se eliminaron los no usados con google-java-format. Al expandir los imports de tests faltaron temporalmente `anyList` y `Entitlement`; se corrigieron antes de la ejecución final exitosa.

Códigos propios representados por enums sin alterar sus valores JSON; valores externos desconocidos siguen llegando como strings para su validación segura. El evaluador devuelve `PolicyEvaluation` sin mutar argumentos. Los conflictos de eventos comparan campos explícitos y ya no concatenan cadenas. Se mantienen los motivos acumulados cuando falla la verificación de eventos. La suite HTTP comprueba el ejemplo ABC123, las respuestas documentadas y los valores del enum NextStep en OpenAPI, además del HTML y configuración de Swagger.

Las convenciones y la obligación de actualizar documentación en cambios futuros quedaron registradas en AGENTS.md. Se conservaron los cambios previos del usuario en README (sin enlace al PDF) y la firma de main de Java 25.

## Corrección de sintaxis del diagrama de secuencia

Se reprodujo el fallo de la versión anterior de `docs/diagrams/assessment-sequence.md` con `mermaid.parse` (Mermaid 11.12.0). Dos textos contenían punto y coma, que el parser interpretaba como separadores de instrucciones; se reformularon sin ese carácter. El mismo parser acepta la versión corregida. Validación realizada con dependencias temporales en `/private/tmp`, sin añadir dependencias al proyecto. Se comprobó sintaxis, no renderizado visual en GitHub; no se repitieron pruebas Java porque solo cambió documentación.


## Catálogo Swagger, escenario tipado y secuencia simplificada

- `DisruptionScenario.DELAY` tipa el puerto de políticas y su modelo. Jackson 3 usa `EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL` exclusivamente en el mapper de fixtures, de modo que escenarios de política desconocidos no se consideran válidos. Los selectores `DemoScenario` desconocidos enviados por HTTP reciben 400.
- 29 requests ejecutados contra `POST /api/demo/assessments` por HTTP real. Sus responses completos se guardaron en `src/main/resources/api-examples/responses/` y cada ejemplo se contrasta con la ejecución actual en tests. Se verifica cobertura de los tres estados y de los 17 motivos de revisión actuales.
- El catálogo OpenAPI final se consultó por HTTP: 29 ejemplos de request, 27 ejemplos de respuesta 200, dos de 400 y uno de 500. La respuesta 500 se contrastó mediante un fallo inesperado inyectado en la prueba HTTP, sin añadir una operación pública que lo fuerce.
- La prueba de perfil `live` confirma 404 para el endpoint demo y ausencia de sus ejemplos en OpenAPI. Cada selector compone adaptadores propios sin estado compartido entre peticiones. Las decisiones continúan dentro del caso de uso y sus servicios.
- Se transformó la combinación de reglas a streams con resultados sin efectos laterales. Las 22 pruebas de application siguen pasando. La suite total final es de 62 pruebas, cero fallos, errores u omisiones.
- El arranque real se comprobó en 8081 porque una instancia previa ocupaba 8080. La primera tentativa en 8080 falló por puerto ocupado y se conservó esa instancia. La instancia temporal de verificación se detuvo con Ctrl-C al finalizar.
- Los tres diagramas actualizados pasan `mermaid.parse` 11.12.0. La secuencia muestra solo cliente, backend y tres integraciones lógicas. Sin exportación SVG ni verificación visual en GitHub.

La prueba del catálogo detectó una comparación de objetos Integer por identidad que mostraba solo parte de los ejemplos de respuesta. Se corrigió usando códigos HTTP primitivos y se repitió la suite y la consulta real de OpenAPI. `docs/api-examples.md` contiene los bodies, vínculos a respuestas completas, instrucciones Swagger y los límites de las simulaciones.
