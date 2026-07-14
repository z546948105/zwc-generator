package com.zwc.generator.service;

import com.zwc.generator.dto.request.CodeTemplateRequest;
import com.zwc.generator.entity.CodeTemplate;
import com.zwc.generator.repository.CodeTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeTemplateService {

    private final CodeTemplateRepository codeTemplateRepository;

    @Transactional
    public CodeTemplate create(CodeTemplateRequest request) {
        CodeTemplate template = CodeTemplate.builder()
                .name(request.getName())
                .content(request.getContent())
                .filePathPattern(request.getFilePathPattern())
                .language(request.getLanguage())
                .description(request.getDescription())
                .projectId(request.getProjectId())
                .enabled(request.getEnabled())
                .build();
        return codeTemplateRepository.save(template);
    }

    @Transactional
    public CodeTemplate update(Long id, CodeTemplateRequest request) {
        CodeTemplate template = codeTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setFilePathPattern(request.getFilePathPattern());
        template.setLanguage(request.getLanguage());
        template.setDescription(request.getDescription());
        template.setProjectId(request.getProjectId());
        template.setEnabled(request.getEnabled());
        
        return codeTemplateRepository.save(template);
    }

    @Transactional
    public void delete(Long id) {
        if (!codeTemplateRepository.existsById(id)) {
            throw new RuntimeException("模板不存在");
        }
        codeTemplateRepository.deleteById(id);
    }

    public CodeTemplate getById(Long id) {
        return codeTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
    }

    public List<CodeTemplate> listAll() {
        return codeTemplateRepository.findAll();
    }

    public List<CodeTemplate> listEnabled() {
        return codeTemplateRepository.findByEnabledTrue();
    }

    public List<CodeTemplate> listByLanguage(String language) {
        return codeTemplateRepository.findByLanguage(language);
    }

    public List<CodeTemplate> listByProjectId(Long projectId) {
        return codeTemplateRepository.findByProjectIdAndEnabledTrue(projectId);
    }
}
