package cl.playground.cli;

import cl.playground.cli.commands.GenerateCommand;
import cl.playground.cli.commands.InitCommand;
import cl.playground.exception.ConfigurationException;

import java.util.Map;

public class CommandLineApp {
    private static final String VERSION = "1.0.0";
    private static final Map<String, Runnable> COMMANDS = Map.of(
        "--version", () -> System.out.println("Sqlift version " + VERSION),
        "init", new InitCommand()::run,
        "generate", new GenerateCommand()::run);

    public static void main(String[] args) {
        try {
            if (args.length == 0 || "--help".equals(args[0])) {
                printHelp();
                return;
            }

            Runnable command = COMMANDS.get(args[0]);
            if (command == null) {
                System.err.println("❌ Unknown command: " + args[0]);
                printHelp();
            } else {
                command.run();
            }
        } catch (ConfigurationException e) {
            System.err.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ An unexpected error occurred. Please contact support.");
        }
    }

    private static void printHelp() {
        System.out.println("""
            Usage:
              sqlift --version       Show the tool's version
              sqlift init            Initialize configuration files
              sqlift generate        Generate Java entity classes from SQL schema""");
    }
}
