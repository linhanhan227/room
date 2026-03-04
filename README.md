# Chat Room - 实时聊天室应用

基于 Spring Boot 3.x 的实时聊天室应用，支持 WebSocket 实时通信、JWT 认证、敏感词过滤、聊天室管理、用户管理、公告、举报等功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.x | 核心框架 |
| Spring Security | - | 安全认证 |
| Spring WebSocket (STOMP) | - | 实时通信 |
| Spring Data JPA | - | 数据持久化 |
| MySQL | 8.x | 关系型数据库 |
| JWT (jjwt) | 0.12.5 | Token 认证 |
| Lombok | - | 代码简化 |
| HikariCP | - | 数据库连接池 |
| Maven | 3.6+ | 构建工具 |

## 功能特性

### 核心功能
- **用户系统**: 注册 / 登录 / 登出（JWT Token 认证）
- **聊天室**: 创建 / 加入 / 退出 / 删除，支持公开 / 私密 / 群组类型
- **实时消息**: 基于 WebSocket + STOMP 协议的实时消息收发
- **在线状态**: 用户在线 / 离线 / 忙碌 / 离开状态实时同步
- **消息持久化**: 聊天消息数据库存储，支持历史消息查询和分页加载
- **邮箱验证**: 注册邮箱验证码验证（含频率限制和每日上限）

### 管理功能
- **用户管理**: 封禁 / 解封用户、设置用户角色
- **聊天室成员管理**:
  - 禁言 / 解禁成员（支持定时禁言，到期自动解除）
  - 踢出成员
  - 拉黑 / 解除拉黑成员
  - 成员角色管理（所有者 / 管理员 / 普通成员）
- **敏感词过滤**: 支持 KMP / Trie / AC 自动机三种算法，可配置启用 / 禁用
- **举报处理**: 提交举报、按状态和类型查询、管理员处理
- **公告管理**: 发布公告、已读状态跟踪
- **系统日志**: 操作日志记录和查询
- **管理仪表盘**: 用户数、聊天室数、消息数等统计概览

### 聊天室推荐算法
- **活跃度推荐** (Activity-Based): 基于最近消息活跃度
- **热度推荐** (Popularity-Based): 基于成员数量
- **最新推荐** (Newest): 最新创建的聊天室
- **随机推荐** (Random): 随机推荐
- **混合推荐** (Hybrid): 加权组合多种策略（可配置权重）

### 权限管理

#### 全局用户角色

| 角色 | 说明 | 权限 |
|------|------|------|
| `USER` | 普通用户 | 基础聊天功能 |
| `ADMIN` | 系统管理员 | 用户管理、敏感词管理、举报处理、公告管理、所有聊天室完全管理权限 |

#### 聊天室成员角色

| 角色 | 说明 | 权限 |
|------|------|------|
| `OWNER` | 所有者 | 完全控制聊天室：删除聊天室、设置管理员、禁言/踢出/拉黑成员（除系统管理员） |
| `ADMIN` | 管理员 | 禁言/踢出/拉黑普通成员 |
| `MEMBER` | 成员 | 基础聊天功能 |

#### 权限优先级
1. **系统管理员 (`ADMIN`)** — 最高优先级，不可被禁言/踢出/拉黑
2. **聊天室所有者 (`OWNER`)** — 不可被禁言/踢出/拉黑
3. **聊天室管理员** — 可管理普通成员
4. **普通成员** — 基础权限

### 安全特性
- JWT Token 认证，可配置过期时间
- BCrypt 密码加密存储
- CORS 跨域配置（使用 `allowedOriginPatterns` 兼容凭证）
- 接口权限控制（基于 Spring Security）
- WebSocket 连接 JWT 鉴权
- 邮件发送频率限制 + 每日上限

## 项目结构

