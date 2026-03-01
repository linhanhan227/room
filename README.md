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

### 管理功能
- 用户管理（封禁/解封/角色设置）
- 敏感词过滤（KMP/Trie/AC自动机算法）
- 举报处理
- 公告管理
- 系统日志

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
│   └── EmailVerificationController.java # 邮箱验证接口
├── service/                    # 服务层
├── repository/                 # 数据访问层
├── entity/                     # 实体类
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

jwt:
  secret: your-256-bit-secret-key-for-jwt-token-generation-must-be-long-enough

  mail:
    host: smtp.example.com
    username: your-email@example.com
    password: your-email-password
```

4. **初始化数据库**

执行 `src/main/resources/db/init.sql` 脚本初始化数据库表结构。

5. **创建敏感词文件**

在项目根目录创建 `sensitive_words.txt` 文件，每行一个敏感词。

6. **构建运行**
```bash
mvn clean package
java -jar target/chatroom.jar
```

服务将在 `http://localhost:8080/api` 启动。

## API 接口

详细API文档请参考 [API_DOCUMENT.md](./API_DOCUMENT.md)

### 主要接口

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/auth/*` | 注册、登录、Token刷新 |
| 用户 | `/users/*` | 用户信息、个人设置 |
| 聊天室 | `/rooms/*` | 聊天室CRUD、加入退出 |
| 消息 | `/messages/*` | 消息历史、发送 |
| 管理 | `/admin/*` | 用户管理、敏感词、举报处理 |
| 公告 | `/announcements/*` | 公告发布、已读状态 |
| WebSocket | `/ws` | 实时消息通信 |

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

| 角色 | 权限 |
|------|------|
| USER | 基础聊天功能 |
| ADMIN | 用户管理、敏感词管理、举报处理、公告管理 |

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

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request
