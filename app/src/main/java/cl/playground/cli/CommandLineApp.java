package cl.playground.cli;

import cl.playground.cli.commands.GenerateCommand;
import cl.playground.cli.commands.InitCommand;

import java.util.Map;

public class CommandLineApp {
    private static final String VERSION = "1.0.0";
    private static final Map<String, Runnable> COMMANDS = Map.of(
        "--version", () -> System.out.println("Sqlift version " + VERSION),
        "init", new InitCommand()::run,
        "generate", new GenerateCommand()::run);

    public static void main(String[] args) {
        if (args.length == 0 || "--help".equals(args[0])) {
            printHelp();
            return;
        }

        COMMANDS.getOrDefault(args[0], () -> {
            System.err.println("❌ Unknown command: " + args[0]);
            printHelp();
        }).run();
    }

    private static void printHelp() {
        System.out.println("""
            Usage:
              sqlift --version       Show the tool's version
              sqlift init            Initialize configuration files
              sqlift generate        Generate Java entity classes from SQL schema""");
    }
}