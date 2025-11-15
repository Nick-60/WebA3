## 目标
- 消除左侧巨大空白与不对齐问题，使导航栏左右分布清晰
- 使主要内容在视觉上居中或规整（容器居中、卡片对称、间距统一）
- 保持现有 AdminLTE + Bootstrap 4 架构，低风险渐进优化

## 根因分析
- 当前 `body` 使用 `sidebar-mini` 导致 AdminLTE 预留侧边栏空间，`content-wrapper` 产生左侧空白
- 导航栏未使用 top-nav 布局与容器，导致对齐与分布不佳
- 页面内容未置于标准 `.container`，导致在宽屏下左侧偏移明显

## 改动方案
### 1) 启用 Top-Nav 布局
- 在 `frontend/dashboard.html` 与 `frontend/apply_leave.html`：
  - 将 `<body class="hold-transition sidebar-mini">` 改为 `<body class="hold-transition layout-top-nav">`
  - 保留 `.wrapper` 与 `.content-wrapper` 结构（AdminLTE 3 兼容）

### 2) 导航栏左右分布与容器宽度
- 两个页面的 `<nav class="main-header navbar ...">` 内部：
  - 包一层 `<div class="container d-flex justify-content-between align-items-center">`
  - 左侧：品牌或返回链接；右侧：退出登录
  - 参考位置：
    - `frontend/dashboard.html:13–20`
    - `frontend/apply_leave.html:13–15`

### 3) 内容容器居中与卡片对称
- 在两个页面的 `<section class="content">` 中：
  - 将现有 `container-fluid` 替换为 `container`（使内容居中并限制最大宽度）
  - Dashboard：保留两列布局，使用 `row` + `col-md-6`，卡片添加 `u-card u-card-elevated`，标题与间距统一（`u-mt-4/u-mb-4`）
    - 参考位置：`frontend/dashboard.html:33–61`
  - 提交请假页：使用 `row justify-content-center` + `col-lg-8 col-xl-6`，卡片 `u-card u-card-elevated`，表单下按钮对齐与间距统一
    - 参考位置：`frontend/apply_leave.html:22–51`

### 4) 细节样式与一致性
- 使用已添加的工具类：`u-mt-4/u-mt-6/u-text-muted/u-card-elevated` 保持统一感
- 将任何残留的内联样式替换为工具类（目前两个页面已基本无内联样式）

## 代码变更摘要（示例片段）
- `frontend/dashboard.html`
  - `<body class="hold-transition layout-top-nav">`
  - `<nav> -> <div class="container d-flex justify-content-between"> [左品牌] [右退出] </div>`
  - `<section class="content"> -> <div class="container"> ... 两列卡片 ... </div>`
- `frontend/apply_leave.html`
  - `<body class="hold-transition layout-top-nav">`
  - `<nav> -> <div class="container d-flex justify-content-between"> [返回仪表盘] [退出登录] </div>`
  - `<section class="content"> -> <div class="container"><div class="row justify-content-center"><div class="col-lg-8 col-xl-6">[卡片+表单]</div></div></div>`

## 验证步骤
- 运行 `npm run dev` 并访问：
  - `http://localhost:5173/dashboard.html`
  - `http://localhost:5173/apply_leave.html`
- 检查：
  - 导航栏左右分布，页面不再左侧留白
  - Dashboard 卡片对称、间距统一
  - 提交请假页内容居中（在大屏居中，小屏自适应）

## 风险与回滚
- 若启用 `layout-top-nav` 影响其他页面，可仅在这两个页面切换；保留原结构便于回滚
- 如需进一步微调容器宽度，可在 `style.css` 增加自定义容器（当前优先用 Bootstrap `.container`）

请确认后我将直接按以上方案改动并展示预览效果。