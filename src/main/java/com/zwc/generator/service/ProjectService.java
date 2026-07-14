package com.zwc.generator.service;

import com.zwc.generator.dto.request.DataSourceConfigRequest;
import com.zwc.generator.dto.request.CodeTemplateRequest;
import com.zwc.generator.entity.CodeTemplate;
import com.zwc.generator.entity.DataSourceConfig;
import com.zwc.generator.entity.Project;
import com.zwc.generator.repository.CodeTemplateRepository;
import com.zwc.generator.repository.DataSourceConfigRepository;
import com.zwc.generator.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DataSourceConfigRepository dataSourceConfigRepository;
    private final CodeTemplateRepository codeTemplateRepository;

    @Transactional
    public Project create(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, Project project) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + id));
        existing.setName(project.getName());
        existing.setPackageName(project.getPackageName());
        existing.setDescription(project.getDescription());
        existing.setEnabled(project.getEnabled());
        return projectRepository.save(existing);
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public List<Project> findAll() {
        return projectRepository.findByEnabledTrue();
    }

    @Transactional
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    @Transactional
    public void copyDatasourceFromProject(Long targetProjectId, Long sourceProjectId, Long datasourceId) {
        DataSourceConfig sourceDatasource = dataSourceConfigRepository.findById(datasourceId)
                .orElseThrow(() -> new RuntimeException("数据源不存在: " + datasourceId));
        
        DataSourceConfig newDatasource = DataSourceConfig.builder()
                .name(sourceDatasource.getName() + "_copy")
                .url(sourceDatasource.getUrl())
                .username(sourceDatasource.getUsername())
                .password(sourceDatasource.getPassword())
                .driverClass(sourceDatasource.getDriverClass())
                .description(sourceDatasource.getDescription())
                .projectId(targetProjectId)
                .enabled(true)
                .build();
        
        dataSourceConfigRepository.save(newDatasource);
        log.info("Copied datasource {} to project {}", datasourceId, targetProjectId);
    }

    @Transactional
    public void copyTemplateFromProject(Long targetProjectId, Long sourceProjectId, Long templateId) {
        CodeTemplate sourceTemplate = codeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        CodeTemplate newTemplate = CodeTemplate.builder()
                .name(sourceTemplate.getName() + "_copy")
                .content(sourceTemplate.getContent())
                .filePathPattern(sourceTemplate.getFilePathPattern())
                .language(sourceTemplate.getLanguage())
                .description(sourceTemplate.getDescription())
                .projectId(targetProjectId)
                .enabled(true)
                .build();
        
        codeTemplateRepository.save(newTemplate);
        log.info("Copied template {} to project {}", templateId, targetProjectId);
    }

    @Transactional
    public void copyAllFromProject(Long targetProjectId, Long sourceProjectId) {
        List<DataSourceConfig> datasources = dataSourceConfigRepository.findByProjectId(sourceProjectId);
        for (DataSourceConfig ds : datasources) {
            DataSourceConfig newDatasource = DataSourceConfig.builder()
                    .name(ds.getName() + "_copy")
                    .url(ds.getUrl())
                    .username(ds.getUsername())
                    .password(ds.getPassword())
                    .driverClass(ds.getDriverClass())
                    .description(ds.getDescription())
                    .projectId(targetProjectId)
                    .enabled(true)
                    .build();
            dataSourceConfigRepository.save(newDatasource);
        }
        
        List<CodeTemplate> templates = codeTemplateRepository.findByProjectId(sourceProjectId);
        for (CodeTemplate tpl : templates) {
            CodeTemplate newTemplate = CodeTemplate.builder()
                    .name(tpl.getName() + "_copy")
                    .content(tpl.getContent())
                    .filePathPattern(tpl.getFilePathPattern())
                    .language(tpl.getLanguage())
                    .description(tpl.getDescription())
                    .projectId(targetProjectId)
                    .enabled(true)
                    .build();
            codeTemplateRepository.save(newTemplate);
        }
        
        log.info("Copied all datasources and templates from project {} to project {}", sourceProjectId, targetProjectId);
    }
}
