# 前端初版（AdminLTE 静态 + 基本交互）

此目录包含最小前端可运行版本（MVP），基于 AdminLTE + Axios。后端基地址：`http://localhost:8080`。

## 页面列表

- `login.html`：登录并将 `accessToken` 存入 `localStorage`
- `dashboard.html`：仪表盘，显示基本菜单和当前用户角色
- `apply_leave.html`：提交请假，调用 `POST /api/leave/request`
- `pending_approvals.html`：经理审批，显示 `GET /api/leave/pending`，支持 `PATCH /{id}/approve` / `PATCH /{id}/reject`
- `hr_report.html`：HR 报表导出，`GET /api/leave/hr/export`，以 Blob 下载 XLSX

## 运行方式

推荐方式：将此目录下的 HTML/JS/CSS 同步至 `backend/src/main/resources/static` 后，启动后端 `Spring Boot` 即可通过 `http://localhost:8080/login.html` 访问。

替代方式（纯静态预览）：如需只预览页面但暂不连后端，可使用任意静态服务器（如 `python -m http.server` 或 `npx serve`）在此目录运行；但交互请求仍会指向 `http://localhost:8080`。

## 验收与测试步骤

1. 打开 `login.html` -> 登录：员工账号（例如 `emp001`/`pass123`）
2. 进入 `apply_leave.html` -> 提交请假
3. 登录经理账号（例如 `mgr`/`mgr123`） -> 打开 `pending_approvals.html` -> 审批通过/拒绝
4. 登录 HR 账号（例如 `hr`/`hr123`） -> 打开 `hr_report.html` -> 导出报表

> 注：若使用数据库初始化脚本，默认种子用户可能为 `emp`/`emp123`、`mgr`/`mgr123`、`hr`/`hr123`。`DataInitializer` 也会创建 `emp001`/`pass123`。请以实际环境为准。

## JS 代码风格

- JS 分号可选（semicolon optional），保持简洁。

## 目录说明

将以下文件拷贝到后端 `static` 目录：

- `login.html`、`dashboard.html`、`apply_leave.html`、`pending_approvals.html`、`hr_report.html`
- `js/config.js`、`js/auth.js`、`js/ui.js`
- `css/style.css`

