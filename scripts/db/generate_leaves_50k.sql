-- 生成 50k 条请假记录用于导出压力测试（MySQL 8）
-- 使用方式：mysql -uroot -p<password> < scripts/db/generate_leaves_50k.sql

USE leave_mgmt;

SET @@session.sql_mode = REPLACE(@@sql_mode, 'ONLY_FULL_GROUP_BY', '');

SET @emp_id := (SELECT id FROM users WHERE username='emp');
SET @mgr_id := (SELECT id FROM users WHERE username='mgr');

WITH RECURSIVE seq(n) AS (
  SELECT 1 UNION ALL SELECT n+1 FROM seq WHERE n < 50000
)
INSERT INTO leave_requests (employee_id, leave_type, start_date, end_date, days, status, approver_id, approval_comment)
SELECT
  @emp_id AS employee_id,
  ELT(((n % 4) + 1), 'ANNUAL','SICK','UNPAID','OTHER') AS leave_type,
  DATE('2024-01-01') + INTERVAL (n % 300) DAY AS start_date,
  DATE('2024-01-01') + INTERVAL (n % 300) DAY + INTERVAL ((n % 5)+1) DAY AS end_date,
  ((n % 5)+1) AS days,
  ELT(((n % 4) + 1), 'PENDING','APPROVED','REJECTED','CANCELLED') AS status,
  @mgr_id AS approver_id,
  CONCAT('auto generated #', n) AS approval_comment
FROM seq;

-- 提示：如需为 HR 部门生成更多员工数据，可复制 users 行并调整 department_id

