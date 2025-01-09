# Imagen base con JVM ligera
FROM eclipse-temurin:17-jre

# Crear el directorio de trabajo
WORKDIR /workspace

# Etiqueta de la versión
ARG VERSION=latest
LABEL version="${VERSION}"

# Copiar el JAR al contenedor
COPY app/build/libs/*.jar /app.jar

# Comando de inicio
ENTRYPOINT ["java", "-jar", "/app.jar"]
