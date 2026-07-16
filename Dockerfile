# --- STAGE 1: Build Java Application ---
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and dependency definitions first to utilize docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source files and build target executable
COPY src ./src
RUN mvn package -DskipTests -B

# --- STAGE 2: Lightweight Production Runtime JRE ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Expose server port
EXPOSE 8080

# Copy target built jar from the builder stage
COPY --from=builder /app/target/hrms-0.0.1-SNAPSHOT.jar app.jar

# Run the spring application jar
ENTRYPOINT ["java", "-jar", "app.jar"]
