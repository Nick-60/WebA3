# WebA3 — 项目基础仓库（MVP）

> REPO_URL: `https://github.com/Nick-60/WebA3`  
> 主分支（MAIN_BRANCH_NAME）: `main`  
> 开发分支（DEV_BRANCH_NAME）: `develop`

## 项目简介
- 后端：Java（Spring Boot 3）
- 前端：静态 AdminLTE 页面（HTML/CSS/JS）
- 目标：以任务为单位，逐步迭代，每次提交都提供可运行的最小版本（MVP）。

## 快速启动（本地）
1. 克隆仓库
   - Windows: `git clone https://github.com/Nick-60/WebA3 && cd WebA3 && dir`
   - Linux/macOS: `git clone https://github.com/Nick-60/WebA3 && cd WebA3 && ls -la`
2. 后端（Maven · Java 21）
   - 开发（H2，test Profile）：
     - 打包：`mvn -q -f backend/pom.xml -DskipTests package`
     - 运行：`mvn -q -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=test`
     - 或：`java -jar backend/target/*.jar --spring.profiles.active=test`
   - 生产（MySQL，prod Profile）：
     - 使用 Docker Compose：`docker compose up -d --build`
     - 或本地 MySQL：设置 `DB_URL/DB_USER/DB_PASSWORD` 后运行 `mvn spring-boot:run`

## 前端预览（服务器重定向方案）

- 在项目根目录启动内置静态服务器（根路径服务端重定向到 `/frontend/login.html`）：
- 命令：`node server.js`（默认端口 `5173`，可通过 `PORT` 环境变量覆盖）
- 访问：`http://127.0.0.1:5173/`

说明：该方案不依赖第三方库，后续如需改为完整入口页或使用 Vite，可在此基础上演进。

### 前端代码风格

- JS：semicolon optional（可省略分号），请保持一致风格。
3. 前端（静态页面）
   - 推荐在 VS Code 安装 Live Server 插件后，右键 `frontend/login.html` → `Open with Live Server`。
   - 或使用 Node：`npx http-server ./frontend -p 5173`（或 `npx serve ./frontend -p 5173`）。
   - 默认前端将调用 `http://localhost:8080` 的后端接口（见 `frontend/js/config.js`）。

## 技术栈
- 后端：Java 21/Spring Boot 3，构建工具 Maven（已选）
- 前端：AdminLTE（静态页面），可后续接入构建工具（如 Vite）

## 目录结构
```
backend/     # 后端工程目录（待初始化）
frontend/    # 前端静态页面（AdminLTE）
docs/        # 文档（开发流程、架构、变更日志等）
scripts/     # 辅助脚本（启动、工具、CI 本地验证）
```

## 提交规范
- 提交信息（commit message）采用 Conventional Commits 的简化版本：
  - `feat: 添加 X`
  - `fix: 修复 Y`
  - `docs: 更新文档`
  - `ci: 配置或修复 CI`
  - `chore: 构建/依赖/其他杂项`
  - `refactor: 代码重构（无功能变化）`
  - `test: 测试相关`

### 分支策略
- 主分支：`main`（稳定可发布）
- 开发分支：`develop`（聚合日常开发）
- 功能分支：`feature/*`（从 `develop` 切出）
- 缺陷分支：`bugfix/*`（从 `develop` 切出）
- 紧急修复：`hotfix/*`（从 `main` 切出，修复后合并回 `main` 与 `develop`）

### PR 与发布
- 所有 `feature/*`、`bugfix/*` 提 PR 到 `develop`；版本发布时将 `develop` 合并到 `main`。
- `hotfix/*` 直接提 PR 到 `main`，修复后再回合 `develop`。
- 每次 PR 提供最小可运行版本（MVP），并在 PR 说明中写明：变更点、测试步骤、需要的 Secret、风险与兼容性。

## 代码风格
- Java：遵循 Google Java Style 或 Spring 官方代码风格（选择其一，后续在 Checkstyle/Spotless 中固化）。
- 前端 JS：semicolon optional（可选分号），建议统一使用 Prettier 格式化。

## CI（GitHub Actions）
- 后端使用 Gradle 或 Maven 时，CI 自动构建并运行测试。
- 当前仓库仅提供目录与文档占位，若未检测到 `backend/gradlew` 或 `backend/pom.xml`，CI 将跳过构建。

