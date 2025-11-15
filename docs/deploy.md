# 部署与启动（MVP）

本指南提供两个最小可运行（MVP）方式：
- 使用 `docker-compose`（推荐）启动 MySQL 与后端服务（Profile=prod）。
- 或在本地以 `test` Profile（内存 H2）快速验证（无需 MySQL），用于功能演示。

## 1. 环境要求
- 服务器：Linux（x86_64）或云主机
- 必备：Docker 24+ 与 Docker Compose v2（`docker compose` 命令）；或 Java 21 + MySQL 8（可选）

## 2. 环境变量（一次性列出占位符）
- 数据库
  - `MYSQL_ROOT_PASSWORD`：MySQL root 密码（默认 `rootpass`）
  - `MYSQL_DATABASE`：数据库名（默认 `leave_mgmt`）
  - `DB_URL`：Spring 数据源 URL（compose 自动传入 `jdbc:mysql://mysql:3306/<DB>?...`）
  - `DB_USER`：数据库用户名（默认 `root`）
  - `DB_PASSWORD`：数据库密码（默认读取 `MYSQL_ROOT_PASSWORD`）
- 安全与令牌
  - `JWT_SECRET`：生产环境的 JWT 密钥（必须自定义）
  - `TOKEN_EXPIRATION_MINUTES`：令牌有效分钟数（默认 `120`）
- 邮件（按需配置）
  - `SMTP_HOST`、`SMTP_PORT`
  - `SMTP_USERNAME`、`SMTP_PASSWORD`
  - `SMTP_AUTH`（默认 `true`）、`SMTP_STARTTLS`（默认 `false`）、`SMTP_DEBUG`（默认 `false`）

> 生产环境请务必提供：`MYSQL_ROOT_PASSWORD`、`JWT_SECRET`、（可选）`SMTP_*`。在 PR 回复中明确这些占位符由负责人提供。

## 3. 使用 docker-compose 部署（推荐）
1) 准备 `.env`（可选）：
```
MYSQL_ROOT_PASSWORD=your-strong-pass
MYSQL_DATABASE=leave_mgmt
JWT_SECRET=please-change-in-prod
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=smtp-user
SMTP_PASSWORD=smtp-pass
SMTP_AUTH=true
SMTP_STARTTLS=true
SMTP_DEBUG=false
```

2) 构建并启动：
```
docker compose up -d --build
docker compose logs -f mysql
docker compose logs -f backend
```

3) 验证：
- 访问静态登录页：`http://<服务器IP或域名>:8080/login.html`
- 调用登录 API（种子用户由 Flyway + 初始化器写入）：
```
curl -s -X POST \
  -H 'Content-Type: application/json' \
  -d '{"username":"emp","password":"emp123"}' \
  http://<服务器IP或域名>:8080/api/auth/login
```
预期返回包含 `accessToken`、`tokenType` 和 `expiresIn`。

## 4. 在 Linux 服务器上部署（单机 JAR + MySQL，备选）
1) 安装 MySQL 8 并创建库、用户；将连接信息按环境变量传给应用。
2) 构建 JAR：
```
mvn -q -f backend/pom.xml -DskipTests package
```
3) 启动（prod）：
```
export SPRING_PROFILES_ACTIVE=prod
export DB_URL='jdbc:mysql://127.0.0.1:3306/leave_mgmt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USER='root'
export DB_PASSWORD='your-pass'
export JWT_SECRET='please-change-in-prod'
java -jar backend/target/*.jar
```

## 5. 使用 test Profile 快速演示（H2 内存库）
适用于本地开发或无 MySQL 的环境，验证服务与页面可访问：
```
mvn -q -f backend/pom.xml -DskipTests package
java -jar backend/target/*.jar --spring.profiles.active=test
curl -s http://127.0.0.1:8080/login.html | head -n 1
```

## 6. systemd service（可选）
`/etc/systemd/system/leave-backend.service`
```
[Unit]
Description=Leave Backend Service
After=network.target

[Service]
Type=simple
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=DB_URL=jdbc:mysql://127.0.0.1:3306/leave_mgmt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
Environment=DB_USER=root
Environment=DB_PASSWORD=your-pass
Environment=JWT_SECRET=please-change-in-prod
WorkingDirectory=/opt/leave-app
ExecStart=/usr/bin/java -jar /opt/leave-app/backend.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```
启用：
```
sudo systemctl daemon-reload
sudo systemctl enable --now leave-backend
sudo systemctl status leave-backend
```

## 7. 文件清单与关键片段
- 新增 `docker-compose.yml`：定义 `mysql` 与 `backend` 服务，传递环境变量。
- 新增 `backend/Dockerfile`：多阶段构建（Maven JDK21 构建，Temurin JRE 运行）。
- 新增 `backend/src/main/resources/application-prod.yml`：启用 Flyway（`classpath:db/migration`），使用环境变量配置。
- 文档 `docs/deploy.md`（本文件）：部署步骤、占位符与验证命令。

## 8. 验收验证命令
- 容器方式：
```
docker compose up -d --build
curl -s http://127.0.0.1:8080/login.html | head -n 1
curl -s -X POST -H 'Content-Type: application/json' -d '{"username":"emp","password":"emp123"}' http://127.0.0.1:8080/api/auth/login
```
- 单机演示（test Profile）：
```
mvn -q -f backend/pom.xml -DskipTests package
java -jar backend/target/*.jar --spring.profiles.active=test
curl -s http://127.0.0.1:8080/login.html | head -n 1
```

