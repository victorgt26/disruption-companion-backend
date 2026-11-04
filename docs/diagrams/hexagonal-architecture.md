# Arquitectura hexagonal implementada

```mermaid
flowchart LR
  Client[Cliente HTTP / Swagger UI]
  subgraph Infrastructure[Infrastructure: adaptadores y configuración]
    Controller[AssessmentController: adaptador REST]
    Demo[DemoAssessmentController: solo perfil demo]
    Factory[AssessmentUseCaseFactory: composición por fixtures]
    Mapper[AssessmentMapper y DTO]
    Mock[JsonMockAdapter: adaptador de salida]
    JSON[(JSON locales: trip, events, policy)]
    Config[AssessmentConfiguration: composición y Clock]
  end
  subgraph Application[Application: lógica y puertos]
    In[Puerto de entrada: AssessDisruptionUseCase]
    UC[AssessDisruptionUseCaseImpl]
    Eval[PolicyEvaluator: resultado sin mutaciones externas]
    EventAssessment[OperationalEventAssessor]
    Freshness[SourceFreshness]
    Trip[Puerto de salida: TripContextPort]
    Events[Puerto de salida: OperationalEventsPort]
    Policy[Puerto de salida: PolicyRulesPort]
  end
  subgraph Domain[Domain: solo datos]
    Data[Records: reserva, eventos, reglas, evaluación y evidencias]
    Enum[Enums: categorías de entrada, escenario, resultado, revisión y beneficio]
  end
  Client --> Controller
  Controller --> In
  Client --> Demo
  Demo --> Factory
  Demo --> In
  Demo --> Mapper
  Factory -. crea .-> UC
  Factory -. crea .-> Mock
  Controller --> Mapper
  UC -. implementa .-> In
  UC --> Eval
  UC --> EventAssessment
  UC --> Freshness
  EventAssessment --> Freshness
  EventAssessment --> Data
  EventAssessment --> Enum
  UC --> Trip
  UC --> Events
  UC --> Policy
  Mock -. implementa .-> Trip
  Mock -. implementa .-> Events
  Mock -. implementa .-> Policy
  Mock --> JSON
  Config -. configura .-> Factory
  UC --> Data
  Eval --> Data
  Mock --> Data
  Mapper --> Data
  UC --> Enum
```

Las flechas continuas representan uso; las discontinuas, implementación o composición. Las lecturas se despachan por interfaces al adaptador. Domain no depende de ninguna otra capa; Application no importa Spring ni Infrastructure. Un único adaptador implementa los tres puertos, con archivos separados por contrato. No hay herramientas de escritura ni modelos de IA.

Lombok genera constructores en compilación; no es un componente de runtime. springdoc genera OpenAPI y sirve Swagger UI en infraestructura. El puerto de entrada expone únicamente `execute`; los servicios auxiliares conservan toda decisión de negocio dentro de Application.

El endpoint demo selecciona conjuntos de fixtures sin estado compartido y llama al mismo puerto de entrada. `DemoOpenApiConfiguration`, también restringido a demo, añade ejemplos documentales desde el catálogo y las respuestas verificadas. No decide derechos ni sustituye la evaluación por un snapshot.
