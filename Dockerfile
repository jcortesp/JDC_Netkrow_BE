# 1) Build con Maven + JDK17 (usamos la imagen oficial maven:3.10.1 que incluye JDK 17)
FROM maven:3.10.1 AS builder
WORKDIR /app

# Copiamos pom, el wrapper y la carpeta .mvn para aprovechar caché y bajar dependencias
COPY pom.xml mvnw .mvn/ ./
RUN mvn dependency:go-offline -B

# Copiamos el resto del código y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# 2) Runtime con JRE 17
FROM eclipse-temurin:17-jre-focal AS runtime
WORKDIR /app

# Copiamos el JAR empaquetado desde el builder
COPY --from=builder /app/target/netkrow-backend-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto que Render le pasará en la variable $PORT
EXPOSE 8080

# Arrancamos Spring Boot indicando que use $PORT
ENTRYPOINT ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
