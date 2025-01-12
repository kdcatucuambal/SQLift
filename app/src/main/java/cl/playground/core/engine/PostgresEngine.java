package cl.playground.core.engine;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PostgresEngine {

    public List<String> extractCreateTableStatements(String sql) {
        List<String> statements = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+.*?;",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            statements.add(matcher.group());
        }

        return statements;
    }

    public List<String> extractColumnDefinitions(String sql) {
        List<String> columnDefinitions = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+\\w+\\s*\\((.*?)\\);",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String columnsDefinition = matcher.group(1);
            String[] lines = columnsDefinition.split(",(?![^(]*\\))");

            for (String line : lines) {
                line = line.trim();
                // Ignorar constraints
                if (!line.isEmpty() &&
                        !line.toUpperCase().startsWith("PRIMARY") &&
                        !line.toUpperCase().startsWith("FOREIGN") &&
                        !line.toUpperCase().startsWith("CONSTRAINT")) {
                    columnDefinitions.add(line);
                }
            }
        }

        return columnDefinitions;
    }

    public String extractTableName(String sql) {
        // Patrón que busca después de CREATE TABLE, ignorando espacios y
        // mayúsculas/minúsculas
        Pattern pattern = Pattern.compile("(?i)\\bCREATE\\s+TABLE\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            // Retorna el nombre de la tabla encontrado (grupo 1 del matcher)
            return matcher.group(1);
        }

        return null; // Retorna null si no encuentra ninguna tabla
    }

    public String extractColumnName(String columnDefinition) {
        // Obtener la primera palabra antes de cualquier tipo de dato o constraint
        String[] parts = columnDefinition.trim().split("\\s+");

        if (parts.length > 0) {
            return parts[0];
        }

        return null;
    }

    public String extractColumnType(String sql) {
        // Eliminar comentarios SQL si existen
        sql = sql.replaceAll("--.*$", "");

        // Patrón para capturar el tipo de dato, incluyendo cualquier precisión/escala
        Pattern pattern = Pattern.compile("\\s+([A-Za-z]+(?:\\s*\\([^)]*\\))?)\\s*(?:ARRAY|\\[\\])?");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String dataType = matcher.group(1).trim();

            // Manejar tipos de arrays
            if (sql.contains("ARRAY") || sql.contains("[]")) {
                dataType += "[]";
            }

            return dataType.toUpperCase();
        }

        return null;
    }

    public boolean isNotNullColumn(String columnDefinition) {
        // Verifica NOT NULL explícito
        Pattern notNullPattern = Pattern.compile(".*\\bNOT\\s+NULL\\b.*",
                Pattern.CASE_INSENSITIVE);
        return notNullPattern.matcher(columnDefinition).matches();
    }

    public boolean isUniqueColumn(String columnDefinition) {
        Pattern pattern = Pattern.compile(".*\\bUNIQUE\\b.*",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(columnDefinition);
        return matcher.matches();
    }

    public String extractDefaultValue(String columnDefinition) {
        Pattern pattern = Pattern.compile(
                "DEFAULT\\s+(" +
                        "true|false|" + // Booleanos
                        "CURRENT_DATE|" + // Funciones de fecha
                        "CURRENT_TIMESTAMP|" +
                        "CURRENT_TIME|" +
                        "NOW\\(\\)" +
                        ")",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(columnDefinition);

        if (matcher.find()) {
            return matcher.group(1); // Retornamos el valor exactamente como lo encontramos
        }
        return null;
    }

    public List<String> extractPrimaryKeyColumns(String sql) {
        List<String> primaryKeys = new ArrayList<>();

        // Limpiar comentarios y procesar todo en una sola pasada
        String cleanSql = sql.replaceAll("--[^\\n]*", "")
            .replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", " ");

        Pattern pkPattern = Pattern.compile(
            // Captura PKs simples
            "(?:(?:\\(|,)\\s*([\"\\w.-]+)\\s+(?:INTEGER|SERIAL|UUID|NUMERIC|BIGINT|VARCHAR|TEXT|TIMESTAMP|DATE|DECIMAL)(?:\\([^)]*\\))?\\s*(?:NOT\\s+NULL\\s+)?(?:UNIQUE\\s+)?(?:CONSTRAINT\\s+\\w+\\s+)?PRIMARY\\s+KEY\\b)" +
                "|" +
                // Captura PKs compuestas
                "(?:CONSTRAINT\\s+[\"\\w.-]+\\s+)?PRIMARY\\s+KEY\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                                           );

        Matcher matcher = pkPattern.matcher(cleanSql);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // PK simple
                String column = matcher.group(1).trim().replaceAll("^\"|\"$", "");
                if (!primaryKeys.contains(column)) primaryKeys.add(column);
            } else if (matcher.group(2) != null) {
                // PK compuesta
                for (String column : matcher.group(2).split(",")) {
                    String cleanColumn = column.trim().replaceAll("^\"|\"$", "").replaceAll("\\s+", " ");
                    if (!primaryKeys.contains(cleanColumn)) primaryKeys.add(cleanColumn);
                }
            }
        }

        return primaryKeys;
    }

    public List<String> extractTableRelations(String sql) {
        List<String> relations = new ArrayList<>();

        // Limpiar comentarios
        String cleanSql = sql.replaceAll("--[^\\n]*", "")
            .replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", " ");

        Pattern fkPattern = Pattern.compile(
            // 1. FK inline en definición de columna
            "(?:(?:\\(|,)\\s*([\"\\w.-]+)\\s+(?:INTEGER|SERIAL|UUID|NUMERIC|BIGINT|VARCHAR|TEXT|TIMESTAMP|DATE|DECIMAL)(?:\\([^)]*\\))?\\s+" +
                "REFERENCES\\s+([\"\\w.-]+)\\s*\\(([^)]+)\\))" +
                "|" +
                // 2. FK con CONSTRAINT nombrado
                "(?:CONSTRAINT\\s+[\"\\w.-]+\\s+)?" +
                "FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s*" +
                "REFERENCES\\s+([\"\\w.-]+)\\s*\\(([^)]+)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                                           );

        Matcher matcher = fkPattern.matcher(cleanSql);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // FK inline
                String sourceColumn = matcher.group(1).trim().replaceAll("^\"|\"$", "");
                String targetTable = matcher.group(2).trim().replaceAll("^\"|\"$", "");
                String targetColumn = matcher.group(3).trim().replaceAll("^\"|\"$", "");
                relations.add(String.format("%s -> %s.%s",
                    sourceColumn.replaceAll("\\s+", " "),
                    targetTable.replaceAll("\\s+", " "),
                    targetColumn.replaceAll("\\s+", " ")));
            } else if (matcher.group(4) != null) {
                // FK con CONSTRAINT
                String[] sourceColumns = matcher.group(4).split(",");
                String targetTable = matcher.group(5).trim().replaceAll("^\"|\"$", "");
                String[] targetColumns = matcher.group(6).split(",");

                // Manejar FKs compuestas
                for (int i = 0; i < sourceColumns.length && i < targetColumns.length; i++) {
                    String sourceColumn = sourceColumns[i].trim().replaceAll("^\"|\"$", "");
                    String targetColumn = targetColumns[i].trim().replaceAll("^\"|\"$", "");
                    relations.add(String.format("%s -> %s.%s",
                        sourceColumn.replaceAll("\\s+", " "),
                        targetTable.replaceAll("\\s+", " "),
                        targetColumn.replaceAll("\\s+", " ")));
                }
            }
        }

        return relations;
    }

}