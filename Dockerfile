# 1) Build con Maven + JDK17
FROM maven:3.10.1-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# 2) Contenedor final con JRE17
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/target/netkrow-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java","-jar","app.jar"]
