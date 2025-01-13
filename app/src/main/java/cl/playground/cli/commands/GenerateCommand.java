package cl.playground.cli.commands;

import cl.playground.config.model.SqliftConfig;
import cl.playground.config.reader.YamlReader;
import cl.playground.core.engine.PostgresEngine;
import cl.playground.core.engine.SchemaProcessor;
import cl.playground.core.generator.EntityGenerator;
import cl.playground.core.model.TableMetadata;
import cl.playground.exception.ConfigurationException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateCommand {
    private static final String CONFIG_FILE = "sqlift.yaml";

    public void run() {
        try {
            String currentDir = System.getProperty("user.dir");
            File yamlFile = new File(currentDir, CONFIG_FILE);

            if (!yamlFile.exists()) {
                throw new ConfigurationException("Configuration file not found in directory: " + currentDir);
            }

            Map<String, Object> context = extractConfigContext(yamlFile.getPath());
            String sqlContent = cl.playground.core.reader.SqlReader.readSql((String) context.get("schema"));
            context.put("sqlContent", sqlContent);

            PostgresEngine engine = new PostgresEngine();
            SchemaProcessor schemaProcessor = new SchemaProcessor(engine);
            List<TableMetadata> tables = schemaProcessor.processSchema(sqlContent);

            EntityGenerator generator = new EntityGenerator((boolean) context.get("useLombok"));
            String packageName = (String) context.get("outputPackage");
            for (TableMetadata table : tables) {
                String entity = generator.generateEntity(table, packageName);
                writeEntityFile(packageName, generator.generateClassName(table.getTableName()), entity);
            }

            System.out.println("✅ Entities generated successfully!");

        } catch (ConfigurationException e) {
            System.err.println("❌ Configuration Error: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Schema Error: " + e.getMessage());

        } catch (Exception e) {
            throw new ConfigurationException("An unexpected error occurred during generation", e);
        }
    }

    private Map<String, Object> extractConfigContext(String configPath) {
        Map<String, Object> context = new HashMap<>();
        SqliftConfig config = YamlReader.readConfig(configPath);

        if (config == null || config.getSql() == null ||
            config.getSql().getEngine() == null || config.getSql().getSchema() == null ||
            config.getSql().getOutput() == null || config.getSql().getOutput().getPackageName() == null) {
            throw new ConfigurationException("Invalid YAML: Missing required fields.");
        }

        context.put("config", config);
        context.put("engine", config.getSql().getEngine());
        context.put("schema", config.getSql().getSchema());
        context.put("outputPackage", config.getSql().getOutput().getPackageName());
        context.put("useLombok", config.getSql().getOutput().isUseLombok());

        return context;
    }

    private void writeEntityFile(String packageName, String className, String content) throws Exception {
        String packagePath = packageName.replace('.', '/');
        Path directory = Paths.get("src/main/java", packagePath);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(className + ".java"), content);
    }
}
