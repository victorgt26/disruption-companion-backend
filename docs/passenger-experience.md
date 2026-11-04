# Experiencia y contrato complementario

Diseño del track Frontend/Experience; no hay aplicación frontend implementada.

## Pantalla y comunicación del caso base

1. El pasajero autenticado abre su viaje. En producción, el backend obtiene una reserva autorizada para su identidad; el localizador no es un mecanismo de autenticación. La demo acepta ABC123 sin identidad y solo escucha en localhost.
2. El cliente solicita la evaluación y muestra carga. En revisión puede mostrar hechos verificados sin transformar incertidumbre en certeza.
3. Mensaje para el ejemplo: «Tu primer tramo tiene un retraso informado de 160 minutos y salida a las 17:10. Aún no podemos confirmar cómo afecta a tu conexión. Con este retraso no está acreditado un cambio sin cargo por la regla de 180 minutos. Tu grupo puede requerir atención prioritaria, sujeta a revisión. Contacta con atención para revisar el viaje». No mostrar que se haya creado un caso, prometido un SLA ni confirmado una reserva.
4. Botón «Contactar con atención»: abre un canal humano cuya integración se definirá con Producto. No invoca escritura en este backend. No hay botones de cambio, reembolso ni compensación automática.

La salida no contiene zona horaria para `newDeparture`; se presenta como horario informado del tramo, sin convertir a hora del dispositivo ni calcular conexiones. Mostrar `assessedAt` como hora de evaluación de la demo y etiquetar explícitamente el escenario histórico. Reloj real y mocks históricos deben mostrar falta de información vigente.

## API implementada

Los requests y responses completos y los pasos para disparar escenarios desde Swagger están en el [catálogo ejecutable](api-examples.md). La experiencia del pasajero consume `/api/assessments`; `/api/demo/assessments` es un arnés de prueba disponible solo en demo, con un selector de fixtures y una solicitud anidada.

