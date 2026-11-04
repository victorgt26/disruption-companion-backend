# Evidencia de validación

Validación realizada por el asistente el 4 de noviembre de 2026. No equivale a revisión humana del candidato.

| Comprobación ejecutada | Resultado observado |
|---|---|
| `./gradlew clean test` final | BUILD SUCCESSFUL; 27 pruebas, 0 fallos, 0 errores, 0 omitidas |
| Suite de application, JUnit y puertos Mockito | 22 ejecuciones, incluidos los cuatro parámetros de umbral |
| Suite HTTP, contexto Spring/MockMvc y JSON reales | 5 pruebas; caso base, formato inválido, JSON malformado, reserva desconocida y compensación con aprobación |
| `./gradlew bootRun` | Arranque correcto: Java 25.0.3, Boot 4.1.2, Tomcat, 127.0.0.1:8080, perfil demo |
| POST HTTP real con curl y ABC123 | REVIEW_REQUIRED; S1 160 min / 17:10; R5 con aprobación; ningún REBOOK_NO_FEE; tres motivos de revisión esperados |
| `./gradlew dependencyInsight --dependency spring-core --configuration runtimeClasspath` | Spring Framework 7.0.10 resuelto por Boot 4.1.2 |
| Inspección de imports con rg y lectura del código | Domain sin dependencias de application/infrastructure/frameworks; Application sin Spring/Jackson/Jakarta ni infrastructure |
| Inspección de contratos, respuesta real y pruebas de privacidad | Sin nombres, contactos, documentos ni localizador en respuesta; errores genéricos; no-store verificado en MockMvc |
| Revisión de documentación y diagramas frente al código | Tres lecturas, evaluación determinística y cero escrituras; límites descritos |

Los XML JUnit están en `build/test-results/test/`; reporte navegable en `build/reports/tests/test/index.html`. Son artefactos locales ignorados por Git. La compilación preliminar sin tests no se contó como evidencia. Hubo fallos HTTP iniciales que permitieron corregir el booleano opcional y el mapeo de `_meta`; la suite final quedó verde. El arranque real se verificó antes de los últimos controles de datos incompletos; estos quedaron cubiertos por la suite final completa, incluido el endpoint con el código actualizado. El servidor de verificación se detuvo con Ctrl-C tras la consulta.

## Alcance de la revisión

- Umbrales 179/180/239/240; datos históricos independientes del reloj actual; frontera inclusiva de frescura; eventos futuros y antiguos; precedencia y contradicción autoritativa; pérdida de conexión no inferida.
- Fallos en contexto/política, conservación de hechos ya verificados, fuente faltante, política futura/reemplazada/vacía y regla desconocida sin ejecutar expresiones.
- Menor y compensación con aprobación; compensación sin escenario DELAY verificado bloqueada; reserva parcial y desconocida sin promesas.
- Revisión de código: controllers solo adaptan entrada/salida; adaptador JSON carga y filtra según contratos; evaluación y decisiones en application; records/enums puros en domain.

No se ejecutaron pruebas de carga, integraciones reales, auditoría de seguridad, reconciliación de escrituras ni revisión humana. No se afirma cobertura exhaustiva ni porcentajes de cobertura. No hay `mmdc` instalado: se entregan fuentes Mermaid sin SVG; su visualización final en GitHub queda pendiente de revisión. README (685 palabras) y AI_USAGE (357) se mantienen compactos para los límites de dos y una páginas equivalentes en Markdown; no se generó un PDF paginado de esos documentos.
