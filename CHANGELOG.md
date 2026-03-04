# 更新日志

所有重要的项目变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)。

## [1.2.0] - 2026-03-04

### 新增
- **聊天室所有权转让** — 新增 `PUT /rooms/{roomId}/transfer?newOwnerId=` 接口
  - 验证当前用户为聊天室所有者或系统管理员
  - 原所有者自动降级为管理员（ADMIN）
  - 新所有者升级为 OWNER 并更新 `chat_rooms.owner_id`
- **WebSocket 错误消息反馈** — 新增 `/user/queue/errors` 订阅通道
  - 消息发送失败时（禁言、敏感词、非成员等）通过该通道向用户推送错误详情
  - 消息格式：`{error: String, timestamp: String}`
- **WebSocket HTTP 握手拦截器** — 新增 `WebSocketHandshakeInterceptor`
  - 在 HTTP → WebSocket 协议升级阶段提取并验证 JWT Token
  - 支持查询参数传递 Token（`ws://host/api/ws?token=xxx`）和请求头传递（`Authorization: Bearer xxx`）
  - 认证通过后将用户信息缓存到 WebSocket Session，后续消息处理无需查询数据库

### 改进
- **加入聊天室黑名单检查** — `joinRoom` 新增黑名单校验，被拉黑用户无法加入聊天室
- **更新聊天室支持修改类型和密码** — `updateRoom` 新增 `type` 和 `password` 字段的修改支持
  - 传入空字符串密码可清除已有密码
- **WebSocket 消息发送流程优化** — `WebSocketChatHandler.sendMessage` 改为调用 `saveMessage` 避免 SecurityContext 问题，并捕获 `BusinessException` 反馈错误
- **断开连接处理优化** — `WebSocketEventListener` 从 `sessionUserMap` 直接获取 userId，避免额外数据库查询
- **封禁用户统计优化** — `AdminService.getDashboardStats` 中 `bannedUsers` 统计改用 `BannedUserRepository.countActiveBans()` 高效聚合查询
- **WebSocket 用户信息获取优化** — `getUserFromSession` 优先从 HTTP 握手阶段缓存的 Session attributes 获取用户信息，降级到 STOMP Principal 查库

### 文档
- 更新 API 接口文档（`docs/API_DOCUMENT.md`）
  - 新增 5.13 转让聊天室所有权接口文档
  - 新增 6.8 设置成员角色接口文档（成员管理模块）
  - 完善 5.2 加入聊天室错误响应（新增黑名单错误）
  - 完善 5.12 更新聊天室信息请求参数说明（新增 type、password 字段）
- 更新 WebSocket 文档（`docs/WEBSOCKET_DOCUMENT.md`）
  - 新增订阅接口：`/user/queue/errors`（错误消息）
  - 新增第 5 节：订阅错误消息详情
  - 更新认证说明：新增 HTTP 握手阶段 Token 查询参数认证方式
  - 更新发送消息说明：失败时通过错误通道反馈
  - 更新客户端示例代码：添加 errors 订阅
  - 新增源码位置：`WebSocketHandshakeInterceptor.java`
- 合并数据库脚本为统一的 `database.sql`（包含完整版本历史）
- 更新 CHANGELOG

### 数据库
- 合并 `database_v1.1.0.sql` 和 `database_v1.2.0.sql` 为统一的 `database.sql`（含完整版本历史，无表结构变更）

## [1.1.1] - 2026-03-04

### 修复
- **[严重]** 修复 `ChatRoom` 和 `User` 实体的 `@ManyToMany` 映射与 `RoomMember` 独立实体冲突问题
  - 移除 `ChatRoom.members` 和 `User.rooms` 的 `@ManyToMany` 双向关联
  - 重写 `ChatRoomRepository` 中 4 个 JPQL 查询，改为通过 `RoomMember` 实体子查询
  - 重写 `UserRepository.findByRoomId` 查询
- **[严重]** 修复 `AdminService.getDashboardStats` 全表扫描导致的严重性能问题和潜在 OOM
  - 使用数据库聚合查询替代内存过滤
  - 新增 `MessageRepository.countMessagesSince()` 方法
- **[高]** 修复 CORS 配置 `allow-credentials: true` 与 `allowed-origins: "*"` 不兼容导致启动失败
  - `allowed-origins` 改为 `allowed-origin-patterns`
  - 同步更新 `AppProperties` 和 `CorsConfig`
- **[高]** 修复 `EmailService.sendVerificationCode` 的 `@Async` 导致异常丢失，用户无法感知发送失败
- **[高]** 修复 `MessageRepository.findRecentMessages` 使用非标准 JPQL `LIMIT` 语法
  - 改为使用 `Pageable` 参数
  - 同步更新 `MessageService.getRecentMessages`