```
src/main/java/com/chat/room/
├── RoomApplication.java            # 启动类
├── config/                          # 配置类
│   ├── AppProperties.java           # 应用自定义配置属性
│   ├── CorsConfig.java              # CORS 跨域配置
│   └── WebSocketConfig.java         # WebSocket + STOMP 配置
├── controller/                      # 控制器层 (REST API)
│   ├── AuthController.java          # 认证接口 (注册/登录/登出)
│   ├── UserController.java          # 用户接口
│   ├── ChatRoomController.java      # 聊天室接口
│   ├── MessageController.java       # 消息接口
│   ├── AdminController.java         # 管理员接口
│   ├── RoomMemberManagementController.java # 成员管理接口
│   ├── AnnouncementController.java  # 公告接口
│   ├── ReportController.java        # 举报接口
│   ├── SensitiveWordController.java # 敏感词管理接口
│   └── EmailVerificationController.java # 邮箱验证接口
├── service/                         # 服务层 (业务逻辑)
│   ├── UserService.java             # 用户服务
│   ├── ChatRoomService.java         # 聊天室服务
│   ├── MessageService.java          # 消息服务
│   ├── AdminService.java            # 管理服务
│   ├── RoomMemberManagementService.java # 成员管理服务
│   ├── AnnouncementService.java     # 公告服务
│   ├── ReportService.java           # 举报服务
│   ├── SensitiveWordService.java    # 敏感词过滤服务
│   ├── EmailService.java            # 邮件发送服务
│   ├── EmailVerificationService.java # 邮箱验证服务
│   └── recommendation/              # 推荐算法
│       ├── RoomRecommendationService.java
│       ├── RoomRecommendationStrategy.java      # 策略接口
│       ├── ActivityBasedRecommendationStrategy.java
│       ├── PopularityBasedRecommendationStrategy.java
│       ├── NewestRecommendationStrategy.java
│       ├── RandomRecommendationStrategy.java
│       └── HybridRecommendationStrategy.java
├── repository/                      # 数据访问层 (Spring Data JPA)
│   ├── UserRepository.java
│   ├── ChatRoomRepository.java
│   ├── MessageRepository.java
│   ├── RoomMemberRepository.java
│   ├── RoomBlacklistRepository.java
│   ├── BannedUserRepository.java
│   ├── AnnouncementRepository.java
│   ├── AnnouncementReadRepository.java
│   ├── ReportRepository.java
│   ├── SystemLogRepository.java
│   ├── EmailVerificationRepository.java
│   └── EmailSendLogRepository.java
├── entity/                          # JPA 实体类
│   ├── BaseEntity.java              # 基础实体 (createdAt, updatedAt)
│   ├── User.java                    # 用户
│   ├── ChatRoom.java                # 聊天室
│   ├── Message.java                 # 消息
│   ├── RoomMember.java              # 聊天室成员 (含角色/禁言状态)
│   ├── RoomMemberId.java            # 成员复合主键
│   ├── RoomBlacklist.java           # 聊天室黑名单
│   ├── BannedUser.java              # 系统封禁用户
│   ├── Announcement.java            # 公告
│   ├── AnnouncementRead.java        # 公告已读记录
│   ├── Report.java                  # 举报
│   ├── SystemLog.java               # 系统日志
│   ├── EmailVerification.java       # 邮箱验证码
│   └── EmailSendLog.java            # 邮件发送日志
├── dto/                             # 数据传输对象
│   ├── ApiResponse.java             # 统一响应格式
│   ├── UserDTO.java
│   ├── ChatRoomDTO.java
│   ├── MessageDTO.java
│   ├── DashboardStats.java          # 仪表盘统计
│   └── ...                          # 其他请求/响应 DTO
├── security/                        # 安全相关
│   ├── SecurityConfig.java          # Spring Security 配置
│   ├── JwtTokenProvider.java        # JWT 工具类
│   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   ├── WebSocketAuthChannelInterceptor.java # WebSocket JWT 鉴权
│   ├── UserPrincipal.java           # 用户认证主体
│   └── CustomUserDetailsService.java
├── util/                            # 工具类 (敏感词过滤算法)
│   ├── ACSensitiveWordFilter.java   # AC 自动机算法 (推荐)
│   ├── TrieSensitiveWordFilter.java # Trie 前缀树算法
│   ├── KMPSensitiveWordFilter.java  # KMP 算法
│   ├── DFASensitiveWordFilter.java  # DFA 算法
│   └── SensitiveWordLoader.java     # 敏感词文件加载器
├── websocket/                       # WebSocket 处理
│   ├── WebSocketChatHandler.java    # STOMP 消息处理器
│   └── WebSocketEventListener.java  # 连接/断开事件监听
└── exception/                       # 异常处理
    ├── GlobalExceptionHandler.java  # 全局异常处理器
    ├── BusinessException.java       # 业务异常 (400)
    ├── ResourceNotFoundException.java # 资源未找到 (404)
    └── ForbiddenException.java      # 权限不足 (403)
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

**1. 克隆项目**
```bash
git clone <repository-url>
cd room
```

**2. 创建数据库**
```sql
CREATE DATABASE chat_room CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**3. 初始化数据库表结构**
```bash
mysql -u root -p chat_room < src/main/resources/db/database.sql
```

