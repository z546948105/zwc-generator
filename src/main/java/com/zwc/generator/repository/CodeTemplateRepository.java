package com.zwc.generator.repository;

import com.zwc.generator.entity.CodeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeTemplateRepository extends JpaRepository<CodeTemplate, Long> {
    
    Optional<CodeTemplate> findByName(String name);
    
    List<CodeTemplate> findByEnabledTrue();
    
    List<CodeTemplate> findByLanguage(String language);
    
    List<CodeTemplate> findByProjectIdAndEnabledTrue(Long projectId);
    
    List<CodeTemplate> findByProjectId(Long projectId);
}
