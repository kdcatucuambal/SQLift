package cl.playground.core.model;

public class RelationMetadata {
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;
    private boolean isManyToOne;

    // Constructores
    public RelationMetadata() {
    }

    public RelationMetadata(String sourceColumn, String targetTable, String targetColumn, boolean isManyToOne) {
        this.sourceColumn = sourceColumn;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
        this.isManyToOne = isManyToOne;
    }

    // Getters y Setters
    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public boolean isManyToOne() {
        return isManyToOne;
    }

    public void setManyToOne(boolean manyToOne) {
        isManyToOne = manyToOne;
    }

    @Override
    public String toString() {
        return "RelationMetadata{" +
                "sourceColumn='" + sourceColumn + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", targetColumn='" + targetColumn + '\'' +
                ", isManyToOne=" + isManyToOne +
                '}';
    }
}