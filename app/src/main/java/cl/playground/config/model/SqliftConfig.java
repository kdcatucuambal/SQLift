package cl.playground.config.model;

public class SqliftConfig {
    private String version;
    private SqlConfig sql;

    public SqliftConfig() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SqlConfig getSql() {
        return sql;
    }

    public void setSql(SqlConfig sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return "SqliftConfig{" +
                "version='" + version + '\'' +
                ", sql=" + sql +
                '}';
    }

    public static class SqlConfig {
        private String engine;
        private String schema;
        private OutputConfig output;

        public SqlConfig() {
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public OutputConfig getOutput() {
            return output;
        }

        public void setOutput(OutputConfig output) {
            this.output = output;
        }

        @Override
        public String toString() {
            return "SqlConfig{" +
                    "engine='" + engine + '\'' +
                    ", schema='" + schema + '\'' +
                    ", output=" + output +
                    '}';
        }
    }

    public static class OutputConfig {
        private String packageName;
        private boolean useLombok;

        public OutputConfig() {
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public boolean isUseLombok() {
            return useLombok;
        }

        public void setUseLombok(boolean useLombok) {
            this.useLombok = useLombok;
        }

        @Override
        public String toString() {
            return "OutputConfig{" +
                    "packageName='" + packageName + '\'' +
                    ", useLombok=" + useLombok +
                    '}';
        }
    }
}
