package com.zwc.generator.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeTemplateRequest {

    @NotBlank(message = "模板名称不能为空")
    private String name;

    @NotBlank(message = "模板内容不能为空")
    private String content;

    @NotBlank(message = "文件路径模板不能为空")
    private String filePathPattern;

    private String language;

    private String description;

    private Long projectId;

    @Builder.Default
    private Boolean enabled = true;
}
