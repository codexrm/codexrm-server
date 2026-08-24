# Runbook — Refresh Token Failed

## Síntoma

- `POST /api/auth/refresh-token` devuelve `401`, `403` o `400` inesperado.
- El cliente reporta que el refresh token "dejó de funcionar".
- Logout parece no invalidar el refresh token o lo invalida demasiado pronto.

## Cómo reproducir

1. Hacer login y guardar JWT + refresh token.
2. Invocar `POST /api/auth/refresh-token` con token válido.
3. Repetir con token expirado, malformado o revocado.
4. Repetir después de `POST /api/auth/logout`.

## Qué mirar primero

- ¿El token existe en base de datos?
- ¿Está expirado según la política vigente?
- ¿Fue revocado por logout o por rotación (cada uso de
  `/refresh-token` invalida el token anterior y emite uno nuevo)?
- ¿La configuración de expiración corresponde al perfil actual?

## Logs y eventos esperados

- Evento de refresh exitoso o fallido con `correlationId`.
- Motivo general: expirado, ausente, revocado o inválido.
- Respuesta consistente con el contrato de error de auth
  (`ErrorResponse`, ver `docs/decision-log.md` ADR-011).

## Causas probables

- Expiración mal configurada por entorno.
- Refresh token revocado por logout, o invalidado por rotación tras
  un uso previo (comportamiento esperado desde la semana 4-6 de Fase 2).
- Divergencia entre la política documentada y la implementación real.
- Error en logout, rotación o persistencia del token.

## Cómo resolver

1. Confirmar el estado real del token en base de datos (tabla
   `refreshtoken`, columna `user_id` es única — solo 1 token activo
   por usuario).
2. Verificar expiración y timezone con datos concretos.
3. Revisar la política documentada en `docs/decision-log.md` y en
   `docs/security/jwt-refresh-token-audit.md`.
4. Reproducir con test de integración antes de corregir
   (`AuthIntegrationTest`).
5. Si el bug afecta logout o rotación, validar ambos flujos antes de
   cerrar.

## Cómo prevenir que vuelva a ocurrir

- Mantener tests para token válido, expirado, revocado, logout
  efectivo, doble login, y rotación (`AuthIntegrationTest`).
- No cambiar expiraciones sin actualizar configuración y docs.
- Registrar la política de lifecycle en `docs/decision-log.md`.

## Enlaces

- `AuthIntegrationTest` (tests de JWT/refresh lifecycle)
- `docs/security/jwt-refresh-token-audit.md`
- Configuración de expiraciones por entorno (`application-*.properties`)
- ADR de política de refresh token en `docs/decision-log.md`