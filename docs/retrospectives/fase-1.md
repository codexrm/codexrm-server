# Retrospectiva Fase 1

## ¿Qué salió bien?

- La red de seguridad de tests (134 unit + 21 integration tests) permitió
  encontrar y corregir con confianza vulnerabilidades reales antes del
  cierre de fase: un JWT secret y una password de admin hardcodeados en
  producción (#136), y un path traversal en el endpoint de export que
  permitía escribir archivos fuera de la carpeta esperada (#137) — el
  hallazgo más serio de toda la fase.
- El manejo de errores quedó unificado: todos los caminos de fallo
  (excepciones de negocio, 401, 403, JSON malformado, archivos
  demasiado grandes) devuelven la misma estructura `ErrorResponse`,
  en vez de mezclar formatos según qué capa rechazara la request.
- Cada corrección se validó con evidencia real — logs de arranque,
  `mvn verify`, capturas de GitHub Actions — en vez de asumir que
  "debería funcionar".
- La disciplina de un PR por issue, con commits explicando el
  *porqué* y no solo el *qué*, dejó un historial de git que se puede
  auditar semana por semana.

## ¿Qué fue más difícil de lo esperado?

Un problema de compatibilidad de versiones entre Testcontainers y el
Docker Engine local, no del código del proyecto. Al correr los tests
de integración (Spring Boot + JUnit 5 + Testcontainers + PostgreSQL
desde IntelliJ en Windows), Testcontainers detectaba el socket de
Docker (`npipe://`) pero el daemon respondía con un
`BadRequestException` (400) y campos vacíos (`ServerVersion`,
`Driver`, `OperatingSystem`, `KernelVersion`), terminando siempre en
`Could not find a valid Docker environment`.

La causa raíz: Testcontainers 1.21.3 usa por defecto la versión 1.32
de la API de Docker, pero el Docker Engine local (29.4.3) requiere
como mínimo la 1.40 — una incompatibilidad de versiones de API, no un
problema de instalación ni de configuración del proyecto. Se
descartó todo lo demás en el camino: dependencias Maven, reimport del
proyecto, y la configuración de `BaseIntegrationTest`, que estaba
correcta desde el principio.

La solución fue actualizar `testcontainers.version` de `1.21.3` a
`2.0.5` en el `pom.xml` (alternativa más simple que forzar
`api.version=1.41` vía `systemPropertyVariables` en el plugin de
Surefire). Al resolver esa incompatibilidad apareció un segundo
problema, más chico: `RefreshToken` usaba
`@GeneratedValue(strategy = GenerationType.AUTO)`, que Hibernate
contra PostgreSQL en este contexto no resolvía de forma consistente;
cambiarlo a `GenerationType.IDENTITY` lo resolvió. Con ambos cambios,
`ApplicationIntegrationTest` (y el resto de la suite crítica) volvió
a correr en verde de forma estable.

## ¿Qué recorté o pospuse?

- **Reestructuración de paquetes (semana 19):** evaluada explícitamente
  contra las 4 condiciones de entrada del plan. Tres se cumplían (suite
  crítica verde, CI verde, sin bugs críticos abiertos), pero no había
  una razón concreta para mover archivos ahora — la estructura actual
  no generó fricción real durante toda la fase. Documentado en ADR-012.
- **Auditoría OWASP exhaustiva del import/export:** el hardening de la
  semana 17 cerró el hallazgo crítico (path traversal), pero una
  revisión OWASP más exhaustiva y sistemática queda para Fase 2, según
  el propio plan de corte.
- **Acoplamiento `UserService` ↔ DTOs de API:** durante la evaluación
  de la semana 19 se encontró que `UserService` recibe tipos de la
  capa `api` directamente (a diferencia de `ReferenceService` y
  `RoleService`, que sí respetan el límite de capas). Corregirlo
  implica cambiar firmas de métodos, así que se documentó como deuda
  explícita (ADR-013) en vez de forzarlo en una semana de solo
  documentación.

Un hallazgo similar pero más chico — lógica de ownership viviendo como
método privado en `ReferenceController` en vez de en el service — sí
se corrigió durante la verificación final de esta semana, porque era
de bajo riesgo (método privado, sin contrato externo) y no requería
tocar ningún test más allá de uno.

## ¿Qué aprendí que no esperaba?

Seguridad real, más allá de "usar HTTPS" o "no compartir contraseñas".
Cosas concretas como: un secret JWT corto de un carácter rompe el
arranque completo de la app (y hay que generarlo con un comando
verificable, no copiando y pegando a ojo); un parámetro de nombre de
archivo sin sanitizar puede convertirse en una escritura de archivo
arbitraria; y que la diferencia entre "funciona en mi máquina" y
"está verificado" es correr el comando y leer el resultado, no confiar
en la memoria de la corrida anterior.

## Post-mortem de la fase

[JWT Secret Length Failures Across Environments](../post-mortems/fase-1-jwt-secret-length.md) —
un bug real, repetido tres veces durante el issue #136, con impacto,
causa raíz, fix y prevención documentados mientras el contexto estaba
fresco.

## Estado del proyecto al cerrar la fase

- [x] Stack actualizado (Java 21, Spring Boot 3.2+)
- [x] `WebSecurityConfigurerAdapter` eliminado (migrado a `SecurityFilterChain`)
- [x] Docker funcionando para desarrollo local
- [x] Spring Profiles configurados (dev/test/prod, verificados funcionando)
- [x] CORS centralizado (`CorsConfig`/`CorsProperties`, sin `@CrossOrigin` en controllers)
- [x] Flyway con migrations versionadas (tabla `app_user`, no `"User"`)
- [x] `@Valid` en todos los controllers (13/13 `@RequestBody` confirmados)
- [x] No existe `return null` donde debería haber `Page.empty()` (un caso encontrado en `buildPageDTO` resultó ser código muerto inalcanzable, sin impacto en la API)
- [x] Manejo de errores centralizado (`GlobalExceptionHandler`, `AuthEntryPointJwt`, `AccessDeniedHandlerImpl`, misma estructura `ErrorResponse`)
- [x] DTOConverter refactorizado
- [x] Lógica de roles sin duplicación
- [x] Controllers delgados (corregido: `filterUserReferences` movido de `ReferenceController` a `ReferenceService` como `filterOwnedReferences`)
- [x] URLs REST normalizadas (24 rutas auditadas: plural, lowercase, sin verbos como nombre de recurso)
- [x] Tests unitarios y suite crítica de integración verdes (140 unit + 21 integration, `BUILD SUCCESS`)
- [x] CI básica verde (últimas 7+ corridas en `dev` en verde en GitHub Actions)
- [x] Documentación mínima útil completa (README, `architecture.md`, `decision-log.md`, `testing.md`, `integration-testing.md`)
- [x] Existe retrospectiva de fase y un post-mortem corto
- [x] Reestructuración de paquetes ejecutada o pospuesta con criterio explícito (pospuesta, ADR-012)

## ¿Estoy listo para la Fase 2?

**Sí.** Los 18 ítems de la checklist de cierre están confirmados con
evidencia real (no de memoria): salida de `mvn verify`, capturas de
GitHub Actions, y búsquedas directas en el código durante esta misma
semana. Los dos hallazgos de arquitectura que aparecieron en el camino
(acoplamiento de `UserService` con DTOs de API, y el gate de
reestructuración) quedaron documentados con criterio explícito en vez
de ignorados o forzados fuera de tiempo — que es exactamente el
comportamiento que la Fase 1 buscaba instalar como hábito.

La deuda pendiente (ADR-013, auditoría OWASP exhaustiva) es conocida,
está escrita, y no bloquea nada del funcionamiento actual del sistema.

## Preview de Fase 2 — Lo que viene

- Revisión profunda de reglas de autorización en `SecurityFilterChain`
- `SecurityScheme` para JWT en Swagger UI (botón "Authorize")
- Validación de inputs más estricta (OWASP)
- Auditoría OWASP residual sobre import/export y otros endpoints
- Resolver el acoplamiento