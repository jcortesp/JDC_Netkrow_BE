# 1) Build con Maven Wrapper + JDK17
FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app

# 1.1 Copia mvnw y pom.xml
COPY mvnw pom.xml ./

# 1.2 Copia TODO el directorio .mvn (incluye wrapper jar, properties, etc)
COPY .mvn .mvn

# 1.3 Da permisos y descarga deps
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# 1.4 Copia el resto del código y compila
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# 2) Runtime con JRE 17
 FROM eclipse-temurin:17-jre-focal AS runtime
 WORKDIR /app

 # Copiamos el JAR empaquetado desde el builder
 COPY --from=builder /app/target/netkrow-backend-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto 8080 (fijo)
EXPOSE 8080

# Arrancamos Spring Boot escuchando siempre en el 8080
ENTRYPOINT ["java","-Dserver.port=8080","-jar","/app/app.jar"]