# Stage 1: Build the application
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.9_9_1.9.7_2.12.18 as builder

WORKDIR /app

# Copy only the dependency-related files first
COPY build.sbt ./
COPY project ./project/

# Run sbt update to cache dependencies
RUN sbt update

# Copy the source code
COPY src ./src/

# Build the application with cached dependencies
RUN sbt clean assembly

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy only the assembled jar
COPY --from=builder /app/target/scala-2.12/app.jar ./app.jar

EXPOSE 8000

CMD ["java", "-jar", "app.jar"]