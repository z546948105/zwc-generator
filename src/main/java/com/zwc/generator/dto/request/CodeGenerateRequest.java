package com.zwc.generator.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenerateRequest {

    @NotBlank(message = "数据源ID不能为空")
    private String dataSourceId;

    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    @NotEmpty(message = "模板ID列表不能为空")
    private List<Long> templateIds;

    @NotBlank(message = "包名不能为空")
    private String packageName;

    private String author;

    private String moduleName;
}
