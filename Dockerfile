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

WORKDIR /app

# Only the jar is copied: no Maven, no JDK, no sources.
COPY --from=build /app/target/server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]