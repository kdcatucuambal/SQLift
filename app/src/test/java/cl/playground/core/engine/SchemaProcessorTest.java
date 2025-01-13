package cl.playground.core.engine;

import cl.playground.core.model.ColumnMetadata;
import cl.playground.core.model.RelationMetadata;
import cl.playground.core.model.TableMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;


class SchemaProcessorTest {

    private SchemaProcessor schemaProcessor;
    private PostgresEngine postgresEngine;

    private final String sqlContent = """
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

    private final String TEST_SCHEMA_IMPOSSIBLE_CASE = """
        -- impossible.sql
            -- Schema con casos extremos de PRIMARY KEYs en PostgreSQL
            -- Este archivo contiene casos complejos y desafiantes de definiciones de claves primarias
        
            -- ===================================================================================
            -- Tabla 1: PRIMARY KEYs con múltiples definiciones y formatos mixtos
            -- ===================================================================================
            CREATE TABLE mixed_keys (
                id1 INTEGER     PRIMARY    KEY,                                    -- Espaciado irregular
                id2 INTEGER NOT NULL CONSTRAINT pk_mixed UNIQUE PRIMARY KEY,       -- Múltiples constraints inline
                id3 INTEGER\s
                    CONSTRAINT another_pk\s
                    PRIMARY\s
                    KEY,                                                          -- PK multilínea
                CONSTRAINT composite_pk PRIMARY KEY (id1, id2, id3)               -- PK compuesta redundante
            );
        
            -- ===================================================================================
            -- Tabla 2: PRIMARY KEYs con comentarios intercalados
            -- ===================================================================================
            CREATE TABLE commented_keys (
                /* Columna principal */
                user_id SERIAL,                                                   -- Auto-incrementing
                /* Segunda columna */
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                /* Tercera columna */
                action_type VARCHAR(50),
                /* Definición de primary key */
                CONSTRAINT\s
                    /* Nombre del constraint */
                    pk_commented\s
                    /* Tipo de constraint */
                    PRIMARY\s
                    /* Keyword final */
                    KEY\s
                    /* Columnas */
                    (user_id,\s
                     -- Primera columna
                     timestamp,\s
                     /* Segunda columna */
                     action_type)
            );
        
            -- ===================================================================================
            -- Tabla 3: PRIMARY KEYs con múltiples estilos de definición
            -- ===================================================================================
            CREATE TABLE multi_pk_styles (
                col1 INTEGER PRIMARY KEY,                                         -- Simple inline
                col2 INTEGER CONSTRAINT pk2 PRIMARY KEY,                          -- Con constraint inline
                col3 INTEGER,
                col4 INTEGER,
                col5 INTEGER     PRIMARY        KEY,                             -- Con espaciado extraño
                FOREIGN KEY (col3) REFERENCES other_table(id),                   -- FK para confundir
                PRIMARY KEY (col3),                                              -- PK sin nombre
                CONSTRAINT pk_custom PRIMARY KEY (col4),                         -- PK con nombre
                CONSTRAINT "pk-special.name" PRIMARY KEY (col5)                  -- PK con nombre especial
            );
        
            -- ===================================================================================
            -- Tabla 4: PRIMARY KEYs con nombres especiales y caracteres especiales
            -- ===================================================================================
            CREATE TABLE "complex.named_table" (
                "user.id" INTEGER,
                "timestamp.created" TIMESTAMP,
                "special-column" VARCHAR(50),
                CONSTRAINT "pk.with.dots" PRIMARY KEY ("user.id"),
                CONSTRAINT "pk-with-hyphens" PRIMARY KEY ("timestamp.created"),
                CONSTRAINT "pk_with_all.types-mixed" PRIMARY KEY ("special-column")
            );
        
            -- ===================================================================================
            -- Tabla 5: PRIMARY KEYs en diferentes posiciones y con diferentes tipos
            -- ===================================================================================
            CREATE TABLE mixed_positions (
                id1 UUID PRIMARY KEY,                                            -- UUID como PK
                id2 BIGINT,
                CONSTRAINT pk_mid PRIMARY KEY (id2),                            -- PK en medio
                id3 DECIMAL(20,2),
                id4 VARCHAR(100),
                PRIMARY KEY (id3, id4),                                         -- PK al final sin nombre
                CHECK (id2 > 0),                                                -- Otros constraints
                UNIQUE (id1, id2)
            );
        
            -- ===================================================================================
            -- Tabla 6: PRIMARY KEYs con tipos de datos complejos y arrays
            -- ===================================================================================
            CREATE TABLE complex_types (
                id1 INTEGER[],                                                  -- Array simple
                id2 NUMERIC(20,5)[],                                           -- Array con precisión
                id3 VARCHAR(100) ARRAY[3],                                     -- Array alterno
                id4 TIMESTAMP WITH TIME ZONE,
                CONSTRAINT pk_arrays PRIMARY KEY (id1, id2[1], id3[1]),        -- PK con arrays
                CONSTRAINT pk_timestamp PRIMARY KEY (id4)                       -- PK con timestamp
            );
    """;


    @BeforeEach
    void setUp() {
        postgresEngine = new PostgresEngine();
        schemaProcessor = new SchemaProcessor(postgresEngine);
    }


    @Test
    void processImpossiblesSchemma() {
        List<TableMetadata> tables = schemaProcessor.processSchema(sqlContent);

        for (TableMetadata table : tables) {
            System.out.println("\n=================================");
            System.out.println("Tabla: " + table.getTableName());
            System.out.println("---------------------------------");

            // Mostrar columnas y sus atributos
            if (table.getColumns().isEmpty()) {
                System.out.println("Sin columnas definidas.");
            } else {
                System.out.println("Columnas:");
                for (ColumnMetadata column : table.getColumns()) {
                    System.out.printf("  - %s (%s) %s%s%s\n",
                        column.getColumnName(),
                        column.getColumnType(),
                        column.isNotNull() ? "NOT NULL " : "",
                        column.isUnique() ? "UNIQUE " : "",
                        column.getDefaultValue() != null ? "DEFAULT " + column.getDefaultValue() : ""
                                     );
                }
            }

            // Mostrar claves primarias
            if (table.getPrimaryKeys().isEmpty()) {
                System.out.println("Sin claves primarias definidas.");
            } else {
                System.out.println("Claves primarias: " + String.join(", ", table.getPrimaryKeys()));
            }

            // Mostrar relaciones
            if (table.getRelations().isEmpty()) {
                System.out.println("Sin relaciones definidas.");
            } else {
                System.out.println("Relaciones:");
                for (RelationMetadata relation : table.getRelations()) {
                    System.out.printf("  - %s -> %s.%s (Many-to-One: %b)\n",
                        relation.getSourceColumn(),
                        relation.getTargetTable(),
                        relation.getTargetColumn(),
                        relation.isManyToOne()
                                     );
                }
            }

            System.out.println("=================================\n");
        }
    }
}
