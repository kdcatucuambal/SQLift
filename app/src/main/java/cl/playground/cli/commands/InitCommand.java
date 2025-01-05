package cl.playground.cli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import picocli.CommandLine.Command;

@Command(name = "init", description = "Initialize SQLift configuration files")
public class InitCommand implements Runnable {

    @Override
    public void run() {
        generateYamlFile();
        generateReadmeFile();
        System.out.println("✅ sqlift.yaml and README.md files generated successfully");
    }

    private void generateYamlFile() {
        String yamlContent = "version: \"1.0\"\n" +
                "sql:\n" +
                "  engine: \"postgres\"\n" +
                "  schema: \"path/to/your/schema.sql\"\n" +
                "  output:\n" +
                "    package: \"com.example.entities\"\n" +
                "    lombok: true\n";

        writeToFile("sqlift.yaml", yamlContent);
    }

    private void generateReadmeFile() {
        String readmeContent = "# SQLift Generator\n\n" +
                "A tool to generate Java entity classes from SQL schema files.\n\n" +
                "## Configuration\n\n" +
                "Edit the `sqlift.yaml` file with the following structure:\n\n" +
                "```yaml\n" +
                "version: \"1.0\"\n" +
                "sql:\n" +
                "  engine: \"postgres\"       # Database engine\n" +
                "  schema: \"path/to/your/schema.sql\"  # Path to your SQL schema file\n" +
                "  output:\n" +
                "    package: \"com.example.entities\"  # Base package for generated entities\n" +
                "    lombok: true                        # Enable/disable Lombok annotations\n" +
                "```\n\n" +
                "### Configuration Options:\n\n" +
                "- `version`: Configuration version (currently \"1.0\")\n" +
                "- `sql`:\n" +
                "  - `engine`: Database engine (e.g., postgresql, mysql, etc.)\n" +
                "  - `schema`: Path to your SQL schema file\n" +
                "  - `output`:\n" +
                "    - `package`: Base package for generated entity classes\n" +
                "    - `lombok`: Boolean flag to enable or disable Lombok annotations\n\n" +
                "## Supported Features\n\n" +
                "- Supported Databases:\n" +
                "  - PostgreSQL\n" +
                "  - MySQL (Coming soon)\n" +
                "  - Oracle (Coming soon)\n" +
                "  - SQL Server (Coming soon)\n\n" +
                "- Code Generation Options:\n" +
                "  - Lombok annotations (@Data, @Getter, @Setter, etc.)\n" +
                "  - JPA annotations (@Entity, @Table, @Column, etc.)\n" +
                "    - Jakarta EE (jakarta.persistence.*)\n" +
                "    - Java EE (javax.persistence.*)\n\n" +
                "## Usage\n\n" +
                "1. Initialize the configuration:\n" +
                "   ```bash\n" +
                "   sqlift init\n" +
                "   ```\n\n" +
                "2. Edit the `sqlift.yaml` file with your configuration:\n" +
                "   - Specify the database engine (`engine`), schema file path (`schema`), and output package (`package`).\n\n"
                +
                "3. Generate the entities:\n" +
                "   ```bash\n" +
                "   sqlift generate\n" +
                "   ```\n\n" +
                "4. The generated classes will be saved in the specified package directory under `src/main/java`.\n";

        writeToFile("README.md", readmeContent);
    }

    private void writeToFile(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
        } catch (IOException e) {
            System.err.println("❌ Error writing " + fileName + ": " + e.getMessage());
        }
    }
}