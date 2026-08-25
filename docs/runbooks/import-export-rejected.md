# Runbook — Import/Export Rejected

## Síntoma

- `POST /api/references/import` rechaza un archivo válido.
- `POST /api/references/import` acepta o intenta procesar un nombre sospechoso.
- `POST /api/references/export` falla con nombre inválido o descarga corrupta.

## Cómo reproducir

1. Importar un archivo válido del formato soportado.
2. Repetir con archivo vacío.
3. Repetir con nombre malicioso como `../evil.ris`.
4. Repetir con extensión o MIME no permitido.
5. Exportar con nombre normal y luego con nombre inválido.

## Qué mirar primero

- ¿El archivo cumple allowlist de formato y tamaño?
- ¿El nombre final lo genera el servidor o se sigue usando el del cliente?
- ¿La ruta final se mantiene dentro del directorio temporal permitido?
- ¿Se limpian los temporales incluso cuando falla el proceso?

## Logs y eventos esperados

> ⚠️ Pendiente: logs estructurados con evento dedicado de
> import/export y `correlationId` end-to-end están planeados para las
> semanas 7-9 de Fase 2 (observabilidad). Hoy, el único log disponible
> es el mensaje de error estándar en `ErrorResponse` (ver
> `docs/decision-log.md` ADR-011) — sin evento de negocio dedicado
> todavía.

- Si hubo bloqueo de seguridad, motivo general sin exponer contenido
  del archivo (garantizado hoy vía `GlobalExceptionHandler`).

## Causas probables

- Path traversal o canonicalización incorrecta.
- Tipo o tamaño no permitido.
- Cleanup incompleto de temporales.
- Rechazo por validación del parser/formato.
- `Content-Disposition` o nombre de export no saneado.

## Cómo resolver

1. Confirmar si el rechazo es de seguridad, de formato o de parser.
2. Verificar la ruta final normalizada y el nombre físico generado.
3. Revisar límites por entorno y metadatos del archivo.
4. Reproducir con test de integración mínimo antes del fix.
5. Validar import y export después del cambio, no solo uno de los dos.

## Cómo prevenir que vuelva a ocurrir

- Mantener tests con nombres maliciosos, archivo vacío y formato inválido.
- No usar `originalFilename` o `fileName` del cliente como path final.
- Documentar mitigaciones y deuda residual en `docs/decision-log.md`.

## Enlaces

- `ImportExportSecurityIntegrationTest` (tests de import/export seguros)
- ADR-017 en `docs/decision-log.md` (decisión OWASP de import/export)