package cl.playground.cli;

import cl.playground.cli.commands.GenerateCommand;
import cl.playground.cli.commands.InitCommand;

public class CommandLineApp {
    public static void main(String[] args) {
        if (args.length == 0 || "--help".equals(args[0])) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "--version":
                printVersion();
                break;
            case "init":
                new InitCommand().run();
                break;
            case "generate":
                new GenerateCommand().run();
                break;
            default:
                System.err.println("❌ Unknown command: " + args[0]);
                printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  sqlift --version       Show the tool's version");
        System.out.println("  sqlift init            Initialize configuration files");
        System.out.println("  sqlift generate        Generate Java entity classes from SQL schema");
    }

    private static void printVersion() {
        System.out.println("Sqlift version 1.0.0");
    }
}
