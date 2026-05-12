# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml FIRST and resolve all dependencies.
# This layer is only invalidated when pom.xml changes,
# so source-only changes skip the full Maven download on rebuild.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source and compile.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

# Create a non-root system user — never run production services as root.
RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup --no-create-home appuser

WORKDIR /app

# Wildcard copy: resilient to version changes in pom.xml (no hardcoded filename).
COPY --from=builder /app/target/*.jar app.jar

# Drop privileges before starting the process.
USER appuser

EXPOSE 8080

# JVM flags for containerized deployments:
#   -XX:+UseContainerSupport      → read cgroup limits (enabled by default in JDK 17+, explicit is best practice)
#   -XX:MaxRAMPercentage=75.0     → use at most 75% of container memory limit for heap
#   -Djava.security.egd=...       → use /dev/urandom to avoid blocking on entropy at startup
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]