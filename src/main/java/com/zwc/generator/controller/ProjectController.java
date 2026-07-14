package com.zwc.generator.controller;

import com.zwc.generator.dto.response.ApiResponse;
import com.zwc.generator.entity.Project;
import com.zwc.generator.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<List<Project>> getAllProjects() {
        return ApiResponse.success(projectService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Project> getProjectById(@PathVariable Long id) {
        return projectService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("项目不存在"));
    }

    @PostMapping
    public ApiResponse<Project> createProject(@RequestBody Project project) {
        return ApiResponse.success(projectService.create(project));
    }

    @PutMapping("/{id}")
    public ApiResponse<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        return ApiResponse.success(projectService.update(id, project));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/{targetProjectId}/copy/datasource/{sourceProjectId}/{datasourceId}")
    public ApiResponse<Void> copyDatasourceFromProject(
            @PathVariable Long targetProjectId,
            @PathVariable Long sourceProjectId,
            @PathVariable Long datasourceId) {
        projectService.copyDatasourceFromProject(targetProjectId, sourceProjectId, datasourceId);
        return ApiResponse.success();
    }

    @PostMapping("/{targetProjectId}/copy/template/{sourceProjectId}/{templateId}")
    public ApiResponse<Void> copyTemplateFromProject(
            @PathVariable Long targetProjectId,
            @PathVariable Long sourceProjectId,
            @PathVariable Long templateId) {
        projectService.copyTemplateFromProject(targetProjectId, sourceProjectId, templateId);
        return ApiResponse.success();
    }

    @PostMapping("/{targetProjectId}/copy/all/{sourceProjectId}")
    public ApiResponse<Void> copyAllFromProject(
            @PathVariable Long targetProjectId,
            @PathVariable Long sourceProjectId) {
        projectService.copyAllFromProject(targetProjectId, sourceProjectId);
        return ApiResponse.success();
    }
}
