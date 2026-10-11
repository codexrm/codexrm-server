# Inventario de estado en el filesystem local

Inventario hecho el 2026-10-11 buscando en todo `src/main` (código
Java y recursos) y en `docker-compose.yml`. Es solo un inventario:
la resolución corresponde a la tarea 3.5 del plan de Fase 3.

## Hallazgos

| # | Ubicación | Qué hace | Ciclo de vida |
|---|---|---|---|
| 1 | `ReferenceController.java:55` | Define `UPLOADED_FOLDER = java.io.tmpdir + "/tempUpload"` | Constante |
| 2 | `ReferenceController.java:352-358` (`saveUploadedFile`, usado en `:264`) | Import: crea la carpeta y escribe el archivo subido | Una petición; se borra en `finally` (`:279-280`) |
| 3 | `ReferenceController.java:268` | Import: la librería lee el archivo por su ruta | Misma petición |
| 4 | `ReferenceController.java:309-315` y `ExportR.java:31` | Export: crea la carpeta y la librería escribe el archivo en la ruta | Una petición |
| 5 | `ReferenceController.java:317` | Export: lee el archivo con `Files.readAllBytes` y lo devuelve | Se borra en `finally` (`:326-327`) |
| 6 | `application.properties:4-5` | Límite de subida de 10 MB | Configuración |

## Observaciones

- Todo el uso de disco vive dentro de una sola petición y se limpia
  en `finally`. No hay estado persistente entre peticiones.
- `file.delete()` no comprueba el resultado. Si el proceso muere a
  mitad de una petición, el archivo queda hasta que se reinicie el
  contenedor.
- Las llamadas a la librería de import/export reciben una ruta de
  archivo. Hay que revisar en la tarea 3.5 si ofrece una variante con
  streams o si se mantiene un archivo temporal.
- Por defecto, Spring Boot puede escribir las partes de una subida
  multipart en el directorio temporal. Es una dependencia implícita
  que no aparece en el código del proyecto. No se verificó en este
  issue.
- El plan de Fase 3 describe la constante como `"tempUpload"`
  (relativa). El código real usa `java.io.tmpdir`.

## Lo que no se encontró

- Ningún otro uso de `File`, `Files` o `Paths` fuera de
  `ReferenceController`, `ExportR` y `ReferenceService`.
- Ninguna caché en filesystem.
- Logs a archivo: `logback-spring.xml` no tiene `<file>` ni
  `FileAppender`, así que los logs salen por consola.
- Volúmenes para la app en `docker-compose.yml`. El único volumen es
  `postgres_data`, de la base de datos, que en la nube será RDS.

## Prioridad

Media. En un contenedor normal con `/tmp` escribible, la app
funciona. Pero el disco del contenedor es efímero y no se comparte
entre copias, y el plan pide eliminar esta dependencia en la
tarea 3.5.