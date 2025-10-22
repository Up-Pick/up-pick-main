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

# Copy main module
COPY uppick-main ./uppick-main

# Build the application
WORKDIR /app
RUN ./gradlew :uppick-main:build -x test -x asciidoctor -x generateRestDocsIndex

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/uppick-main/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
