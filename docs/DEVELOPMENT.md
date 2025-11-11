# 开发与交付流程（DEVELOPMENT）

本文件说明如何分步交付、如何提交 PR、以及如何书写变更日志，确保团队协作规范统一。

## 分支与提交流程
- 主分支：`main`（稳定，可发布）
- 开发分支：`develop`（日常集成）
- 工作分支：
  - `feature/<short-title>`：新功能，从 `develop` 切出
  - `bugfix/<short-title>`：缺陷修复，从 `develop` 切出
  - `hotfix/<short-title>`：紧急修复，从 `main` 切出，合并回 `main` 与 `develop`

### 提交信息（Conventional Commits 简化版）
- `feat: <subject>` 新功能
- `fix: <subject>` 缺陷修复
- `docs: <subject>` 文档更新
- `ci: <subject>` CI 配置或修复
- `chore: <subject>` 构建/依赖/其他杂项
- `refactor: <subject>` 代码重构（无功能变化）
- `test: <subject>` 测试相关

## PR 提交流程（每一步提供 MVP）
1. 从 `develop` 切出分支：`git checkout -b feature/<short-title>`
2. 实现最小可运行版本（MVP），包含必要的文档与测试
3. 使用上述规范提交：`git commit -m "feat: 完成 X 的最小实现"`
4. 推送分支：`git push -u origin feature/<short-title>`
5. 发起到 `develop` 的 PR，并填写以下模板：

### PR 模板（示例）
- 完成内容（bullets）：
  - 添加/修改了哪些文件与模块
  - 关键业务逻辑或配置变化
- 测试与验证方式：
  - 本地运行/单元测试命令
  - 预期输出或页面位置
- 需要的 Secret（如有）：
  - `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`、`SMTP_*`
- 风险与兼容性：
  - 是否有潜在破坏性变更或迁移步骤
- 变更日志：
  - 以 bullets 书写本次变更（将来也可聚合到 CHANGELOG）

## 变更日志（Changelog）
- 统一使用 bullets，建议遵循 Keep a Changelog 的风格
- PR 中至少附上本次变更点；发布时聚合为版本说明

## 发布
- 正常发布：将 `develop` 合并到 `main`，创建 release tag 与说明
- 紧急修复：`hotfix/*` 合并回 `main` 并同步 `develop`

## 验证命令（本地/远程）
- 克隆与结构校验：
  - `git clone https://github.com/Nick-60/WebA3 && cd WebA3 && ls`（Windows: `dir`）
- 前端静态预览：
  - `npx http-server ./frontend -p 5173` 或 `python -m http.server 5173 -d frontend`
- 后端（初始化后）：
  - Gradle：`cd backend && ./gradlew test`、`./gradlew bootRun`
  - Maven：`cd backend && mvn -B test`、`mvn -B spring-boot:run`

## CI（GitHub Actions）
- 当检测到 `backend/gradlew` 或 `backend/pom.xml` 时，CI 会自动构建并运行测试
- 当前为目录与文档占位，若未检测到上述文件，CI 会跳过构建并提示

---

## 示例提交记录
```bash
# 初始化结构
git add .
git commit -m "chore: 初始化基础目录与占位文件"

# 文档
git commit -m "docs: 添加 README 与 DEVELOPMENT 指南"

# CI
git commit -m "ci: 新增 Java 构建与测试工作流"
```

