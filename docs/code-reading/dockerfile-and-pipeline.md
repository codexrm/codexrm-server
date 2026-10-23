# Code reading: Dockerfile and pipeline of hapi-fhir-jpaserver-starter

## What I read
- Repository: hapifhir/hapi-fhir-jpaserver-starter (Spring Boot + JPA server, active project; last commits seen on 2026-10-22).
- Files: `Dockerfile`, `.github/workflows/build-images.yaml`, `.github/workflows/maven.yml`.
- Not read: `smoke-tests.yml`, `spotless-check.yml`, `chart-*.yaml`, `stale.yaml`, `docker-compose.yml`.

## What I understood
### Dockerfile
- Four stages: `build-hapi` (Maven + JDK 17, compiles), `build-distroless` (repackages and compiles a small HealthCheck class), `tomcat` (optional variant), `default` (final image).
- Layer order: `pom.xml` is copied and dependencies are downloaded (`dependency:go-offline`) before `src/` is copied. A code-only change reuses the dependency layer.
- Final image: distroless Java 21, non-root user (UID 65532, numeric so Kubernetes can verify it). Maven, JDK and sources stay in the build stages.
- Versions are pinned (Maven 3.9.12, agent 2.24.0), no moving tags.
- Ownership and permissions are prepared while root, before switching to the non-root user.

### Pipeline
- `build-images.yaml`: builds on pull requests (no login, no push) and publishes only when a Git tag `image/v*` is pushed. The image tag comes from the Git tag.
- Registry credentials live in GitHub Secrets and are referenced as `secrets.*`, never written in the file.
- Docker layers are cached between runs with `actions/cache`, because every run starts on an empty machine.
- `maven.yml`: runs on every branch and on pull requests; builds the jar with `mvn -B package verify`; no dependency cache; `MAVEN_OPTS=-Xmx4g` as a memory fix.
- Image publishing and tests are separate workflows; from the files read, neither waits for the other.

## What I did not understand
- The Tomcat variant and why they keep two images.
- The OpenTelemetry agent: I understand it collects traces and metrics without changing application code, but I did not find how it is activated in this project (the `ENTRYPOINT` has no `-javaagent` flag).
- `-Djdk.lang.Process.launchMechanism=vfork`: I did not understand which problem it solves; it looks like an environment-specific workaround.
- `PropertiesLauncher`: I understand it lets them add extra classes to a packaged WAR; a normal executable jar does not need it.
- Multi-architecture builds (QEMU, `linux/arm64`).
- Where the container health probe is configured (the `HealthCheck` class exists, but there is no `HEALTHCHECK` line in the Dockerfile).

## What I can apply in codexrm-server
| Idea | Where |
|---|---|
| Copy `pom.xml` and resolve dependencies before `src/` | Multi-stage issue |
| Pin base image versions | Multi-stage issue |
| Final stage without Maven, sources or build tools | Multi-stage issue |
| Non-root numeric UID, prepare permissions before `USER` | Runtime hardening issue |
| Build the image on pull requests without publishing | CI alignment (backlog) |
| `package` in CI to catch packaging errors | CI alignment (backlog) |
| `'**'` branch trigger to cover `dev` | CI alignment (backlog) |
| Publish only on an explicit version | Phase 3 deployment pipeline |
| Cache Docker layers between CI runs | To decide |

### Differences with codexrm-server today
| Topic | This project | codexrm-server today |
|---|---|---|
| Layer order | `pom.xml` first | `COPY . .` first |
| Final image | Distroless, only what is needed | Full JDK |
| User | Non-root | Root |
| Dependencies | Public repositories, no credentials | GitHub Packages, needs a build secret |
| `.dockerignore` | Present | Missing |
| CI builds the image | Yes (no publish) | No |

## What design decisions they made and whether I agree
- Distroless final image: I agree on security (less to attack), but it removes the shell and tools like `curl`, so they had to write their own health check class. For this project I will evaluate it in the hardening issue.
- Publishing only on tags: I agree, publishing is a deliberate act.
- Tests and image build as independent workflows: I would not copy this. Deployment should depend on tests passing.
- Optional Tomcat variant and OpenTelemetry agent: not relevant to our scope.