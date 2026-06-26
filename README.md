# Farm2Future Backend

Farm2Future Backend 是 Farm2Future 项目的后端服务，主要为前端系统提供 REST API 接口支持。

本项目基于 Spring Boot 开发，使用 MySQL 作为数据库，MyBatis-Plus 作为 ORM 框架，主要用于处理农场数据、ESG 评分、Token 记录、交易记录、用户角色以及后台仪表盘数据。

---

## 一、项目简介

Farm2Future 是一个面向农业数字化管理的平台，旨在通过后端系统管理农场数据、ESG 评分数据和 Token 奖励数据，为前端页面提供稳定的数据接口。

后端主要负责：

- 提供 RESTful API 接口
- 管理农场相关数据
- 管理 ESG 评分数据
- 管理 Token 奖励记录
- 管理交易记录
- 连接 MySQL 数据库
- 为前端 Dashboard 页面提供统计数据
- 支持后续用户登录、身份认证和角色权限控制

---

## 二、技术栈

| 技术 | 说明 |
|---|---|
| Java 21 | 后端主要开发语言 |
| Spring Boot 3 | 后端主框架 |
| Spring Web | 用于开发 REST API |
| Spring Security | 用于安全认证和权限控制 |
| MyBatis-Plus | 数据库 ORM 框架 |
| MySQL | 关系型数据库 |
| JWT | Token 身份认证支持 |
| Lombok | 简化 Java 代码 |
| Spring Boot Actuator | 项目健康检查和监控 |
| Springdoc OpenAPI | API 文档支持 |
| Maven | 项目依赖管理和打包工具 |

---

## 三、项目结构

```text
Fram2Future-Backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.farm2future.farm2future_backend
│   │   │       ├── common
│   │   │       ├── model
│   │   │       │   ├── dashboard
│   │   │       │   ├── fram
│   │   │       │   └── user
│   │   │       └── Farm2futureBackendApplication.java
│   │   └── resources
│   │       └── application.yml
│   └── test
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## 四、主要功能

### 1. Dashboard 数据接口

后端提供 Dashboard 总览接口，用于给前端首页展示统计数据。

接口示例：

```http
GET /api/dashboard/overview
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalFarms": 0,
    "totalTokens": 0,
    "totalTransactions": 0,
    "averageEsgScore": 0
  }
}
```

---

### 2. 统一返回结果

项目使用统一的 API 返回格式，方便前端处理接口数据。

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

---

### 3. MySQL 数据库连接

数据库配置文件位置：

```text
src/main/resources/application.yml
```

示例配置：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/farm2future?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Kuala_Lumpur&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: your_password
```

注意：不要把真实数据库密码直接提交到 GitHub，建议使用环境变量。

```yml
password: ${DB_PASSWORD}
```

---

## 五、运行环境要求

运行本项目之前，需要安装以下环境：

- Java 21 或以上版本
- Maven 3.8 或以上版本
- MySQL 8 或以上版本
- Git

查看 Java 版本：

```bash
java -version
```

查看 Maven 版本：

```bash
mvn -version
```

---

## 六、本地运行步骤

### 1. 克隆项目

```bash
git clone https://github.com/yinsizhe-spec/Fram2Future-Backend.git
cd Fram2Future-Backend
```

---

### 2. 创建数据库

进入 MySQL 后执行：

```sql
CREATE DATABASE farm2future CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### 3. 修改数据库配置

打开配置文件：

```text
src/main/resources/application.yml
```

修改数据库连接信息：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/farm2future?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Kuala_Lumpur&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: your_password
```

---

### 4. 启动项目

使用 Maven 启动：

```bash
mvn spring-boot:run
```

或者使用 Maven Wrapper：

```bash
./mvnw spring-boot:run
```

Windows 系统使用：

```bash
mvnw.cmd spring-boot:run
```

---

## 七、项目打包

执行以下命令打包项目：

```bash
mvn clean package
```

如果想跳过测试：

```bash
mvn clean package -DskipTests
```

打包完成后，JAR 文件会生成在：

```text
target/
```

运行 JAR 文件：

```bash
java -jar target/farm2future-backend-0.0.1-SNAPSHOT.jar
```

---

## 八、接口说明

| 请求方法 | 接口地址 | 说明 |
|---|---|---|
| GET | `/api/dashboard/overview` | 获取 Dashboard 总览数据 |
| GET | `/actuator/health` | 检查后端运行状态 |
| GET | `/actuator/info` | 查看应用信息 |

---

## 九、API 文档

项目集成了 Springdoc OpenAPI。

启动项目后，可以访问 Swagger 页面：

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON 地址：

```text
http://localhost:8080/v3/api-docs
```

如果项目端口不是 `8080`，需要把地址中的端口改成实际端口。

---

## 十、服务器部署

### 1. 打包项目

```bash
mvn clean package -DskipTests
```

---

### 2. 上传 JAR 文件到服务器

示例服务器目录：

```text
/root/farm2future/
```

---

### 3. 启动后端服务

```bash
nohup java -jar farm2future-backend.jar > app.log 2>&1 &
```

---

### 4. 查看项目是否运行

```bash
ps -ef | grep farm2future-backend
```

---

### 5. 查看运行日志

```bash
tail -f app.log
```

---

## 十一、服务器访问示例

如果后端部署在服务器上，接口访问格式如下：

```text
http://服务器IP:端口号/接口路径
```

例如：

```text
http://64.176.57.254:7020/api/dashboard/overview
```

---

## 十二、常见问题

### 1. MySQL 连接失败

请检查：

- MySQL 服务是否正在运行
- 数据库名称是否正确
- 用户名和密码是否正确
- 数据库端口是否开放
- 服务器防火墙是否允许访问 MySQL
- 远程连接时 MySQL 是否允许远程访问

---

### 2. Public Key Retrieval is not allowed

如果出现：

```text
Public Key Retrieval is not allowed
```

可以在数据库连接 URL 中加入：

```text
allowPublicKeyRetrieval=true
```

示例：

```yml
url: jdbc:mysql://localhost:3306/farm2future?allowPublicKeyRetrieval=true&useSSL=false
```

---

### 3. 端口被占用

查看端口占用情况：

```bash
lsof -i:8080
```

结束进程：

```bash
kill -9 PID
```

或者修改 `application.yml` 中的端口：

```yml
server:
  port: 8081
```

---

### 4. 后端启动成功但接口无法访问

请检查：

- 后端是否真的启动成功
- 访问端口是否正确
- 服务器防火墙是否开放端口
- 云服务器安全组是否开放端口
- 前端配置的 API 地址是否正确
- 如果使用 Nginx，检查反向代理配置是否正确

---

## 十三、Git 提交示例

添加 README 文件：

```bash
git add README.md
```

提交修改：

```bash
git commit -m "docs: add Chinese README for backend project"
```

推送到 GitHub：

```bash
git push origin main
```

---

## 十四、后续开发计划

后续可以继续完善以下功能：

- 完成用户登录接口
- 添加 JWT 身份认证
- 添加 Farmer、Buyer、Regulator 角色权限控制
- 完善农场管理接口
- 完善 ESG 评分计算逻辑
- 完善 Token 奖励发放逻辑
- 添加交易记录查询接口
- 完善 Swagger API 文档
- 添加单元测试和集成测试
- 添加 Docker 部署支持
- 添加 Jenkins 或 GitHub Actions 自动化部署

---

## 十五、项目作者

本项目由 Farm2Future 小组开发。

后端仓库地址：

```text
https://github.com/yinsizhe-spec/Fram2Future-Backend
```
