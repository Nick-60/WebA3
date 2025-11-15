# Online Leave Management System — 数据库设计

> DB_NAME: `leave_mgmt` · MySQL 8 · InnoDB · utf8mb4

## ER 图（ASCII 简要）
```
----------------+        1     n        +-------------------+
|  departments   |---------------------->|       users       |
|----------------| manager_id (FK)       |-------------------|
| id (PK)        |<--------------------+ | id (PK)           |
| name (UNIQUE)  |                     | | username (UNIQUE) |
| manager_id (FK)|                     | | email (UNIQUE)    |
----------------+                     | | department_id (FK)|
                                        | | role              |
                                        | | password_hash     |
                                        | | created_at        |
                                        +---------------------+
                                                   |
                                                   | 1     n
                                                   v
                                        +---------------------+
                                        |   leave_requests    |
                                        |---------------------|
                                        | id (PK)            |
                                        | employee_id (FK)   |
                                        | approver_id (FK)   |
                                        | leave_type         |
                                        | start_date         |
                                        | end_date           |
                                        | days               |
                                        | status             |
                                        | approval_comment   |
                                        | created_at         |
                                        | updated_at         |
                                        +---------------------+
                                                   |
                                                   | n     1
                                                   v
                                        +---------------------+
                                        |     audit_logs      |
                                        |---------------------|
                                        | id (PK)            |
                                        | entity             |
                                        | entity_id          |
                                        | action             |
                                        | user_id (FK)       |
                                        | timestamp          |
                                        | details (JSON)     |
                                        +---------------------+
```

## 表定义与约束

### 1) `departments`
- 字段
  - `id` BIGINT UNSIGNED PK AUTO_INCREMENT
  - `name` VARCHAR(100) NOT NULL UNIQUE
  - `manager_id` BIGINT UNSIGNED NULL（FK -> `users.id`，`ON DELETE SET NULL`，`ON UPDATE CASCADE`）
- 索引
  - `UNIQUE(name)`
  - `INDEX(manager_id)`

### 2) `users`
- 字段
  - `id` BIGINT UNSIGNED PK AUTO_INCREMENT
  - `username` VARCHAR(50) NOT NULL UNIQUE
  - `password_hash` VARCHAR(255) NOT NULL
  - `role` ENUM('EMPLOYEE','MANAGER','HR','ADMIN') NOT NULL
  - `department_id` BIGINT UNSIGNED NULL（FK -> `departments.id`，`ON DELETE SET NULL`，`ON UPDATE CASCADE`）
  - `email` VARCHAR(120) NOT NULL UNIQUE
  - `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- 索引
  - `UNIQUE(username)`、`UNIQUE(email)`
  - `INDEX(department_id)`

### 3) `leave_requests`
- 字段
  - `id` BIGINT UNSIGNED PK AUTO_INCREMENT
  - `employee_id` BIGINT UNSIGNED NOT NULL（FK -> `users.id`，`ON DELETE CASCADE`，`ON UPDATE CASCADE`）
  - `leave_type` ENUM('ANNUAL','SICK','UNPAID','OTHER') NOT NULL
  - `start_date` DATE NOT NULL
  - `end_date` DATE NOT NULL
  - `days` DECIMAL(5,2) NOT NULL（支持半天/小时折算）
  - `status` ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING'
  - `approver_id` BIGINT UNSIGNED NULL（FK -> `users.id`，`ON DELETE SET NULL`，`ON UPDATE CASCADE`）
  - `approval_comment` VARCHAR(500) NULL
  - `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  - `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
- 索引
  - `INDEX(employee_id)`、`INDEX(approver_id)`、`INDEX(status)`

### 4) `audit_logs`
- 字段
  - `id` BIGINT UNSIGNED PK AUTO_INCREMENT
  - `entity` VARCHAR(50) NOT NULL（如 'users'、'leave_requests'）
  - `entity_id` BIGINT UNSIGNED NOT NULL
  - `action` ENUM('CREATE','UPDATE','DELETE','APPROVE','REJECT','CANCEL','AUTH') NOT NULL
  - `user_id` BIGINT UNSIGNED NULL（FK -> `users.id`，`ON DELETE SET NULL`，`ON UPDATE CASCADE`）
  - `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  - `details` JSON NULL（记录差异或上下文）
- 索引
  - 复合索引：`INDEX(entity, entity_id)`
  - `INDEX(user_id)`、`INDEX(timestamp)`

## 设计说明
- 使用 `utf8mb4` 保证完整 Unicode 支持；引擎统一为 `InnoDB`。
- 循环外键（`departments.manager_id` -> `users.id`，`users.department_id` -> `departments.id`）通过表创建顺序与后置 `ALTER TABLE` 添加外键解决；插入顺序为：先建部门、再建用户、最后更新部门的 `manager_id`。
- 删除员工时，其请假申请记录（`leave_requests`）级联删除；审批人字段置空以保留历史记录。
- 审计日志使用 JSON 保存详情，便于后续检索与可观察性。

## 验证命令
- 初始化：`mysql -uroot -pnick030201 < scripts/db/init.sql`
- 检查：`mysql -uroot -pnick030201 -e "USE leave_mgmt; SHOW TABLES; SELECT COUNT(*) FROM users;"`