Probar en [Swagger UI](http://127.0.0.1:8080/swagger-ui.html), con el ejemplo `ABC123`, después de `./gradlew bootRun`. Contrato generado: `/v3/api-docs`. La consola pertenece a la infraestructura de documentación de la API, sin implementar el frontend de pasajero.

`POST /api/assessments`, JSON:

```json
{"bookingId":"ABC123","compensationRequested":false}
```

`bookingId` obligatorio, seis caracteres A-Z/0-9 (supuesto de formato del ejemplo). `compensationRequested` es opcional, omitido/null equivale a false. No se recibe texto libre ni datos personales. Respuesta 200 con `Cache-Control: no-store` incluso si requiere revisión; 400 para JSON o campos inválidos con ProblemDetail genérico. Fallos de las fuentes se representan como evaluación incompleta, sin fingir ausencia de interrupción; errores inesperados devuelven 500 genérico. Reserva inexistente devuelve revisión, sin confirmar identidad del titular.

| Campo | Interpretación |
|---|---|
| `status` | `DISRUPTION`, `NO_VERIFIED_DISRUPTION` o `REVIEW_REQUIRED`; el segundo no garantiza ausencia de cambios futuros |
| `assessedAt` | Instant UTC del Clock |
| `messageCode` | `VERIFIED_DELAY` o `NO_CONFIRMED_CHANGE`; traducir junto al estado y motivos |
| `disruptions` | Solo retrasos verificados: segmentId, eventId, delayMinutes, newDeparture |
| `evidence` | eventId, segmentId, source, recordedAt y decision; trazabilidad técnica, no copiar como texto de pasajero |
| `entitlements` | code, policyRef, ruleId, requiresHumanApproval; coincidencias de política, nunca una ejecución confirmada |
| `reviewReasons` | Códigos de incertidumbre o necesidad de aprobación; guiar la revisión |
| `nextStep` | `CONTACT_HUMAN_SUPPORT` o `READ_VERIFIED_INFORMATION`; una instrucción, sin side effects |

Los códigos de estado, mensaje, siguiente paso, revisión, evidencia y beneficios son enums en Java y valores enumerados en OpenAPI. Su representación JSON sigue siendo una cadena con los valores descritos; identificadores y condiciones externas conservan strings. Completitud, tipo de pasajero, tipo/estado/fuente operacional y beneficio de política están tipados; valores externos desconocidos se leen como null y no se interpretan como categorías verificadas. Las políticas usan `DisruptionScenario.DELAY`; un escenario externo desconocido se convierte en null y no acredita derechos.

No mostrar beneficios de `entitlements` como ofertas confirmadas si `REVIEW_REQUIRED`, ni compensación sin aprobación. Motivos principales: TRIP_PARTIAL, TRIP_STALE_OR_UNVERIFIED, TRIP_INCOMPLETE, TRIP_NOT_AVAILABLE, PASSENGERS_UNKNOWN, EVENTS_UNAVAILABLE_OR_STALE, NO_AUTHORITATIVE_EVENT, CONFLICTING_AUTHORITATIVE_EVENTS, EVENT_INCOMPLETE, UNSUPPORTED_OPERATIONAL_STATUS, CONNECTION_OUTCOME_UNKNOWN, POLICY_UNVERIFIED, UNKNOWN_POLICY_RULE, INTERNATIONAL_STATUS_UNKNOWN, HUMAN_APPROVAL_REQUIRED, COMPENSATION_SCENARIO_UNVERIFIED y SOURCE_UNAVAILABLE. El cliente debe tener un mensaje seguro para códigos futuros.

## Consulta ambigua y posible IA

«¿Puedo tomar el siguiente vuelo con mi hijo y me corresponde compensación?» se descompone en contexto autorizado, hechos operacionales con fuentes/tiempos, evaluación de reglas y acción solicitada. Este backend ejecuta las tres lecturas de contexto/eventos/política para DELAY y devuelve incertidumbres. No conoce llegada efectiva, conexión mínima, internacionalidad ni disponibilidad; no los deduce. La solicitud de compensación se marca mediante el booleano explícito, no mediante interpretación automática de texto.

Una evolución podría usar IA exclusivamente para clasificar intención o redactar sobre hechos ya validados y desidentificados. No decide elegibilidad ni modifica reservas. Un validador determinístico debe exigir correspondencia entre beneficios, policyRef y evidencia; si falta contexto, se contradicen hechos o se pide acción sensible, se detiene y pasa a una persona. La primera versión utiliza plantillas porque evita inferencia, latencia, costo y exposición innecesaria.

## Escrituras fuera de alcance y operación

- `get_eligible_options`: solo candidatos. O1 exige validar elegibilidad, vigencia TTL=120 s, inventario y grupo completo; O2 no aloja al grupo de dos; O3 requiere política de reembolso independiente. La confianza del proveedor no sustituye reglas.
- `create_rebooking_request`: identidad y autorización de reserva, opción revalidada, política vigente, consentimiento explícito, aprobación cuando corresponda, clave idempotente persistida, auditoría y reconciliación. 201 es PENDING_CONFIRMATION, no reserva confirmada; 409 recupera la solicitud existente; 503 implica resultado indeterminado, no reintentar con otra clave ni anunciar fallo definitivo. Requiere contrato adicional de consulta/reconciliación que el PDF no proporciona.
- `create_human_case`: consentimiento/minimización, razón y evidencia validadas, autorización y deduplicación acordadas con Operaciones. No inventar case_id ni SLA; mostrar solo los recibidos de una creación efectiva.
- `notify_passenger`: canal autorizado y consentimiento; mensaje validado con policy_ref, sin PII innecesaria. 202 solo encolado; 422 implica corregir/retener contenido, sin enviarlo por otra vía automáticamente.

Producción: timeouts, presupuesto de latencia por fuente, métricas sin identificadores, trazas autorizadas con retención definida, control de acceso, límites de consultas y alertas ante degradación. No hay red externa en las lecturas actuales, reintentos ni métricas de rendimiento medidas. Confirmar con Operaciones precedencia/frescura y con Producto lenguaje y canal de derivación; con Seguridad, autorización y retención. El SLA de 45 minutos es solo un ejemplo de una escritura no implementada.
