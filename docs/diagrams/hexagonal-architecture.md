# Arquitectura hexagonal implementada

```mermaid
flowchart LR
  Client[Cliente HTTP]
  subgraph Infrastructure[Infrastructure: adaptadores y configuración]
    Controller[AssessmentController: adaptador REST]
    Mapper[AssessmentMapper y DTO]
    Mock[JsonMockAdapter: adaptador de salida]
    JSON[(JSON locales: trip, events, policy)]
    Config[AssessmentConfiguration: composición y Clock]
  end
  subgraph Application[Application: lógica y puertos]
    In[Puerto de entrada: AssessDisruption]
    UC[AssessDisruptionUseCase]
    Eval[PolicyEvaluator]
    Trip[Puerto de salida: TripContextPort]
    Events[Puerto de salida: OperationalEventsPort]
    Policy[Puerto de salida: PolicyRulesPort]
  end
  subgraph Domain[Domain: solo datos]
    Data[Records: reserva, eventos, reglas, evaluación y evidencias]
    Enum[AssessmentStatus]
  end
  Client --> Controller
  Controller --> In
  Controller --> Mapper
  UC -. implementa .-> In
  UC --> Eval
  UC --> Trip
  UC --> Events
  UC --> Policy
  Mock -. implementa .-> Trip
  Mock -. implementa .-> Events
  Mock -. implementa .-> Policy
  Mock --> JSON
  Config -. inyecta .-> UC
  Config -. crea .-> Mock
  UC --> Data
  Eval --> Data
  Mock --> Data
  Mapper --> Data
  UC --> Enum
```

Las flechas continuas representan uso; las discontinuas, implementación o composición. Las lecturas se despachan por interfaces al adaptador. Domain no depende de ninguna otra capa; Application no importa Spring ni Infrastructure. Un único adaptador implementa los tres puertos, con archivos separados por contrato. No hay herramientas de escritura ni modelos de IA.
