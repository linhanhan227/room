# Chat Room - 聊天室应用

基于 Spring Boot 3.x 的实时聊天室应用，支持 WebSocket 实时通信、JWT 认证、敏感词过滤等功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.x | 核心框架 |
| Spring Security | - | 安全认证 |
| Spring WebSocket | - | 实时通信 |
| Spring Data JPA | - | 数据持久化 |
| MySQL | 8.x | 数据库 |
| JWT (jjwt) | 0.12.5 | Token认证 |
| Lombok | - | 代码简化 |

## 功能特性

### 核心功能
- 用户注册/登录（JWT认证）
- 聊天室创建/加入/退出
- 实时消息收发（WebSocket）
- 公私聊聊天室支持
- 在线状态显示
- 聊天消息持久化存储

### 管理功能
- 用户管理（封禁/解封/角色设置）
- 聊天室成员管理
  - 禁言/解禁成员（支持定时禁言）
  - 踢出成员
  - 拉黑/解除拉黑成员
  - 成员角色管理（所有者/管理员/成员）
- 敏感词过滤（KMP/Trie/AC自动机算法）
- 举报处理
- 公告管理
- 系统日志

### 权限管理
- 系统管理员权限（最高优先级）
- 聊天室所有者权限
- 聊天室管理员权限
- 普通成员权限
- 管理员权限保护（系统管理员不能被禁言/踢出/拉黑）

### 安全特性
- JWT Token 认证
- 密码加密存储
- CORS 跨域配置
- 接口权限控制

### 其他功能
- 邮箱验证
- 邮件发送频率限制
- 敏感词自动过滤

## 项目结构

```
src/main/java/com/chat/room/
├── config/                     # 配置类
│   ├── CorsConfig.java         # 跨域配置
│   └── WebSocketConfig.java    # WebSocket配置
├── controller/                 # 控制器层
│   ├── AuthController.java     # 认证接口
│   ├── UserController.java     # 用户接口
│   ├── ChatRoomController.java # 聊天室接口
│   ├── MessageController.java  # 消息接口
│   ├── AdminController.java    # 管理接口
│   ├── AnnouncementController.java  # 公告接口
│   ├── ReportController.java   # 举报接口
│   ├── SensitiveWordController.java # 敏感词接口
│   ├── EmailVerificationController.java # 邮箱验证接口
│   └── RoomMemberManagementController.java # 聊天室成员管理接口
├── service/                    # 服务层
│   ├── UserService.java         # 用户服务
│   ├── ChatRoomService.java     # 聊天室服务
│   ├── MessageService.java      # 消息服务
│   ├── AdminService.java        # 管理服务
│   ├── RoomMemberManagementService.java # 聊天室成员管理服务
│   ├── AnnouncementService.java # 公告服务
│   ├── ReportService.java       # 举报服务
│   ├── SensitiveWordService.java # 敏感词服务
│   ├── EmailService.java        # 邮件服务
│   ├── EmailVerificationService.java # 邮箱验证服务
│   └── recommendation/          # 推荐算法
│       ├── RoomRecommendationService.java
│       ├── RoomRecommendationStrategy.java
│       ├── ActivityBasedRecommendationStrategy.java
│       ├── PopularityBasedRecommendationStrategy.java
│       ├── NewestRecommendationStrategy.java
│       ├── RandomRecommendationStrategy.java
│       └── HybridRecommendationStrategy.java
├── repository/                 # 数据访问层
│   ├── UserRepository.java     # 用户数据访问
│   ├── ChatRoomRepository.java # 聊天室数据访问
│   ├── MessageRepository.java  # 消息数据访问
│   ├── RoomMemberRepository.java # 聊天室成员数据访问
│   ├── RoomBlacklistRepository.java # 聊天室黑名单数据访问
│   ├── AnnouncementRepository.java # 公告数据访问
│   ├── AnnouncementReadRepository.java # 公告阅读记录数据访问
│   ├── ReportRepository.java   # 举报数据访问
│   ├── BannedUserRepository.java # 禁用用户数据访问
│   ├── SystemLogRepository.java # 系统日志数据访问
│   ├── EmailVerificationRepository.java # 邮箱验证数据访问
│   └── EmailSendLogRepository.java # 邮件发送日志数据访问
├── entity/                     # 实体类
│   ├── User.java               # 用户实体
│   ├── ChatRoom.java           # 聊天室实体
│   ├── Message.java            # 消息实体
│   ├── RoomMember.java         # 聊天室成员实体
│   ├── RoomBlacklist.java      # 聊天室黑名单实体
│   ├── Announcement.java       # 公告实体
│   ├── AnnouncementRead.java  # 公告阅读记录实体
│   ├── Report.java             # 举报实体
│   ├── BannedUser.java         # 禁用用户实体
│   ├── SystemLog.java          # 系统日志实体
│   ├── EmailVerification.java  # 邮箱验证实体
│   ├── EmailSendLog.java       # 邮件发送日志实体
│   └── BaseEntity.java        # 基础实体
├── dto/                        # 数据传输对象
├── security/                   # 安全相关
│   ├── JwtTokenProvider.java   # JWT工具类
│   ├── JwtAuthenticationFilter.java # JWT过滤器
│   ├── SecurityConfig.java     # 安全配置
│   └── CustomUserDetailsService.java # 用户详情服务
├── util/                       # 工具类
│   ├── KMPSensitiveWordFilter.java  # KMP算法
│   ├── TrieSensitiveWordFilter.java # Trie树算法
│   ├── ACSensitiveWordFilter.java   # AC自动机算法
│   └── SensitiveWordLoader.java     # 敏感词加载器
├── websocket/                  # WebSocket处理
├── exception/                  # 异常处理
└── RoomApplication.java        # 启动类
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd room
```

