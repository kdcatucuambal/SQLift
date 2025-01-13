# Etapa Runtime
FROM gcr.io/distroless/java17-debian11:nonroot

WORKDIR /app

# Etiqueta de la versión
ARG VERSION=latest
LABEL version="${VERSION}"

# Copiar el JAR del contexto de build (será descargado por GH Actions)
COPY app/build/libs/app.jar /app/app.jar

# Comando predeterminado
ENTRYPOINT ["java", "-jar", "/app/app.jar"]