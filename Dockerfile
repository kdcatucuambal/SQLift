# Imagen base ligera
FROM debian:buster-slim

# Instalar dependencias necesarias para el binario nativo
RUN apt-get update && apt-get install -y libz-dev libstdc++6 && apt-get clean

# Variables de entorno
ARG BINARY_PATH=/sqlift
ARG VERSION=latest

# Establecer el directorio de trabajo
WORKDIR /workspace

# Copiar el binario nativo al contenedor
COPY ${BINARY_PATH} /usr/local/bin/sqlift
RUN chmod +x /usr/local/bin/sqlift

# Etiqueta de la versión de la CLI
LABEL version="${VERSION}"

# Punto de entrada
ENTRYPOINT ["/usr/local/bin/sqlift"]
