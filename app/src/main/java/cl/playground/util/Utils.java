package cl.playground.util;

public class Utils {
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        // Primero convertimos todo a minúsculas
        str = str.toLowerCase();
        // Luego capitalizamos la primera letra
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Primero convertimos todo a minúsculas
        input = input.toLowerCase();

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_' || c == ' ' || c == '-') {
                capitalizeNext = true;
            } else {
                if (i == 0) {
                    result.append(Character.toLowerCase(c));
                } else if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    public static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Primero convertimos todo a minúsculas
        input = input.toLowerCase();

        return input
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                .toLowerCase();
    }

    public static String toPlural(String input) {
        if (input == null || input.isEmpty() || input.endsWith("s")) {
            return input;
        }

        // Reglas básicas de pluralización
        if (input.endsWith("z")) {
            return input.substring(0, input.length() - 1) + "ces";
        }

        if (input.endsWith("n") || input.endsWith("l") || input.endsWith("r") ||
                input.endsWith("d") || input.endsWith("j") ||
                input.endsWith("ch") || input.endsWith("sh")) {
            return input + "es";
        }

        // Regla por defecto
        return input + "s";
    }

    private static boolean isPlural(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        return word.endsWith("es") ||
                word.endsWith("ces") ||
                (word.endsWith("s") && !word.endsWith("us"));
    }

    public static String singularToPlural(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        String lowercase = word.toLowerCase();

        // Si ya está en plural, retornarlo tal cual
        if (isPlural(lowercase)) {
            return word;
        }

        // Convertir a plural si está en singular
        if (lowercase.endsWith("s") || lowercase.endsWith("x") || lowercase.endsWith("z")) {
            return word + "es";
        }

        if (lowercase.endsWith("ón")) {
            return word.substring(0, word.length() - 2) + "ones";
        }

        return word + "s";
    }
}
