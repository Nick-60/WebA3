## 目标
- 提升视觉一致性与规整度，减少内联与页面内脚本造成的分散样式
- 强化交互反馈与可访问性，优化移动端体验
- 在当前 AdminLTE + Bootstrap + jQuery + 原生 JS 架构下，低风险渐进式优化

## 现状概览
- 架构：MPA，多页面位于 `frontend/*.html`，Vite 开发/构建（`vite.config.js`）
- 样式：主要在 `frontend/css/style.css`，存在少量内联样式（如 `frontend/dashboard.html:42–43`、`frontend/hr_report.html:39`）
- 交互：页面内调用共享 `frontend/js/{ui,config,auth}.js`；提示与加载状态较弱
- 资产：依赖 CDN 的 AdminLTE/Bootstrap/jQuery/Axios；无本地图片/字体
- 可访问性：基础语义与视口齐备，ARIA 和键盘可达性薄弱

## 设计规范（新增到 `frontend/css/style.css`）
- 颜色与阴影：在 `:root` 定义 CSS 变量（主色、文本、边框、阴影）
- 字体与排版：统一字号层级（`h1–h6`、正文、次要），设置行高与字距
- 间距体系：定义 8px 基础刻度（`--space-1..6`），用于外边距/内边距
- 圆角与边框：统一半径与边框色，匹配 AdminLTE 风格
- 工具类：少量通用类（如 `mt-* / mb-* / px-* / text-muted / card-elevated`），减少内联样式

## 结构优化（各页面 `frontend/*.html`）
- 语义容器：补充 `<main>`、`<header>`、`<footer>` 结构；主要内容放入 `<main>`
- 移除内联样式：将 `dashboard.html:42–43`、`hr_report.html:39` 等内联样式替换为工具类
- 响应式容器：表格外层包裹 `div.table-responsive`（移动端横向滚动）
- 空状态模板：列表页（`pending_approvals.html`）在无数据时显示提示占位

## 样式重构（集中到 `frontend/css/style.css`）
- 全局卡片与容器：规范卡片间距、阴影与圆角，减少视觉杂乱
- 表单样式：统一输入框、标签与错误提示风格；聚焦态清晰可见
- 按钮体系：主次按钮颜色与禁用态一致；悬停/按下态更明显

## 交互与反馈（`frontend/js/ui.js` 与页面脚本）
- Toast/通知：扩展 `ui.js` 的提示方法，统一替代 `alert` 与分散提示
- 加载与禁用：提交时显示加载指示，禁用按钮避免重复点击；完成后恢复
- 验证与错误：在表单下方显示就地错误信息（避免仅弹框），提高可理解性
- 列表交互：审批页加入操作后的轻量反馈与行状态更新

## 可访问性（A11y）
- ARIA 属性：为关键交互元素补充 `aria-label/aria-live`；表格使用 `scope` 关联
- 键盘可达性：确保所有操作可用键盘触达；设置可见 `:focus` 样式
- 跳转链接：在页面顶部添加“跳到主内容”隐藏链接

## 性能与资源
- 按需脚本：各页面仅保留必要的脚本引用，避免无用加载
- 脚本顺序：第三方脚本使用 `defer`，保证首屏结构优先渲染
- 体量评估：若页面简单，减少 AdminLTE 组件使用；保留 Bootstrap 栅格与必要工具类

## 具体改动清单
- `frontend/css/style.css`
  - 增加 `:root` CSS 变量与间距/阴影/圆角体系
  - 新增通用工具类（间距、文本、卡片提升等）
  - 表单与按钮统一风格完善
- `frontend/dashboard.html`
  - 移除内联样式（`dashboard.html:42–43`），改用工具类
  - `<main>` 包裹主要内容；卡片使用统一类
- `frontend/pending_approvals.html`
  - `div.table-responsive` 包裹表格
  - 空状态与操作后行状态反馈
- `frontend/apply_leave.html`
  - 表单错误就地提示；提交时禁用按钮与加载指示
- `frontend/hr_report.html`
  - 移除内联样式（`hr_report.html:39`）；表单分组间距统一
- `frontend/js/ui.js`
  - 扩展统一通知方法（成功/错误/信息）
  - 提供加载蒙层或按钮局部加载工具

## 验证与交付
- 开发验证：运行 `npm run dev`，逐页检查布局、交互与可访问性（键盘导航、屏幕阅读器）
- 构建产物：`npm run build:frontend` 输出到 `dist`，进行一次预览与体量检查
- 验收标准：
  - 无内联样式残留；页面元素间距与排版一致
  - 表单与列表交互有明确、不中断的反馈；移动端可用性提升
  - 可访问性基本通过（焦点可见、ARIA 合理、语义结构完整）

## 里程碑
- 第1阶段：样式体系与工具类（`style.css`）
- 第2阶段：页面结构与内联样式清理（各 `*.html`）
- 第3阶段：交互反馈与加载/禁用（`ui.js` + 页面脚本）
- 第4阶段：A11y 与移动端细节
- 第5阶段：按需脚本与性能微调

请确认是否按以上计划执行；确认后我将逐项落地，边改边验证并展示预览。