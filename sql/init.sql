 -- 创建数据库


CREATE DATABASE IF NOT EXISTS kuaitong DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE kuaitong;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `gender` TINYINT DEFAULT 1 COMMENT '性别（1：男，2：女）',
    `birthday` DATETIME DEFAULT NULL COMMENT '生日',
    `signature` VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '部门',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0：禁用，1：启用）',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `online_status` TINYINT DEFAULT 0 COMMENT '在线状态（0：离线，1：在线，2：忙碌，3：离开）',
    `user_type` TINYINT DEFAULT 0 COMMENT '用户类型（0：普通用户，1：管理员，2：超级管理员）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_online_status` (`online_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 好友表
CREATE TABLE IF NOT EXISTS `sys_friend` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注',
    `group_name` VARCHAR(50) DEFAULT NULL COMMENT '分组',
    `is_star` TINYINT DEFAULT 0 COMMENT '是否星标好友（0：否，1：是）',
    `is_black` TINYINT DEFAULT 0 COMMENT '是否拉黑（0：否，1：是）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_friend_id` (`friend_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友表';

-- 好友申请表
CREATE TABLE IF NOT EXISTS `sys_friend_request` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `sender_id` BIGINT NOT NULL COMMENT '申请者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `message` VARCHAR(255) DEFAULT NULL COMMENT '申请消息',
    `status` TINYINT DEFAULT 0 COMMENT '状态（0：待处理，1：已同意，2：已拒绝）',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

-- 会话表
CREATE TABLE IF NOT EXISTS `chat_conversation` (
    `id` BIGINT NOT NULL COMMENT '会话ID',
    `conversation_type` TINYINT NOT NULL COMMENT '会话类型（1：单聊，2：群聊）',
    `name` VARCHAR(100) DEFAULT NULL COMMENT '会话名称（群聊时有值）',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '会话头像（群聊时有值）',
    `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
    `last_message_content` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `last_message_sender_id` BIGINT DEFAULT NULL COMMENT '最后消息发送者ID',
    `member_count` INT DEFAULT 0 COMMENT '成员数量',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `announcement` TEXT DEFAULT NULL COMMENT '群公告',
    `do_not_disturb` TINYINT DEFAULT 0 COMMENT '免打扰（0：否，1：是）',
    `is_top` TINYINT DEFAULT 0 COMMENT '置顶（0：否，1：是）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_type` (`conversation_type`),
    KEY `idx_last_message_time` (`last_message_time`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 会话成员表
CREATE TABLE IF NOT EXISTS `chat_conversation_member` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '成员昵称（群内昵称）',
    `role` TINYINT DEFAULT 0 COMMENT '角色（0：普通成员，1：管理员，2：群主）',
    `do_not_disturb` TINYINT DEFAULT 0 COMMENT '免打扰（0：否，1：是）',
    `is_top` TINYINT DEFAULT 0 COMMENT '置顶（0：否，1：是）',
    `last_read_time` DATETIME DEFAULT NULL COMMENT '最后读取时间',
    `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
    `join_time` DATETIME DEFAULT NULL COMMENT '加入时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_user` (`conversation_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员表';

-- 消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `message_type` TINYINT NOT NULL COMMENT '消息类型（1：文本，2：图片，3：文件，4：视频，5：语音，6：系统，7：撤回）',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型（MIME类型）',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `duration` INT DEFAULT NULL COMMENT '视频时长（秒）',
    `reply_message_id` BIGINT DEFAULT NULL COMMENT '回复的消息ID',
    `at_user_ids` VARCHAR(500) DEFAULT NULL COMMENT '@的用户ID列表（JSON格式）',
    `status` TINYINT DEFAULT 0 COMMENT '消息状态（0：正常，1：已撤回，2：已删除）',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读（0：未读，1：已读）',
    `client_message_id` VARCHAR(100) DEFAULT NULL COMMENT '客户端消息ID（用于去重）',
    `sequence` BIGINT DEFAULT NULL COMMENT '序列号（用于消息排序）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_sequence` (`sequence`),
    KEY `idx_client_message_id` (`client_message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 消息已读状态表
CREATE TABLE IF NOT EXISTS `chat_message_read` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `message_id` BIGINT NOT NULL COMMENT '消息ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `read_time` DATETIME NOT NULL COMMENT '已读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_user` (`message_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息已读状态表';

-- 用户置顶会话表
CREATE TABLE IF NOT EXISTS `chat_user_top_conversation` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建者',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新者',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志（0：未删除，1：已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_conversation` (`user_id`, `conversation_id`),
    KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户置顶会话表';

-- 初始化超级管理员账号
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`, `user_type`, `create_time`, `update_time`, `deleted`)
VALUES (1, 'admin', '$2b$10$wuEwSSVbRT12HbUguUtZLeIHnJ05zrLxLkkVMKaoJYLggxUnVXr96', '超级管理员', 1, 2, NOW(), NOW(), 0);