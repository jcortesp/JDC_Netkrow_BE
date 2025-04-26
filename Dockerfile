# 1) Build con Maven Wrapper + JDK17
FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app

# Copiamos mvnw, .mvn y pom.xml
COPY mvnw .mvn pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiamos el código y compilamos
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# 2) Runtime con JRE 17
FROM eclipse-temurin:17-jre-focal AS runtime
WORKDIR /app

# Copiamos el JAR desde el builder
COPY --from=builder /app/target/netkrow-backend-0.0.1-SNAPSHOT.jar app.jar

# Exponer y arrancar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
