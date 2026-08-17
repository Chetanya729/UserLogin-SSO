# ---------- Stage 1: Build the jar ----------
FROM eclipse-temurin:26-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and pom first — Docker caches this layer if pom.xml doesn't change
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Now copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---------- Stage 2: Slim runtime image ----------
FROM eclipse-temurin:26-jre

WORKDIR /app

# Grab only the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
