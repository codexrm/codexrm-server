# ---------- Build stage ----------
FROM maven:3.9.12-eclipse-temurin-21@sha256:c3c9d3ac4ce8431a3995c0318b8d390f448e693dd4fabc16e9b68d2e1f3d7b46 AS build

WORKDIR /app

# Dependency layer: only pom.xml is copied, so this layer is reused
# until the pom changes. The GitHub Packages credentials come from a
# BuildKit secret and are never stored in the image.
COPY pom.xml .
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -B -ntp dependency:go-offline

# Code layer: only this layer is rebuilt when the source changes.
COPY src ./src
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    mvn -B -ntp clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:21.0.12.1_1-jre@sha256:f37956012e0cb88400e5b3689aeae2d2d735364c1922ae2225536163b5a99f56

# Unprivileged user with a fixed numeric UID, so orchestrators can
# verify that the container does not run as root.
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app

# Only the jar is copied: no Maven, no JDK, no sources.
# It stays owned by root and read-only for the application user.
COPY --from=build /app/target/server-0.0.1-SNAPSHOT.jar app.jar

# Record which commit built the image.
ARG GIT_SHA=unknown
LABEL org.opencontainers.image.revision=$GIT_SHA

USER 10001:10001

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS -o /dev/null http://localhost:8080/actuator/health || exit 1

# JVM memory as a percentage of the container limit, not a fixed value.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]