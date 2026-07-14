package com.zwc.generator.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.zwc.generator.dto.request.CodeGenerateRequest;
import com.zwc.generator.dto.response.ColumnInfo;
import com.zwc.generator.dto.response.TableInfo;
import com.zwc.generator.entity.CodeTemplate;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.sql.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeGenerateService {

    private final DataSourceService dataSourceService;
    private final CodeTemplateService codeTemplateService;
    private final Configuration freemarkerConfig;

    public List<TableInfo> getTableInfoList(Long dataSourceId, List<String> tableNames) {
        List<TableInfo> tableInfoList = new ArrayList<>();
        
        for (String tableName : tableNames) {
            TableInfo tableInfo = getTableInfo(dataSourceId, tableName);
            tableInfoList.add(tableInfo);
        }
        
        return tableInfoList;
    }

    public TableInfo getTableInfo(Long dataSourceId, String tableName) {
        DruidDataSource dataSource = dataSourceService.getDruidDataSource(dataSourceId);
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseType = metaData.getDatabaseProductName().toLowerCase();
            
            String tableComment = getTableComment(connection, tableName, databaseType);
            List<ColumnInfo> columns = getColumnInfoList(metaData, connection, tableName, databaseType);
            
            String className = convertTableNameToClassName(tableName);
            
            return TableInfo.builder()
                    .tableName(tableName)
                    .tableComment(tableComment)
                    .className(className)
                    .classSimpleName(className.substring(className.lastIndexOf('.') + 1))
                    .columns(columns)
                    .build();
                    
        } catch (SQLException e) {
            log.error("获取表信息失败: {}", tableName, e);
            throw new RuntimeException("获取表信息失败: " + e.getMessage());
        }
    }

    private String getTableComment(Connection connection, String tableName, String databaseType) {
        try (Statement stmt = connection.createStatement()) {
            if (databaseType.contains("mysql") || databaseType.contains("oceanbase")) {
                ResultSet rs = stmt.executeQuery("SHOW TABLE STATUS LIKE '" + tableName + "'");
                if (rs.next()) {
                    return rs.getString("Comment");
                }
            } else if (databaseType.contains("postgresql")) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT obj_description(oid, 'pg_class') FROM pg_class WHERE relname = '" + tableName + "'");
                if (rs.next()) {
                    return rs.getString(1);
                }
            } else if (databaseType.contains("oracle")) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT comments FROM user_tab_comments WHERE table_name = '" + tableName.toUpperCase() + "'");
                if (rs.next()) {
                    return rs.getString(1);
                }
            } else if (databaseType.contains("sqlserver")) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT value FROM sys.extended_properties WHERE major_id = OBJECT_ID('" + tableName + "') AND minor_id = 0 AND name = 'MS_Description'");
                if (rs.next()) {
                    return rs.getString(1);
                }
            } else if (databaseType.contains("h2")) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT REMARKS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'");
                if (rs.next()) {
                    return rs.getString(1);
                }
            } else if (databaseType.contains("dameng")) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COMMENTS FROM ALL_TAB_COMMENTS WHERE TABLE_NAME = '" + tableName.toUpperCase() + "'");
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            log.warn("获取表注释失败: {}", tableName, e);
        }
        
        return "";
    }

    private List<ColumnInfo> getColumnInfoList(DatabaseMetaData metaData, Connection connection, 
                                               String tableName, String databaseType) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        
        Set<String> primaryKeys = getPrimaryKeys(metaData, tableName, databaseType);
        
        String schema = getSchema(connection, databaseType);
        
        ResultSet columnRs = metaData.getColumns(schema, null, tableName.toUpperCase(), "%");
        if (!columnRs.next()) {
            columnRs = metaData.getColumns(schema, null, tableName.toLowerCase(), "%");
        }
        if (!columnRs.isBeforeFirst()) {
            columnRs = metaData.getColumns(schema, null, tableName, "%");
        }
        
        Map<String, String> columnComments = getColumnComments(connection, tableName, databaseType, schema);
        
        columnRs.beforeFirst();
        while (columnRs.next()) {
            String columnName = columnRs.getString("COLUMN_NAME");
            String columnType = columnRs.getString("TYPE_NAME");
            String columnComment = columnRs.getString("REMARKS");
            
            if (columnComment == null || columnComment.isEmpty()) {
                columnComment = columnComments.get(columnName.toUpperCase());
                if (columnComment == null) {
                    columnComment = columnComments.get(columnName.toLowerCase());
                }
            }
            
            int nullable = columnRs.getInt("NULLABLE");
            
            ColumnInfo column = ColumnInfo.builder()
                    .columnName(columnName)
                    .columnType(columnType)
                    .javaType(convertDbTypeToJavaType(columnType))
                    .javaFieldName(convertColumnNameToFieldName(columnName))
                    .columnComment(columnComment != null ? columnComment : "")
                    .nullable(nullable == DatabaseMetaData.columnNullable)
                    .primaryKey(primaryKeys.contains(columnName.toUpperCase()) 
                              || primaryKeys.contains(columnName.toLowerCase())
                              || primaryKeys.contains(columnName))
                    .build();
            
            columns.add(column);
        }
        
        return columns;
    }

    private Map<String, String> getColumnComments(Connection connection, String tableName, 
                                                   String databaseType, String schema) {
        Map<String, String> comments = new HashMap<>();
        
        try (Statement stmt = connection.createStatement()) {
            if (databaseType.contains("oracle")) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT column_name, comments FROM user_col_comments WHERE table_name = '" + tableName.toUpperCase() + "'");
                while (rs.next()) {
                    comments.put(rs.getString("column_name"), rs.getString("comments"));
                }
            } else if (databaseType.contains("sqlserver")) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT c.name AS column_name, ep.value AS comments " +
                    "FROM sys.columns c LEFT JOIN sys.extended_properties ep " +
                    "ON ep.major_id = c.object_id AND ep.minor_id = c.column_id AND ep.name = 'MS_Description' " +
                    "WHERE c.object_id = OBJECT_ID('" + tableName + "')");
                while (rs.next()) {
                    comments.put(rs.getString("column_name"), rs.getString("comments"));
                }
            }
        } catch (SQLException e) {
            log.warn("获取列注释失败: {}", tableName, e);
        }
        
        return comments;
    }

    private Set<String> getPrimaryKeys(DatabaseMetaData metaData, String tableName, String databaseType) throws SQLException {
        Set<String> primaryKeys = new HashSet<>();
        String schema = null;
        
        if (databaseType.contains("oracle")) {
            try {
                schema = metaData.getUserName().toUpperCase();
            } catch (SQLException e) {
                log.warn("获取Oracle schema失败", e);
            }
        }
        
        ResultSet pkRs = metaData.getPrimaryKeys(null, schema, tableName.toUpperCase());
        if (!pkRs.next()) {
            pkRs = metaData.getPrimaryKeys(null, schema, tableName.toLowerCase());
        }
        if (!pkRs.isBeforeFirst()) {
            pkRs = metaData.getPrimaryKeys(null, schema, tableName);
        }
        
        pkRs.beforeFirst();
        while (pkRs.next()) {
            String pkName = pkRs.getString("COLUMN_NAME");
            if (pkName != null) {
                primaryKeys.add(pkName);
                primaryKeys.add(pkName.toUpperCase());
                primaryKeys.add(pkName.toLowerCase());
            }
        }
        
        return primaryKeys;
    }

    private String getSchema(Connection connection, String databaseType) {
        if (databaseType.contains("oracle")) {
            try {
                return connection.getMetaData().getUserName().toUpperCase();
            } catch (SQLException e) {
                log.warn("获取Oracle schema失败", e);
            }
        } else if (databaseType.contains("postgresql") || databaseType.contains("gaussdb")) {
            try {
                return connection.getSchema();
            } catch (SQLException e) {
                log.warn("获取PostgreSQL/GaussDB schema失败", e);
            }
        } else if (databaseType.contains("dm dbms") || databaseType.contains("dameng")) {
            try {
                return connection.getMetaData().getUserName().toUpperCase();
            } catch (SQLException e) {
                log.warn("获取达梦数据库schema失败", e);
            }
        }
        return null;
    }

    private String convertTableNameToClassName(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return "";
        }
        
        String cleanTableName = tableName;
        if (cleanTableName.contains(".")) {
            cleanTableName = cleanTableName.substring(cleanTableName.lastIndexOf('.') + 1);
        }
        
        StringBuilder sb = new StringBuilder();
        boolean nextUpperCase = true;
        
        for (char c : cleanTableName.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    sb.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        
        String result = sb.toString();
        if (result.length() > 0 && Character.isLowerCase(result.charAt(0))) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        
        return result;
    }

    private String convertColumnNameToFieldName(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        boolean nextUpperCase = false;
        
        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);
            
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    sb.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        
        return sb.toString();
    }

    private String convertDbTypeToJavaType(String dbType) {
        if (dbType == null) {
            return "String";
        }
        
        String type = dbType.toUpperCase();
        
        if (type.contains("INT") || type.contains("INTEGER")) {
            return "Integer";
        } else if (type.contains("BIGINT")) {
            return "Long";
        } else if (type.contains("DECIMAL") || type.contains("NUMERIC") || type.contains("DOUBLE") || type.contains("FLOAT") || type.contains("NUMBER")) {
            return "BigDecimal";
        } else if (type.contains("DATE") || type.contains("TIME") || type.contains("TIMESTAMP")) {
            return "LocalDateTime";
        } else if (type.contains("BOOLEAN") || type.contains("BIT")) {
            return "Boolean";
        } else if (type.contains("CHAR") || type.contains("VARCHAR") || type.contains("TEXT") || type.contains("CLOB")) {
            return "String";
        } else if (type.contains("BLOB") || type.contains("BYTE")) {
            return "byte[]";
        } else {
            return "String";
        }
    }

    public Map<String, String> generateCode(CodeGenerateRequest request) {
        Map<String, String> generatedFiles = new HashMap<>();
        
        List<TableInfo> tableInfoList = getTableInfoList(Long.parseLong(request.getDataSourceId()), request.getTableNames());
        
        for (Long templateId : request.getTemplateIds()) {
            CodeTemplate template = codeTemplateService.getById(templateId);
            
            for (TableInfo tableInfo : tableInfoList) {
                String content = renderTemplate(template, tableInfo, request);
                String filePath = generateFilePath(template.getFilePathPattern(), tableInfo, request);
                generatedFiles.put(filePath, content);
            }
        }
        
        return generatedFiles;
    }

    private String renderTemplate(CodeTemplate template, TableInfo tableInfo, CodeGenerateRequest request) {
        try {
            Map<String, Object> dataModel = buildDataModel(tableInfo, request);
            
            Template freemarkerTemplate = new Template(template.getName(), template.getContent(), freemarkerConfig);
            StringWriter writer = new StringWriter();
            freemarkerTemplate.process(dataModel, writer);
            
            return writer.toString();
        } catch (Exception e) {
            log.error("渲染模板失败: {}", template.getName(), e);
            throw new RuntimeException("渲染模板失败: " + e.getMessage());
        }
    }

    private Map<String, Object> buildDataModel(TableInfo tableInfo, CodeGenerateRequest request) {
        Map<String, Object> dataModel = new HashMap<>();
        
        dataModel.put("tableInfo", tableInfo);
        dataModel.put("packageName", request.getPackageName());
        dataModel.put("author", request.getAuthor());
        dataModel.put("moduleName", request.getModuleName());
        dataModel.put("date", new java.util.Date());
        
        String packagePath = request.getPackageName().replace(".", "/");
        dataModel.put("packagePath", packagePath);
        
        return dataModel;
    }

    private String generateFilePath(String pattern, TableInfo tableInfo, CodeGenerateRequest request) {
        String filePath = pattern
                .replace("${packagePath}", request.getPackageName().replace(".", "/"))
                .replace("${packageName}", request.getPackageName())
                .replace("${className}", tableInfo.getClassName())
                .replace("${classSimpleName}", tableInfo.getClassSimpleName())
                .replace("${tableName}", tableInfo.getTableName())
                .replace("${moduleName}", request.getModuleName() != null ? request.getModuleName() : "");
        
        return filePath;
    }
}
