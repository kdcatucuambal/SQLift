CREATE TABLE USUARIOS (
    usuario_id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) UNIQUE NOT NULL,
    fecha_creacion DATE DEFAULT CURRENT_DATE
);

CREATE TABLE PRODUCTOS (
    producto_id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE,
    precio NUMERIC(10, 2) NOT NULL CHECK (precio >= 0),
    stock INTEGER DEFAULT 0 CHECK (stock >= 0),
    fecha_creacion DATE DEFAULT CURRENT_DATE
);

CREATE TABLE ORDEN (
    usuario_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    fecha_orden TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, producto_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios (usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos (producto_id) ON DELETE RESTRICT
);