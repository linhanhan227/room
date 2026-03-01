-- =============================================
-- 聊天室系统数据库初始化脚本
-- 版本: v1.2.0
-- 日期: 2024-01-20
-- 描述: 包含用户、聊天室、消息、管理等功能
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS chat_room 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE chat_room;

-- =============================================
-- 1. 用户表 (users)
-- 存储系统用户信息
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键自增',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一，不能重复',
    password VARCHAR(255) NOT NULL COMMENT '密码，加密存储',
    nickname VARCHAR(50) COMMENT '昵称，显示名称',
    email VARCHAR(100) UNIQUE COMMENT '邮箱地址，唯一',
    avatar VARCHAR(255) COMMENT '头像URL地址',
    status VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '用户状态：ONLINE-在线, OFFLINE-离线, BUSY-忙碌, AWAY-离开',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色：USER-普通用户, ADMIN-管理员',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除（软删除标记）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引定义
    INDEX idx_username (username) COMMENT '用户名索引，加速用户名查询',
    INDEX idx_email (email) COMMENT '邮箱索引，加速邮箱查询',
    INDEX idx_status (status) COMMENT '状态索引，加速在线用户查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 聊天室表 (chat_rooms)
-- 存储聊天室基本信息
-- =============================================
CREATE TABLE IF NOT EXISTS chat_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '聊天室ID，主键自增',
    name VARCHAR(100) NOT NULL COMMENT '聊天室名称',
    description VARCHAR(500) COMMENT '聊天室描述',
    avatar VARCHAR(255) COMMENT '聊天室头像URL',
    owner_id BIGINT NOT NULL COMMENT '聊天室所有者ID',
    type VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '聊天室类型：PUBLIC-公开, PRIVATE-私密, GROUP-群组',
    password VARCHAR(255) COMMENT '私密聊天室密码（加密存储）',
    max_members INT DEFAULT 100 COMMENT '最大成员数量',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '聊天室状态：ACTIVE-活跃, INACTIVE-不活跃, ARCHIVED-已归档',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除（软删除标记）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '所有者外键关联用户表',
    
    -- 索引定义
    INDEX idx_name (name) COMMENT '名称索引，加速搜索',
    INDEX idx_owner (owner_id) COMMENT '所有者索引',
    INDEX idx_type (type) COMMENT '类型索引',
    INDEX idx_status (status) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室表';

-- =============================================
-- 3. 聊天室成员表 (room_members)
-- 存储聊天室与用户的关联关系
-- =============================================
CREATE TABLE IF NOT EXISTS room_members (
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) DEFAULT 'MEMBER' COMMENT '成员角色：OWNER-所有者, ADMIN-管理员, MEMBER-普通成员',
    last_read_at DATETIME COMMENT '最后阅读消息时间',
    muted BOOLEAN DEFAULT FALSE COMMENT '是否静音',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否已退出（软删除标记）',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    
    -- 联合主键
    PRIMARY KEY (room_id, user_id),
    
    -- 外键约束
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE COMMENT '聊天室外键',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '用户外键',
    
    -- 索引定义
    INDEX idx_room (room_id) COMMENT '聊天室索引',
    INDEX idx_user (user_id) COMMENT '用户索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天室成员表';

-- =============================================
-- 4. 消息表 (messages)
-- 存储聊天消息记录
-- =============================================
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID，主键自增',
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    sender_id BIGINT NOT NULL COMMENT '发送者用户ID',
    content TEXT NOT NULL COMMENT '消息内容',
    type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本, IMAGE-图片, FILE-文件, SYSTEM-系统消息, EMOJI-表情',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除（软删除标记）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE COMMENT '聊天室外键',
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '发送者外键',
    
    -- 索引定义
    INDEX idx_room (room_id) COMMENT '聊天室索引，加速消息查询',
    INDEX idx_sender (sender_id) COMMENT '发送者索引',
    INDEX idx_created_at (created_at) COMMENT '创建时间索引，加速时间范围查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =============================================
-- 5. 封禁用户表 (banned_users)
-- 存储被封禁用户记录
-- =============================================
CREATE TABLE IF NOT EXISTS banned_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '封禁记录ID，主键自增',
    user_id BIGINT NOT NULL COMMENT '被封禁用户ID',
    banned_by BIGINT NOT NULL COMMENT '执行封禁的管理员ID',
    reason VARCHAR(500) COMMENT '封禁原因',
    type VARCHAR(20) DEFAULT 'TEMPORARY' COMMENT '封禁类型：PERMANENT-永久, TEMPORARY-临时, WARNING-警告',
    end_time DATETIME COMMENT '封禁结束时间（临时封禁时有效）',
    active BOOLEAN DEFAULT TRUE COMMENT '是否生效中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '被封禁用户外键',
    FOREIGN KEY (banned_by) REFERENCES users(id) ON DELETE CASCADE COMMENT '执行封禁管理员外键',
    
    -- 索引定义
    INDEX idx_user (user_id) COMMENT '用户索引',
    INDEX idx_active (active) COMMENT '生效状态索引',
    INDEX idx_end_time (end_time) COMMENT '结束时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='封禁用户表';

