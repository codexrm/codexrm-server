## JWT / Refresh Token Lifecycle Audit

### Expiration
- JWT: `codexrm.app.jwtExpirationMs` — 3,600,000 ms (1 hora), igual en
  dev/test/prod.
- Refresh token: `codexrm.app.jwtRefreshExpirationMs` — 864,000,000 ms
  (10 días), igual en dev/test/prod.

### Creation
- Un refresh token se crea en cada `POST /api/auth/signin`, vía
  `RefreshTokenService.createRefreshToken(userId)`.
- La tabla `refreshtoken` tiene `user_id UNIQUE` — diseño intencional
  de 1 token activo por usuario (`OneToOne`, según el comentario en
  la migración).

### 🔴 CRITICAL BUG FOUND: double login crashes with 500
`createRefreshToken()` siempre inserta un `RefreshToken` nuevo sin
verificar si el usuario ya tiene uno. Como `user_id` es `UNIQUE`, un
segundo `signin` del mismo usuario sin logout previo (login desde
otro dispositivo/pestaña, o reintento) rompe con:

ConstraintViolationException: duplicate key value violates unique
constraint "refreshtoken_user_id_key"


Confirmado en vivo vía Swagger UI — 500 real reproducido con 2
signins consecutivos del mismo usuario. No hay rotación ni reemplazo
del token existente; el segundo intento simplemente falla.

### Invalidation on logout
- `POST /api/auth/logout` (ya requiere JWT desde #150) llama
  `refreshTokenService.deleteByUserId(userId)`, que borra el/los
  refresh token(s) del usuario. Funciona correctamente cuando se usa.

### Expiration check
- `verifyExpiration()` chequea `expiryDate` contra `Instant.now()` y
  borra el token si venció, lanzando `TokenRefreshException` (mapea
  a 404 en `GlobalExceptionHandler`, per ADR-014's exception mapping
  — aunque semánticamente un token expirado es más un 401 que un
  404; a revisar si vale la pena ajustar, dado que no es
  "no encontrado" sino "encontrado pero inválido").

### No rotation on refresh
- `POST /api/auth/refresh-token` genera un JWT nuevo, pero **reutiliza
  el mismo refresh token** indefinidamente hasta que expira — no hay
  rotación (invalidar el viejo y emitir uno nuevo en cada uso). Esto
  es una práctica de seguridad débil: si un refresh token se filtra,
  sigue siendo válido hasta su expiración natural (10 días), sin
  ninguna señal de uso indebido.

## Summary of gaps to fix in Issue 2
1. **Critical**: double login must not crash with 500 — needs an
   upsert/replace strategy in `createRefreshToken()`.
2. **Medium**: no refresh token rotation on use — a filtered token
   stays valid up to 10 days with no detection mechanism.
3. **Minor, needs decision**: expired refresh token maps to 404
   (`ResourceNotFoundException`-style) via `TokenRefreshException`,
   when 401 might be more semantically correct — flagging for
   discussion, not necessarily a required fix.