package com.zwc.generator.controller;

import com.zwc.generator.dto.request.CodeGenerateRequest;
import com.zwc.generator.dto.response.ApiResponse;
import com.zwc.generator.dto.response.TableInfo;
import com.zwc.generator.service.CodeGenerateService;
import com.zwc.generator.service.FileDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
public class CodeGenerateController {

    private final CodeGenerateService codeGenerateService;
    private final FileDownloadService fileDownloadService;

    @GetMapping("/table-info")
    public ApiResponse<List<TableInfo>> getTableInfo(@RequestParam Long dataSourceId, 
                                                      @RequestParam List<String> tableNames) {
        List<TableInfo> tableInfoList = codeGenerateService.getTableInfoList(dataSourceId, tableNames);
        return ApiResponse.success(tableInfoList);
    }

    @PostMapping("/preview")
    public ApiResponse<Map<String, String>> previewCode(@RequestBody CodeGenerateRequest request) {
        Map<String, String> files = codeGenerateService.generateCode(request);
        return ApiResponse.success("预览成功", files);
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadCode(@RequestBody CodeGenerateRequest request) throws IOException {

        Map<String, String> files = codeGenerateService.generateCode(request);
        byte[] zipBytes = fileDownloadService.generateZip(files);
        
        String fileName = "code-generate-" + System.currentTimeMillis() + ".zip";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                .body(zipBytes);
    }
}
