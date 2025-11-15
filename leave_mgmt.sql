/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80039
 Source Host           : localhost:3306
 Source Schema         : leave_mgmt

 Target Server Type    : MySQL
 Target Server Version : 80039
 File Encoding         : 65001

 Date: 13/11/2025 23:51:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for audit_logs
-- ----------------------------
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `entity` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `entity_id` bigint UNSIGNED NOT NULL,
  `action` enum('CREATE','UPDATE','DELETE','APPROVE','REJECT','CANCEL','AUTH') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint UNSIGNED NULL DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `details` json NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_audit_entity`(`entity` ASC, `entity_id` ASC) USING BTREE,
  INDEX `idx_audit_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_audit_timestamp`(`timestamp` ASC) USING BTREE,
  CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_logs
-- ----------------------------
INSERT INTO `audit_logs` VALUES (1, 'users', 1, 'CREATE', 3, '2025-11-11 23:58:38', '{\"note\": \"seed data\"}');
INSERT INTO `audit_logs` VALUES (2, 'leave_requests', 2, 'CREATE', 4, '2025-11-12 08:56:06', '{\"days\": 3, \"range\": \"2025-12-01~2025-12-03\", \"employeeId\": 4}');
INSERT INTO `audit_logs` VALUES (3, 'leave_requests', 2, 'CANCEL', 4, '2025-11-12 08:56:06', '{\"status\": \"CANCELLED\", \"leaveId\": 2, \"employeeId\": 4}');
INSERT INTO `audit_logs` VALUES (4, 'leave_requests', 3, 'CREATE', 1, '2025-11-13 13:31:38', '{\"days\": 7, \"range\": \"2025-11-14~2025-11-20\", \"employeeId\": 1}');
INSERT INTO `audit_logs` VALUES (5, 'leave_requests', 3, 'APPROVE', 2, '2025-11-13 13:34:06', '{\"status\": \"APPROVED\", \"comment\": \"test1 mgr\", \"leaveId\": 3, \"employeeId\": 1}');
INSERT INTO `audit_logs` VALUES (6, 'notifications', 3, 'UPDATE', 2, '2025-11-13 13:34:06', '{\"message\": \"Your leave has been approved\", \"toUserId\": 1}');
INSERT INTO `audit_logs` VALUES (7, 'leave_requests', 4, 'CREATE', 1, '2025-11-13 15:56:42', '{\"days\": 2, \"range\": \"2025-11-29~2025-11-30\", \"employeeId\": 1}');
INSERT INTO `audit_logs` VALUES (8, 'leave_requests', 4, 'APPROVE', 2, '2025-11-13 16:37:32', '{\"status\": \"APPROVED\", \"comment\": \"test\", \"leaveId\": 4, \"employeeId\": 1}');
INSERT INTO `audit_logs` VALUES (9, 'notifications', 4, 'UPDATE', 2, '2025-11-13 16:37:32', '{\"message\": \"Your leave has been approved\", \"toUserId\": 1}');
INSERT INTO `audit_logs` VALUES (10, 'leave_requests', 5, 'CREATE', 1, '2025-11-13 21:56:24', '{\"days\": 2, \"range\": \"2025-11-25~2025-11-26\", \"employeeId\": 1}');
INSERT INTO `audit_logs` VALUES (11, 'leave_requests', 5, 'APPROVE', 2, '2025-11-13 21:56:50', '{\"status\": \"APPROVED\", \"comment\": \"test\", \"leaveId\": 5, \"employeeId\": 1}');
INSERT INTO `audit_logs` VALUES (12, 'notifications', 5, 'UPDATE', 2, '2025-11-13 21:56:50', '{\"message\": \"Your leave has been approved\", \"toUserId\": 1}');

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `manager_id` bigint UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_departments_name`(`name` ASC) USING BTREE,
  INDEX `idx_departments_manager_id`(`manager_id` ASC) USING BTREE,
  CONSTRAINT `fk_departments_manager` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of departments
