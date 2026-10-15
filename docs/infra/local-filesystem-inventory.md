# Local filesystem state inventory

Inventory made on 2026-10-11 by searching all of `src/main` (Java
code and resources) and `docker-compose.yml`. It is only an inventory:
the resolution belongs to task 3.5 of the Phase 3 plan.

## Findings

| # | Location | What it does | Lifecycle |
|---|---|---|---|
| 1 | `ReferenceController.java:55` | Defines `UPLOADED_FOLDER = java.io.tmpdir + "/tempUpload"` | Constant |
| 2 | `ReferenceController.java:352-358` (`saveUploadedFile`, used at `:264`) | Import: creates the folder and writes the uploaded file | One request; deleted in `finally` (`:279-280`) |
| 3 | `ReferenceController.java:268` | Import: the library reads the file by its path | Same request |
| 4 | `ReferenceController.java:309-315` and `ExportR.java:31` | Export: creates the folder and the library writes the file to the path | One request |
| 5 | `ReferenceController.java:317` | Export: reads the file with `Files.readAllBytes` and returns it | Deleted in `finally` (`:326-327`) |
| 6 | `application.properties:4-5` | 10 MB upload limit | Configuration |

## Observations

- All disk use lives inside a single request and is cleaned up in
  `finally`. There is no persistent state between requests.
- `file.delete()` does not check the result. If the process dies in
  the middle of a request, the file stays until the container is
  restarted.
- The calls to the import/export library receive a file path. Task 3.5
  must check whether it offers a stream variant or whether a
  temporary file is kept.
- By default, Spring Boot may write the parts of a multipart upload to
  the temporary directory. This is an implicit dependency that does
  not appear in the project's code. It was not verified in this issue.
- The Phase 3 plan describes the constant as `"tempUpload"`
  (relative). The real code uses `java.io.tmpdir`.

## What was not found

- No other use of `File`, `Files` or `Paths` outside
  `ReferenceController`, `ExportR` and `ReferenceService`.
- No filesystem cache.
- File logging: `logback-spring.xml` has no `<file>` or
  `FileAppender`, so logs go to the console.
- Volumes for the app in `docker-compose.yml`. The only volume is
  `postgres_data`, for the database, which in the cloud will be RDS.

## Priority

Medium. In a normal container with a writable `/tmp`, the app
works. But the container's disk is ephemeral and is not shared
between copies, and the plan asks to remove this dependency in
task 3.5.