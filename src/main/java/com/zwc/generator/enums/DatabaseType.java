package com.zwc.generator.enums;

public enum DatabaseType {
    
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://", "SELECT 1", "information_schema", "TABLE_NAME", "TABLE_COMMENT"),
    
    ORACLE("oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:", "SELECT 1 FROM DUAL", "user_tables", "TABLE_NAME", "COMMENTS"),
    
    OCEANBASE("oceanbase", "com.oceanbase.jdbc.Driver", "jdbc:oceanbase://", "SELECT 1", "information_schema", "TABLE_NAME", "TABLE_COMMENT"),
    
    POSTGRESQL("postgresql", "org.postgresql.Driver", "jdbc:postgresql://", "SELECT 1", "information_schema.tables", "TABLE_NAME", "TABLE_COMMENT"),
    
    SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://", "SELECT 1", "information_schema.tables", "TABLE_NAME", "TABLE_COMMENT"),
    
    H2("h2", "org.h2.Driver", "jdbc:h2:", "SELECT 1", "information_schema.tables", "TABLE_NAME", "REMARKS");
    
    private final String name;
    private final String driverClass;
    private final String urlPrefix;
    private final String validationQuery;
    private final String tableSchema;
    private final String tableNameColumn;
    private final String tableCommentColumn;
    
    DatabaseType(String name, String driverClass, String urlPrefix, String validationQuery, 
                 String tableSchema, String tableNameColumn, String tableCommentColumn) {
        this.name = name;
        this.driverClass = driverClass;
        this.urlPrefix = urlPrefix;
        this.validationQuery = validationQuery;
        this.tableSchema = tableSchema;
        this.tableNameColumn = tableNameColumn;
        this.tableCommentColumn = tableCommentColumn;
    }
    
    public static DatabaseType fromUrl(String url) {
        if (url == null) {
            return null;
        }
        String lowerUrl = url.toLowerCase();
        for (DatabaseType type : values()) {
            if (lowerUrl.startsWith(type.urlPrefix.toLowerCase())) {
                return type;
            }
        }
        return null;
    }
    
    public static DatabaseType fromName(String name) {
        if (name == null) {
            return null;
        }
        for (DatabaseType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDriverClass() {
        return driverClass;
    }
    
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public String getValidationQuery() {
        return validationQuery;
    }
    
    public String getTableSchema() {
        return tableSchema;
    }
    
    public String getTableNameColumn() {
        return tableNameColumn;
    }
    
    public String getTableCommentColumn() {
        return tableCommentColumn;
    }
}