-- ----------------------------
INSERT INTO `departments` VALUES (1, 'Engineering', 2);
INSERT INTO `departments` VALUES (2, 'HR', 3);

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
INSERT INTO `flyway_schema_history` VALUES (1, '1', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'root', '2025-11-13 16:29:26', 0, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '2', 'add employee comment', 'SQL', 'V2__add_employee_comment.sql', -756450883, 'root', '2025-11-13 16:29:27', 70, 0);

-- ----------------------------
-- Table structure for leave_requests
-- ----------------------------
DROP TABLE IF EXISTS `leave_requests`;
CREATE TABLE `leave_requests`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `employee_id` bigint UNSIGNED NOT NULL,
  `leave_type` enum('ANNUAL','SICK','UNPAID','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `days` decimal(5, 2) NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING',
  `approver_id` bigint UNSIGNED NULL DEFAULT NULL,
  `approval_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `employee_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_leave_employee`(`employee_id` ASC) USING BTREE,
  INDEX `idx_leave_approver`(`approver_id` ASC) USING BTREE,
  INDEX `idx_leave_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_leave_approver` FOREIGN KEY (`approver_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_leave_employee` FOREIGN KEY (`employee_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of leave_requests
-- ----------------------------
INSERT INTO `leave_requests` VALUES (2, 4, 'ANNUAL', '2025-12-01', '2025-12-03', 3.00, 'CANCELLED', NULL, '????', '2025-11-12 08:56:06', '2025-11-12 08:56:06', NULL);
INSERT INTO `leave_requests` VALUES (3, 1, 'ANNUAL', '2025-11-14', '2025-11-20', 7.00, 'APPROVED', 2, 'test1 mgr', '2025-11-13 13:31:38', '2025-11-13 13:31:38', NULL);
INSERT INTO `leave_requests` VALUES (4, 1, 'OTHER', '2025-11-29', '2025-11-30', 2.00, 'APPROVED', 2, 'test', '2025-11-13 15:56:42', '2025-11-13 15:56:42', NULL);
INSERT INTO `leave_requests` VALUES (5, 1, 'OTHER', '2025-11-25', '2025-11-26', 2.00, 'APPROVED', 2, 'test', '2025-11-13 21:56:24', '2025-11-13 21:56:24', 'test');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` enum('EMPLOYEE','MANAGER','HR','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `department_id` bigint UNSIGNED NULL DEFAULT NULL,
  `email` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_users_email`(`email` ASC) USING BTREE,
  INDEX `idx_users_department_id`(`department_id` ASC) USING BTREE,
  CONSTRAINT `fk_users_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'emp', '$2a$10$Qks6FNknjBQ/rJiiJYDtd.DiRe6PJ07M2UiSaKbdx54RZ4XgYBe6C', 'EMPLOYEE', 1, 'emp@example.com', '2025-11-11 23:58:37');
INSERT INTO `users` VALUES (2, 'mgr', '$2a$10$DtoqehuBkqY5Yh2pHkt0zuOmPuEhxpXkiCR9PN1N8e57qYoK3EuQ.', 'MANAGER', 1, 'mgr@example.com', '2025-11-11 23:58:37');
INSERT INTO `users` VALUES (3, 'hr', '$2a$10$OdZ61C4zxx0CY2DLqQPu9eMB4eMEDF1WQr509jXIQfRooSB4ZOYCK', 'HR', 2, 'hr@example.com', '2025-11-11 23:58:37');
INSERT INTO `users` VALUES (4, 'emp001', '$2a$10$RJ656NyloJGf7YCiY2C/KumgHZ9wJIcn.kHwRy5/p4RETC6p8v0BW', 'EMPLOYEE', NULL, 'emp001@example.com', '2025-11-11 17:28:45');

SET FOREIGN_KEY_CHECKS = 1;