- **[高]** 修复 `ChatRoomService.deleteRoom` 未清理 `room_blacklist` 表导致外键约束错误
- **[高]** 修复 `AdminService.deleteRoom` 未清理成员和黑名单数据
- **[中]** 修复 `EmailVerificationService` 中 3 处 `RuntimeException` 应为 `BusinessException`
- **[中]** 修复 `EmailVerificationController` 中 `RuntimeException` 应为 `BusinessException`
- **[中]** 修复 `RoomMemberManagementService.isUserMuted` 缺少 `@Transactional` 注解
- **[低]** 修复 `WebSocketChatHandler.getUserFromSession` 重复 3 次数据库查询
- **[低]** 修复 `SensitiveWordService` 未检查功能启用状态

### 改进
- 优化实体模型：统一通过 `RoomMember` 实体管理聊天室成员关系，消除 ORM 映射歧义
- 优化管理后台仪表盘统计查询性能，从全表扫描改为数据库聚合查询
- WebSocket 消息处理中用户信息获取从 3 次数据库查询减少为 1 次

### 文档
- 新增 Bug 修复报告（`docs/BUGFIX_REPORT.md`）
- 更新项目 README 文档
- 更新 CHANGELOG

## [1.1.0] - 2026-03-02

### 新增
- 聊天室成员管理功能
  - 禁言/解禁成员（支持定时禁言）
  - 踢出成员
  - 拉黑/解除拉黑成员
  - 成员角色管理（所有者/管理员/成员）
- 实时聊天数据存储到数据库
- WebSocket实时通信功能
  - 聊天室消息广播
  - 用户在线状态同步
  - 输入状态提示
  - 心跳机制
- 聊天室推荐算法
  - 活跃度推荐
  - 热度推荐
  - 最新推荐
  - 随机推荐
  - 混合推荐策略
- 敏感词过滤功能
  - KMP算法
  - Trie树算法
  - AC自动机算法
- 邮箱验证功能
- 举报功能
- 公告功能
- 系统日志功能

### 改进
- 完善权限管理系统
  - 系统管理员权限（最高优先级）
  - 聊天室所有者权限
  - 聊天室管理员权限
  - 普通成员权限
- 管理员权限保护（系统管理员不能被禁言/踢出/拉黑）
- 数据库表结构优化
  - 添加`muted_until`字段支持定时禁言
  - 新增`room_blacklist`表支持拉黑功能

### 修复
- 修复SecurityConfig中AuthenticationManager Bean创建失败的问题
  - 移除authenticationManager方法中的UserDetailsService参数
  - 使用类字段userDetailsService避免参数名冲突
- 修复Controller路由映射冲突问题
  - 移除ChatRoomController中重复的成员管理接口
  - 将成员管理相关接口统一到RoomMemberManagementController
  - 在RoomMemberManagementService中添加setMemberRole方法

### 文档
- 新增API接口文档（API_DOCUMENT.md）
- 新增WebSocket实时通信文档（WEBSOCKET_DOCUMENT.md）
- 新增部署和运行指南（DEPLOYMENT.md）
- 更新README.md，添加快速部署命令和文档链接

### 数据库
- 合并数据库脚本（init_database.sql + upgrade_v1.1.0.sql → database_v1.1.0.sql → database.sql）
- 统一数据库版本管理

## [1.0.0] - 2024-01-01

### 新增
- 用户注册/登录功能（JWT认证）
- 聊天室创建/加入/退出功能
- 公私聊聊天室支持
- 基础消息收发功能
- 用户管理功能
  - 用户信息查询
  - 用户状态管理
  - 用户封禁/解封
- 聊天室管理功能
  - 聊天室CRUD
  - 聊天室搜索
  - 聊天室成员列表
- 消息功能
  - 消息历史查询
  - 消息分页加载

### 安全
- JWT Token认证机制
- 密码加密存储（BCrypt）
- CORS跨域配置
- 接口权限控制

### 技术栈
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- MySQL 8.x
- JWT (jjwt)
- Lombok

---

## 版本说明

### 版本号格式
- 主版本号.次版本号.修订号（例如：1.1.0）
- 主版本号：重大功能更新或不兼容的API变更
- 次版本号：新功能添加，向后兼容
- 修订号：Bug修复或小改进

### 更新类型
- **新增** - 新功能
- **改进** - 现有功能的改进
- **修复** - Bug修复
- **文档** - 文档更新
- **安全** - 安全相关更新
- **移除** - 功能移除
- **弃用** - 即将移除的功能

---

## 未来计划

### [1.3.0] - 计划中
- 文件上传功能
- 语音消息功能
- 视频通话功能
- 消息撤回功能
- 消息已读回执
- 群组功能
- 好友功能
- 消息搜索功能
- 表情包功能
- 消息引用功能

### [2.0.0] - 长期计划
- 移动端应用（Android/iOS）
- 桌面应用（Windows/Mac/Linux）
- 多语言支持
- 主题切换功能
- 插件系统
- API限流
- 消息加密
- 端到端加密

---

*最后更新: 2026年3月*
