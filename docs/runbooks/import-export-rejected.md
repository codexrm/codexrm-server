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

Eventos de negocio emitidos por `ReferenceController`
(catálogo completo en `docs/observability/event-catalog.md`):

- `event=reference.import outcome=success` (INFO), con `username`,
  `format` y `count`.
- `event=reference.import outcome=rejected reason=empty_file` (WARN):
  archivo vacío.
- `event=reference.import outcome=rejected reason=unsupported_format`
  (WARN), con `format`: formato no soportado.
- `event=reference.export outcome=success` (INFO), con `username`,
  `format` y `count`.

Estos eventos nunca registran el contenido del archivo ni el nombre
que envió el cliente.

Sin evento propio: los errores de parseo del archivo y otros rechazos
de validación no emiten un evento de negocio. Solo se ven como
respuesta `ErrorResponse` (ADR-011), con el `correlationId` en el
header `X-Correlation-Id`.

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