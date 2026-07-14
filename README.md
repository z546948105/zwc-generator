# zwc-generator

代码生成工具 - 支持多数据源、模板可维护、项目管理

## 技术栈

- **框架**: Spring Boot 2.7.18
- **JDK**: 1.8
- **数据库**: H2 (默认), MySQL, Oracle, OceanBase
- **连接池**: Druid 1.2.20
- **模板引擎**: FreeMarker 2.3.32
- **ORM**: Spring Data JPA
- **构建工具**: Maven

## 功能特性

- ✅ 多数据源管理（支持 MySQL、Oracle、OceanBase）
- ✅ 代码模板管理（Entity、Mapper、Service、ServiceImpl、Controller）
- ✅ 项目管理（可创建项目，复制数据源和模板）
- ✅ 代码生成与下载
- ✅ 包名和模板名可维护
- ✅ 多环境配置文件

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+

### 运行项目

```bash
# 进入项目目录
cd zwc-generator

# 使用默认 H2 数据库启动
mvn spring-boot:run

# 使用 MySQL 启动
mvn spring-boot:run -Dspring.profiles.active=mysql
```

### 访问地址

- **前端页面**: http://localhost:8899/index.html
- **H2 控制台**: http://localhost:8899/h2-console

### 使用 Docker 启动

```bash
# 进入项目目录
cd zwc-generator

# 构建并启动（包含 MySQL 数据库）
docker-compose up -d

# 查看日志
docker-compose logs -f zwc-generator

# 停止服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v
```

**Docker 环境说明：**
- MySQL 版本：5.7
- MySQL 端口：3306
- 数据库名：zwc_generator
- 用户名：root
- 密码：root

## 配置说明

### 数据库配置

项目支持多种数据库，通过 Spring Profiles 切换：

| Profile | 文件 | 说明 |
|---------|------|------|
| h2 | application-h2.yml | 默认，嵌入式数据库 |
| mysql | application-mysql.yml | MySQL 数据库 |
| oracle | application-oracle.yml | Oracle 数据库 |
| oceanbase | application-oceanbase.yml | OceanBase 数据库 |

### MySQL 配置示例

修改 `application-mysql.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zwc_generator?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 表结构

系统启动时自动创建以下表：

- `project` - 项目信息表
- `data_source_config` - 数据源配置表
- `code_template` - 代码模板表

## 使用方法

### 1. 项目管理

在"项目管理"标签页中：

- **添加项目**: 点击"添加项目"按钮，填写项目名称和包名
- **编辑项目**: 点击项目列表中的"编辑"按钮
- **复制数据源/模板**: 点击"复制数据源"或"复制模板"按钮，可从其他项目复制
- **复制全部**: 一键复制所有数据源和模板

### 2. 数据源管理

在"数据源管理"标签页中：

- **添加数据源**: 点击"添加数据源"按钮
- **选择数据库类型**: 支持 MySQL、Oracle、OceanBase 等
- **测试连接**: 点击"测试连接"验证数据源配置
- **关联项目**: 可选择所属项目（或设为全局）

### 3. 模板管理

在"模板管理"标签页中：

- **预置模板**: 系统预置 5 个模板（Entity、Mapper、Service、ServiceImpl、Controller）
- **添加模板**: 点击"添加模板"按钮，自定义模板内容
- **关联项目**: 可选择所属项目（或设为全局）
- **模板变量**: `${packageName}`, `${className}`, `${tableName}`, `${column.columnName}` 等

### 4. 代码生成

在"代码生成"标签页中：

- **选择数据源**: 从已配置的数据源中选择
- **输入表名**: 输入要生成代码的表名（多个表用逗号分隔）
- **选择模板**: 选择要使用的模板（可多选）
- **输入包名**: 输入目标包名
- **生成代码**: 点击"生成代码"按钮，下载生成的 ZIP 文件

## API 接口

### 项目管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/project | 获取所有项目 |
| GET | /api/project/{id} | 获取单个项目 |
| POST | /api/project | 创建项目 |
| PUT | /api/project/{id} | 更新项目 |
| DELETE | /api/project/{id} | 删除项目 |
| POST | /api/project/{target}/copy/all/{source} | 复制全部 |

### 数据源管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/datasource | 获取所有数据源 |
| POST | /api/datasource | 添加数据源 |
| PUT | /api/datasource/{id} | 更新数据源 |
| DELETE | /api/datasource/{id} | 删除数据源 |
| GET | /api/datasource/project/{projectId} | 按项目查询 |

### 模板管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/template | 获取所有模板 |
| POST | /api/template | 添加模板 |
| PUT | /api/template/{id} | 更新模板 |
| DELETE | /api/template/{id} | 删除模板 |
| GET | /api/template/project/{projectId} | 按项目查询 |

### 代码生成

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/generate/download | 生成代码并下载 |

## 项目结构

```
zwc-generator/
├── src/main/java/com/zwc/generator/
│   ├── controller/     # REST API 控制层
│   ├── service/        # 业务逻辑层
│   ├── repository/     # 数据访问层
│   ├── entity/         # 实体类
│   ├── dto/            # 数据传输对象
│   ├── config/         # 配置类
│   ├── enums/          # 枚举类
│   └── GeneratorApplication.java
├── src/main/resources/
│   ├── application.yml         # 主配置文件
│   ├── application-h2.yml      # H2 配置
│   ├── application-mysql.yml   # MySQL 配置
│   ├── application-oracle.yml  # Oracle 配置
│   ├── application-oceanbase.yml # OceanBase 配置
│   ├── schema.sql              # 建表语句
│   ├── data.sql                # 初始化数据
│   └── static/index.html       # 前端页面
└── pom.xml
```

## License

MIT License
