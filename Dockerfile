# Imagen base ligera
FROM alpine:latest

# Crear el directorio de trabajo
WORKDIR /workspace

# Variables de entorno
ARG BINARY_PATH=/sqlift
ARG VERSION=latest

# Copiar el binario nativo estático al contenedor
COPY ${BINARY_PATH} /usr/local/bin/sqlift
RUN chmod +x /usr/local/bin/sqlift

# Etiqueta de la versión de la CLI
LABEL version="${VERSION}"

# Punto de entrada
ENTRYPOINT ["/usr/local/bin/sqlift"]
