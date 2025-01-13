package cl.playground.core.generator;

import cl.playground.core.model.TableMetadata;
import cl.playground.core.model.ColumnMetadata;
import cl.playground.core.model.RelationMetadata;
import cl.playground.core.types.PostgreSQLToJavaType;

import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class EntityGenerator {

    private boolean useLombok;

    public EntityGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public String generateEntity(TableMetadata table, String packageName) {
        StringBuilder entityBuilder = new StringBuilder();

        // 0. Agregar declaración del paquete
        entityBuilder.append("package ").append(packageName).append(";\n\n");

        // 1. Generar imports
        generateImports(table, entityBuilder);

        // 2. Generar anotaciones de clase
        generateClassAnnotations(table, entityBuilder);

        // 3. Generar declaración de clase
        String className = generateClassName(table.getTableName());
        generateClassDeclaration(className, table, entityBuilder);

        // 4. Generar campos con sus anotaciones
        generateFields(table, entityBuilder);

        // 5. Generar constructores
        generateConstructors(table, className, entityBuilder);

        // 6. Generar getters y setters
        generateGettersAndSetters(table, entityBuilder);

        // 7. Si tiene clave primaria compuesta, generar clase estática al final
        if (needsCompositeKey(table)) {
            generateCompositeKeyClass(table, entityBuilder);
        }

        // Cerrar la clase
        entityBuilder.append("}");

        return entityBuilder.toString();
    }

    private void generateImports(TableMetadata table, StringBuilder builder) {
        Set<String> imports = new HashSet<>();

        // Agregar imports básicos
        imports.add("import jakarta.persistence.Entity;");
        imports.add("import jakarta.persistence.Table;");
        imports.add("import jakarta.persistence.Column;");

        // Agregar imports de Lombok si está habilitado
        if (useLombok) {
            imports.add("import lombok.Getter;");
            imports.add("import lombok.Setter;");
            imports.add("import lombok.ToString;");
            imports.add("import lombok.NoArgsConstructor;");
            imports.add("import lombok.AllArgsConstructor;");
        }

        // Otros imports relacionados con JPA según el esquema
        if (needsCompositeKey(table)) {
            imports.add("import jakarta.persistence.EmbeddedId;");
            imports.add("import jakarta.persistence.Embeddable;");
            imports.add("import java.io.Serializable;");
        } else {
            imports.add("import jakarta.persistence.Id;");
            if (table.getColumns().stream()
                    .filter(c -> table.getPrimaryKeys().contains(c.getColumnName()))
                    .anyMatch(c -> c.getColumnType().toUpperCase().contains("SERIAL"))) {
                imports.add("import jakarta.persistence.GeneratedValue;");
                imports.add("import jakarta.persistence.GenerationType;");
            }
        }

        boolean usesMapsId = table.getRelations().stream()
            .anyMatch(RelationMetadata::isManyToOne); // Si hay relaciones ManyToOne que usan @MapsId

        if (usesMapsId && needsCompositeKey(table)) {
            imports.add("import jakarta.persistence.MapsId;");
        }

        for (RelationMetadata relation : table.getRelations()) {
            if (relation.isManyToOne()) {
                imports.add("import jakarta.persistence.ManyToOne;");
                imports.add("import jakarta.persistence.JoinColumn;");
                imports.add("import jakarta.persistence.ForeignKey;");
            } else {
                imports.add("import jakarta.persistence.OneToMany;");
                imports.add("import jakarta.persistence.CascadeType;");
                imports.add("import java.util.Set;");
                imports.add("import java.util.HashSet;");
            }
        }

        if (table.getColumns().stream().anyMatch(ColumnMetadata::isUnique)) {
            imports.add("import jakarta.persistence.UniqueConstraint;");
        }

        for (ColumnMetadata column : table.getColumns()) {
            String importStatement = PostgreSQLToJavaType.getImportStatement(column.getColumnType());
            if (importStatement != null && !importStatement.contains("java.lang.")) {
                imports.add(importStatement);
            }
        }

        // Ordenar y agregar imports al builder
        imports.stream().sorted().forEach(imp -> builder.append(imp).append("\n"));
        builder.append("\n");
    }

    private void generateClassAnnotations(TableMetadata table, StringBuilder builder) {
        builder.append("@Entity\n");

        // Agregar anotaciones de Lombok si está habilitado
        if (useLombok) {
            builder.append("@Getter\n");
            builder.append("@Setter\n");

            // Generar @ToString con exclude para las relaciones ManyToOne
            List<String> foreignKeyFields = table.getRelations().stream()
                .filter(RelationMetadata::isManyToOne)
                .map(relation -> {
                    if (relation.getSourceColumn().contains("_")) {
                        String[] parts = relation.getSourceColumn().split("_id")[0].split("_");
                        if (parts.length > 1) {
                            StringBuilder fieldNameBuilder = new StringBuilder(parts[0]);
                            for (int i = 1; i < parts.length; i++) {
                                fieldNameBuilder.append(Character.toUpperCase(parts[i].charAt(0)))
                                    .append(parts[i].substring(1));
                            }
                            return fieldNameBuilder.toString();
                        }
                    }
                    String fieldName = generateFieldName(relation.getTargetTable());
                    return fieldName.endsWith("s") ? fieldName.substring(0, fieldName.length() - 1) : fieldName;
                })
                .collect(Collectors.toList());

            boolean hasMapsId = table.getRelations().stream()
                .filter(RelationMetadata::isManyToOne)
                .anyMatch(rel -> table.getPrimaryKeys().contains(rel.getSourceColumn()));

            if (!foreignKeyFields.isEmpty() && !hasMapsId) {
                builder.append("@ToString(exclude = {");
                for (int i = 0; i < foreignKeyFields.size(); i++) {
                    builder.append("\"").append(foreignKeyFields.get(i)).append("\"");
                    if (i < foreignKeyFields.size() - 1) {
                        builder.append(", ");
                    }
                }
                builder.append("})\n");
            } else {
                builder.append("@ToString\n");
            }

            builder.append("@NoArgsConstructor\n");
            builder.append("@AllArgsConstructor\n");
        }

        // Asegurar que el nombre de la tabla siempre esté en plural
        String tableName = table.getTableName().toLowerCase();
        if (!tableName.endsWith("s")) {
            tableName = toPlural(tableName);
        }

        builder.append("@Table(name = \"").append(tableName).append("\"");

        // Agregar restricciones de unicidad si existen
        List<ColumnMetadata> uniqueColumns = table.getColumns().stream()
            .filter(ColumnMetadata::isUnique)
            .collect(Collectors.toList());

        if (!uniqueColumns.isEmpty()) {
            builder.append(",\n    uniqueConstraints = {\n");
            for (int i = 0; i < uniqueColumns.size(); i++) {
                ColumnMetadata column = uniqueColumns.get(i);
                builder.append("        @UniqueConstraint(\n")
                    .append("            name = \"uk_")
                    .append(tableName)
                    .append("_")
                    .append(column.getColumnName().toLowerCase())
                    .append("\",\n")
                    .append("            columnNames = {\"")
                    .append(column.getColumnName())
                    .append("\"}\n")
                    .append("        )");
                if (i < uniqueColumns.size() - 1) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("    }");
        }
        builder.append(")\n");
    }

    private void generateClassDeclaration(String className, TableMetadata table, StringBuilder builder) {
        // Si la clase tiene clave compuesta, debe implementar Serializable
        if (needsCompositeKey(table)) {
            builder.append("public class ").append(className)
                    .append(" {\n\n");
        } else {
            builder.append("public class ").append(className).append(" {\n\n");
        }
    }

    private boolean needsCompositeKey(TableMetadata table) {
        return table.getPrimaryKeys().size() > 1;
    }

    public String generateClassName(String tableName) {
        // Convertir nombre_tabla a NombreTabla en singular
        String singular = tableName.toLowerCase();
        if (singular.endsWith("s")) {
            singular = singular.substring(0, singular.length() - 1);
        }
        // Si termina en "e" después de quitar la "s", también la quitamos
        if (singular.endsWith("e")) {
            singular = singular.substring(0, singular.length() - 1);
        }

        String[] parts = singular.split("_");
        StringBuilder className = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                className.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return className.toString();
    }

    private void generateFields(TableMetadata table, StringBuilder builder) {
        Set<String> foreignKeyColumns = new HashSet<>();
        Set<String> processedOneToManyFields = new HashSet<>();

        // Recolectar las columnas que son FKs de relaciones ManyToOne
        for (RelationMetadata relation : table.getRelations()) {
            if (relation.isManyToOne()) {
                foreignKeyColumns.add(relation.getSourceColumn());
            }
        }

        if (needsCompositeKey(table)) {
            String className = generateClassName(table.getTableName());
            builder.append("    @EmbeddedId\n")
                .append("    private ")
                .append(className)
                .append("Id id;\n\n");
        }

        // Generar campos para las columnas
        for (ColumnMetadata column : table.getColumns()) {
            boolean isForeignKey = foreignKeyColumns.contains(column.getColumnName());
            boolean isPartOfCompositeKey = needsCompositeKey(table)
                && table.getPrimaryKeys().contains(column.getColumnName());
            boolean isForeignKeyInCompositeKey = needsCompositeKey(table)
                && isForeignKey;

            if (!isForeignKey && !isPartOfCompositeKey && !isForeignKeyInCompositeKey) {
                generateFieldAnnotations(column, table.getPrimaryKeys(), builder);
                generateFieldDeclaration(column, builder);
            }
        }

        // Generar campos para las relaciones
        for (RelationMetadata relation : table.getRelations()) {
            String targetClass = generateClassName(relation.getTargetTable());

            if (relation.isManyToOne()) {
                // Generar nombre del campo basado en la columna de origen
                String fieldName;
                if (relation.getSourceColumn().contains("_")) {
                    String[] parts = relation.getSourceColumn().split("_id")[0].split("_");
                    if (parts.length > 1) {
                        // Para casos como "sucursal_origen_id" -> "sucursalOrigen"
                        StringBuilder fieldNameBuilder = new StringBuilder(parts[0]);
                        for (int i = 1; i < parts.length; i++) {
                            fieldNameBuilder.append(Character.toUpperCase(parts[i].charAt(0)))
                                .append(parts[i].substring(1));
                        }
                        fieldName = fieldNameBuilder.toString();
                    } else {
                        fieldName = generateFieldName(relation.getTargetTable());
                    }
                } else {
                    fieldName = generateFieldName(relation.getTargetTable());
                    if (fieldName.endsWith("s")) {
                        fieldName = fieldName.substring(0, fieldName.length() - 1);
                    }
                }

                builder.append("    @ManyToOne\n");

                if (needsCompositeKey(table)) {
                    builder.append("    @MapsId(\"")
                        .append(generateFieldName(relation.getSourceColumn()))
                        .append("\")\n");
                }

                builder.append("    @JoinColumn(\n")
                    .append("        name = \"")
                    .append(relation.getSourceColumn())
                    .append("\",\n")
                    .append("        nullable = false,\n")
                    .append("        foreignKey = @ForeignKey(name = \"fk_")
                    .append(table.getTableName().toLowerCase())
                    .append("_")
                    .append(relation.getTargetTable().toLowerCase())
                    .append("\")\n")
                    .append("    )\n")
                    .append("    private ")
                    .append(targetClass)
                    .append(" ")
                    .append(fieldName)
                    .append(";\n\n");
            } else {
                String fieldName = generateFieldName(relation.getTargetTable());
                String pluralFieldName = toPlural(fieldName);
                if (!processedOneToManyFields.contains(pluralFieldName)) {
                    processedOneToManyFields.add(pluralFieldName);

                    String targetFieldName = generateFieldName(table.getTableName());
                    if (targetFieldName.endsWith("s")) {
                        targetFieldName = targetFieldName.substring(0, targetFieldName.length() - 1);
                    }

                    builder.append("    @OneToMany(\n")
                        .append("        mappedBy = \"")
                        .append(targetFieldName)
                        .append("\",\n")
                        .append("        cascade = CascadeType.ALL,\n")
                        .append("        orphanRemoval = true\n")
                        .append("    )\n")
                        .append("    private Set<")
                        .append(targetClass)
                        .append("> ")
                        .append(pluralFieldName)
                        .append(" = new HashSet<>();\n\n");
                }
            }
        }
    }

    private void generateFieldAnnotations(ColumnMetadata column, List<String> primaryKeys, StringBuilder builder) {
        // Si es parte de la clave primaria
        if (primaryKeys.contains(column.getColumnName())) {
            builder.append("    @Id\n");
            if (column.getColumnType().toUpperCase().contains("SERIAL")) {
                builder.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
            }
        }

        // Generar @Column con sus propiedades
        builder.append("    @Column(name = \"").append(column.getColumnName()).append("\"");

        if (column.isNotNull()) {
            builder.append(", nullable = false");
        }

        // Si el tipo es VARCHAR, agregar length
        if (column.getColumnType().toUpperCase().startsWith("VARCHAR")) {
            String length = column.getColumnType().replaceAll("\\D+", "");
            builder.append(", length = ").append(length);
        }

        // Si tiene valor por defecto, incluirlo en columnDefinition
        if (column.getDefaultValue() != null) {
            builder.append(", columnDefinition = \"")
                    .append(column.getColumnType())
                    .append(" DEFAULT ")
                    .append(column.getDefaultValue())
                    .append("\"");
        }

        builder.append(")\n");
    }

    private void generateFieldDeclaration(ColumnMetadata column, StringBuilder builder) {
        String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
        String fieldName = generateFieldName(column.getColumnName());

        builder.append("    private ").append(javaType).append(" ")
                .append(fieldName).append(";\n\n");
    }

    private String generateFieldName(String columnName) {
        // 1. Convertir a minúsculas y dividir por guiones bajos
        String[] parts = columnName.toLowerCase().split("_");
        StringBuilder fieldName = new StringBuilder();

        // 2. Manejar el caso especial de ID
        boolean isIdField = false;
        if (parts.length > 0) {
            if (parts[parts.length - 1].equals("id")) {
                isIdField = true;
                if (parts.length == 1) {
                    return "id";
                }
            }
        }

        // 3. Construir el nombre del campo en camelCase
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            // Saltar "id" si es el último elemento y el campo es un ID
            if (isIdField && i == parts.length - 1) {
                continue;
            }

            if (i == 0) {
                // Primera palabra siempre en minúscula y en singular
                String singularPart = toSingular(part);
                fieldName.append(singularPart);
            } else {
                // Capitalizar las siguientes palabras
                if (part.length() > 0) {
                    String singularPart = toSingular(part);
                    fieldName.append(Character.toUpperCase(singularPart.charAt(0)))
                            .append(singularPart.substring(1));
                }
            }
        }

        // 4. Agregar el sufijo "Id" si es un campo de ID
        if (isIdField) {
            fieldName.append("Id");
        }

        return fieldName.toString();
    }

    private void generateCompositeKeyClass(TableMetadata table, StringBuilder builder) {
        String className = generateClassName(table.getTableName());

        // Mover la clase al final
        builder.append("\n    @Embeddable\n");

        if (useLombok) {
            builder.append("    @Getter\n");
            builder.append("    @Setter\n");
            builder.append("    @ToString\n");
            builder.append("    @NoArgsConstructor\n");
            builder.append("    @AllArgsConstructor\n");
        }

        builder.append("    static class ").append(className).append("Id implements Serializable {\n");

        // Campos de la clave compuesta
        for (String primaryKey : table.getPrimaryKeys()) {
            // Manejar el caso donde no se encuentra una columna para la clave primaria
            ColumnMetadata column = table.getColumns().stream()
                .filter(c -> c.getColumnName().equals(primaryKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró una columna para la clave primaria: " + primaryKey + " en la tabla: " + table.getTableName()));

            String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
            String fieldName = generateFieldName(primaryKey);

            builder.append("        @Column(name = \"")
                .append(primaryKey.toLowerCase())
                .append("\")\n")
                .append("        private ")
                .append(javaType)
                .append(" ")
                .append(fieldName)
                .append(";\n");
        }

        if (!useLombok) {
            builder.append("\n        public ").append(className).append("Id() {}\n");
        }

        builder.append("    }\n");
    }


    private void generateConstructors(TableMetadata table, String className, StringBuilder builder) {
        // Si Lombok está habilitado, no generar constructores
        if (useLombok) {
            return;
        }

        // Constructor vacío
        builder.append("    public ").append(className).append("() {}\n\n");

        // Constructor con todos los campos
        builder.append("    public ").append(className).append("(");

        List<String> constructorParams = new ArrayList<>();

        if (needsCompositeKey(table)) {
            constructorParams.add(className + "Id id");

            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                        .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!table.getPrimaryKeys().contains(column.getColumnName()) && !isForeignKey) {
                    String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
                    String fieldName = generateFieldName(column.getColumnName());
                    constructorParams.add(javaType + " " + fieldName);
                }
            }

            for (RelationMetadata relation : table.getRelations()) {
                if (relation.isManyToOne()) {
                    String targetClass = generateClassName(relation.getTargetTable());
                    String fieldName = generateFieldName(relation.getTargetTable());
                    constructorParams.add(targetClass + " " + fieldName);
                }
            }

            builder.append(String.join(", ", constructorParams));
            builder.append(") {\n");

            builder.append("        this.id = id;\n");

            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                        .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!table.getPrimaryKeys().contains(column.getColumnName()) && !isForeignKey) {
                    String fieldName = generateFieldName(column.getColumnName());
                    builder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                }
            }
        } else {
            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                        .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!isForeignKey) {
                    String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());
                    String fieldName = generateFieldName(column.getColumnName());
                    constructorParams.add(javaType + " " + fieldName);
                }
            }

            for (RelationMetadata relation : table.getRelations()) {
                if (relation.isManyToOne()) {
                    String targetClass = generateClassName(relation.getTargetTable());
                    String fieldName = generateFieldName(relation.getTargetTable());
                    constructorParams.add(targetClass + " " + fieldName);
                }
            }

            builder.append(String.join(", ", constructorParams));
            builder.append(") {\n");

            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                        .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!isForeignKey) {
                    String fieldName = generateFieldName(column.getColumnName());
                    builder.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
                }
            }
        }

        builder.append("    }\n\n");
    }

    private void generateGettersAndSetters(TableMetadata table, StringBuilder builder) {
        // Si Lombok está habilitado, no generar getters y setters
        if (useLombok) {
            return;
        }

        if (needsCompositeKey(table)) {
            String className = generateClassName(table.getTableName());
            builder.append("    public ").append(className).append("Id getId() {\n")
                    .append("        return id;\n")
                    .append("    }\n\n");

            builder.append("    public void setId(").append(className).append("Id id) {\n")
                    .append("        this.id = id;\n")
                    .append("    }\n\n");

            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                        .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!table.getPrimaryKeys().contains(column.getColumnName()) && !isForeignKey) {
                    generateGetterAndSetter(column, builder);
                }
            }
        } else {
            for (ColumnMetadata column : table.getColumns()) {
                boolean isForeignKey = table.getRelations().stream()
                        .anyMatch(rel -> rel.getSourceColumn().equals(column.getColumnName()) && rel.isManyToOne());
                if (!isForeignKey) {
                    generateGetterAndSetter(column, builder);
                }
            }
        }

        generateRelationGettersAndSetters(table, builder);
    }

    // Nuevo método auxiliar para generar getter y setter de una columna
    private void generateGetterAndSetter(ColumnMetadata column, StringBuilder builder) {
        String fieldName = generateFieldName(column.getColumnName());
        String capitalizedField = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String javaType = PostgreSQLToJavaType.getJavaType(column.getColumnType());

        // Getter
        builder.append("    public ").append(javaType).append(" get")
                .append(capitalizedField).append("() {\n")
                .append("        return ").append(fieldName).append(";\n")
                .append("    }\n\n");

        // Setter
        builder.append("    public void set").append(capitalizedField)
                .append("(").append(javaType).append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ")
                .append(fieldName).append(";\n")
                .append("    }\n\n");
    }

    // Nuevo método auxiliar para generar getters y setters de relaciones
    private void generateRelationGettersAndSetters(TableMetadata table, StringBuilder builder) {
        for (RelationMetadata relation : table.getRelations()) {
            String fieldName = generateFieldName(relation.getTargetTable());
            String targetClass = generateClassName(relation.getTargetTable());

            if (relation.isManyToOne()) {
                if (fieldName.endsWith("s")) {
                    fieldName = fieldName.substring(0, fieldName.length() - 1);
                }
                generateRelationGetterAndSetter(fieldName, targetClass, false, builder);
            } else {
                String pluralField = toPlural(fieldName);
                generateRelationGetterAndSetter(pluralField, targetClass, true, builder);
            }
        }
    }

    // Nuevo método auxiliar para generar getter y setter de una relación
    private void generateRelationGetterAndSetter(String fieldName, String targetClass, boolean isCollection,
            StringBuilder builder) {
        String capitalizedField = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String type = isCollection ? "Set<" + targetClass + ">" : targetClass;

        // Getter
        builder.append("    public ").append(type).append(" get")
                .append(capitalizedField).append("() {\n")
                .append("        return ").append(fieldName).append(";\n")
                .append("    }\n\n");

        // Setter
        builder.append("    public void set").append(capitalizedField)
                .append("(").append(type).append(" ").append(fieldName).append(") {\n")
                .append("        this.").append(fieldName).append(" = ")
                .append(fieldName).append(";\n")
                .append("    }\n\n");
    }

    private String toSingular(String part) {
        if (part == null || part.isEmpty()) {
            return part;
        }

        // Reglas básicas para convertir plurales a singular en español
        if (part.endsWith("s")) {
            // Casos especiales primero
            if (part.endsWith("es")) {
                // Palabras que terminan en -es
                return part.substring(0, part.length() - 2);
            }
            // Caso general: quitar la 's' final
            return part.substring(0, part.length() - 1);
        }

        return part;
    }

    public String toPlural(String input) {
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
}
