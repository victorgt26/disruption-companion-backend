# Evaluación de una interrupción: flujo e integraciones

```mermaid
sequenceDiagram
  actor Client as Cliente HTTP / Swagger
  participant Backend as Backend de evaluación
  participant Trips as Contexto de reserva
  participant Operations as Información operacional
  participant Policies as Políticas versionadas

  Client->>Backend: Solicitar evaluación de reserva
  alt Solicitud inválida
    Backend-->>Client: 400, error de validación
  else Solicitud válida
    Backend->>Trips: Consultar contexto del viaje
    Trips-->>Backend: Pasajeros, segmentos y calidad de datos
    alt Reserva ausente o sin segmentos válidos
      Backend-->>Client: 200, revisión requerida
    else Contexto utilizable
      Backend->>Operations: Consultar eventos de los segmentos
      Operations-->>Backend: Eventos, fuentes y timestamps
      Note over Backend: Verificar frescura y fuentes, resolver contradicciones
      alt Retraso verificado
        Backend->>Policies: Consultar reglas de DELAY
        Policies-->>Backend: Reglas y versión de política
        Note over Backend: Evaluar derechos acreditados y aprobación humana
      else Sin retraso verificado
        Note over Backend: Conservar incertidumbres sin aplicar política DELAY
      end
      Backend-->>Client: 200, hechos, evidencias y siguiente paso
    end
  end
  Note over Backend,Policies: Fallos de lectura requieren revisión, errores inesperados devuelven 500
```

El diagrama muestra integraciones lógicas y el orden real de las lecturas, sin detallar clases, adaptadores ni archivos. Actualmente las tres integraciones se simulan mediante JSON locales. Un fallo conserva los hechos ya verificados y evita prometer beneficios. La revisión indica contacto humano, sin crear casos ni modificar reservas.

El endpoint demo permite seleccionar fixtures y ejecuta este mismo flujo. No reproduce respuestas pregrabadas. Las responsabilidades técnicas de puertos y adaptadores permanecen en el [diagrama de arquitectura](hexagonal-architecture.md).
