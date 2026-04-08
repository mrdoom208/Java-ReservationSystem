# -----------------------------
# Stage 1: Build the app
# -----------------------------
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom.xml first to leverage Docker cache
COPY pom.xml .
# Copy all source code
COPY src ./src

# Build the jar (skip tests for faster build)
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2: Run the app
# -----------------------------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-jar", "app.jar"]