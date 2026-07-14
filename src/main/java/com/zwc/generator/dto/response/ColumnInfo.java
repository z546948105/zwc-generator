package com.zwc.generator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfo {

    private String columnName;

    private String columnType;

    private String javaType;

    private String javaFieldName;

    private String columnComment;

    private Boolean nullable;

    private Boolean primaryKey;
}
