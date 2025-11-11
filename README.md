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
2. 前端（静态预览，二选一，当前为占位页目录）
   - 使用 Node：`npx http-server ./frontend -p 5173`（或 `npx serve ./frontend -p 5173`）
   - 使用 Python：`python -m http.server 5173 -d frontend`
3. 后端（当初始化完成后，二选一）
   - Gradle：`cd backend && ./gradlew bootRun`（Windows: `gradlew.bat bootRun`）
   - Maven：`cd backend && mvn -B spring-boot:run`

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

