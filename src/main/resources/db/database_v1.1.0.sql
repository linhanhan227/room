-- =============================================
-- 聊天室系统数据库完整脚本
-- 版本: v1.1.0
-- 数据库名: chat_room
-- 字符集: UTF-8
-- =============================================
-- 使用说明:
-- 1. 新建数据库时直接执行此脚本
-- 2. 从 v1.0.0 升级时，请使用 upgrade_v1.1.0.sql
-- =============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;
SET character_set_client=utf8mb4;
SET character_set_results=utf8mb4;

-- =============================================
-- 1. 用户表 (users)
-- 对应实体: User.java extends BaseEntity
-- 修正: nickname VARCHAR(50)，与实体 length=50 一致
-- =============================================
CREATE TABLE IF NOT EXISTS `users` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`   VARCHAR(50) NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname`   VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email`      VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `status`     VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '状态: ONLINE/OFFLINE/BUSY/AWAY',
    `role`       VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `deleted`    BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 聊天室表 (chat_rooms)
-- 对应实体: ChatRoom.java extends BaseEntity
-- =============================================
CREATE TABLE IF NOT EXISTS `chat_rooms` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '聊天室ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '聊天室名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `owner_id`    BIGINT NOT NULL COMMENT '所有者ID',
    `type`        VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '类型: PUBLIC/PRIVATE/GROUP',
    `password`    VARCHAR(255) DEFAULT NULL COMMENT '密码',
    `max_members` INT DEFAULT 100 COMMENT '最大成员数',
    `status`      VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE/ARCHIVED',
    `deleted`     BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    `created_at`  DATETIME NOT NULL COMMENT '创建时间',
    `updated_at`  DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_chat_rooms_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室表';

-- =============================================
-- 3. 聊天室成员关联表 (room_members)
-- 对应实体: RoomMember.java（复合主键 room_id + user_id）
-- =============================================
CREATE TABLE IF NOT EXISTS `room_members` (
    `room_id`      BIGINT NOT NULL COMMENT '聊天室ID',
    `user_id`      BIGINT NOT NULL COMMENT '用户ID',
    `role`         VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色: OWNER/ADMIN/MEMBER',
    `last_read_at` DATETIME DEFAULT NULL COMMENT '最后阅读时间',
    `muted`        BOOLEAN DEFAULT FALSE COMMENT '是否禁言',
    `muted_until`  DATETIME DEFAULT NULL COMMENT '禁言到期时间',
    `deleted`      BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    `joined_at`    DATETIME DEFAULT NULL COMMENT '加入时间',
    PRIMARY KEY (`room_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role` (`role`),
    KEY `idx_muted_until` (`muted_until`),
    CONSTRAINT `fk_room_members_room` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`),
    CONSTRAINT `fk_room_members_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室成员关联表';

-- =============================================
-- 4. 消息表 (messages)
-- 对应实体: Message.java extends BaseEntity
-- =============================================
CREATE TABLE IF NOT EXISTS `messages` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `room_id`    BIGINT NOT NULL COMMENT '聊天室ID',
    `sender_id`  BIGINT NOT NULL COMMENT '发送者ID',
    `content`    TEXT NOT NULL COMMENT '消息内容',
    `type`       VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/FILE/SYSTEM/EMOJI',
    `deleted`    BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_messages_room` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`),
    CONSTRAINT `fk_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =============================================
