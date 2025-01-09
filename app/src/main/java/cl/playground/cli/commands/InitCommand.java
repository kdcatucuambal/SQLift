package cl.playground.cli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InitCommand {
    public void run() {
        generateYamlFile();
        generateReadmeFile();
        System.out.println("✅ sqlift.yaml and README.md files generated successfully");
    }

    private void generateYamlFile() {
        String yamlContent = """
            version: "1.0"
            sql:
              engine: "postgres"
              schema: "path/to/your/schema.sql"
              output:
                package: "com.example.entities"
                lombok: true
            """;

        writeToFile("sqlift.yaml", yamlContent);
    }

    private void generateReadmeFile() {
        String readmeContent = """
            # SQLift Generator
            
            A tool to generate Java entity classes from SQL schema files.
            
            ## Configuration File Structure (sqlift.yml)
            
            ```yaml
            version: "1.0"              # Version number (required)
            
            sql:
              engine: "postgres"        # Database engine: postgres, mysql (required)
              schema: "schema.sql"      # Path to your SQL schema file (required)
              output:
                package: "com.example"  # Base package for generated entities (required)
                lombok: true           # Use Lombok annotations (optional)
                jpa: "jakarta"         # JPA annotations style: jakarta/javax (optional)
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
