# Retrospectiva Fase 2

## ¿Qué salió bien?

El hallazgo del bug de doble login (semanas 4-6) fue lo mejor logrado
de toda la fase: no llegó por un reporte de un usuario afectado, sino
por una auditoría deliberada del lifecycle de JWT/refresh token —
leer el schema, notar la constraint `UNIQUE` en `user_id`, formular
la hipótesis "esto debería romper con un segundo login", y
confirmarla en vivo antes de que jamás se convirtiera en un problema
real. Es exactamente el tipo de disciplina que esta fase buscaba
instalar: no esperar a que algo se rompa para investigarlo.

También salieron bien: el `correlationId` funcionando de punta a
punta desde el primer intento en vivo (el mismo UUID en el header de
respuesta y en el log de `AuthEntryPointJwt`, un camino que corre
fuera del flujo normal de controller); y el hecho de que 3 de los 4
hallazgos de seguridad reales de la fase (doble login, JWT en
plaintext, AUDITOR/logout) surgieron de auditorías deliberadas, no de
casualidad.

## ¿Qué fue más difícil de lo esperado?

Alternar entre Docker (perfil `prod`) y `mvn spring-boot:run` (perfil
`dev`) para verificaciones puntuales generó fricción recurrente
durante toda la fase: credenciales de datasource distintas
(`postgres`/`postgres` vs `codexrm`/`codexrm`), conflictos de puerto
cuando ambos quedaban corriendo a la vez, y la necesidad de recordar
revertir cambios temporales en `application-dev.properties` antes de
cada commit. Ninguno de estos fue un bug del proyecto — todos fueron
fricción de entorno local — pero consumieron tiempo real en más de
una verificación (Swagger con JWT, doble login, rate limiting).

## ¿Qué recorté o pospuse?

- **ADR-012 (acoplamiento `UserService`/DTOs de api):** evaluado
  explícitamente en la semana 0 contra las 5 categorías del gate de
  Fase 2 (auth, OWASP, observabilidad, rate limiting, runbooks) — no
  encaja en ninguna. Es deuda de arquitectura general, no de
  seguridad, y queda pospuesta a Fase 3.
- Nada más quedó pospuesto sin resolver dentro del alcance real de
  Fase 2 — los 4 hallazgos de seguridad encontrados durante la fase
  se corrigieron en el momento, no se dejaron para después.

## ¿Qué aprendí que no esperaba?

Que un fallback de errores genérico y bien diseñado (el `500`
estándar de `GlobalExceptionHandler`, sin filtrar detalles internos)
puede esconder perfectamente un bug completamente prevenible durante
mucho tiempo — el doble login estuvo ahí, sin detectarse, protegido
por un manejo de errores que hacía su trabajo correctamente pero no
alertaba de que el caso en sí era evitable. También aprendí lo frágil
que puede ser una combinación aparentemente inocente como
`@Component` + `@MockBean` en Spring Boot: agregar una anotación
estándar rompió silenciosamente cada test de controller del proyecto,
sin ningún mensaje de error que apuntara a la causa real.

## Post-mortem de la fase

[Double Login Crashes with 500](../post-mortems/fase-2-double-login-crash.md) —
impacto, causa raíz, fix y prevención documentados del hallazgo más
valioso de la fase.

## Checklist de cierre (verificada con evidencia, semana 12)

- [x] Autorización endurecida (matriz vigente, confirmado vía `git log`)
- [x] JWT/refresh testeados (`mvn verify`, upsert + rotación)
- [x] CORS por entorno (dev/prod distintos, sin wildcard)
- [x] OWASP import/export cerrado (nombres 100% server-generated, confirmado vía `git log`)
- [x] Logs estructurados visibles (JSON, correlationId presente)
- [x] Rate limiting con 429 testeado (verificado en vivo y en tests)
- [x] Suite crítica ampliada verde (`mvn verify`, 145+ unit / 35+ integration)
- [x] Baseline de carga documentado (k6, 2 escenarios)
- [x] Runbooks publicados (los 3, verificados en vivo)
- [x] Sin vulnerabilidad crítica sin fix (4 hallazgos, todos resueltos)

Ver [ADR-019](../decision-log.md) para el detalle completo del cierre.

## ¿Estoy lista para la Fase 3?

**Sí.** Los 10 ítems de la checklist de cierre están confirmados con
evidencia real generada hoy (salida de `mvn verify`, historial de
`git log` sobre los archivos críticos, no memoria de semanas
anteriores). Los 4 hallazgos de seguridad reales de la fase se
encontraron por auditoría deliberada y se corrigieron en el momento,
no quedaron pendientes. La única deuda conocida (ADR-012) es
arquitectura general, explícitamente fuera del alcance de seguridad
de Fase 2, documentada como candidata a Fase 3 en vez de ignorada.

## Preview de Fase 3

Sin un plan detallado todavía — a definir. Candidatos conocidos desde
esta fase: resolver ADR-012 (acoplamiento `UserService`), y cualquier
tema que surja de operar el sistema con la observabilidad y el rate
limiting ya en producción.