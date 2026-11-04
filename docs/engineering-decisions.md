# Decisiones de tipado y estado de la entrega

## Auditoría de strings

Se revisaron los literales de `src/main/java` y los campos de los modelos. La elección depende del significado y del uso del valor, no de que aparezca entre comillas. Las categorías que gobiernan decisiones están tipadas; identificadores, textos y expresiones de contratos conservan su naturaleza de string.

| Categoría / campo | Decisión | Motivo |
|---|---|---|
| `Metadata.completeness` | `DataCompleteness`: COMPLETE, PARTIAL | El caso de uso decide revisión según la calidad de la información |
| `Passenger.type` | `PassengerType`: ADULT, CHILD | Participa en la identificación del grupo con menor |
| `OperationalEvent.type` | `OperationalEventType`: DELAY, STATUS, GATE_CHANGE | Selección de hechos y contexto operacional |
| `OperationalEvent.status` | `OperationalStatus`: ON_TIME, SCHEDULED | Estados interpretados por el vertical slice actual |
| Fuente de evento y listas de disponibilidad/degradación | `OperationalSource`: OPS_CORE, AIRPORT_FEED, LEGACY_MIRROR | Identidad consistente entre evento, confianza y metadatos |
| `PolicyRule.entitlement` y `Entitlement.code` | `EntitlementCode` | Comparación tipada entre beneficio de la regla y el esperado |
| Escenario de política | `DisruptionScenario.DELAY` | Único escenario de negocio implementado |
| Estado, mensaje, siguiente paso, revisión y decisión de evidencia | Enums existentes | Vocabulario finito y coherente en aplicación y API |
| Selección de fixtures | `DemoScenario` en infraestructura | Catálogo técnico de pruebas, distinto del escenario de negocio |
| Condiciones de política | String en el contrato y catálogo interno `KnownRule` | Se conserva la condición original y solo se reconocen pares conocidos de condición/beneficio, sin ejecutar expresiones |
| Identificadores de reserva, pasajeros, segmentos, eventos, reglas y versiones | String | Son referencias abiertas, no categorías finitas |
| Código de aeropuerto, número de vuelo, puerta y familia tarifaria LIGHT | String | Datos externos que no gobiernan reglas en este alcance, sin inventar un catálogo incompleto |
| Campos ausentes y versiones reemplazadas | Listas de strings | Rutas de campos e identificadores externos |
| Perfiles, properties, rutas, recursos, claves JSON y nombres de ejemplos | Strings de infraestructura | Nombres declarativos y contratos técnicos; un enum no aporta semántica de negocio |
| Descripciones Swagger, mensajes de error y textos libres | String | Contenido textual, no una categoría de decisión |
| Códigos HTTP de anotaciones Swagger | Strings requeridos por la anotación | Su representación forma parte del contrato OpenAPI |

Los enums de domain contienen solo constantes. No incorporan reglas ni dependencias de serialización. El mapper de mocks de infraestructura interpreta códigos externos desconocidos como null mediante `EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL`. El caso de uso y sus servicios los tratan de forma conservadora: calidad desconocida exige revisión, pasajero sin categoría ni edad exige revisión, evento sin tipo/fuente reconocidos no acredita un hecho, estado no reconocido no se interpreta y beneficio desconocido no acredita una regla. Una edad disponible se sigue considerando según la lógica existente.

No se añaden categorías no proporcionadas para aparentar soporte. Por ejemplo, CANCELLED en el fixture de estado no soportado provoca revisión, sin implementar cancelaciones. Una fuente desconocida se serializa como null en la evidencia y no se considera confiable, aunque figure en la lista de fuentes disponibles. Los valores conocidos mantienen exactamente su representación JSON. Las respuestas capturadas del catálogo se verifican sin regenerarlas tras este cambio de tipado.
