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
            "A tool to generate Java entity classes from SQL schema files.\n";
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