## 后续可能需要的 Secret（占位符）
- 数据库：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`
- Token：`JWT_SECRET`
- 邮件：`SMTP_HOST`、`SMTP_PORT`、`SMTP_USERNAME`、`SMTP_PASSWORD`
- 使用位置：本地 `.env` 文件或 GitHub Secrets（`Settings -> Secrets and variables -> Actions`）。

## 本地检查命令（验收）
- `git clone https://github.com/Nick-60/WebA3 && ls`（或 Windows: `dir`）
- 能看到 `backend/`、`frontend/`、`docs/`、`scripts/` 目录与本 README。

---

## 在 VS Code 启动项目（详细指南）

本项目开发推荐使用 Vite 作为前端开发服务器（多页面入口），Spring Boot 作为后端 API。

### 前端（Vite，多页面入口）

- 启动命令：`npm run dev`
- 访问地址：`http://127.0.0.1:5173/login.html`
- 说明：Vite 已将 `frontend` 设为根目录（`vite.config.js`），多页面 HTML 入口包括：
  - `login.html`
  - `dashboard.html`
  - `apply_leave.html`
  - `pending_approvals.html`
  - `hr_report.html`
- 预览构建产物：`npm run preview`

### 后端（Spring Boot）

- 启动命令：`npm run dev:backend` 或在 `backend` 目录运行 `mvn spring-boot:run`
- 如使用本地 MySQL：设置 `DB_URL` / `DB_USER` / `DB_PASSWORD` 后运行上述命令
- 验证接口：可通过前端页面登录后触发 API；或直接使用工具（如 curl/Postman）访问后端接口

### 数据库迁移与启动

- 默认开发配置：已禁用迁移（`spring.flyway.enabled=false`），适用于你已手动维护数据库结构的场景；直接 `npm run dev:backend` 即可。
- 启用迁移（需要自动执行 SQL 时）：将 `application.yml` 改为 `spring.flyway.enabled=true`，并确保已配置 MySQL 适配依赖与兼容脚本。
- 临时禁用迁移（不改文件）：启动参数 `--spring.flyway.enabled=false` 或环境变量 `SPRING_FLYWAY_ENABLED=false`。
- 修复校验失败：如遇“Validate failed / failed migration”，先清理半完成变更，再执行修复（repair）以恢复历史记录；或参考迁移校验灵活配置，以忽略特定校验项。


### VS Code 操作建议

- 打开集成终端，分别运行前后端命令（前端：`npm run dev`，后端：`npm run dev:backend`）。
- 建议安装 VS Code 扩展：ESLint（前端）、Java（Spring Boot工具）、EditorConfig。
- 前端 JS 风格：semicolon optional（可省略分号），请保持一致风格。

## 快速启动（命令汇总）

- 前端开发：`npm run dev`
- 前端构建：`npm run build:frontend`
- 前端预览：`npm run preview`
- 后端开发：`npm run dev:backend`

## 备注

- 若遇到端口占用，请调整 `vite.config.js` 的 `server.port` 或使用 `PORT` 环境变量启动后端。
- 如需将根路径 `/` 跳转到登录页，可保留此前的 `index.html`；在本方案下推荐直接访问具体页面如 `/login.html`。

### 先决条件
- 安装 VS Code、Java 21（Temurin 推荐）、Maven 3.9+、Docker（可选）
- VS Code 插件：Extension Pack for Java（包含 Debugger for Java）、Spring Boot Tools（可选）、Live Server（前端预览）

### 启动后端（两种方式）
- 方式 A：开发模式（H2 内存库）
  - 在 VS Code 终端执行：`mvn -q -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=test`
  - 或使用“运行与调试”面板，在主类 `LeaveBackendApplication` 上点击“运行”，并在 `args` 中加入 `--spring.profiles.active=test`。
  - 说明：`application-test.yml` 已配置 H2 与 Flyway（`db/migration_h2`），无需 MySQL 即可运行与验证。
- 方式 B：生产/集成验证（MySQL）
  - 执行：`docker compose up -d --build`（启动 `mysql` 与 `backend` 服务，Profile=prod）
  - 或本地 MySQL：设置环境变量 `DB_URL`、`DB_USER`、`DB_PASSWORD` 后，运行：`mvn -q -f backend/pom.xml spring-boot:run`
  - 说明：`application-prod.yml` 启用 Flyway（`classpath:db/migration`），后端会初始化表结构与种子数据（emp/mgr/hr）。

