# 邮件通知配置与故障排查（MVP）

本步骤集成 Spring Boot Mail，在关键事件（提交申请、审批通过/驳回）发送文本邮件通知：
- 员工提交申请 -> 通知其部门经理
- 经理审批通过/驳回 -> 通知该员工

## 环境变量（SMTP）
- `SMTP_HOST`：SMTP 服务器地址（示例：`smtp.mailtrap.io` 或 `localhost`）
- `SMTP_PORT`：SMTP 端口（Mailtrap 通常 `2525`；本地 MailHog `1025`）
- `SMTP_USER`（或 `SMTP_USERNAME`）：SMTP 用户名（作为 `from`）
- `SMTP_PASSWORD`（或 `SMTP_PASS`）：SMTP 密码
- 可选：
  - `SMTP_AUTH`（默认 `true`）
  - `SMTP_STARTTLS`（默认 `false`，Mailtrap/生产通常 `true`）
  - `SMTP_DEBUG`（默认 `true`）

应用映射位于 `backend/src/main/resources/application.yml` 的 `spring.mail.*`。

## 两种推荐方案

1) Mailtrap（云测试环境）
- 注册后获取 SMTP 主机、端口、用户名、密码
- 参考：
  ```bash
  $env:SMTP_HOST="smtp.mailtrap.io"
  $env:SMTP_PORT="2525"
  $env:SMTP_USER="<mailtrap-username>"
  $env:SMTP_PASSWORD="<mailtrap-password>"
  $env:SMTP_STARTTLS="true"
  ```
- 登录 Mailtrap 收件箱即可看到发送记录

2) MailHog（本地）
- 启动：`docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`
- 配置：
  ```bash
  $env:SMTP_HOST="localhost"
  $env:SMTP_PORT="1025"
  $env:SMTP_DEBUG="true"
  ```
- 打开 `http://localhost:8025` 查看收件箱

## 触发点
- 提交申请：`LeaveService#createLeave(...)` 保存后查找部门经理并发送通知
- 审批通过：`LeaveService#approveLeave(...)` 保存后发送通知给员工
- 审批驳回：`LeaveService#rejectLeave(...)` 保存后发送通知给员工

## 邮件示例
```
Subject: 请假申请提交通知
To: mgr@example.com
Body:
员工 emp(emp@example.com) 提交了请假申请：ANNUAL 2025-11-13 ~ 2025-11-15，共 3 天。
申请ID：123。
```

```
Subject: 请假审批通过通知
To: emp@example.com
Body:
您的请假申请已通过：ANNUAL 2025-11-13 ~ 2025-11-15，共 3.00 天。
审批人：mgr，备注：同意。
申请ID：123，状态：APPROVED
```

```
Subject: 请假审批驳回通知
To: emp@example.com
Body:
您的请假申请被驳回：SICK 2025-11-13 ~ 2025-11-13，共 1.00 天。
审批人：mgr，备注：不符合请假规则。
申请ID：124，状态：REJECTED
```

## 故障排查
- 看日志（默认 DEBUG）：`com.example.leave.service.mail`
  - 成功：`[mail] sent -> to=... subject=...`
  - 失败：`[mail] send failed -> to=... subject=... error=...`
- 常见问题：
  - 连接被拒绝：检查 `SMTP_HOST` 与 `SMTP_PORT`（MailHog 是否已启动？）
  - 认证失败：检查 `SMTP_USER` / `SMTP_PASSWORD` 是否正确；`SMTP_AUTH` 是否为 `true`
  - TLS/SSL 问题：尝试设置 `SMTP_STARTTLS=true`
  - from 地址为空：确保 `SMTP_USER` 已设置（用于 `from`）
- 重要说明：邮件失败不会影响主业务流程（服务层捕获异常并仅记录日志）。

## 本地验证（命令）
1) 启动 SMTP（任选其一）：
   - MailHog：`docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`
   - Mailtrap：设置云端 SMTP 环境变量
2) 启动后端：
   ```powershell
   mvn -f backend/pom.xml -DskipTests package
   mvn -f backend/pom.xml spring-boot:run
   ```
3) 登录并提交请假：
   ```powershell
   # 登录
   $LOGIN = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -Body (@{username='emp';password='emp123'} | ConvertTo-Json) -ContentType 'application/json'
   $TOKEN = $LOGIN.data.token

   # 提交请假
   Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/leave/request -Headers @{Authorization="Bearer $TOKEN"} -Body (@{leaveType='ANNUAL';startDate='2025-11-13';endDate='2025-11-15';comment='年假'} | ConvertTo-Json) -ContentType 'application/json'
   ```
4) 检查收件箱：
   - MailHog：打开 `http://localhost:8025` 查看最新邮件
   - Mailtrap：进入对应收件箱

## 占位符与密钥
- 需要设置：`DB_URL`、`DB_USER`、`DB_PASSWORD`、`JWT_SECRET`
- 新增本步骤需要：`SMTP_HOST`、`SMTP_PORT`、`SMTP_USER`、`SMTP_PASSWORD`

