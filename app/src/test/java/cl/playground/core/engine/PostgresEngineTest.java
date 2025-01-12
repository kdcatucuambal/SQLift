package cl.playground.core.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

}