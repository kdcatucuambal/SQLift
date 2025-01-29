# SQLift

## Official Page

[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/andressep95/sqlift-install)
[![License](https://img.shields.io/badge/license-MIT-green)](https://github.com/andressep95/sqlift-install/blob/main/LICENSE)

[SQLift](https://andressep95.github.io/sqlift-install) - Don’t forget to leave your ⭐ and report issues!

## Introduction

SQLift is a tool that simplifies mapping SQL queries to Java objects. You can install and use it in two ways: through a
native installer or by using Docker.

## Supported Architectures

### Native Executables

| Operating System | Architecture | Status  |
|------------------|--------------|---------|
| macOS            | ARM64        | ✅       |
| macOS            | AMD64        | ✅       |
| Linux            | AMD64        | ✅       |
| Windows          | Any          | ⚠️ Only via Docker |

## Installation Methods

### 1. Native Installer

This method installs SQLift directly on your system, providing a simpler usage of commands.

#### Requirements

- macOS (Apple Silicon or Intel) or Linux
- Access to terminal with installation permissions

#### Installation Steps

**For macOS:**

```bash
curl -fsSL https://raw.githubusercontent.com/andressep95/sqlift-install/main/macos-install.sh | bash
```

**For Linux:**

```bash
curl -fsSL https://raw.githubusercontent.com/andressep95/sqlift-install/main/linux-install.sh | bash
```

The installer:

- Downloads the binary for your system
- Installs it in ~/.sqlift
- Adds it to your PATH

#### Verification

```bash
sqlift --version
```

#### Basic Commands

```bash
sqlift init        # Initialize the configuration
sqlift generate    # Generate the entities
```

### 2. Using Docker

This method requires Docker to be installed, but offers greater portability.

#### Requirements

- Docker installed and running
- Permissions to run Docker commands
---

### **Installation and Usage Steps**

#### **1. Download the image**

```bash
docker pull ghcr.io/andressep95/sqlift-cli:latest
```

---

### **Linux/macOS**

#### **Initialize configuration**

```bash
docker run --rm -v $(pwd):/workspace ghcr.io/andressep95/sqlift-cli:latest init /workspace
```

#### **Generate entities**

```bash
docker run --rm -v $(pwd):/workspace ghcr.io/andressep95/sqlift-cli:latest generate /workspace
```

#### **Interactive mode**

```bash
docker run -it -v $(pwd):/workspace ghcr.io/andressep95/sqlift-cli:latest
```

---

### **Windows (PowerShell)**

#### **Initialize configuration**

```bash
docker run --rm -v ${PWD}:/workspace ghcr.io/andressep95/sqlift-cli:latest init /workspace
```

#### **Generate entities**

```bash
docker run --rm -v ${PWD}:/workspace ghcr.io/andressep95/sqlift-cli:latest generate /workspace
```

#### **Interactive mode**

```bash
docker run -it -v ${PWD}:/workspace ghcr.io/andressep95/sqlift-cli:latest
``` 

## Configuration (Common for both methods)

After installation, you need to configure SQLift using the sqlift.yml file:

```yaml
version: "1.0"
sql:
  engine: "postgres"  # Database engine
  schema: "schema.sql"  # Path to the SQL schema file
  output:
    package: "cl.playground.projectname.target"  # Base package for the entities
    lombok: true  # Enable/disable Lombok annotations
```

## Required Schema Structure

```sql
CREATE TABLE sucursales (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE stock_sucursales (
    id BIGINT PRIMARY KEY,
    cantidad INT NOT NULL,
    sucursal_id BIGINT,
    FOREIGN KEY (sucursal_id) REFERENCES sucursales(id)
);

CREATE TABLE movimientos (
    id BIGINT PRIMARY KEY,
    tipo_movimiento VARCHAR(50) NOT NULL,
    sucursal_origen_id BIGINT,
    sucursal_destino_id BIGINT,
    FOREIGN KEY (sucursal_origen_id) REFERENCES sucursales(id),
    FOREIGN KEY (sucursal_destino_id) REFERENCES sucursales(id)
);

``` 
