# Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew ./
COPY gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle ./
COPY build.gradle ./

# Copy common module
COPY uppick-common ./uppick-common

# Copy api-gateway module
COPY uppick-api-gateway ./uppick-api-gateway

# Build the application
WORKDIR /app
RUN ./gradlew :uppick-api-gateway:build -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/uppick-api-gateway/build/libs/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
