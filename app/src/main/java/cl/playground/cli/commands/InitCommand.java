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
            
            SQLift is a tool that generates Java entity classes from an SQL schema.
            
            ## Configuration File Structure (sqlift.yml)
            
            After running `sqlift init`, a `sqlift.yml` file will be created that needs to be configured. Here is an example of how it should look:
            
            ```yaml
            version: "1.0"
            sql:
                engine: "postgres"  # Database engine (only postgres supported at the moment)
                schema: "schema.sql"  # Path to the SQL schema file
                output:
                    package: "com.example.project.target"  # Base package for the generated entities
                    lombok: true  # Enable/disable Lombok annotations
            ```
            
            **Field descriptions**:
            - `engine`: Defines the database engine (e.g., `postgres`, `mysql`).
            - `schema`: Path to the `.sql` file containing the database schema.
            - `output`: Defines the package where the Java entities will be generated and whether Lombok annotations are enabled.
            
            ## SQL Schema Structure
            
            For SQLift to work properly, the SQL schema must have the relationships between tables defined within the `CREATE TABLE` statements, not through `ALTER TABLE` commands.
            
            Here is an example of a compatible SQL schema:
            
            ```sql
            CREATE TABLE branches (
                id BIGINT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                address VARCHAR(200) NOT NULL,
                phone VARCHAR(20),
                email VARCHAR(100),
                active BOOLEAN DEFAULT TRUE
            );
            
            CREATE TABLE branch_stock (
                id BIGINT PRIMARY KEY,
                quantity INT NOT NULL,
                branch_id BIGINT,
                FOREIGN KEY (branch_id) REFERENCES branches(id)
            );
            
            CREATE TABLE movements (
                id BIGINT PRIMARY KEY,
                movement_type VARCHAR(50) NOT NULL,
                origin_branch_id BIGINT,
                destination_branch_id BIGINT,
                FOREIGN KEY (origin_branch_id) REFERENCES branches(id),
                FOREIGN KEY (destination_branch_id) REFERENCES branches(id)
            );
            ```
            
            **Important points**:
            - Relationships between tables must be defined within the `CREATE TABLE` statement, as shown in the examples above.
            - Be sure to use foreign keys (`FOREIGN KEY`) in the appropriate place to establish relationships between tables.
            
            With this configuration and the correct schema, you can run `sqlift generate` to generate the corresponding Java entity classes.
            
            Ready to get started!
            """;

        // Eliminar el espaciado hacia la izquierda en cada línea
        String formattedContent = readmeContent.stripIndent();

        writeToFile("sqlift.md", formattedContent);
    }


    private void writeToFile(String fileName, String content) {
        try {
            Files.write(Paths.get(fileName), content.getBytes());
        } catch (IOException e) {
            System.err.println("❌ Error writing " + fileName + ": " + e.getMessage());
        }
    }
}