**4. 修改配置文件**

编辑 `src/main/resources/application.yaml`，修改以下关键配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chat_room?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: your_username     # 修改为你的数据库用户名
    password: your_password     # 修改为你的数据库密码

  mail:
    host: smtp.example.com      # 修改为实际 SMTP 服务器
    username: your-email        # 修改为实际邮箱
    password: your-password     # 修改为实际密码/授权码

app:
  jwt:
    secret: your-256-bit-secret-key  # 修改为自定义密钥（至少256位）
  email:
    from: noreply@yourdomain.com     # 修改为实际发件地址
```

**5. 创建敏感词文件**

在项目根目录创建 `sensitive_words.txt` 文件，每行一个敏感词。

**6. 构建运行**
```bash
# 编译打包（跳过测试）
mvn clean package -DskipTests

# 运行
java -jar target/chatroom.jar
```

服务将在 `http://localhost:8080/api` 启动。

### 快速部署命令

```bash
# Windows PowerShell
Start-Service -Name MySQL80
mysql -u root -p chat_room < src/main/resources/db/database.sql
mvn clean package -DskipTests
java -jar target/chatroom.jar

# Linux
sudo systemctl start mysql
mysql -u root -p chat_room < src/main/resources/db/database.sql
mvn clean package -DskipTests
java -jar target/chatroom.jar
```

## API 接口

详细接口文档请参考 [API_DOCUMENT.md](./docs/API_DOCUMENT.md)

