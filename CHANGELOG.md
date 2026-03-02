# 更新日志

所有重要的项目变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)。

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
- 合并数据库脚本（init_database.sql + upgrade_v1.1.0.sql → database_v1.1.0.sql）
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

### [1.2.0] - 计划中
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
