# Leave Backend (Spring Boot 3 · Maven · Java 21)

## 启动步骤
### 方式 A：开发模式（H2，test Profile，无需 MySQL）
- 打包：`mvn -f backend/pom.xml -DskipTests package`
- 运行：`mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=test`
- 或 JAR：`java -jar backend/target/leave-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=test`

### 方式 B：MySQL（prod Profile）
1. 确认本地或容器中 MySQL 已创建库 `leave_mgmt`（也可用 `docker compose up -d mysql` 启动容器）。
2. 配置环境变量（可选，或直接使用默认值）：
   - `DB_URL=jdbc:mysql://localhost:3306/leave_mgmt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `DB_USER=root`
   - `DB_PASSWORD=nick030201`
   - `JWT_SECRET=change-me-in-prod`
   - 邮件（SMTP）：
     - `SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASSWORD`
     - 可选：`SMTP_AUTH=true`、`SMTP_STARTTLS=false`、`SMTP_DEBUG=true`
3. 构建并运行：
   - 构建：`mvn -f backend/pom.xml -DskipTests package`
   - 运行（prod）：`mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=prod`
   - 运行（JAR）：`java -jar backend/target/leave-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`

## 健康检查
- 接口：`GET /api/health`
- 示例：
  - PowerShell：`curl http://localhost:8080/api/health`
  - Bash：`curl -s http://localhost:8080/api/health | jq .`
- 预期响应：`{"status":"UP"}`

## 依赖
- Spring Boot：web、data-jpa、security、mail
- 数据库：MySQL Connector/J
- JWT：JJWT（api/impl/jackson）
- 文档与工具：Apache POI（Excel/Word），Flyway（可选迁移）

## Flyway
- dev 默认禁用（`application.yml -> spring.flyway.enabled: false`）。
- prod 启用（`application-prod.yml`），位置：`classpath:db/migration`（已包含 `V1__init.sql`）。
- H2 测试（`application-test.yml`）使用 `classpath:db/migration_h2`。
## 邮件通知（MVP）
- 事件：提交申请 -> 通知经理；审批通过/驳回 -> 通知员工
- 配置：见 `docs/mail_config.md`
- 调试：可使用 Mailtrap 或本地 MailHog（`docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`）