### 启动前端（静态页面）
- VS Code Live Server：打开 `frontend/login.html`，点击右下角“Go Live”（默认端口 5500 或 5173）。
- Node http-server：`npx http-server ./frontend -p 5173`
- 浏览器访问：`http://localhost:5173/login.html`（或 Live Server 提供的 URL）。前端 JS 会指向 `http://localhost:8080`；如需修改，请编辑 `frontend/js/config.js`。

### 基本验证
- 健康检查：`curl http://localhost:8080/api/health`（预期 `{"status":"UP"}`）
- 登录 API（种子用户，仅 test/prod 已初始化时可用）：
  - PowerShell：
    - `Invoke-WebRequest -Uri http://localhost:8080/api/auth/login -Method POST -ContentType 'application/json' -Body '{"username":"emp","password":"emp123"}'`
  - Bash：
    - `curl -s -X POST -H 'Content-Type: application/json' -d '{"username":"emp","password":"emp123"}' http://localhost:8080/api/auth/login`

## 测试账号（默认种子）

- `EMPLOYEE`：`emp / emp123`（或 `emp001 / pass123`）
- `MANAGER`：`mgr / mgr123`
- `HR`：`hr / hr123`

来源：
- 数据库迁移脚本：`db/migrations/V1__init.sql:85–93`
- 应用数据初始化：`backend/src/main/java/com/example/leave/config/DataInitializer.java:21–50`

使用说明：
- 直接在前端 `login.html` 输入上述账户与密码即可登录
- 命令行验证参考“基本验证”章节中的示例

安全提示：
- 这些账号仅用于本地/测试环境，请勿在生产环境保留默认口令
- 生产部署前应删除或修改默认测试用户

## 角色与权限

项目内置 4 类角色（见 `db/migrations/V1__init.sql:19`）：`EMPLOYEE`、`MANAGER`、`HR`、`ADMIN`。

- EMPLOYEE（员工）
  - 职责：提交个人请假、查看进度、可在未审批前取消（如后续扩展）
  - 前端页面：`frontend/apply_leave.html`、`frontend/dashboard.html`
  - 权限范围：仅能创建并查看自身请假单；无审批权限
  - 相关接口：`createLeaveRequest`（`frontend/js/auth.js:7`）、`fetchProfile`（`frontend/js/auth.js:6`）
  - 页面参考：提交表单交互（`frontend/apply_leave.html:72–93`）

- MANAGER（经理）
  - 职责：审批所辖员工的请假（同意/拒绝），填写备注
  - 前端页面：`frontend/pending_approvals.html`
  - 权限范围：查看待审批列表、执行通过/拒绝操作
  - 相关接口：`listPendingApprovals`（`frontend/js/auth.js:8`）、`approveLeave`（`frontend/js/auth.js:9`）、`rejectLeave`（`frontend/js/auth.js:10`）
  - 页面参考：审批列表与操作（`frontend/pending_approvals.html:62–115`）

- HR（人力资源）
  - 职责：导出报表（按日期区间与部门），用于核算与合规
  - 前端页面：`frontend/hr_report.html`
  - 权限范围：访问与下载 HR 报表导出
  - 相关接口：`exportHrReport`（`frontend/js/auth.js:11`）
  - 页面参考：报表导出交互（`frontend/hr_report.html:75–93`）

- ADMIN（管理员）
  - 职责：系统配置与管理（用户、部门、策略等）
  - 前端页面：当前未提供专属管理页面；作为后端预留角色以便后续扩展
  - 权限范围：具备系统级全局权限（后续通过后端策略控制）

说明：
- 登录后由后端返回的 `profile.role` 控制前端可见入口（见 `frontend/dashboard.html:85–90`）
- 角色与权限的强制校验由后端在接口层执行；前端仅做可见性与交互上的引导

### 常见问题
- 8080 无法访问：`Test-NetConnection -ComputerName localhost -Port 8080`
- PowerShell 中 `curl` 与 `Invoke-WebRequest` 别名冲突：使用 `curl.exe` 或显式 `Invoke-WebRequest -Uri ...`
- 邮件发送失败日志：开发环境可忽略；如需禁用或降噪，设置 `SMTP_*` 或在 `application.yml` 调整日志级别。


## 初次推送与分支创建（命令参考）
```bash
git init
git remote add origin https://github.com/Nick-60/WebA3
git add .
git commit -m "chore: 初始化基础目录与占位文件"
git branch -M main
git push -u origin main
# 创建 develop 分支
git checkout -b develop
git push -u origin develop
```

