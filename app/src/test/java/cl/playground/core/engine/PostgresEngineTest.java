package cl.playground.core.engine;

import cl.playground.core.model.TableMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgresEngineTest {

    private PostgresEngine engine;
    private final String TEST_SCHEMA = """
            -- Tabla 1: PRIMARY KEY simple básica
            CREATE TABLE users (
                id INTEGER PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                email VARCHAR(100)
            );
        
            -- Tabla 2: PRIMARY KEY con SERIAL
            CREATE TABLE products (
                product_id SERIAL PRIMARY KEY,
                name VARCHAR(100),
                price NUMERIC(10,2)
            );
        
            -- Tabla 3: PRIMARY KEY con CONSTRAINT nombrado
            CREATE TABLE categories (
                category_id INTEGER CONSTRAINT pk_category PRIMARY KEY,
                name VARCHAR(50),
                description TEXT
            );
        
            -- Tabla 4: PRIMARY KEY con NOT NULL y UNIQUE
            CREATE TABLE customers (
                customer_id UUID NOT NULL UNIQUE PRIMARY KEY,
                name VARCHAR(100),
                address TEXT
            );
        
            -- Tabla 5: PRIMARY KEY compuesta con CONSTRAINT
            CREATE TABLE order_items (
                order_id INTEGER,
                product_id INTEGER,
                quantity INTEGER,
                price NUMERIC(10,2),
                CONSTRAINT pk_order_items PRIMARY KEY (order_id, product_id)
            );
        
            -- Tabla 6: PRIMARY KEY compuesta sin nombre de CONSTRAINT
            CREATE TABLE inventory_movements (
                product_id INTEGER,
                warehouse_id INTEGER,
                movement_date TIMESTAMP,
                quantity INTEGER,
                PRIMARY KEY (product_id, warehouse_id, movement_date)
            );
        
            -- Tabla 7: PRIMARY KEY con tipo numérico con precisión
            CREATE TABLE financial_records (
                transaction_id NUMERIC(20,0) PRIMARY KEY,
                amount NUMERIC(15,2),
                transaction_date TIMESTAMP
            );
        
            -- Tabla 8: PRIMARY KEY con múltiples constraints
            CREATE TABLE employees (
                employee_id INTEGER NOT NULL CONSTRAINT emp_pk PRIMARY KEY,
                email VARCHAR(100) UNIQUE,
                hire_date DATE NOT NULL
            );
        
            -- Tabla 9: PRIMARY KEY tipo BIGINT con espacios adicionales
            CREATE TABLE logs (
                log_id BIGINT     PRIMARY    KEY,
                log_date TIMESTAMP,
                message TEXT
            );
        """;

    private final String TEST_SCHEMA_COMPOSITE = """
            -- Tabla 1: PK compuesta básica con CONSTRAINT
            CREATE TABLE order_items (
                order_id INTEGER,
                product_id INTEGER,
                quantity INTEGER,
                CONSTRAINT pk_order_items PRIMARY KEY (order_id, product_id)
            );
        
            -- Tabla 2: PK compuesta sin nombre de CONSTRAINT
            CREATE TABLE inventory_log (
                product_id INTEGER,
                date_time TIMESTAMP,
                warehouse_id INTEGER,
                quantity INTEGER,
                PRIMARY KEY (product_id, date_time, warehouse_id)
            );
        
            -- Tabla 3: PK compuesta con columnas inline y constraint
            CREATE TABLE document_versions (
                doc_id INTEGER PRIMARY KEY,
                version_id INTEGER,
                content TEXT,
                CONSTRAINT pk_version PRIMARY KEY (version_id)
            );
        
            -- Tabla 4: PK compuesta con múltiples constraints
            CREATE TABLE shipment_details (
                shipment_id INTEGER,
                container_id INTEGER,
                product_id INTEGER,
                CONSTRAINT pk_shipment PRIMARY KEY (shipment_id),
                CONSTRAINT pk_container PRIMARY KEY (container_id, product_id)
            );
        
            -- Tabla 5: PK compuesta con NOT NULL y otros modificadores
            CREATE TABLE financial_transactions (
                account_id INTEGER NOT NULL,
                transaction_date TIMESTAMP NOT NULL,
                sequence_number INTEGER NOT NULL,
                amount NUMERIC(10,2),
                CONSTRAINT pk_transaction PRIMARY KEY (account_id, transaction_date, sequence_number)
            );
        
            -- Tabla 6: PK compuesta con espaciado irregular
            CREATE TABLE audit_log (
                entity_id     INTEGER,
                action_type   VARCHAR(50),
                timestamp    TIMESTAMP,
                CONSTRAINT    pk_audit    PRIMARY    KEY    (   entity_id   ,    action_type,timestamp   )
            );
        """;

    private final String TEST_SCHEMA_IMPOSSIBLE = """
        -- impossible.sql
        -- Schema con casos extremos de PRIMARY KEYs en PostgreSQL
        -- Este archivo contiene casos complejos y desafiantes de definiciones de claves primarias
        
        -- ===================================================================================
        -- Tabla 1: PRIMARY KEYs con múltiples definiciones y formatos mixtos
        -- ===================================================================================
        CREATE TABLE mixed_keys (
            id1 INTEGER     PRIMARY    KEY,                                    -- Espaciado irregular
            id2 INTEGER NOT NULL CONSTRAINT pk_mixed UNIQUE PRIMARY KEY,       -- Múltiples constraints inline
            id3 INTEGER 
                CONSTRAINT another_pk 
                PRIMARY 
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
            CONSTRAINT 
                /* Nombre del constraint */
                pk_commented 
                /* Tipo de constraint */
                PRIMARY 
                /* Keyword final */
                KEY 
                /* Columnas */
                (user_id, 
                 -- Primera columna
                 timestamp, 
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

    private final String TEST_SCHEMA_RELATIONS1 = """
            CREATE TABLE users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE
            );

            CREATE TABLE posts (
                id SERIAL PRIMARY KEY,
                user_id INT NOT NULL,
                title VARCHAR(255),
                FOREIGN KEY (user_id) REFERENCES users (id)
            );

            CREATE TABLE comments (
                id SERIAL PRIMARY KEY,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                content TEXT,
                FOREIGN KEY (post_id) REFERENCES posts (id),
                FOREIGN KEY (user_id) REFERENCES users (id)
            );

            CREATE TABLE orders (
                id SERIAL PRIMARY KEY,
                customer_id INT,
                FOREIGN KEY (customer_id) REFERENCES customers (id)
            );
            """;

    private final String TEST_SCHEMA_RELATIONS_IMPOSSIBLE ="""
    CREATE TABLE authors (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL
    );

    CREATE TABLE books (
        id SERIAL PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        author_id INT NOT NULL,
        FOREIGN KEY (author_id) REFERENCES authors (id)
    );

    CREATE TABLE publishers (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL
    );

    CREATE TABLE book_publishers (
        book_id INT NOT NULL,
        publisher_id INT NOT NULL,
        PRIMARY KEY (book_id, publisher_id),
        FOREIGN KEY (book_id) REFERENCES books (id),
        FOREIGN KEY (publisher_id) REFERENCES publishers (id)
    );

    CREATE TABLE reviews (
        id SERIAL PRIMARY KEY,
        book_id INT NOT NULL,
        reviewer_name VARCHAR(100),
        rating INT,
        FOREIGN KEY (book_id) REFERENCES books (id)
    );

    CREATE TABLE categories (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL
    );

    CREATE TABLE book_categories (
        book_id INT NOT NULL,
        category_id INT NOT NULL,
        PRIMARY KEY (book_id, category_id),
        FOREIGN KEY (book_id) REFERENCES books (id),
        FOREIGN KEY (category_id) REFERENCES categories (id)
    );

    CREATE TABLE libraries (
        id SERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL
    );

    CREATE TABLE library_books (
        library_id INT NOT NULL,
        book_id INT NOT NULL,
        quantity INT NOT NULL,
        PRIMARY KEY (library_id, book_id),
        FOREIGN KEY (library_id) REFERENCES libraries (id),
        FOREIGN KEY (book_id) REFERENCES books (id)
    );

    CREATE TABLE orders (
        id SERIAL PRIMARY KEY,
        library_id INT NOT NULL,
        FOREIGN KEY (library_id) REFERENCES libraries (id)
    );

    CREATE TABLE order_items (
        order_id INT NOT NULL,
        book_id INT NOT NULL,
        quantity INT NOT NULL,
        PRIMARY KEY (order_id, book_id),
        FOREIGN KEY (order_id) REFERENCES orders (id),
        FOREIGN KEY (book_id) REFERENCES books (id)
    );
    """;


    @BeforeEach
    void setUp() {
        engine = new PostgresEngine();
    }

    @Test
    void extractPrimaryKeyColumnsTest() {
        // Extraer todas las declaraciones CREATE TABLE
        List<String> statements = engine.extractCreateTableStatements(TEST_SCHEMA);

        for (int i = 0; i < statements.size(); i++) {
            String statement = statements.get(i);
            String tableName = engine.extractTableName(statement);
            List<String> primaryKeys = engine.extractPrimaryKeyColumns(statement);

            System.out.println("\n=================================");
            System.out.println("Tabla " + (i + 1) + ": " + tableName);
            System.out.println("---------------------------------");
            System.out.println("CREATE TABLE statement:");
            System.out.println(statement.trim());
            System.out.println("---------------------------------");
            System.out.println("Primary Keys encontradas: " + primaryKeys);
            System.out.println("=================================\n");

            // Realizar las verificaciones según el caso
            switch (i) {
                case 0: // users
                    assertEquals(List.of("id"), primaryKeys,
                        "Debería extraer PK simple básica");
                    break;
                case 1: // products
                    assertEquals(List.of("product_id"), primaryKeys,
                        "Debería extraer PK con SERIAL");
                    break;
                case 2: // categories
                    assertEquals(List.of("category_id"), primaryKeys,
                        "Debería extraer PK con CONSTRAINT nombrado");
                    break;
                case 3: // customers
                    assertEquals(List.of("customer_id"), primaryKeys,
                        "Debería extraer PK con NOT NULL y UNIQUE");
                    break;
                case 4: // order_items
                    assertEquals(List.of("order_id", "product_id"), primaryKeys,
                        "Debería extraer PK compuesta con CONSTRAINT");
                    break;
                case 5: // inventory_movements
                    assertEquals(List.of("product_id", "warehouse_id", "movement_date"), primaryKeys,
                        "Debería extraer PK compuesta sin nombre de CONSTRAINT");
                    break;
                case 6: // financial_records
                    assertEquals(List.of("transaction_id"), primaryKeys,
                        "Debería extraer PK con tipo numérico con precisión");
                    break;
                case 7: // employees
                    assertEquals(List.of("employee_id"), primaryKeys,
                        "Debería extraer PK con múltiples constraints");
                    break;
                case 8: // logs
                    assertEquals(List.of("log_id"), primaryKeys,
                        "Debería extraer PK con espacios adicionales");
                    break;
            }
        }
    }

    @Test
    void testCompositePrimaryKeys() {
        List<String> statements = engine.extractCreateTableStatements(TEST_SCHEMA_COMPOSITE);

        for (int i = 0; i < statements.size(); i++) {
            String statement = statements.get(i);
            String tableName = engine.extractTableName(statement);
            List<String> primaryKeys = engine.extractPrimaryKeyColumns(statement);

            System.out.println("\n=================================");
            System.out.println("Tabla " + (i + 1) + ": " + tableName);
            System.out.println("---------------------------------");
            System.out.println("CREATE TABLE statement:");
            System.out.println(statement.trim());
            System.out.println("---------------------------------");
            System.out.println("Primary Keys encontradas: " + primaryKeys);
            System.out.println("=================================\n");

            switch (i) {
                case 0: // order_items
                    assertEquals(List.of("order_id", "product_id"), primaryKeys,
                        "Debería extraer PK compuesta con CONSTRAINT");
                    break;

                case 1: // inventory_log
                    assertEquals(List.of("product_id", "date_time", "warehouse_id"), primaryKeys,
                        "Debería extraer PK compuesta sin nombre de CONSTRAINT");
                    break;

                case 2: // document_versions
                    assertEquals(List.of("doc_id", "version_id"), primaryKeys,
                        "Debería extraer PK compuesta con mezcla de inline y constraint");
                    break;

                case 3: // shipment_details
                    assertEquals(List.of("shipment_id", "container_id", "product_id"), primaryKeys,
                        "Debería extraer PK compuesta con múltiples constraints");
                    break;

                case 4: // financial_transactions
                    assertEquals(List.of("account_id", "transaction_date", "sequence_number"), primaryKeys,
                        "Debería extraer PK compuesta con NOT NULL");
                    break;

                case 5: // audit_log
                    assertEquals(List.of("entity_id", "action_type", "timestamp"), primaryKeys,
                        "Debería extraer PK compuesta con espaciado irregular");
                    break;
            }
        }
    }

    @Test
    void testImpossiblePrimaryKeys() {
        List<String> statements = engine.extractCreateTableStatements(TEST_SCHEMA_IMPOSSIBLE);

        for (int i = 0; i < statements.size(); i++) {
            String statement = statements.get(i);
            String tableName = engine.extractTableName(statement);
            List<String> primaryKeys = engine.extractPrimaryKeyColumns(statement);
            List<String> expectedKeys;

            System.out.println("\n=================================");
            System.out.println("Tabla " + (i + 1) + ": " + tableName);
            System.out.println("---------------------------------");
            System.out.println("CREATE TABLE statement:");
            System.out.println(statement.trim());
            System.out.println("---------------------------------");
            System.out.println("Primary Keys encontradas: " + primaryKeys);
            System.out.println("=================================\n");

            switch (i) {
                case 0: // mixed_keys
                    expectedKeys = List.of("id1", "id2", "id3");
                    assertPrimaryKeys(expectedKeys, primaryKeys, "mixed_keys");
                    break;

                case 1: // commented_keys
                    expectedKeys = List.of("user_id", "timestamp", "action_type");
                    assertPrimaryKeys(expectedKeys, primaryKeys, "commented_keys");
                    break;

                case 2: // multi_pk_styles
                    expectedKeys = List.of("col1", "col2", "col3", "col4", "col5");
                    assertPrimaryKeys(expectedKeys, primaryKeys, "multi_pk_styles");
                    break;

                case 3: // complex.named_table
                    expectedKeys = List.of("user.id", "timestamp.created", "special-column");
                    assertPrimaryKeys(expectedKeys, primaryKeys, "complex.named_table");
                    break;

                case 4: // mixed_positions
                    expectedKeys = List.of("id1", "id2", "id3", "id4");
                    assertPrimaryKeys(expectedKeys, primaryKeys, "mixed_positions");
                    break;

                case 5: // complex_types
                    expectedKeys = List.of("id1", "id2[1]", "id3[1]", "id4");
                    assertPrimaryKeys(expectedKeys, primaryKeys, "complex_types");
                    break;
            }
        }
    }

    private void assertPrimaryKeys(List<String> expected, List<String> actual, String tableName) {
        assertEquals(
            expected.size(),
            actual.size(),
            String.format("Tabla %s: Número incorrecto de PKs.", tableName)
                    );

        assertTrue(
            new HashSet<>(actual).containsAll(expected),
            String.format("Tabla %s: No se encontraron todas las PKs esperadas.\nEsperadas: %s\nEncontradas: %s",
                tableName, expected, actual)
                  );
    }

    @Test
    void testExtractTableRelations() {

        List<String> statements = engine.extractCreateTableStatements(TEST_SCHEMA_RELATIONS_IMPOSSIBLE);

        for (String statement : statements) {
            String tableName = engine.extractTableName(statement);
            List<String> relations = engine.extractTableRelations(statement);

            System.out.println("\n=================================");
            System.out.println("Evaluando sentencia: \n" + statement.trim());
            System.out.println("---------------------------------");
            System.out.println("Tabla: " + tableName);
            System.out.println("Relaciones encontradas: " + relations);
            System.out.println("=================================\n");

            // Validar que se extrajeron relaciones correctamente
            assertNotNull(relations, "Las relaciones no deberían ser nulas para la tabla: " + tableName);
            relations.forEach(relation -> assertFalse(relation.isBlank(), "Una relación extraída está vacía para la tabla: " + tableName));
        }
    }
}