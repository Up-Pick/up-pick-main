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
COPY api-gateway ./api-gateway

# Build the application
WORKDIR /app
RUN ./gradlew :api-gateway:build -x test

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/api-gateway/build/libs/*.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
