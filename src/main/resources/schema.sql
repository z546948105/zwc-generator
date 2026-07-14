CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    package_name VARCHAR(200),
    description VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS data_source_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(500) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(200) NOT NULL,
    driver_class VARCHAR(200),
    description VARCHAR(500),
    project_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS code_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    file_path_pattern VARCHAR(500) NOT NULL,
    language VARCHAR(50),
    description VARCHAR(500),
    project_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE SET NULL
);
