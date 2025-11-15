-- H2-compatible schema for tests (no MySQL-specific ENUM/JSON/ON UPDATE)

CREATE TABLE IF NOT EXISTS departments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  manager_id BIGINT
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_departments_name ON departments(name);
CREATE INDEX IF NOT EXISTS idx_departments_manager_id ON departments(manager_id);

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  department_id BIGINT,
  email VARCHAR(120) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_department_id ON users(department_id);
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS fk_users_department FOREIGN KEY (department_id)
  REFERENCES departments(id);

CREATE TABLE IF NOT EXISTS leave_requests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  leave_type VARCHAR(20) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  days DECIMAL(5,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  approver_id BIGINT,
  approval_comment VARCHAR(500),
  employee_comment VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_leave_employee ON leave_requests(employee_id);
CREATE INDEX IF NOT EXISTS idx_leave_approver ON leave_requests(approver_id);
CREATE INDEX IF NOT EXISTS idx_leave_status   ON leave_requests(status);
ALTER TABLE leave_requests ADD CONSTRAINT IF NOT EXISTS fk_leave_employee FOREIGN KEY (employee_id)
  REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE leave_requests ADD CONSTRAINT IF NOT EXISTS fk_leave_approver FOREIGN KEY (approver_id)
  REFERENCES users(id);

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  entity VARCHAR(50) NOT NULL,
  entity_id BIGINT NOT NULL,
  action VARCHAR(20) NOT NULL,
  user_id BIGINT,
  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  details VARCHAR(2000)
);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_user   ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp);
ALTER TABLE audit_logs ADD CONSTRAINT IF NOT EXISTS fk_audit_user FOREIGN KEY (user_id)
  REFERENCES users(id);

-- Late add FK to avoid circular dependency creation errors
ALTER TABLE departments ADD CONSTRAINT IF NOT EXISTS fk_departments_manager FOREIGN KEY (manager_id)
  REFERENCES users(id);

-- Seed departments; users will be seeded by Test profile initializer
INSERT INTO departments(name) VALUES ('Engineering');
INSERT INTO departments(name) VALUES ('HR');
