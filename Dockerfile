# Imagen base con JVM ligera
FROM eclipse-temurin:17-jre

# Crear el directorio de trabajo
WORKDIR /workspace

# Variables de entorno
ARG JAR_FILE=app/build/libs/app.jar
ARG VERSION=latest

# Copiar el JAR al contenedor
COPY ${JAR_FILE} app.jar

# Etiqueta de la versión
LABEL version="${VERSION}"

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
