# 1) Build con Maven + JDK17
FROM maven:3.10.1-openjdk-17 AS builder
WORKDIR /app

# Copiamos pom y wrapper para descargar dependencias offline
COPY pom.xml mvnw .mvn/ ./
RUN mvn dependency:go-offline -B

# Copiamos el resto y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# 2) Runtime con JRE 17
FROM eclipse-temurin:17-jre-focal AS runtime
WORKDIR /app

# Copiamos el JAR empaquetado desde el builder
COPY --from=builder /app/target/netkrow-backend-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto que Render asignará vía $PORT
EXPOSE 8080

# Arrancamos Spring Boot indicando que use $PORT
ENTRYPOINT ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
