package com.zwc.generator.controller;

import com.zwc.generator.dto.request.DataSourceConfigRequest;
import com.zwc.generator.dto.response.ApiResponse;
import com.zwc.generator.entity.DataSourceConfig;
import com.zwc.generator.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/datasource")
@RequiredArgsConstructor
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @PostMapping
    public ApiResponse<DataSourceConfig> create(@RequestBody DataSourceConfigRequest request) {
        DataSourceConfig config = dataSourceService.create(request);
        return ApiResponse.success("创建成功", config);
    }

    @PutMapping("/{id}")
    public ApiResponse<DataSourceConfig> update(@PathVariable Long id, @RequestBody DataSourceConfigRequest request) {
        DataSourceConfig config = dataSourceService.update(id, request);
        return ApiResponse.success("更新成功", config);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dataSourceService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<DataSourceConfig> getById(@PathVariable Long id) {
        DataSourceConfig config = dataSourceService.getById(id);
        return ApiResponse.success(config);
    }

    @GetMapping
    public ApiResponse<List<DataSourceConfig>> listAll() {
        List<DataSourceConfig> list = dataSourceService.listAll();
        return ApiResponse.success(list);
    }

    @GetMapping("/enabled")
    public ApiResponse<List<DataSourceConfig>> listEnabled() {
        List<DataSourceConfig> list = dataSourceService.listEnabled();
        return ApiResponse.success(list);
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<DataSourceConfig>> listByProjectId(@PathVariable Long projectId) {
        List<DataSourceConfig> list = dataSourceService.listByProjectId(projectId);
        return ApiResponse.success(list);
    }

    @GetMapping("/{id}/tables")
    public ApiResponse<List<String>> getTableNames(@PathVariable Long id) {
        List<String> tableNames = dataSourceService.getTableNames(id);
        return ApiResponse.success(tableNames);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Void> testConnection(@PathVariable Long id) {
        dataSourceService.testConnection(id);
        return ApiResponse.success("连接测试成功", null);
    }
}
