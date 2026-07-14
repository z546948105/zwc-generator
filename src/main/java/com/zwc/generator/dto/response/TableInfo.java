package com.zwc.generator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo {

    private String tableName;

    private String tableComment;

    private String className;

    private String classSimpleName;

    private List<ColumnInfo> columns;
}
