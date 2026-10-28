# syntax=docker/dockerfile:1

# ---------- Stage 1: build the JAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only pom.xml first so this dependency layer is cached
# and only re-runs when pom.xml changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the source and build (tests already run in the Jenkins Test stage).
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: slim runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create an unprivileged user and group to run the app.
RUN addgroup -S spring && adduser -S spring -G spring

# Copy ONLY the built JAR from the build stage.
COPY --from=build /workspace/target/calculator-api.jar app.jar

USER spring
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
