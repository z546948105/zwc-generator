package com.zwc.generator.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "code_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "file_path_pattern", nullable = false)
    private String filePathPattern;

    @Column(name = "language")
    private String language;

    @Column(name = "description")
    private String description;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;
}
