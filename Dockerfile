# Etapa 1: Build
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copiamos wrapper y pom
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw

# Descarga dependencias principales (sin el go-offline asesino)
RUN ./mvnw -B -DskipTests dependency:resolve dependency:resolve-plugins

# Copiamos el código fuente
COPY src src

# Compilamos la app
RUN ./mvnw -B -DskipTests package

# Etapa 2: Run
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia el JAR generado (ajustamos con wildcard)
COPY --from=build /app/target/*SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
