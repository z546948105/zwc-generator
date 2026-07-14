package com.zwc.generator.repository;

import com.zwc.generator.entity.DataSourceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfig, Long> {
    
    Optional<DataSourceConfig> findByName(String name);
    
    List<DataSourceConfig> findByEnabledTrue();
    
    List<DataSourceConfig> findByProjectIdAndEnabledTrue(Long projectId);
    
    List<DataSourceConfig> findByProjectId(Long projectId);
}
