CREATE TABLE SUCURSAL (
    id SERIAL,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (id)
);

CREATE TABLE CATEGORIA (
    id SERIAL,
    nombre VARCHAR(50) NOT NULL,
    descripcion TEXT,
    stock_minimo INTEGER DEFAULT 10,
    PRIMARY KEY (id)
);

CREATE TABLE PROVEEDOR (
    id SERIAL,
    nombre VARCHAR(100) NOT NULL,
    rut VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (id)
);

CREATE TABLE PRODUCTO (
    id SERIAL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio_compra DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    categoria_id INTEGER NOT NULL,
    proveedor_id INTEGER NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (id),
    FOREIGN KEY (categoria_id) REFERENCES CATEGORIA(id) ON DELETE RESTRICT,
    FOREIGN KEY (proveedor_id) REFERENCES PROVEEDOR(id) ON DELETE RESTRICT
);

CREATE TABLE STOCK_SUCURSAL (
    sucursal_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 0,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sucursal_id, producto_id),
    FOREIGN KEY (sucursal_id) REFERENCES SUCURSAL(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES PRODUCTO(id) ON DELETE RESTRICT
);

CREATE TABLE MOVIMIENTO (
    id SERIAL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    sucursal_origen_id INTEGER NOT NULL,
    sucursal_destino_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL,
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    PRIMARY KEY (id),
    FOREIGN KEY (sucursal_origen_id) REFERENCES SUCURSAL(id) ON DELETE RESTRICT,
    FOREIGN KEY (sucursal_destino_id) REFERENCES SUCURSAL(id) ON DELETE RESTRICT,
    FOREIGN KEY (producto_id) REFERENCES PRODUCTO(id) ON DELETE RESTRICT
);

CREATE TABLE COMPRA_PROVEEDOR (
    id SERIAL,
    proveedor_id INTEGER NOT NULL,
    fecha_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    numero_factura VARCHAR(50),
    total DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (proveedor_id) REFERENCES PROVEEDOR(id) ON DELETE RESTRICT
);

CREATE TABLE DETALLE_COMPRA (
    compra_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (compra_id, producto_id),
    FOREIGN KEY (compra_id) REFERENCES COMPRA_PROVEEDOR(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES PRODUCTO(id) ON DELETE RESTRICT
);