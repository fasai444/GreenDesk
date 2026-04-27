# Stage 1: Build
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy all files
COPY . .

# Build application (skip tests for faster build)
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
