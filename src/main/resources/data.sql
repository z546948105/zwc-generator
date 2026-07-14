INSERT INTO project (name, package_name, description, enabled) VALUES ('默认项目', 'com.example.demo', '系统默认项目', true);

INSERT INTO code_template (name, content, file_path_pattern, language, description, project_id, enabled) VALUES ('Entity模板', 
'package ${packageName}.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ${tableInfo.tableComment}
 */
@Entity
@Table(name = "${tableInfo.tableName}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${tableInfo.className} {

<#list tableInfo.columns as column>
<#if column.primaryKey>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
</#if>
    @Column(name = "${column.columnName}"<#if !column.nullable>, nullable = false</#if>)
    private ${column.javaType} ${column.javaFieldName};

</#list>
}',
'${packagePath}/entity/${classSimpleName}.java',
'java',
'Java实体类模板',
NULL,
true);

INSERT INTO code_template (name, content, file_path_pattern, language, description, project_id, enabled) VALUES ('Mapper模板',
'package ${packageName}.mapper;

import ${packageName}.entity.${classSimpleName};
import org.apache.ibatis.annotations.Mapper;

/**
 * ${tableInfo.tableComment} Mapper
 */
@Mapper
public interface ${classSimpleName}Mapper {

    int insert(${classSimpleName} record);

    int insertSelective(${classSimpleName} record);

    ${classSimpleName} selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(${classSimpleName} record);

    int updateByPrimaryKey(${classSimpleName} record);

    int deleteByPrimaryKey(Long id);
}',
'${packagePath}/mapper/${classSimpleName}Mapper.java',
'java',
'MyBatis Mapper接口模板',
NULL,
true);

INSERT INTO code_template (name, content, file_path_pattern, language, description, project_id, enabled) VALUES ('Service模板',
'package ${packageName}.service;

import ${packageName}.entity.${classSimpleName};

/**
 * ${tableInfo.tableComment} Service
 */
public interface ${classSimpleName}Service {

    ${classSimpleName} getById(Long id);

    ${classSimpleName} create(${classSimpleName} entity);

    ${classSimpleName} update(${classSimpleName} entity);

    void delete(Long id);
}',
'${packagePath}/service/${classSimpleName}Service.java',
'java',
'Service接口模板',
NULL,
true);

INSERT INTO code_template (name, content, file_path_pattern, language, description, project_id, enabled) VALUES ('ServiceImpl模板',
'package ${packageName}.service.impl;

import ${packageName}.entity.${classSimpleName};
import ${packageName}.mapper.${classSimpleName}Mapper;
import ${packageName}.service.${classSimpleName}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ${tableInfo.tableComment} Service实现
 */
@Service
@RequiredArgsConstructor
public class ${classSimpleName}ServiceImpl implements ${classSimpleName}Service {

    private final ${classSimpleName}Mapper ${classSimpleName?uncap_first}Mapper;

    @Override
    public ${classSimpleName} getById(Long id) {
        return ${classSimpleName?uncap_first}Mapper.selectByPrimaryKey(id);
    }

    @Override
    public ${classSimpleName} create(${classSimpleName} entity) {
        ${classSimpleName?uncap_first}Mapper.insert(entity);
        return entity;
    }

    @Override
    public ${classSimpleName} update(${classSimpleName} entity) {
        ${classSimpleName?uncap_first}Mapper.updateByPrimaryKeySelective(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        ${classSimpleName?uncap_first}Mapper.deleteByPrimaryKey(id);
    }
}',
'${packagePath}/service/impl/${classSimpleName}ServiceImpl.java',
'java',
'Service实现类模板',
NULL,
true);

INSERT INTO code_template (name, content, file_path_pattern, language, description, project_id, enabled) VALUES ('Controller模板',
'package ${packageName}.controller;

import ${packageName}.entity.${classSimpleName};
import ${packageName}.service.${classSimpleName}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ${tableInfo.tableComment} Controller
 */
@RestController
@RequestMapping("/api/${tableName?lower_case}")
@RequiredArgsConstructor
public class ${classSimpleName}Controller {

    private final ${classSimpleName}Service ${classSimpleName?uncap_first}Service;

    @GetMapping("/{id}")
    public ${classSimpleName} getById(@PathVariable Long id) {
        return ${classSimpleName?uncap_first}Service.getById(id);
    }

    @PostMapping
    public ${classSimpleName} create(@RequestBody ${classSimpleName} entity) {
        return ${classSimpleName?uncap_first}Service.create(entity);
    }

    @PutMapping
    public ${classSimpleName} update(@RequestBody ${classSimpleName} entity) {
        return ${classSimpleName?uncap_first}Service.update(entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ${classSimpleName?uncap_first}Service.delete(id);
    }
}',
'${packagePath}/controller/${classSimpleName}Controller.java',
'java',
'Controller模板',
NULL,
true);
