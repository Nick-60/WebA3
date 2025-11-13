## 目标
- 视觉规整：消除左侧空白、内容居中、卡片与表格更清晰
- 交互优化：审批更顺滑（按钮反馈、分页状态、空状态明晰）
- 保持现有 AdminLTE + Bootstrap 4 + 原生 JS 架构，改动低风险

## 现状问题（依据 d:\WebAssign3\frontend\pending_approvals.html）
- 布局：`body` 使用 `sidebar-mini` 导致左侧预留侧栏空间（第11行），导航未用容器包裹（第13–15行）
- 内容：`container-fluid` 让内容贴左，未居中（第23行）
- 表格：无条纹/对齐优化，空状态为“无数据”文本不明显（第26–39行）
- 分页：按钮始终可点，未在边界禁用（第40–44行；脚本第119–124行）
- 提示：`alert` 弹窗打断操作，缺少统一非阻塞通知（脚本第92–96、107–115行）

## 改动方案
### 1) 顶部导航与居中容器
- 切换 `layout-top-nav`：`<body class="hold-transition layout-top-nav">`（替换第11行的 `sidebar-mini`）
- 导航容器：在 `<nav>` 内加入 `<div class="container d-flex justify-content-between align-items-center">`，左“返回仪表盘”，右“退出登录”（第13–15行）
- 主体居中：`container-fluid` → `container`，使内容在大屏居中（第23行）

### 2) 卡片与表格视觉增强
- 卡片：外层 `card` 添加 `u-card u-card-elevated`，强化层次（第24行）
- 表格：增加 `table-striped table-hover`，并用 `.align-middle` 居中行高；“类型”列显示 `badge`（如 `badge-info`/`badge-warning`）
- 空状态：保留当前“暂无待审批”占位（我们已加），补充轻量图标（文本即可），增加顶部说明小段 `u-text-muted`

### 3) 分页与状态反馈
- 分页禁用：到第一页禁用“上一页”，到最后一页禁用“下一页”；按钮样式改为 `btn-outline-secondary`
- 非阻塞通知：使用已扩展的 `ui.js` `showSuccess/showError` 替代 `alert`
- 按钮加载：审批按钮调用 `withButtonLoading`，避免重复点击
- 操作后反馈：行短暂高亮或刷新数据后提示（先做提示+刷新，行高亮可选）

### 4) 审批备注与确认（轻量）
- 将 `prompt` 改为 Bootstrap Modal：在页面底部添加一个小型确认/备注输入模态框（标题“审批备注（可选）”），统一交互体验
- 脚本：点击“同意/拒绝”时打开模态框，确认后调用 `approveLeave/rejectLeave`

## 代码变更摘要
- `frontend/pending_approvals.html`
  - 布局：`body` 切换为 `layout-top-nav`；`<nav>` 内加 `container` 并左右分布；`section.content` 使用 `container`
  - 视觉：卡片 `u-card u-card-elevated`；表格 `table-striped table-hover align-middle`；类型列渲染 `badge`
  - 分页：按钮改为 `btn btn-outline-secondary`，并在脚本中根据 `page`/`totalPages` 设置 `disabled`
  - 提示与加载：在脚本中用 `showSuccess/showError`，审批按钮包裹 `withButtonLoading`
  - 模态框：在 HTML 尾部新增一个 Bootstrap 模态，用于输入备注并确认

## 验证步骤
- 运行前端开发服务并访问 `pending_approvals.html`
- 查看导航左右分布与居中内容；无数据时空状态明显
- 翻页到边界时按钮自动禁用；审批时按钮出现加载指示，操作完成有轻量通知

## 风险与回滚
- `layout-top-nav` 若影响其他页面，仅在本页使用；结构保持原样可快速回退
- 模态框为附加结构，若不希望保留，可还原为 `prompt`，其他优化不受影响

确认后我将直接实施上述优化并提供预览链接。