# SQLift

## Pagina Oficial

[SQLift](https://andressep95.github.io/sqlift-install) - ¡No olvides dejar tu ⭐ y reportar issues!

## Introducción

SQLift es una herramienta que facilita el mapeo de consultas SQL a objetos Java. Puedes instalarla y utilizarla de dos
formas: mediante un instalador nativo o usando Docker.

## Métodos de Instalación

### 1. Instalador Nativo

Este método instala SQLift directamente en tu sistema, permitiendo un uso más simple de los comandos.

#### Requisitos

- macOS (Apple Silicon o Intel) o Linux
- Acceso a terminal con permisos de instalación

#### Pasos de Instalación

**Para macOS:**

```bash
curl -fsSL https://raw.githubusercontent.com/andressep95/sqlift-install/main/macos-install.sh | bash
```

**Para Linux:**

```bash
curl -fsSL https://raw.githubusercontent.com/andressep95/sqlift-install/main/linux-install.sh | bash
```

El instalador:

- Descarga el binario correspondiente a tu sistema
- Lo instala en `~/.sqlift`
- Lo agrega a tu `PATH`

#### Verificación

```bash
sqlift --version
```

#### Comandos Básicos

```bash
sqlift init        # Inicializa la configuración
sqlift generate    # Genera las entidades
```

### 2. Usando Docker

Este método requiere Docker instalado pero ofrece mayor portabilidad.

#### Requisitos

- Docker instalado y en ejecución
- Permisos para ejecutar comandos Docker

---

### **Pasos de Instalación y Uso**

#### **1. Descargar la imagen**

```bash
docker pull ghcr.io/andressep95/sqlift-cli:latest
```

---

### **Linux/macOS**

#### **Inicializar configuración**

```bash
docker run --rm -v $(pwd):/workspace ghcr.io/andressep95/sqlift-cli:latest init /workspace
```

#### **Generar entidades**

```bash
docker run --rm -v $(pwd):/workspace ghcr.io/andressep95/sqlift-cli:latest generate /workspace
```

#### **Modo interactivo**

```bash
docker run -it -v $(pwd):/workspace ghcr.io/andressep95/sqlift-cli:latest
```

---

### **Windows (PowerShell)**

#### **Inicializar configuración**

```bash
docker run --rm -v ${PWD}:/workspace ghcr.io/andressep95/sqlift-cli:latest init /workspace
```

#### **Generar entidades**

```bash
docker run --rm -v ${PWD}:/workspace ghcr.io/andressep95/sqlift-cli:latest generate /workspace
```

#### **Modo interactivo**

```bash
docker run -it -v ${PWD}:/workspace ghcr.io/andressep95/sqlift-cli:latest
``` 

## Configuración (Común para ambos métodos)

Después de la instalación, deberás configurar SQLift mediante el archivo `sqlift.yml`:

```yaml
version: "1.0"
sql:
  engine: "postgres"  # Motor de base de datos
  schema: "schema.sql"  # Ruta al archivo de esquema SQL
  output:
    package: "cl.playground.SpringSecurityBackend.model"  # Paquete base para las entidades
    lombok: true  # Activar/desactivar anotaciones de Lombok
```

## Características Soportadas

### Motores de Base de Datos

- PostgreSQL ✅
- MySQL (Próximamente)
- Oracle (Próximamente)
- SQL Server (Próximamente)

### Generación de Código

- Anotaciones Lombok (@Data, @Getter, @Setter)
- Anotaciones JPA:
    - Jakarta EE (@Entity, @Table, @Column)
    - Java EE (javax.persistence.*)