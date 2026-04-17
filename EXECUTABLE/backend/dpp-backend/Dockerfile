# Build stage (mit Dependency-Cache)
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

# 1. Nur pom.xml + Dependencies (ändert sich selten → Docker cached das!)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Source kopieren + bauen (ändert sich oft → nur das wird neu gebaut)
COPY src ./src
RUN mvn -B package -DskipTests

# Runtime stage (noch kleiner mit Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/target/dpp-backend-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]