2. **创建数据库**
```sql
CREATE DATABASE chat_room CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **修改配置文件**

编辑 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chat_room?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: your_username
    password: your_password

app:
  jwt:
    secret: your-256-bit-secret-key-for-jwt-token-generation-must-be-long-enough

  mail:
    host: smtp.example.com
    username: your-email@example.com
    password: your-email-password
```

4. **初始化数据库**

执行 `src/main/resources/db/database_v1.1.0.sql` 脚本初始化数据库表结构。

5. **创建敏感词文件**

在项目根目录创建 `sensitive_words.txt` 文件，每行一个敏感词。

6. **构建运行**
```bash
mvn clean package
java -jar target/chatroom.jar
```

服务将在 `http://localhost:8080/api` 启动。

### 详细部署指南

完整的部署和运行指南请参考 [DEPLOYMENT.md](./docs/DEPLOYMENT.md)

#### 快速部署命令

```bash
# 1. 启动MySQL服务
# Windows
Start-Service -Name MySQL80

# Linux
sudo systemctl start mysql

# 2. 初始化数据库
mysql -u root -p chat_room < src/main/resources/db/database_v1.1.0.sql

# 3. 打包项目
mvn clean package -DskipTests

# 4. 运行项目
java -jar target/chatroom.jar
```

## API 接口

详细API文档请参考 [API_DOCUMENT.md](./API_DOCUMENT.md)

