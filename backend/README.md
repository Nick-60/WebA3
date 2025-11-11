# Leave Backend (Spring Boot 3 · Maven · Java 21)

## 启动步骤
1. 确认本地 MySQL 已创建库 `leave_mgmt`（可运行：`mysql -uroot -pnick030201 < ../scripts/db/init.sql`）。
2. 配置环境变量（可选，或直接使用默认值）：
   - `DB_URL=jdbc:mysql://localhost:3306/leave_mgmt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `DB_USER=root`
   - `DB_PASSWORD=nick030201`
   - `JWT_SECRET=change-me-in-prod`
3. 构建并运行：
   - 构建：`mvn -f backend/pom.xml -DskipTests package`
   - 运行（开发）：`mvn -f backend/pom.xml spring-boot:run`
   - 运行（可执行 JAR）：`java -jar backend/target/leave-backend-0.0.1-SNAPSHOT.jar --spring.flyway.enabled=false`

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
- 默认禁用（`spring.flyway.enabled: false`），位置：`classpath:db/migration`（已包含 `V1__init.sql`）。
- 如需启用迁移：
  - 修改 `application.yml` 中 `spring.flyway.enabled: true`，或运行时追加 `--spring.flyway.enabled=true`。