-- 5. 聊天室黑名单表 (room_blacklist)
-- 对应实体: RoomBlacklist.java
-- =============================================
CREATE TABLE IF NOT EXISTS `room_blacklist` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '黑名单记录ID',
    `room_id`     BIGINT NOT NULL COMMENT '聊天室ID',
    `user_id`     BIGINT NOT NULL COMMENT '被拉黑用户ID',
    `added_by_id` BIGINT NOT NULL COMMENT '操作者ID',
    `reason`      VARCHAR(500) DEFAULT NULL COMMENT '拉黑原因',
    `deleted`     BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    `created_at`  DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_user` (`room_id`, `user_id`),
    KEY `idx_room_user` (`room_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_room_user_deleted` (`room_id`, `user_id`, `deleted`),
    CONSTRAINT `fk_room_blacklist_room` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_room_blacklist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_room_blacklist_added_by` FOREIGN KEY (`added_by_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室黑名单表';

-- =============================================
-- 6. 公告表 (announcements)
-- 对应实体: Announcement.java（无 BaseEntity，自管理时间字段）
-- =============================================
CREATE TABLE IF NOT EXISTS `announcements` (
    `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`        VARCHAR(200) NOT NULL COMMENT '标题',
    `content`      TEXT NOT NULL COMMENT '内容',
    `type`         VARCHAR(20) DEFAULT 'NORMAL' COMMENT '类型: NORMAL/IMPORTANT/SYSTEM/MAINTENANCE/UPDATE',
    `priority`     VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级: LOW/NORMAL/HIGH/URGENT',
    `author_id`    BIGINT DEFAULT NULL COMMENT '作者ID',
    `is_pinned`    BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    `is_published` BOOLEAN DEFAULT FALSE COMMENT '是否发布',
    `publish_at`   DATETIME DEFAULT NULL COMMENT '发布时间',
    `expire_at`    DATETIME DEFAULT NULL COMMENT '过期时间',
    `view_count`   INT DEFAULT 0 COMMENT '浏览次数',
    `created_at`   DATETIME NOT NULL COMMENT '创建时间',
    `updated_at`   DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_type` (`type`),
    KEY `idx_priority` (`priority`),
    KEY `idx_is_published` (`is_published`),
    KEY `idx_publish_at` (`publish_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- =============================================
-- 7. 公告阅读记录表 (announcement_reads)
-- 对应实体: AnnouncementRead.java（自增主键 id，无联合唯一约束）
-- 保留唯一约束以防止重复阅读记录（业务合理性）
-- =============================================
CREATE TABLE IF NOT EXISTS `announcement_reads` (
    `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `user_id`         BIGINT NOT NULL COMMENT '用户ID',
    `read_at`         DATETIME NOT NULL COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_read_at` (`read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告阅读记录表';

-- =============================================
-- 8. 禁用用户表 (banned_users)
-- 对应实体: BannedUser.java extends BaseEntity
-- =============================================
CREATE TABLE IF NOT EXISTS `banned_users` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '禁用记录ID',
    `user_id`    BIGINT NOT NULL COMMENT '被禁用用户ID',
    `banned_by`  BIGINT NOT NULL COMMENT '操作者ID',
    `reason`     VARCHAR(500) DEFAULT NULL COMMENT '禁用原因',
    `type`       VARCHAR(20) DEFAULT 'TEMPORARY' COMMENT '禁用类型: PERMANENT/TEMPORARY/WARNING',
    `end_time`   DATETIME DEFAULT NULL COMMENT '结束时间',
    `active`     BOOLEAN DEFAULT TRUE COMMENT '是否生效',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_banned_by` (`banned_by`),
    KEY `idx_active` (`active`),
    KEY `idx_end_time` (`end_time`),
    CONSTRAINT `fk_banned_users_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_banned_users_banned_by` FOREIGN KEY (`banned_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='禁用用户表';

-- =============================================
-- 9. 举报表 (reports)
-- 对应实体: Report.java（无 BaseEntity，自管理时间字段）
-- =============================================
CREATE TABLE IF NOT EXISTS `reports` (
    `id`                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    `reporter_id`         BIGINT NOT NULL COMMENT '举报人ID',
    `reported_user_id`    BIGINT DEFAULT NULL COMMENT '被举报用户ID',
    `reported_room_id`    BIGINT DEFAULT NULL COMMENT '被举报聊天室ID',
    `reported_message_id` BIGINT DEFAULT NULL COMMENT '被举报消息ID',
    `type`                VARCHAR(30) NOT NULL COMMENT '举报类型: SPAM/HARASSMENT/INAPPROPRIATE_CONTENT/VIOLENCE/FRAUD/OTHER',
    `target_type`         VARCHAR(20) NOT NULL COMMENT '目标类型: USER/ROOM/MESSAGE',
    `reason`              VARCHAR(1000) DEFAULT NULL COMMENT '举报原因',
    `description`         TEXT DEFAULT NULL COMMENT '详细描述',
    `status`              VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/RESOLVED/REJECTED',
    `handler_id`          BIGINT DEFAULT NULL COMMENT '处理人ID',
    `handle_result`       VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `handled_at`          DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at`          DATETIME NOT NULL COMMENT '创建时间',
    `updated_at`          DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_reported_user_id` (`reported_user_id`),
    KEY `idx_reported_room_id` (`reported_room_id`),
    KEY `idx_status` (`status`),
    KEY `idx_target_type` (`target_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- =============================================
-- 10. 系统日志表 (system_logs)
-- 对应实体: SystemLog.java extends BaseEntity
-- =============================================
CREATE TABLE IF NOT EXISTS `system_logs` (
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `action`         VARCHAR(100) NOT NULL COMMENT '操作',
    `entity_type`    VARCHAR(50) DEFAULT NULL COMMENT '实体类型',
    `entity_id`      BIGINT DEFAULT NULL COMMENT '实体ID',
    `operator_id`    BIGINT DEFAULT NULL COMMENT '操作者ID',
    `target_user_id` BIGINT DEFAULT NULL COMMENT '目标用户ID',
    `details`        TEXT DEFAULT NULL COMMENT '详细信息',
    `ip_address`     VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `level`          VARCHAR(20) DEFAULT 'INFO' COMMENT '日志级别: INFO/WARNING/ERROR/CRITICAL',
    `created_at`     DATETIME NOT NULL COMMENT '创建时间',
    `updated_at`     DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_target_user_id` (`target_user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_entity_type` (`entity_type`),
    KEY `idx_level` (`level`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_system_logs_operator` FOREIGN KEY (`operator_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- =============================================
-- 11. 邮箱验证表 (email_verifications)
-- 对应实体: EmailVerification.java（自管理 created_at）
-- =============================================
CREATE TABLE IF NOT EXISTS `email_verifications` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '验证ID',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `code`       VARCHAR(10) NOT NULL COMMENT '验证码',
    `type`       VARCHAR(20) NOT NULL COMMENT '验证类型: REGISTER/RESET_PASSWORD/CHANGE_EMAIL',
    `expires_at` DATETIME NOT NULL COMMENT '过期时间',
    `used`       BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_email` (`email`),
    KEY `idx_code` (`code`),
    KEY `idx_type` (`type`),
    KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证表';

-- =============================================
-- 12. 邮件发送日志表 (email_send_logs)
-- 对应实体: EmailSendLog.java（自管理时间字段）
-- =============================================
CREATE TABLE IF NOT EXISTS `email_send_logs` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `type`       VARCHAR(30) DEFAULT NULL COMMENT '邮件类型',
    `send_date`  DATE NOT NULL COMMENT '发送日期',
    `send_count` INT DEFAULT 1 COMMENT '发送次数',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email_date_type` (`email`, `send_date`, `type`),
    KEY `idx_send_date` (`send_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送日志表';

-- =============================================
-- 脚本执行完成
-- =============================================
-- 版本: v1.1.0
-- 修正内容（基于实体类核对）:
-- 1. [修正] users.nickname 由 VARCHAR(100) → VARCHAR(50)，与实体 length=50 一致
-- 2. [保留] announcement_reads 唯一约束 uk_announcement_user，防止重复阅读记录
-- 3. 其余表结构与实体类完全一致，无需改动
-- =============================================
