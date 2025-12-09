# =================================================================

# Used for initial fetching of dependencies and setup
# =================================================================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and configuration files first (for efficient caching)
COPY mvnw .
COPY .mvn .mvn/
COPY pom.xml .

# Install dependencies (this layer is only rebuilt if pom.xml changes)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# =================================================================
# Stage 1: Test Execution (Unit & Integration Tests)
# =================================================================
FROM build AS test

# Copy all source code, including test sources
COPY src ./src

RUN ./mvnw test -Dmaven.test.failure.ignore=false

# =================================================================
# Stage 2: Package Stage
# Builds the final JAR (only runs if the 'test' stage passed)
# =================================================================
FROM build AS package

# Copy source code (needed for the package goal)
COPY src ./src

# Build the final JAR file, skipping tests this time
RUN ./mvnw clean package -DskipTests

# =================================================================
# Stage 4: Final Runtime Image
# A minimal JRE for running the application
# =================================================================
FROM eclipse-temurin:21-jre-alpine AS final

WORKDIR /app

# Copy the built JAR from the 'package' stage
COPY --from=package /app/target/*.jar /app/app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]