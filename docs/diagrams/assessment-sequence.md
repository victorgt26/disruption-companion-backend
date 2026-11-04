# Secuencia del caso base

```mermaid
sequenceDiagram
  actor Client as Cliente HTTP
  participant Controller as AssessmentController
  participant In as Puerto AssessDisruptionUseCase
  participant UC as AssessDisruptionUseCaseImpl
  participant Trip as Puerto TripContextPort
  participant Events as Puerto OperationalEventsPort
  participant Policy as Puerto PolicyRulesPort
  participant Mock as JsonMockAdapter
  participant EventAssessment as OperationalEventAssessor
  participant Freshness as SourceFreshness
  participant Rules as PolicyEvaluator
  Client->>Controller: POST bookingId, compensationRequested
  Note over Controller: MVC y Jakarta validan JSON y formato
  alt Solicitud inválida
    Controller-->>Client: 400 ProblemDetail sin payload
  else Solicitud válida
    Controller->>In: execute(bookingId, flag)
    In->>UC: Despacho a implementación
    UC->>Trip: getTripContext(bookingId)
    Trip->>Mock: Leer trip.json
    Mock-->>UC: Reserva parcial con menor
    UC->>Freshness: isFresh(trip.retrievedAt, now)
    Freshness-->>UC: Vigencia de la reserva
    UC->>Events: getOperationalEvents(segmentIds)
    Events->>Mock: Leer y filtrar events.json
    Mock-->>UC: Eventos y metadatos
    UC->>EventAssessment: assess(segmentIds, feed, now)
    EventAssessment->>Freshness: isFresh(timestamp, now)
    Freshness-->>EventAssessment: Vigencia
    Note over EventAssessment: Fuente, último estado y conflictos
    EventAssessment-->>UC: OperationalAssessment inmutable
    alt Retraso verificado
      UC->>Policy: getPolicyRules(DELAY)
      Policy->>Mock: Leer policy.json
      Mock-->>UC: Reglas versionadas
      UC->>Rules: Evaluar hechos conocidos y reglas
      Rules-->>UC: PolicyEvaluation inmutable: R5 con aprobación; R1 no cumple
    else Sin retraso verificado
      Note over UC: No evaluar política DELAY; conservar incertidumbres
    end
    Note over UC,Mock: Cualquier SourceUnavailableException produce revisión
    UC-->>In: Assessment con evidencias y motivos
    In-->>Controller: Resultado
    Note over Controller: AssessmentMapper construye DTO
    Controller-->>Client: 200 REVIEW_REQUIRED, no-store
  end
```

Los retornos del adaptador al caso de uso simplifican visualmente el despacho por puerto. La revisión no crea un caso humano; no hay rebooking ni notificaciones. Excepciones inesperadas se traducen a 500 genérico en ApiExceptionHandler.
