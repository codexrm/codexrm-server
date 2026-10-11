# Criterio de "está verde" para Fase 3

Versión vigente: 2026-10-11 (semana 0). Se amplía semana a semana,
a medida que entran Docker, Actuator y Terraform. Parte de la lista
de 13 pasos del plan de Fase 3.

## Criterio vigente hoy

### Parte A: build y tests (paso 1 del plan)

Desde `codexrm-server`, con Docker Desktop abierto (Testcontainers
levanta PostgreSQL):

    mvn clean verify

Está verde si termina en `BUILD SUCCESS`, sin fallos, con al menos
142 tests unitarios y 36 de integración (línea base de entrada a
Fase 3, medida con los reportes de surefire y failsafe).

### Parte B: smoke test manual del `.jar` (pasos 3, 9 y 11)

1. Levantar la base (`docker compose up -d db`) y arrancar el `.jar`
   con el perfil `prod` y las variables de
   `docs/infra/environment-variables.md`.
2. `POST /api/auth/signin` con un usuario de prueba: debe devolver
   `200` con un token. El límite de rate limiting en `prod` es de
   5 intentos por minuto por IP.
3. En la consola, los logs deben salir en JSON, y los de una
   petición llevan `correlationId`.

## Relación con la CI

La CI ejecuta `mvn clean test` y
`mvn failsafe:integration-test failsafe:verify`. Son los mismos
tests que la Parte A, pero la CI no ejecuta la fase `package`: no
construye ni reempaqueta el `.jar`. Un problema de empaquetado (como
el `spring-boot-maven-plugin` sin versión, corregido en el gate de
entrada) no lo detecta la CI y sí lo detecta `mvn clean verify`.

## Pasos del plan que todavía no aplican

| Paso | Qué comprueba | Por qué no aplica hoy |
|---|---|---|
| 2 | `docker build` | Falta la imagen de producción (semanas 1-2) |
| 4 | `GET /actuator/health` | Falta Actuator, ver hallazgos |
| 5-8, 12 | `terraform fmt`, `validate`, `plan`, `apply`, `destroy` | Terraform entra en las semanas 5-8 |
| 10 | El import va a S3 | Es la tarea 3.5 |
| 13 | Repetir contra AWS real | Ventana de AWS real (semanas 11-13) |

## Hallazgos

- El `README.md` documenta `/actuator/health`, pero el `pom.xml` no
  incluye `spring-boot-starter-actuator`: el endpoint no existe hoy.
  El plan lo resuelve en las semanas 1-2.
- Existe el wrapper `mvnw`, pero ni la CI ni las instrucciones del
  proyecto lo usan: la CI usa el `mvn` del runner y en local se usa
  el `mvn` instalado. Se deja así; unificarlos para mayor
  reproducibilidad queda como decisión futura.