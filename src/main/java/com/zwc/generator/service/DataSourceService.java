package com.zwc.generator.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.zwc.generator.dto.request.DataSourceConfigRequest;
import com.zwc.generator.entity.DataSourceConfig;
import com.zwc.generator.enums.DatabaseType;
import com.zwc.generator.repository.DataSourceConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSourceService {

    private final DataSourceConfigRepository dataSourceConfigRepository;
    
    private final Map<String, DruidDataSource> dataSourceCache = new ConcurrentHashMap<>();

    @Transactional
    public DataSourceConfig create(DataSourceConfigRequest request) {
        String driverClass = request.getDriverClass();
        if (driverClass == null || driverClass.isEmpty()) {
            DatabaseType dbType = DatabaseType.fromUrl(request.getUrl());
            if (dbType != null) {
                driverClass = dbType.getDriverClass();
            }
        }
        
        DataSourceConfig config = DataSourceConfig.builder()
                .name(request.getName())
                .url(request.getUrl())
                .username(request.getUsername())
                .password(request.getPassword())
                .driverClass(driverClass)
                .description(request.getDescription())
                .projectId(request.getProjectId())
                .enabled(request.getEnabled())
                .build();
        return dataSourceConfigRepository.save(config);
    }

    @Transactional
    public DataSourceConfig update(Long id, DataSourceConfigRequest request) {
        DataSourceConfig config = dataSourceConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据源配置不存在"));
        
        if (!config.getName().equals(request.getName())) {
            dataSourceCache.remove(config.getName());
        }
        
        String driverClass = request.getDriverClass();
        if (driverClass == null || driverClass.isEmpty()) {
            DatabaseType dbType = DatabaseType.fromUrl(request.getUrl());
            if (dbType != null) {
                driverClass = dbType.getDriverClass();
            }
        }
        
        config.setName(request.getName());
        config.setUrl(request.getUrl());
        config.setUsername(request.getUsername());
        config.setPassword(request.getPassword());
        config.setDriverClass(driverClass);
        config.setDescription(request.getDescription());
        config.setProjectId(request.getProjectId());
        config.setEnabled(request.getEnabled());
        
        return dataSourceConfigRepository.save(config);
    }

    @Transactional
    public void delete(Long id) {
        DataSourceConfig config = dataSourceConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据源配置不存在"));
        dataSourceCache.remove(config.getName());
        dataSourceConfigRepository.delete(config);
    }

    public DataSourceConfig getById(Long id) {
        return dataSourceConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据源配置不存在"));
    }

    public List<DataSourceConfig> listAll() {
        return dataSourceConfigRepository.findAll();
    }

    public List<DataSourceConfig> listEnabled() {
        return dataSourceConfigRepository.findByEnabledTrue();
    }

    public List<DataSourceConfig> listByProjectId(Long projectId) {
        return dataSourceConfigRepository.findByProjectIdAndEnabledTrue(projectId);
    }

    public DruidDataSource getDruidDataSource(Long id) {
        DataSourceConfig config = getById(id);
        return getDruidDataSourceByName(config.getName());
    }

    public DruidDataSource getDruidDataSourceByName(String name) {
        return dataSourceCache.computeIfAbsent(name, n -> {
            DataSourceConfig config = dataSourceConfigRepository.findByName(n)
                    .orElseThrow(() -> new RuntimeException("数据源配置不存在: " + n));
            
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(config.getUrl());
            dataSource.setUsername(config.getUsername());
            dataSource.setPassword(config.getPassword());
            
            String driverClass = config.getDriverClass();
            if (driverClass == null || driverClass.isEmpty()) {
                DatabaseType dbType = DatabaseType.fromUrl(config.getUrl());
                if (dbType != null) {
                    driverClass = dbType.getDriverClass();
                }
            }
            if (driverClass != null && !driverClass.isEmpty()) {
                dataSource.setDriverClassName(driverClass);
            }
            
            dataSource.setInitialSize(1);
            dataSource.setMinIdle(1);
            dataSource.setMaxActive(10);
            dataSource.setMaxWait(60000);
            dataSource.setTimeBetweenEvictionRunsMillis(60000);
            dataSource.setMinEvictableIdleTimeMillis(300000);
            
            String validationQuery = getValidationQuery(config.getUrl());
            dataSource.setValidationQuery(validationQuery);
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(false);
            dataSource.setTestOnReturn(false);
            
            return dataSource;
        });
    }

    private String getValidationQuery(String url) {
        DatabaseType dbType = DatabaseType.fromUrl(url);
        if (dbType != null) {
            return dbType.getValidationQuery();
        }
        return "SELECT 1";
    }

    public List<String> getTableNames(Long dataSourceId) {
        List<String> tableNames = new ArrayList<>();
        DruidDataSource dataSource = getDruidDataSource(dataSourceId);
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseType = metaData.getDatabaseProductName().toLowerCase();
            
            String[] tableTypes = getTableTypes(databaseType);
            String schema = getSchema(connection, databaseType);
            
            java.sql.ResultSet tables = metaData.getTables(schema, null, "%", tableTypes);
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (tableName != null && !tableName.toLowerCase().startsWith("sys_") 
                    && !tableName.toLowerCase().startsWith("v$")) {
                    tableNames.add(tableName);
                }
            }
        } catch (SQLException e) {
            log.error("获取表名失败", e);
            throw new RuntimeException("获取表名失败: " + e.getMessage());
        }
        
        return tableNames;
    }

    private String[] getTableTypes(String databaseType) {
        if (databaseType.contains("oracle")) {
            return new String[]{"TABLE"};
        } else if (databaseType.contains("postgresql")) {
            return new String[]{"TABLE"};
        } else if (databaseType.contains("sqlserver")) {
            return new String[]{"BASE TABLE"};
        }
        return new String[]{"TABLE"};
    }

    private String getSchema(Connection connection, String databaseType) {
        if (databaseType.contains("oracle")) {
            try {
                return connection.getMetaData().getUserName().toUpperCase();
            } catch (SQLException e) {
                log.warn("获取Oracle schema失败", e);
            }
        } else if (databaseType.contains("postgresql")) {
            try {
                return connection.getSchema();
            } catch (SQLException e) {
                log.warn("获取PostgreSQL schema失败", e);
            }
        }
        return null;
    }

    public void testConnection(Long dataSourceId) {
        DruidDataSource dataSource = getDruidDataSource(dataSourceId);
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) {
                throw new RuntimeException("连接无效");
            }
        } catch (SQLException e) {
            throw new RuntimeException("连接测试失败: " + e.getMessage());
        }
    }

    public DatabaseType getDatabaseType(Long dataSourceId) {
        DataSourceConfig config = getById(dataSourceId);
        return DatabaseType.fromUrl(config.getUrl());
    }
}