-- =============================================
-- 6. 系统日志表 (system_logs)
-- 存储系统操作日志
-- =============================================
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID，主键自增',
    action VARCHAR(100) NOT NULL COMMENT '操作类型，如：BAN_USER, DELETE_ROOM等',
    entity_type VARCHAR(50) COMMENT '实体类型，如：User, ChatRoom, Message等',
    entity_id BIGINT COMMENT '实体ID',
    operator_id BIGINT COMMENT '操作者用户ID',
    target_user_id BIGINT COMMENT '目标用户ID',
    details TEXT COMMENT '操作详情（JSON格式）',
    ip_address VARCHAR(50) COMMENT '操作者IP地址',
    level VARCHAR(20) DEFAULT 'INFO' COMMENT '日志级别：INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE SET NULL COMMENT '操作者外键',
    
    -- 索引定义
    INDEX idx_action (action) COMMENT '操作类型索引',
    INDEX idx_entity (entity_type, entity_id) COMMENT '实体索引',
    INDEX idx_operator (operator_id) COMMENT '操作者索引',
    INDEX idx_target_user (target_user_id) COMMENT '目标用户索引',
    INDEX idx_created_at (created_at) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- =============================================
-- 7. 邮箱验证表 (email_verifications)
-- 存储邮箱验证码记录
-- =============================================
CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '验证记录ID，主键自增',
    email VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    type VARCHAR(20) NOT NULL COMMENT '验证类型：REGISTER-注册, RESET_PASSWORD-重置密码, CHANGE_EMAIL-更改邮箱',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    used BOOLEAN DEFAULT FALSE COMMENT '是否已使用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引定义
    INDEX idx_email (email) COMMENT '邮箱索引',
    INDEX idx_code (code) COMMENT '验证码索引',
    INDEX idx_type (type) COMMENT '类型索引',
    INDEX idx_expires_at (expires_at) COMMENT '过期时间索引',
    INDEX idx_used (used) COMMENT '使用状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证表';