### 主要接口

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/auth/*` | 注册、登录、Token刷新 |
| 用户 | `/users/*` | 用户信息、个人设置 |
| 聊天室 | `/rooms/*` | 聊天室CRUD、加入退出 |
| 聊天室成员管理 | `/rooms/{roomId}/members/*` | 禁言、踢出、拉黑、角色管理 |
| 消息 | `/messages/*` | 消息历史、发送 |
| 管理 | `/admin/*` | 用户管理、敏感词、举报处理 |
| 公告 | `/announcements/*` | 公告发布、已读状态 |
| WebSocket | `/ws` | 实时消息通信 |

### 聊天室成员管理接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/rooms/{roomId}/members/mute` | POST | 禁言成员 | 所有者/管理员/系统管理员 |
| `/rooms/{roomId}/members/{userId}/unmute` | POST | 解除禁言 | 所有者/管理员/系统管理员 |
| `/rooms/{roomId}/members/{userId}` | DELETE | 踢出成员 | 所有者/管理员/系统管理员 |
| `/rooms/{roomId}/members/blacklist` | POST | 拉黑成员 | 所有者/管理员/系统管理员 |
| `/rooms/{roomId}/members/blacklist/{userId}` | DELETE | 解除拉黑 | 所有者/管理员/系统管理员 |
| `/rooms/{roomId}/members/blacklist` | GET | 获取黑名单 | 所有者/管理员/系统管理员 |
| `/rooms/{roomId}/members` | GET | 获取成员列表 | 聊天室成员 |
| `/rooms/{roomId}/members/{userId}/role` | PUT | 设置成员角色 | 所有者/系统管理员 |

### WebSocket 使用

连接地址：`ws://localhost:8080/api/ws`

订阅主题：
- `/topic/room.{roomId}` - 聊天室消息
- `/topic/user.{userId}` - 私信
- `/topic/user.status` - 用户状态

## 配置说明

### JWT 配置
```yaml
jwt:
  secret: your-secret-key  # JWT密钥（至少256位）
  expiration: 86400000     # 过期时间（毫秒），默认24小时
```

### 邮件配置
```yaml
email:
  from: noreply@example.com
  verification:
    code-expiration: 300   # 验证码过期时间（秒）
    code-length: 6         # 验证码长度
  daily-limit:
    enabled: true
    max-count: 10          # 每日最大发送次数
```

### 敏感词配置
```yaml
sensitive-word:
  file-path: sensitive_words.txt  # 敏感词文件路径
  enabled: true
  algorithm: AC                   # 算法：KMP/TRIE/AC
```

## 敏感词过滤算法

| 算法 | 时间复杂度 | 特点 |
|------|-----------|------|
| KMP | O(n+m) | 单模式匹配，适合少量敏感词 |
| Trie | O(n×m) | 前缀匹配，适合大量敏感词 |
| AC | O(n) | 多模式匹配，推荐使用 |

## 用户角色

### 全局用户角色

| 角色 | 说明 | 权限 |
|------|------|------|
| USER | 普通用户 | 基础聊天功能 |
| ADMIN | 系统管理员 | 用户管理、敏感词管理、举报处理、公告管理、所有聊天室管理权限 |

### 聊天室成员角色

| 角色 | 说明 | 权限 |
|------|------|------|
| OWNER | 所有者 | 完全控制聊天室，可以删除聊天室、设置管理员、禁言/踢出/拉黑任何成员（除系统管理员） |
| ADMIN | 管理员 | 禁言/踢出/拉黑普通成员 |
| MEMBER | 成员 | 基础聊天功能 |

### 权限优先级

1. **系统管理员（ADMIN）** - 权限最高
   - 可以删除任何聊天室
   - 可以禁言/踢出/拉黑任何成员（除聊天室所有者）
   - 可以修改任何聊天室信息
   - 可以设置任何成员角色
   - **系统管理员不能被禁言/踢出/拉黑**

2. **聊天室所有者（OWNER）**
   - 可以删除自己的聊天室
   - 可以禁言/踢出/拉黑普通成员和管理员
   - 可以修改聊天室信息
   - 可以设置成员角色
   - **聊天室所有者不能被禁言/踢出/拉黑**

3. **聊天室管理员（ADMIN）**
   - 可以禁言/踢出/拉黑普通成员
   - 不能对系统管理员和聊天室所有者执行操作

4. **普通成员（MEMBER）**
   - 只能发送和接收消息
   - 不能执行管理操作

## 开发指南

### 运行测试
```bash
mvn test
```

### 代码规范
- 使用 Lombok 简化代码
- 遵循 RESTful API 设计
- 统一异常处理
- 统一响应格式

### 分支管理
- `main` - 生产分支
- `develop` - 开发分支
- `feature/*` - 功能分支

## 许可证

MIT License

## 更新日志

详细的更新日志请参考 [CHANGELOG.md](./CHANGELOG.md)

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 文档

- [API接口文档](./docs/API_DOCUMENT.md) - 完整的REST API接口文档
- [WebSocket文档](./docs/WEBSOCKET_DOCUMENT.md) - WebSocket实时通信文档
- [部署指南](./docs/DEPLOYMENT.md) - 详细的部署和运行指南
- [更新日志](./CHANGELOG.md) - 版本更新历史

## 许可证

MIT License
