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

# Copy batch module
COPY uppick-batch-service ./uppick-batch-service

# Build the application
WORKDIR /app
RUN ./gradlew :uppick-batch-service:build -x test

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/uppick-batch-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