### 主要接口概览

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/auth/*` | 注册、登录、登出 |
| 用户 | `/users/*` | 用户信息查询和修改 |
| 聊天室 | `/rooms/*` | 聊天室 CRUD、加入退出 |
| 成员管理 | `/rooms/{roomId}/members/*` | 禁言、踢出、拉黑、角色管理 |
| 消息 | `/messages/*` | 消息发送和历史查询 |
| 管理 | `/admin/*` | 用户管理、敏感词、举报处理、仪表盘 |
| 公告 | `/announcements/*` | 公告发布和已读状态 |
| 举报 | `/reports/*` | 举报提交和处理 |
| 邮箱验证 | `/email-verification/*` | 验证码发送和验证 |
| WebSocket | `/ws` | 实时消息通信 (STOMP) |

### 成员管理接口

| 接口 | 方法 | 说明 | 权限要求 |
|------|------|------|---------|
| `/rooms/{id}/members/mute` | POST | 禁言成员 | 所有者/管理员/系统管理员 |
| `/rooms/{id}/members/{userId}/unmute` | POST | 解除禁言 | 所有者/管理员/系统管理员 |
| `/rooms/{id}/members/{userId}` | DELETE | 踢出成员 | 所有者/管理员/系统管理员 |
| `/rooms/{id}/members/blacklist` | POST | 拉黑成员 | 所有者/管理员/系统管理员 |
| `/rooms/{id}/members/blacklist/{userId}` | DELETE | 解除拉黑 | 所有者/管理员/系统管理员 |
| `/rooms/{id}/members/blacklist` | GET | 获取黑名单列表 | 所有者/管理员/系统管理员 |
| `/rooms/{id}/members` | GET | 获取成员列表 | 聊天室成员 |
| `/rooms/{id}/members/{userId}/role` | PUT | 设置成员角色 | 所有者/系统管理员 |

### WebSocket 使用

**连接地址**: `ws://localhost:8080/api/ws`

**订阅主题**:
| 主题 | 说明 |
|------|------|
| `/topic/room.{roomId}` | 聊天室消息 |
| `/topic/user.{userId}` | 私信通知 |
| `/topic/user.status` | 用户状态变更 |

## 配置说明

所有自定义配置位于 `application.yaml` 的 `app` 前缀下：

### JWT 配置
```yaml
app.jwt:
  secret: your-secret-key     # JWT 密钥（至少256位）
  expiration: 86400000         # 过期时间（毫秒），默认24小时
  header: Authorization        # Token 请求头
  prefix: "Bearer "            # Token 前缀
```

### 邮件配置
```yaml
app.email:
  from: noreply@example.com    # 发件人地址
  verification:
    code-expiration: 300       # 验证码有效期（秒）
    code-length: 6             # 验证码长度
    max-send-per-hour: 5       # 每小时最大发送次数
    send-interval-seconds: 60  # 发送间隔（秒）
  daily-limit:
    enabled: true              # 每日限制开关
    max-count: 10              # 每日最大发送次数
```

### 敏感词配置
```yaml
app.sensitive-word:
  file-path: sensitive_words.txt  # 敏感词文件路径
  enabled: true                    # 启用/禁用开关
  algorithm: AC                    # 算法: KMP / TRIE / AC（推荐）
  replacement: "***"               # 替换文本
  auto-reload: true                # 自动重载
  reload-interval: 300000          # 重载间隔（毫秒）
```

### CORS 配置
```yaml
app.cors:
  allowed-origin-patterns: ["*"]   # 允许的来源模式（兼容 credentials）
  allowed-methods: [GET, POST, PUT, DELETE, OPTIONS, PATCH]
  allowed-headers: ["*"]
  allow-credentials: true
  max-age: 3600
```

### 推荐算法配置
```yaml
app.room-recommendation:
  enabled: true                    # 启用推荐
  default-strategy: HYBRID         # ACTIVITY / POPULARITY / NEWEST / RANDOM / HYBRID
  recommendation-limit: 10         # 推荐数量
  hybrid-weights:                  # 混合策略权重
    activity-weight: 0.3
    popularity-weight: 0.3
    newest-weight: 0.2
    random-weight: 0.2
```

## 敏感词过滤算法

| 算法 | 配置值 | 时间复杂度 | 适用场景 |
|------|--------|-----------|---------|
| KMP | `KMP` | O(n+m) | 少量敏感词，单模式匹配 |
| Trie 前缀树 | `TRIE` | O(n×m) | 大量敏感词，前缀匹配 |
| AC 自动机 | `AC` | O(n) | 大量敏感词，多模式匹配（**推荐**） |

## 开发指南

### 运行测试
```bash
# 全部测试
mvn test

# 指定测试类
mvn test -Dtest=ChatRoomServiceTest

# 编译检查
mvn compile test-compile
```

### 代码规范
- 使用 Lombok 简化 getter/setter/builder
- 遵循 RESTful API 设计规范
- 统一异常处理（`GlobalExceptionHandler`）
- 统一响应格式（`ApiResponse`）
- 业务异常使用 `BusinessException`，禁止直接抛出 `RuntimeException`
- 聊天室成员关系统一通过 `RoomMember` 实体管理

### 数据库版本管理
- 数据库脚本位于 `src/main/resources/db/`
- 当前版本：`database.sql`（v1.2.0，含完整版本历史）

## 文档

| 文档 | 说明 |
|------|------|
| [API 接口文档](./docs/API_DOCUMENT.md) | 完整的 REST API 接口文档 |
| [WebSocket 文档](./docs/WEBSOCKET_DOCUMENT.md) | WebSocket 实时通信文档 |
| [Bug 修复报告](./docs/BUGFIX_REPORT.md) | v1.1.1 Bug 修复详细报告 |
| [更新日志](./CHANGELOG.md) | 版本更新历史 |

## 更新日志

详细更新日志请参考 [CHANGELOG.md](./CHANGELOG.md)

### 最新版本 v1.1.1 (2026-03-04)
- 修复 14 个 Bug（含 2 个严重级别）
- 优化数据库查询性能
- 修复 ORM 映射冲突
- 修复数据完整性问题

## 许可证

MIT License

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request
