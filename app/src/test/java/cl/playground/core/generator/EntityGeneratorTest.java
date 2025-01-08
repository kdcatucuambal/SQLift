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
        URL resourceUrl = getClass().getClassLoader().getResource("alumnos.sql");
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
}