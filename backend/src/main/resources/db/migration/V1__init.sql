-- 复制迁移示例到 resources（Flyway 默认路径）
-- 与仓库根 db/migrations/V1__init.sql 内容一致

CREATE TABLE IF NOT EXISTS departments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  manager_id BIGINT UNSIGNED NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_departments_name (name),
  KEY idx_departments_manager_id (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('EMPLOYEE','MANAGER','HR','ADMIN') NOT NULL,
  department_id BIGINT UNSIGNED NULL,
  email VARCHAR(120) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_department_id (department_id),
  CONSTRAINT fk_users_department FOREIGN KEY (department_id)
    REFERENCES departments(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS leave_requests (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  employee_id BIGINT UNSIGNED NOT NULL,
  leave_type ENUM('ANNUAL','SICK','UNPAID','OTHER') NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  days DECIMAL(5,2) NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  approver_id BIGINT UNSIGNED NULL,
  approval_comment VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_leave_employee (employee_id),
  KEY idx_leave_approver (approver_id),
  KEY idx_leave_status (status),
  CONSTRAINT fk_leave_employee FOREIGN KEY (employee_id)
    REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id)
    REFERENCES users(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  entity VARCHAR(50) NOT NULL,
  entity_id BIGINT UNSIGNED NOT NULL,
  action ENUM('CREATE','UPDATE','DELETE','APPROVE','REJECT','CANCEL','AUTH') NOT NULL,
  user_id BIGINT UNSIGNED NULL,
  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  details JSON NULL,
  PRIMARY KEY (id),
  KEY idx_audit_entity (entity, entity_id),
  KEY idx_audit_user (user_id),
  KEY idx_audit_timestamp (timestamp),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE departments
  ADD CONSTRAINT fk_departments_manager FOREIGN KEY (manager_id)
  REFERENCES users(id)
  ON DELETE SET NULL ON UPDATE CASCADE;

INSERT INTO departments(name) VALUES ('Engineering'),('HR')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO users (username, password_hash, role, department_id, email)
VALUES
  ('emp', SHA2('emp123',256), 'EMPLOYEE', (SELECT id FROM departments WHERE name='Engineering'), 'emp@example.com'),
  ('mgr', SHA2('mgr123',256), 'MANAGER',  (SELECT id FROM departments WHERE name='Engineering'), 'mgr@example.com'),
  ('hr',  SHA2('hr123',256),  'HR',       (SELECT id FROM departments WHERE name='HR'),         'hr@example.com')
ON DUPLICATE KEY UPDATE email = VALUES(email);

UPDATE departments SET manager_id = (SELECT id FROM users WHERE username='mgr') WHERE name='Engineering';
UPDATE departments SET manager_id = (SELECT id FROM users WHERE username='hr')  WHERE name='HR';

