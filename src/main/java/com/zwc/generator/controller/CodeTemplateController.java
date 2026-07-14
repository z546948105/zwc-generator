package com.zwc.generator.controller;

import com.zwc.generator.dto.request.CodeTemplateRequest;
import com.zwc.generator.dto.response.ApiResponse;
import com.zwc.generator.entity.CodeTemplate;
import com.zwc.generator.service.CodeTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class CodeTemplateController {

    private final CodeTemplateService codeTemplateService;

    @PostMapping
    public ApiResponse<CodeTemplate> create(@RequestBody CodeTemplateRequest request) {
        CodeTemplate template = codeTemplateService.create(request);
        return ApiResponse.success("创建成功", template);
    }

    @PutMapping("/{id}")
    public ApiResponse<CodeTemplate> update(@PathVariable Long id, @RequestBody CodeTemplateRequest request) {
        CodeTemplate template = codeTemplateService.update(id, request);
        return ApiResponse.success("更新成功", template);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        codeTemplateService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<CodeTemplate> getById(@PathVariable Long id) {
        CodeTemplate template = codeTemplateService.getById(id);
        return ApiResponse.success(template);
    }

    @GetMapping
    public ApiResponse<List<CodeTemplate>> listAll() {
        List<CodeTemplate> list = codeTemplateService.listAll();
        return ApiResponse.success(list);
    }

    @GetMapping("/enabled")
    public ApiResponse<List<CodeTemplate>> listEnabled() {
        List<CodeTemplate> list = codeTemplateService.listEnabled();
        return ApiResponse.success(list);
    }

    @GetMapping("/language/{language}")
    public ApiResponse<List<CodeTemplate>> listByLanguage(@PathVariable String language) {
        List<CodeTemplate> list = codeTemplateService.listByLanguage(language);
        return ApiResponse.success(list);
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<CodeTemplate>> listByProjectId(@PathVariable Long projectId) {
        List<CodeTemplate> list = codeTemplateService.listByProjectId(projectId);
        return ApiResponse.success(list);
    }
}
