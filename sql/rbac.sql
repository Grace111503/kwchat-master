-- RBAC权限控制表结构

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0：禁用，1：启用）',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统内置（0：否，1：是）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL COMMENT '权限ID',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型（menu：菜单，button：按钮，api：接口）',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '路径/接口地址',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0：禁用，1：启用）',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 部门表
CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` BIGINT NOT NULL COMMENT '部门ID',
    `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `dept_code` VARCHAR(50) NOT NULL COMMENT '部门编码',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID',
    `leader_id` BIGINT DEFAULT NULL COMMENT '部门负责人ID',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0：禁用，1：启用）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code` (`dept_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 用户表添加部门ID字段
ALTER TABLE `sys_user` ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID' AFTER `user_type`;

-- ========== 初始化数据 ==========

-- 初始化角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `sort_order`, `status`, `is_system`, `deleted`) VALUES
(1, 'admin', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1, 1, 0),
(2, 'boss', '老板', '公司高层，可以查看所有数据', 2, 1, 1, 0),
(3, 'manager', '部门经理', '部门负责人，管理本部门成员', 3, 1, 1, 0),
(4, 'member', '部门成员', '普通员工', 4, 1, 1, 0);

-- 初始化权限
INSERT INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `parent_id`, `sort_order`, `deleted`) VALUES
-- 用户管理
(1, 'user:view', '查看用户', 'button', 0, 1, 0),
(2, 'user:create', '创建用户', 'button', 0, 2, 0),
(3, 'user:edit', '编辑用户', 'button', 0, 3, 0),
(4, 'user:delete', '删除用户', 'button', 0, 4, 0),
(5, 'user:export', '导出用户', 'button', 0, 5, 0),
-- 部门管理
(11, 'dept:view', '查看部门', 'button', 0, 11, 0),
(12, 'dept:create', '创建部门', 'button', 0, 12, 0),
(13, 'dept:edit', '编辑部门', 'button', 0, 13, 0),
(14, 'dept:delete', '删除部门', 'button', 0, 14, 0),
-- 角色管理
(21, 'role:view', '查看角色', 'button', 0, 21, 0),
(22, 'role:create', '创建角色', 'button', 0, 22, 0),
(23, 'role:edit', '编辑角色', 'button', 0, 23, 0),
(24, 'role:delete', '删除角色', 'button', 0, 24, 0),
-- 群组管理
(31, 'group:view', '查看群组', 'button', 0, 31, 0),
(32, 'group:create', '创建群组', 'button', 0, 32, 0),
(33, 'group:edit', '编辑群组', 'button', 0, 33, 0),
(34, 'group:delete', '删除群组', 'button', 0, 34, 0),
(35, 'group:manage', '管理群组', 'button', 0, 35, 0),
-- 消息管理
(41, 'msg:view', '查看消息', 'button', 0, 41, 0),
(42, 'msg:delete', '删除消息', 'button', 0, 42, 0),
(43, 'msg:recall', '撤回消息', 'button', 0, 43, 0),
-- 文件管理
(51, 'file:view', '查看文件', 'button', 0, 51, 0),
(52, 'file:delete', '删除文件', 'button', 0, 52, 0),
-- 系统设置
(61, 'system:config', '系统配置', 'button', 0, 61, 0),
(62, 'system:log', '查看日志', 'button', 0, 62, 0),
-- AI功能
(71, 'ai:config', 'AI配置', 'button', 0, 71, 0),
(72, 'ai:use', '使用AI', 'button', 0, 72, 0),
-- 数据统计
(81, 'stats:view', '查看统计', 'button', 0, 81, 0),
(82, 'stats:export', '导出统计', 'button', 0, 82, 0);

-- 角色权限关联 - 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `deleted`)
SELECT ROW_NUMBER() OVER () + 100, 1, id, 0 FROM `sys_permission` WHERE deleted = 0;

-- 角色权限关联 - 老板
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `deleted`) VALUES
(201, 2, 1, 0),  -- user:view
(202, 2, 11, 0), -- dept:view
(203, 2, 31, 0), -- group:view
(204, 2, 41, 0), -- msg:view
(205, 2, 51, 0), -- file:view
(206, 2, 62, 0), -- system:log
(207, 2, 72, 0), -- ai:use
(208, 2, 81, 0), -- stats:view
(209, 2, 82, 0); -- stats:export

-- 角色权限关联 - 部门经理
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `deleted`) VALUES
(301, 3, 1, 0),  -- user:view
(302, 3, 2, 0),  -- user:create
(303, 3, 3, 0),  -- user:edit
(304, 3, 11, 0), -- dept:view
(305, 3, 31, 0), -- group:view
(306, 3, 32, 0), -- group:create
(307, 3, 33, 0), -- group:edit
(308, 3, 41, 0), -- msg:view
(309, 3, 51, 0), -- file:view
(310, 3, 72, 0); -- ai:use

-- 角色权限关联 - 部门成员
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `deleted`) VALUES
(401, 4, 1, 0),  -- user:view
(402, 4, 31, 0), -- group:view
(403, 4, 32, 0), -- group:create
(404, 4, 41, 0), -- msg:view
(405, 4, 51, 0), -- file:view
(406, 4, 72, 0); -- ai:use

-- 初始化部门
INSERT INTO `sys_department` (`id`, `dept_name`, `dept_code`, `parent_id`, `sort_order`, `status`, `deleted`) VALUES
(1, '总公司', 'company', 0, 1, 1, 0),
(2, '技术部', 'tech', 1, 1, 1, 0),
(3, '产品部', 'product', 1, 2, 1, 0),
(4, '运营部', 'operation', 1, 3, 1, 0),
(5, '市场部', 'market', 1, 4, 1, 0),
(6, '人事部', 'hr', 1, 5, 1, 0);

-- 给管理员分配超级管理员角色
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `deleted`) VALUES (1, 1, 1, 0);

-- 更新管理员的部门
UPDATE `sys_user` SET `dept_id` = 1 WHERE `id` = 1;