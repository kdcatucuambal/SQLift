package cl.playground.core.generator.factory;

import cl.playground.core.model.ColumnMetadata;
import cl.playground.core.model.RelationMetadata;
import cl.playground.core.model.TableMetadata;
import cl.playground.core.types.PostgreSQLToJavaType;

import java.util.HashSet;
import java.util.Set;

public class ImportGenerator {

    private final boolean useLombok;

    public ImportGenerator(boolean useLombok) {
        this.useLombok = useLombok;
    }

    public void generateImports(TableMetadata table, StringBuilder builder) {
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

            // Solo añadir EqualsAndHashCode si se necesita una clase compuesta
            if (needsCompositeKey(table)) {
                imports.add("import lombok.EqualsAndHashCode;");
            }
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

    private boolean needsCompositeKey(TableMetadata table) {
        return table.getPrimaryKeys().size() > 1;
    }
}