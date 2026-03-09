# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# copiar todo el proyecto (necesario para multi-module)
COPY . .

# compilar solo el server y sus dependencias
RUN mvn -pl codexrm-server -am clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jdk

WORKDIR /app

# copiar el jar generado
COPY --from=builder /app/codexrm-server/target/server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]