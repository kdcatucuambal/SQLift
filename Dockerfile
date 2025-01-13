# Etapa 1: Build
FROM eclipse-temurin:17-jdk as builder

# Crear el directorio de trabajo
WORKDIR /workspace

# Copiar el proyecto completo al contenedor
COPY . .

# Construir el JAR usando Gradle
RUN ./gradlew clean build -x test

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre

# Crear el directorio de trabajo
WORKDIR /workspace

# Etiqueta de la versión
ARG VERSION=latest
LABEL version="${VERSION}"

# Copiar el JAR generado desde la etapa de construcción
COPY --from=builder /workspace/app/build/libs/*.jar /app.jar

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app.jar"]
