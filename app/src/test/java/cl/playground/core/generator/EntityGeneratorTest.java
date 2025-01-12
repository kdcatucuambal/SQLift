package cl.playground.core.generator;

import cl.playground.core.engine.PostgresEngine;
import cl.playground.core.engine.SchemaProcessor;
import cl.playground.core.model.TableMetadata;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityGeneratorTest {

    @Test
    public void testEntityGeneration() throws IOException, URISyntaxException {
        // 1. Leer el schema SQL desde los recursos de prueba
        URL resourceUrl = getClass().getClassLoader().getResource("impossible.sql");
        if (resourceUrl == null) {
            fail("No se pudo encontrar el archivo schema.sql en los recursos de prueba");
        }

        String sqlContent = Files.readString(Path.of(resourceUrl.toURI()));

        // El resto del código sigue igual...
        PostgresEngine engine = new PostgresEngine();
        SchemaProcessor schemaProcessor = new SchemaProcessor(engine);
        List<TableMetadata> tables = schemaProcessor.processSchema(sqlContent);

        EntityGenerator entityGenerator = new EntityGenerator(true);
        String packageOutput = "cl.playground.alumnos.entities";

        tables.forEach(table -> {
            System.out.println("\n=== Entidad generada para " + table.getTableName() + " ===\n");
            String entityCode = entityGenerator.generateEntity(table, packageOutput);
            System.out.println(entityCode);
            System.out.println("\n=== Fin de la entidad ===\n");
        });
    }


    @Test
    public void testEntity() {
        // Schema SQL como string
        String sqlContent = """
            CREATE TABLE SUCURSALES (
                id SERIAL,
                nombre VARCHAR(100) NOT NULL,
                direccion VARCHAR(200) NOT NULL,
                telefono VARCHAR(20),
                email VARCHAR(100),
                activo BOOLEAN DEFAULT TRUE,
                PRIMARY KEY (id)
            );
            CREATE TABLE CATEGORIAS (
                id SERIAL,
                nombre VARCHAR(50) NOT NULL,
                descripcion TEXT,
                stock_minimo INTEGER DEFAULT 10,
                PRIMARY KEY (id)
            );
            CREATE TABLE PROVEEDORES (
                id SERIAL,
                nombre VARCHAR(100) NOT NULL,
                rut VARCHAR(20) NOT NULL UNIQUE,
                direccion VARCHAR(200),
                telefono VARCHAR(20),
                email VARCHAR(100),
                activo BOOLEAN DEFAULT TRUE,
                PRIMARY KEY (id)
            );
            CREATE TABLE PRODUCTOS (
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
                FOREIGN KEY (categoria_id) REFERENCES CATEGORIAS(id) ON DELETE RESTRICT,
                FOREIGN KEY (proveedor_id) REFERENCES PROVEEDORES(id) ON DELETE RESTRICT
            );
            CREATE TABLE STOCK_SUCURSAL (
                sucursal_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL DEFAULT 0,
                ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (sucursal_id, producto_id),
                FOREIGN KEY (sucursal_id) REFERENCES SUCURSALES(id) ON DELETE CASCADE,
                FOREIGN KEY (producto_id) REFERENCES PRODUCTOS(id) ON DELETE RESTRICT
            );
            CREATE TABLE MOVIMIENTOS (
                id SERIAL,
                tipo_movimiento VARCHAR(20) NOT NULL,
                sucursal_origen_id INTEGER NOT NULL,
                sucursal_destino_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                observacion TEXT,
                PRIMARY KEY (id),
                FOREIGN KEY (sucursal_origen_id) REFERENCES SUCURSALES(id) ON DELETE RESTRICT,
                FOREIGN KEY (sucursal_destino_id) REFERENCES SUCURSALES(id) ON DELETE RESTRICT,
                FOREIGN KEY (producto_id) REFERENCES PRODUCTOS(id) ON DELETE RESTRICT
            );
            CREATE TABLE COMPRAS_PROVEEDOR (
                id SERIAL,
                proveedor_id INTEGER NOT NULL,
                fecha_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                numero_factura VARCHAR(50),
                total DECIMAL(10,2) NOT NULL,
                PRIMARY KEY (id),
                FOREIGN KEY (proveedor_id) REFERENCES PROVEEDORES(id) ON DELETE RESTRICT
            );
            CREATE TABLE DETALLE_COMPRA (
                compra_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                cantidad INTEGER NOT NULL,
                precio_unitario DECIMAL(10,2) NOT NULL,
                subtotal DECIMAL(10,2) NOT NULL,
                PRIMARY KEY (compra_id, producto_id),
                FOREIGN KEY (compra_id) REFERENCES COMPRAS_PROVEEDOR(id) ON DELETE CASCADE,
                FOREIGN KEY (producto_id) REFERENCES PRODUCTOS(id) ON DELETE RESTRICT
            );
            """;

        PostgresEngine engine = new PostgresEngine();
        SchemaProcessor schemaProcessor = new SchemaProcessor(engine);
        List<TableMetadata> tables = schemaProcessor.processSchema(sqlContent);

        EntityGenerator entityGenerator = new EntityGenerator(true);
        String packageOutput = "cl.inventario.entities";

        tables.forEach(table -> {
            System.out.println("\n=== Entidad generada para " + table.getTableName() + " ===\n");
            String entityCode = entityGenerator.generateEntity(table, packageOutput);
            System.out.println(entityCode);
            System.out.println("\n=== Fin de la entidad ===\n");
        });
    }
}