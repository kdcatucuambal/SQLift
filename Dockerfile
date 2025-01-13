# Etapa 1: Build
FROM eclipse-temurin:17-jdk as builder

# Crear el directorio de trabajo
WORKDIR /workspace

# Copiar dependencias de Gradle para cachear
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
RUN ./gradlew --no-daemon dependencies || true

# Copiar el resto del proyecto
COPY . .

# Construir el JAR
RUN ./gradlew clean build -x test

# Etapa 2: Runtime
FROM gcr.io/distroless/java17-debian11:nonroot

# Crear el directorio de trabajo
WORKDIR /workspace

# Etiqueta de la versión
ARG VERSION=latest
LABEL version="${VERSION}"

# Copiar el JAR generado
COPY --from=builder /workspace/app/build/libs/*.jar /app.jar

# Comando predeterminado
ENTRYPOINT ["java", "-jar", "/app.jar"]
