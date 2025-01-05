package cl.playground.cli.commands;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.playground.config.model.SqliftConfig;
import cl.playground.config.reader.YamlReader;
import cl.playground.core.engine.PostgresEngine;
import cl.playground.core.engine.SchemaProcessor;
import cl.playground.core.generator.EntityGenerator;
import cl.playground.core.model.TableMetadata;
import cl.playground.core.reader.SqlReader;
import cl.playground.exception.ConfigurationException;
import cl.playground.util.LogContent;
import picocli.CommandLine.Command;

@Command(name = "generate", description = "Generate Java entity classes from SQL schema")
public class GenerateCommand implements Runnable {

    private static final String CONFIG_FILE = "sqlift.yaml";

    @Override
    public void run() {
        try {
            // Buscar archivo en ubicacion actual
            String currentDir = System.getProperty("user.dir");
            File yamlFile = new File(currentDir, CONFIG_FILE);

            if (!yamlFile.exists()) {
                System.out.println("Configuration file not found in directory: " + currentDir);
                return;
            }

            // Leer configuracion y almacenarla
            Map<String, Object> context = extractConfigContext(yamlFile.getPath());

            // Mostrar configuracion
            LogContent.logConfiguration(context);

            // Leer y mostrar el contenido SQL
            String sqlContent = SqlReader.readSql((String) context.get("schema"));
            LogContent.logSqlContent(sqlContent);

            // Agregar contenido SQL al contexto
            context.put("sqlContent", sqlContent);

            // Preparar clases para procesar informacion SQL
            PostgresEngine engine = new PostgresEngine();
            SchemaProcessor schemaProcessor = new SchemaProcessor(engine);
            List<TableMetadata> tables = schemaProcessor.processSchema(sqlContent);

            EntityGenerator generator = new EntityGenerator((boolean) context.get("useLombok"));
            String packageName = (String) context.get("outputPackage");
            for (TableMetadata table : tables) {
                String entity = generator.generateEntity(table, packageName); // Pasar el paquete al generador
                writeEntityFile(packageName, generator.generateClassName(table.getTableName()), entity);
            }

            System.out.println("✅ Entities generated successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            throw new ConfigurationException("Failed to read configuration", e);
        }

    }

    private Map<String, Object> extractConfigContext(String configPath) {
        // Abrimos un sitio donde guardar informacion
        Map<String, Object> context = new HashMap<>();

        // Almacenamos la configuracion
        SqliftConfig config = YamlReader.readConfig(configPath);

        // Validar configuracion requerida
        if (config == null ||
                config.getSql() == null ||
                config.getSql().getEngine() == null ||
                config.getSql().getSchema() == null ||
                config.getSql().getOutput() == null ||
                config.getSql().getOutput().getPackageName() == null) {
            throw new ConfigurationException("Invalid YAML: Missing required fields.");

        }

        // Almacenar informacion
        context.put("config", config);
        context.put("engine", config.getSql().getEngine());
        context.put("schema", config.getSql().getSchema());
        context.put("outputPackage", config.getSql().getOutput().getPackageName());
        context.put("useLombok", config.getSql().getOutput().isUseLombok());

        // Presentar informacion
        System.out.println("✔ YAML configuration loaded successfully.");
        System.out.println(config.toString());

        return context;
    }

    private void writeEntityFile(String packageName, String className, String content)
            throws java.io.IOException {
        String packagePath = packageName.replace('.', '/');
        Path directory = Paths.get("src/main/java", packagePath);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(className + ".java"), content);
    }

}
