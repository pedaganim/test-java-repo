# --- Build stage ---
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Gradle wrapper and build files first to leverage Docker cache
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Copy sources
COPY src ./src

# Build application JAR (skip tests for faster image builds)
RUN ./gradlew --no-daemon clean bootJar -x test

# --- Runtime stage ---
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built jar
COPY --from=build /app/build/libs/*.jar app.jar

# Default to 8080
EXPOSE 8080

# Profile can be overridden: dev | test | pg
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java","-jar","/app/app.jar"]