-- =============================================
-- 8. 举报表 (reports)
-- 存储用户举报记录
-- =============================================
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '举报ID，主键自增',
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    reported_user_id BIGINT COMMENT '被举报用户ID',
    reported_room_id BIGINT COMMENT '被举报聊天室ID',
    reported_message_id BIGINT COMMENT '被举报消息ID',
    type VARCHAR(30) NOT NULL COMMENT '举报类型：SPAM-垃圾信息, HARASSMENT-骚扰, INAPPROPRIATE_CONTENT-不当内容, VIOLENCE-暴力, FRAUD-欺诈, OTHER-其他',
    target_type VARCHAR(20) NOT NULL COMMENT '举报目标类型：USER-用户, ROOM-聊天室, MESSAGE-消息',
    reason VARCHAR(1000) COMMENT '举报原因',
    description TEXT COMMENT '详细描述',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, REJECTED-已拒绝',
    handler_id BIGINT COMMENT '处理人ID',
    handle_result VARCHAR(500) COMMENT '处理结果',
    handled_at DATETIME COMMENT '处理时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引定义
    INDEX idx_reporter (reporter_id) COMMENT '举报人索引',
    INDEX idx_reported_user (reported_user_id) COMMENT '被举报用户索引',
    INDEX idx_reported_room (reported_room_id) COMMENT '被举报聊天室索引',
    INDEX idx_reported_message (reported_message_id) COMMENT '被举报消息索引',
    INDEX idx_type (type) COMMENT '举报类型索引',
    INDEX idx_status (status) COMMENT '处理状态索引',
    INDEX idx_created_at (created_at) COMMENT '创建时间索引',
    
    -- 外键约束
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (reported_room_id) REFERENCES chat_rooms(id) ON DELETE SET NULL,
    FOREIGN KEY (reported_message_id) REFERENCES messages(id) ON DELETE SET NULL,
    FOREIGN KEY (handler_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- =============================================
-- 9. 邮件发送日志表 (email_send_logs)
-- 记录邮件发送次数，用于每日限制
-- =============================================
CREATE TABLE IF NOT EXISTS email_send_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID，主键自增',
    email VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    type VARCHAR(30) COMMENT '邮件类型：VERIFICATION-验证邮件, SIMPLE-简单邮件, HTML-HTML邮件',
    send_date DATE NOT NULL COMMENT '发送日期',
    send_count INT DEFAULT 1 COMMENT '当日发送次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引定义
    INDEX idx_email (email) COMMENT '邮箱索引',
    INDEX idx_send_date (send_date) COMMENT '发送日期索引',
    UNIQUE KEY uk_email_date (email, send_date) COMMENT '邮箱日期唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送日志表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认管理员账户
-- 用户名: admin, 密码: admin123 (实际使用时请修改密码)
INSERT INTO users (username, password, nickname, email, status, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@example.com', 'OFFLINE', 'ADMIN')
ON DUPLICATE KEY UPDATE username = username;

-- 插入测试用户
INSERT INTO users (username, password, nickname, email, status, role) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', 'test@example.com', 'OFFLINE', 'USER')
ON DUPLICATE KEY UPDATE username = username;

-- =============================================
-- 视图定义
-- =============================================

-- 创建在线用户视图
CREATE OR REPLACE VIEW v_online_users AS
SELECT 
    id, 
    username, 
    nickname, 
    email, 
    avatar, 
    status, 
    role, 
    created_at 
FROM users 
WHERE status = 'ONLINE' AND deleted = FALSE
COMMENT '在线用户视图';

-- 创建活跃聊天室视图
CREATE OR REPLACE VIEW v_active_rooms AS
SELECT 
    cr.id,
    cr.name,
    cr.description,
    cr.avatar,
    cr.owner_id,
    u.nickname AS owner_name,
    cr.type,
    cr.max_members,
    cr.status,
    cr.created_at,
    COUNT(rm.user_id) AS member_count
FROM chat_rooms cr
LEFT JOIN room_members rm ON cr.id = rm.room_id AND rm.deleted = FALSE
LEFT JOIN users u ON cr.owner_id = u.id
WHERE cr.deleted = FALSE AND cr.status = 'ACTIVE'
GROUP BY cr.id
COMMENT '活跃聊天室视图';

-- =============================================
-- 存储过程定义
-- =============================================

-- 清理过期验证码存储过程
DELIMITER //
CREATE PROCEDURE sp_cleanup_expired_verifications()
BEGIN
    -- 删除过期的邮箱验证码
    DELETE FROM email_verifications 
    WHERE expires_at < NOW();
    
    -- 返回删除的记录数
    SELECT ROW_COUNT() AS deleted_count;
END //
DELIMITER ;

-- 获取用户统计信息存储过程
DELIMITER //
CREATE PROCEDURE sp_get_user_statistics()
BEGIN
    SELECT 
        COUNT(*) AS total_users,
        SUM(CASE WHEN status = 'ONLINE' THEN 1 ELSE 0 END) AS online_users,
        SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) AS admin_users,
        SUM(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS new_users_week
    FROM users
    WHERE deleted = FALSE;
END //
DELIMITER ;

-- 获取聊天室统计信息存储过程
DELIMITER //
CREATE PROCEDURE sp_get_room_statistics()
BEGIN
    SELECT 
        COUNT(*) AS total_rooms,
        SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_rooms,
        SUM(CASE WHEN type = 'PUBLIC' THEN 1 ELSE 0 END) AS public_rooms,
        SUM(CASE WHEN type = 'PRIVATE' THEN 1 ELSE 0 END) AS private_rooms
    FROM chat_rooms
    WHERE deleted = FALSE;
END //
DELIMITER ;

-- =============================================
-- 触发器定义
-- =============================================

-- 用户状态变更触发器（记录日志）
DELIMITER //
CREATE TRIGGER tr_user_status_change
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO system_logs (action, entity_type, entity_id, details, level)
        VALUES (
            'STATUS_CHANGE',
            'User',
            NEW.id,
            CONCAT('用户状态从 ', OLD.status, ' 变更为 ', NEW.status),
            'INFO'
        );
    END IF;
END //
DELIMITER ;

-- 新用户注册触发器（记录日志）
DELIMITER //
CREATE TRIGGER tr_user_register
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO system_logs (action, entity_type, entity_id, details, level)
    VALUES (
        'USER_REGISTER',
        'User',
        NEW.id,
        CONCAT('新用户注册: ', NEW.username),
        'INFO'
    );
END //
DELIMITER ;

-- =============================================
-- 事件调度器定义
-- =============================================

-- 开启事件调度器
SET GLOBAL event_scheduler = ON;

-- 每天凌晨2点清理过期验证码
CREATE EVENT IF NOT EXISTS ev_cleanup_verifications
ON SCHEDULE EVERY 1 DAY
STARTS CONCAT(CURDATE() + INTERVAL 1 DAY, ' 02:00:00')
DO CALL sp_cleanup_expired_verifications();

-- =============================================
-- 索引优化建议
-- =============================================
-- 1. 对于频繁查询的字段，建议添加索引
-- 2. 对于联合查询，建议创建联合索引
-- 3. 定期分析表并优化索引
-- 4. 对于大表，考虑分区策略

-- 分析表（优化索引）
ANALYZE TABLE users;
ANALYZE TABLE chat_rooms;
ANALYZE TABLE room_members;
ANALYZE TABLE messages;
ANALYZE TABLE banned_users;
ANALYZE TABLE system_logs;
ANALYZE TABLE email_verifications;

-- =============================================
-- 完成提示
-- =============================================
SELECT '数据库初始化完成！' AS message;
SELECT '默认管理员账户: admin / admin123' AS info;
SELECT '请及时修改默认管理员密码！' AS warning;
