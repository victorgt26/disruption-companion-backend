# Procesamiento de la consulta

```mermaid
flowchart TD
  Demo[POST /api/demo/assessments, solo perfil demo] --> Fixtures[Validar selector y componer lecturas con fixtures aislados]
  Fixtures --> Valid
  Request[POST /api/assessments] --> Valid{JSON y bookingId válidos?}
  Valid -- No --> Bad[400 ProblemDetail genérico]
  Valid -- Sí --> Trip[AssessDisruptionUseCaseImpl.execute: leer reserva mediante puerto]
  Trip --> HasTrip{Reserva y segmentos disponibles?}
  HasTrip -- No --> Review[Marcar revisión y conservar solo hechos verificados]
  HasTrip -- Sí --> Quality[Marcar parcialidad, antigüedad o pasajeros desconocidos]
  Quality --> Feed[Leer eventos por segmentos]
  Feed --> Fresh{Respuesta y metadatos vigentes?}
  Fresh -- No --> Review
  Fresh -- Sí --> Filter[OperationalEventAssessor y SourceFreshness: descartar eventos antiguos, futuros y fuentes degradadas]
  Filter --> Select[Por tramo: último DELAY o STATUS de OPS_CORE]
  Select --> Conflict{Falta fuente autoritativa o hay empate contradictorio?}
  Conflict -- Sí --> Flag[Marcar revisión del tramo y continuar con los demás]
  Conflict -- No --> Complete{Evento completo y estado soportado?}
  Complete -- No --> Flag
  Complete -- Sí --> Facts[Conservar retrasos positivos verificados]
  Flag --> Delay
  Facts --> Delay{Hay retraso verificado?}
  Delay -- No --> NoPolicy[No aplicar política DELAY; compensación solicitada requiere revisión]
  Delay -- Sí --> Connection[Si hay varios tramos: conexión no confirmada]
  Connection --> Policy[Leer reglas DELAY]
  Policy --> Verified{Política verificable y vigente?}
  Verified -- No --> Review
  Verified -- Sí --> Rules[PolicyEvaluator: devolver coincidencias y motivos sin mutar argumentos]
  Rules --> Unknown[No inferir conexión perdida ni internacionalidad; señalar reglas desconocidas]
  Unknown --> Approval[Menor o compensación: aprobación humana; umbral de cambio ≥180 min]
  Approval --> Build
  NoPolicy --> Build
  Review --> Build[Construir estado, hechos, evidencias, reglas y siguiente paso]
  Build --> Response[200 sin efectos laterales; no-store]
  SourceFailure[Fallo de lectura en cualquier puerto] --> Review
  Unexpected[Error inesperado] --> Error[500 genérico]
```

La decisión de cada tramo se repite para todos los segmentos. Un fallo de política conserva el retraso ya verificado; un fallo anterior no inventa hechos. La indisponibilidad de fuente se representa con revisión, no como ausencia de interrupción.

El selector de fixtures demo solo modifica las respuestas de las integraciones simuladas. Ambos endpoints ejecutan el mismo caso de uso. Escenarios de política externos distintos de `DisruptionScenario.DELAY` no se consideran verificados.
