# Stage 1: Build the application
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.9_9_1.9.7_2.13.12 AS builder

WORKDIR /app

# Copy only the dependency-related files first
COPY project project
COPY build.sbt .

# Run sbt update to cache dependencies
RUN sbt update

# Copy the rest of the project
COPY . .

# Clean and build the application
RUN sbt clean assembly

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /app/target/scala-2.13/app.jar /app/app.jar

# Create required directories
RUN mkdir -p /app/logs /app/conversations && \
    chmod 777 /app/logs /app/conversations

# Ensure stdout is properly handled
ENV JAVA_OPTS="-Dlogback.configurationFile=/app/logback.xml"

# Set the entry point
ENTRYPOINT ["java", "-jar", "/app/app.jar"]