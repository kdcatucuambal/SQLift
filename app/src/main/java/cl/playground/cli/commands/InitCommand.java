package cl.playground.cli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InitCommand {
    public void run() {
        generateYamlFile();
        generateReadmeFile();
        System.out.println("✅ sqlift.yaml and sqlift.md files generated successfully");
    }

    private void generateYamlFile() {
        String yamlContent = """
            version: "1.0"
            sql:
              engine: "postgres"
              schema: "schema.sql"
              output:
                package: "com.example.project.target"
                lombok: true
            """;

        writeToFile("sqlift.yaml", yamlContent);
    }

    private void generateReadmeFile() {
        String readmeContent = """
            # SQLift Generator
            
            A tool to generate Java entity classes from SQL schema files.
            
            ## Configuration File Structure (sqlift.yaml)
            
            ```yaml
            version: "1.0"
            sql:
              engine: "postgres"  # Motor de base de datos
              schema: "schema.sql"  # Ruta al archivo de esquema SQL
              output:
                package: "com.example.project.target"  # Paquete base para las entidades
                lombok: true  # Activar/desactivar anotaciones de Lombok
            ```
            """;

        writeToFile("sqlift.md", readmeContent);
    }

    private void writeToFile(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
        } catch (IOException e) {
            System.err.println("❌ Error writing " + fileName + ": " + e.getMessage());
        }
    }
}
