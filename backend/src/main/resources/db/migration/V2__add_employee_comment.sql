-- Add employee_comment column to leave_requests
ALTER TABLE leave_requests
  ADD COLUMN IF NOT EXISTS employee_comment VARCHAR(500) NULL;
