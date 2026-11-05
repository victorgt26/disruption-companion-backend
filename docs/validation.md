## Validaciones pendientes sobre eventos operacionales

Se identificó un caso que requiere mayor definición del contrato de eventos: la relación entre las actualizaciones `DELAY` y `STATUS` de un mismo segmento.

Antes de modificar la lógica de selección, se deben comprobar los siguientes escenarios:

| Escenario | Validación esperada |
|---|---|
| `DELAY` seguido de `STATUS: ON_TIME` | Confirmar si el segundo evento reemplaza el retraso o si existe ambigüedad |
| `STATUS: ON_TIME` seguido de `DELAY` | Reconocer el retraso si representa el nuevo estado vigente |
| Eventos contradictorios con igual timestamp | No tomar decisiones arbitrarias |
| `DELAY` seguido de `GATE_CHANGE` | Mantener la evidencia de retraso |
| Eventos recibidos fuera de orden | Evaluar según semántica, fuente y timestamp, no según orden de llegada |

La implementación actual selecciona el último evento válido `DELAY` o `STATUS` de OPS_CORE. Este comportamiento debe contrastarse con la semántica real de los eventos antes de utilizar integraciones productivas.

**Estado:** pendiente de validación adicional. No se incluyen estos escenarios como pruebas exitosas hasta haberlos ejecutado.
