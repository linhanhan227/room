# WebSocket 实时通信文档

## 目录

1. [概述](#1-概述)
2. [连接配置](#2-连接配置)
3. [认证机制](#3-认证机制)
4. [消息格式](#4-消息格式)
5. [订阅主题](#5-订阅主题)
6. [消息发送](#6-消息发送)
7. [心跳机制](#7-心跳机制)
8. [事件通知](#8-事件通知)
9. [错误处理](#9-错误处理)
10. [客户端示例](#10-客户端示例)
11. [最佳实践](#11-最佳实践)
12. [附录](#附录)

---

## 1. 概述

### 1.1 技术栈

本项目使用 **STOMP** (Simple Text Oriented Messaging Protocol) over WebSocket 进行实时通信。

**核心技术**:
- Spring WebSocket
- STOMP 协议
- SockJS (可选，用于降级支持)

### 1.2 为什么选择 STOMP

STOMP 提供了比原生 WebSocket 更丰富的功能：

| 特性 | 原生 WebSocket | STOMP |
|------|---------------|-------|
| 消息格式 | 自定义 | 标准化 |
| 订阅/发布 | 需自行实现 | 内置支持 |
| 认证 | 需自行实现 | 支持握手认证 |
| 错误处理 | 基础 | 完善的错误帧 |
| 心跳 | 需自行实现 | 内置心跳机制 |

### 1.3 架构图

```
┌─────────────┐      WebSocket      ┌─────────────────┐
│   Client    │◄──────────────────►│   Spring Boot   │
│  (Browser)  │    STOMP Protocol  │    Server       │
└─────────────┘                     └─────────────────┘
       │                                   │
       │ Subscribe                         │
       │ /topic/room.{id}                  │
       ▼                                   ▼
┌─────────────┐                     ┌─────────────────┐
│   Topic     │                     │   Message       │
│   Broker    │◄────────────────────│   Handler       │
└─────────────┘                     └─────────────────┘
```

---

## 2. 连接配置

### 2.1 连接端点

| 环境 | 端点地址 |
|------|----------|
| 开发环境 | `ws://localhost:8080/ws` |
| 生产环境 | `wss://your-domain.com/ws` |

### 2.2 连接参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| heartbeatInterval | Long | 30000 | 心跳间隔（毫秒） |
| heartbeatTimeout | Long | 90000 | 心跳超时（毫秒） |
| messageSizeLimit | Integer | 131072 | 消息大小限制（字节，128KB） |
| sendBufferSizeLimit | Integer | 524288 | 发送缓冲区大小（字节，512KB） |
| sendTimeLimit | Integer | 20000 | 发送超时（毫秒） |
| timeToFirstMessage | Integer | 30000 | 首条消息超时（毫秒） |

**配置方式**：

在 `application.yml` 或 `application.properties` 中配置：

```yaml
app:
  websocket:
    endpoint: "/ws"
    heartbeat-interval: 30000
    heartbeat-timeout: 90000
    message-size-limit: 131072
    send-buffer-size-limit: 524288
    send-time-limit: 20000
    time-to-first-message: 30000
    allowed-origins: "*"
```

### 2.3 SockJS 降级支持

服务器同时支持原生 WebSocket 和 SockJS 降级：

```javascript
// 原生 WebSocket
const socket = new WebSocket('ws://localhost:8080/ws');

// SockJS 降级
const socket = new SockJS('http://localhost:8080/ws');
```

**SockJS 降级策略**:
1. 首选 WebSocket
2. HTTP Streaming
3. HTTP Long Polling

### 2.4 跨域配置

允许的源（Allowed Origins）通过配置文件设置：

```yaml
app:
  websocket:
    allowed-origins: "*"
```

---

## 3. 认证机制

### 3.1 连接认证

WebSocket 连接时需要在 STOMP CONNECT 帧中携带 JWT Token：

```javascript
const headers = {
    'Authorization': 'Bearer ' + jwtToken
};

stompClient.connect(headers, function(frame) {
    console.log('Connected: ' + frame);
}, function(error) {
    console.log('Error: ' + error);
});
```

### 3.2 认证流程

```
┌─────────┐                    ┌─────────────┐
│  Client │                    │   Server    │
└────┬────┘                    └──────┬──────┘
     │                                │
     │  1. HTTP Login                 │
     │───────────────────────────────►│
     │                                │
     │  2. JWT Token                  │
     │◄───────────────────────────────│
     │                                │
     │  3. WebSocket Connect          │
     │  + Authorization Header        │
     │───────────────────────────────►│
     │                                │
     │  4. Validate Token             │
     │                                ├───►
     │                                │
     │  5. Connected                  │
     │◄───────────────────────────────│
     │                                │
```

### 3.3 Token 验证

服务器端验证流程：

1. 从 STOMP CONNECT 帧获取 `Authorization` 头
2. 提取 Bearer Token（去掉 "Bearer " 前缀）
3. 验证 Token 有效性
4. 从 Token 解析用户名
5. 从数据库查询用户信息
6. 创建 UserPrincipal 并设置到 Session

**服务器代码**：

```java
@Override
public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
        String token = accessor.getFirstNativeHeader("Authorization");

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);

            if (tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null) {
                    UserPrincipal principal = UserPrincipal.create(user);
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
                    accessor.setUser(auth);
                }
            }
        }
    }

    return message;
}
```

**认证拦截器**：

- 拦截器：`WebSocketAuthChannelInterceptor`
- 拦截时机：CONNECT 命令
- 认证方式：JWT Bearer Token
- 用户信息存储：Spring Security Authentication

### 3.4 认证失败

认证失败时，服务器将拒绝连接：

```json
{
    "command": "ERROR",
    "headers": {
        "message": "Unauthorized"
    },
    "body": "Invalid or expired token"
}
```

---

## 4. 消息格式

### 4.1 STOMP 帧格式

STOMP 协议使用帧（Frame）进行通信：

```
COMMAND
header1:value1
header2:value2

Body^@
```

### 4.2 消息类型

#### 4.2.1 文本消息

```json
{
    "id": 1,
    "roomId": 100,
    "senderId": 123,
    "senderName": "张三",
    "senderAvatar": "https://example.com/avatar.jpg",
    "content": "大家好！",
    "type": "TEXT",
    "createdAt": "2024-01-20T10:30:00"
}
```

#### 4.2.2 图片消息

```json
{
    "id": 2,
    "roomId": 100,
    "senderId": 123,
    "senderName": "张三",
    "content": "https://example.com/images/photo.jpg",
    "type": "IMAGE",
    "createdAt": "2024-01-20T10:31:00"
}
```

#### 4.2.3 系统消息

```json
{
    "id": null,
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "content": "张三 加入了聊天室",
    "type": "SYSTEM",
    "createdAt": "2024-01-20T10:32:00"
}
```

#### 4.2.4 表情消息

```json
{
    "id": 3,
    "roomId": 100,
    "senderId": 123,
    "senderName": "张三",
    "content": "😀",
    "type": "EMOJI",
    "createdAt": "2024-01-20T10:33:00"
}
```

### 4.3 消息类型枚举

| 类型 | 说明 |
|------|------|
| TEXT | 文本消息 |
| IMAGE | 图片消息 |
| FILE | 文件消息 |
| SYSTEM | 系统消息 |
| EMOJI | 表情消息 |

---

## 5. 订阅主题

### 5.1 主题前缀说明

| 前缀 | 说明 | 用途 |
|------|------|------|
| `/topic` | 广播主题 | 多用户订阅，消息广播给所有订阅者 |
| `/queue` | 点对点队列 | 单用户订阅，消息只发送给特定用户 |
| `/user` | 用户专属 | 用户私有消息 |

### 5.2 聊天室消息主题

**订阅地址**: `/topic/room.{roomId}`

**用途**: 接收聊天室内的所有消息

**示例**:
```javascript
stompClient.subscribe('/topic/room.100', function(message) {
    const msg = JSON.parse(message.body);
    console.log('收到消息:', msg);
});
```

**消息内容**:
```json
{
    "id": 1,
    "roomId": 100,
    "senderId": 123,
    "senderName": "张三",
    "content": "大家好！",
    "type": "TEXT",
    "createdAt": "2024-01-20T10:30:00"
}
```

---

### 5.3 输入状态主题

**订阅地址**: `/topic/room.{roomId}.typing`

**用途**: 显示用户正在输入的状态

**示例**:
```javascript
stompClient.subscribe('/topic/room.100.typing', function(message) {
    const data = JSON.parse(message.body);
    console.log(data.username + ' 正在输入...');
});
```

**消息内容**:
```json
{
    "userId": 123,
    "username": "张三",
    "typing": true
}
```

---

### 5.4 用户状态主题

**订阅地址**: `/topic/user.status`

**用途**: 监听用户在线状态变化

**示例**:
```javascript
stompClient.subscribe('/topic/user.status', function(message) {
    const data = JSON.parse(message.body);
    console.log('用户 ' + data.username + ' 状态: ' + data.status);
});
```

**消息内容**:
```json
{
    "userId": 123,
    "username": "张三",
    "status": "ONLINE"
}
```

**状态类型**:

| 状态 | 说明 |
|------|------|
| ONLINE | 在线 |
| OFFLINE | 离线 |
| BUSY | 忙碌 |
| AWAY | 离开 |

---

### 5.5 用户私有消息主题

**订阅地址**: `/user/queue/private`

**用途**: 接收私有消息（如系统通知、私信等）

**示例**:
```javascript
stompClient.subscribe('/user/queue/private', function(message) {
    const notification = JSON.parse(message.body);
    console.log('收到私有消息:', notification);
});
```

---

## 6. 消息发送

### 6.1 发送聊天消息

**目标地址**: `/app/chat.send.{roomId}`

**请求体**:
```json
{
    "roomId": 100,
    "content": "大家好！",
    "type": "TEXT"
}
```

**示例**:
```javascript
stompClient.send('/app/chat.send.100', {}, JSON.stringify({
    roomId: 100,
    content: '大家好！',
    type: 'TEXT'
}));
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| content | String | 是 | 消息内容 |
| type | String | 否 | 消息类型，默认 TEXT |

**服务器处理流程**：

1. 验证用户身份（从 Session 获取）
2. 检查用户是否在聊天室中
3. 更新用户心跳时间
4. 保存消息到数据库
5. 广播消息到 `/topic/room.{roomId}`
6. 返回消息对象

**错误处理**：

- 未授权：静默失败，记录警告日志
- 不在聊天室：静默失败，记录警告日志
- 消息保存失败：记录错误日志

---

### 6.2 加入聊天室

**目标地址**: `/app/chat.join.{roomId}`

**用途**: 通知其他用户有人加入聊天室

**示例**:
```javascript
stompClient.send('/app/chat.join.100', {}, '');
```

**服务器响应**: 向 `/topic/room.{roomId}` 发送系统消息

```json
{
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "content": "张三 joined the room",
    "type": "SYSTEM"
}
```

---

### 6.3 离开聊天室

**目标地址**: `/app/chat.leave.{roomId}`

**用途**: 通知其他用户有人离开聊天室

**示例**:
```javascript
stompClient.send('/app/chat.leave.100', {}, '');
```

**服务器响应**: 向 `/topic/room.{roomId}` 发送系统消息

```json
{
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "content": "张三 left the room",
    "type": "SYSTEM"
}
```

---

### 6.4 发送输入状态

**目标地址**: `/app/chat.typing.{roomId}`

**用途**: 通知其他用户正在输入

**示例**:
```javascript
// 用户开始输入时
stompClient.send('/app/chat.typing.100', {}, '');
```

**服务器响应**: 向 `/topic/room.{roomId}.typing` 发送输入状态

---

### 6.5 更新用户状态

**目标地址**: `/app/user.status`

**请求体**:
```json
{
    "status": "BUSY"
}
```

**示例**:
```javascript
stompClient.send('/app/user.status', {}, JSON.stringify({
    status: 'BUSY'
}));
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 用户状态（ONLINE/OFFLINE/BUSY/AWAY） |

**服务器处理流程**：

1. 验证用户身份（从 Session 获取）
2. 更新用户心跳时间
3. 更新数据库中的用户状态
4. 向 `/topic/user.status` 广播状态更新

**服务器响应**: 向 `/topic/user.status` 广播状态更新

```json
{
    "userId": 123,
    "status": "BUSY"
}
```

---

## 7. 心跳机制

### 7.1 心跳配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| 心跳间隔 | 30秒 | 客户端/服务器发送心跳的间隔 |
| 心跳超时 | 90秒 | 超过此时间未收到心跳则认为断线 |

### 7.2 心跳流程

```
┌─────────┐                    ┌─────────────┐
│  Client │                    │   Server    │
└────┬────┘                    └──────┬──────┘
     │                                │
     │  Heartbeat (every 30s)         │
     │───────────────────────────────►│
     │                                │
     │  Heartbeat ACK                 │
     │◄───────────────────────────────│
     │                                │
     │  ... (90s no heartbeat)        │
     │                                │
     │                                │  Mark user OFFLINE
     │                                │  Broadcast status
     │                                │
```

### 7.3 客户端心跳

客户端可以发送心跳消息：

**目标地址**: `/app/heartbeat`

**示例**:
```javascript
// 定时发送心跳
setInterval(function() {
    stompClient.send('/app/heartbeat', {}, '');
}, 30000);
```

### 7.4 自动心跳

STOMP 客户端库通常支持自动心跳配置：

```javascript
const client = Stomp.client('ws://localhost:8080/ws');

// 配置心跳
client.heartbeat.outgoing = 30000;  // 客户端发送心跳间隔
client.heartbeat.incoming = 30000;  // 期望服务器心跳间隔
```

### 7.5 心跳超时处理

服务器端自动检测心跳超时：

1. 每60秒检查一次所有用户的心跳时间
2. 超过90秒未收到心跳的用户被标记为离线
3. 向 `/topic/user.status` 广播离线状态

**实现细节**：

```java
@Scheduled(fixedRate = 60000)
public void checkHeartbeats() {
    long currentTime = System.currentTimeMillis();
    long timeout = 90000;
    
    userLastHeartbeat.forEach((userId, lastHeartbeat) -> {
        if (currentTime - lastHeartbeat > timeout) {
            userService.updateUserStatus(userId, User.UserStatus.OFFLINE);
            messagingTemplate.convertAndSend("/topic/user.status", Map.of(
                    "userId", userId,
                    "status", "OFFLINE"
            ));
            userLastHeartbeat.remove(userId);
        }
    });
}
```

**心跳更新时机**：

- 用户发送任何消息时自动更新心跳
- 用户调用 `/app/heartbeat` 时更新心跳
- 用户加入/离开聊天室时更新心跳

---

## 8. 事件通知

### 8.1 连接事件

#### 服务器端连接处理

当用户成功连接 WebSocket 时，服务器执行以下操作：

1. 从 STOMP CONNECT 帧获取用户信息
2. 验证 JWT Token
3. 将用户 ID 存储到 Session 映射
4. 初始化用户心跳时间
5. 更新用户状态为 ONLINE
6. 向 `/topic/user.status` 广播用户上线

**服务器代码**：

```java
@EventListener
public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    
    if (headerAccessor.getUser() != null) {
        String username = headerAccessor.getUser().getName();
        UserDTO user = userService.getUserByUsername(username);
        
        sessionUserMap.put(headerAccessor.getSessionId(), user.getId());
        userLastHeartbeat.put(user.getId(), System.currentTimeMillis());
        
        userService.updateUserStatus(user.getId(), User.UserStatus.ONLINE);
        
        messagingTemplate.convertAndSend("/topic/user.status", Map.of(
                "userId", user.getId(),
                "status", "ONLINE",
                "username", user.getUsername()
        ));
    }
}
```

#### 客户端连接成功

```javascript
stompClient.connect(headers, function(frame) {
    console.log('连接成功:', frame);
    // 订阅主题...
});
```

#### 连接失败

```javascript
stompClient.connect(headers, function(frame) {
    // 成功回调
}, function(error) {
    console.error('连接失败:', error);
    // 处理错误...
});
```

### 8.2 断开事件

#### 服务器端断开处理

当用户断开 WebSocket 连接时，服务器执行以下操作：

1. 从 Session 映射中移除用户
2. 从心跳映射中移除用户
3. 更新用户状态为 OFFLINE
4. 向 `/topic/user.status` 广播用户离线

**服务器代码**：

```java
@EventListener
public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    
    if (headerAccessor.getUser() != null) {
        String username = headerAccessor.getUser().getName();
        UserDTO user = userService.getUserByUsername(username);
        
        sessionUserMap.remove(headerAccessor.getSessionId());
        userLastHeartbeat.remove(user.getId());
        
        userService.updateUserStatus(user.getId(), User.UserStatus.OFFLINE);
        
        messagingTemplate.convertAndSend("/topic/user.status", Map.of(
                "userId", user.getId(),
                "status", "OFFLINE",
                "username", user.getUsername()
        ));
    }
}
```

#### 客户端处理

```javascript
stompClient.disconnect(function() {
    console.log('已断开连接');
});
```

### 8.3 重连机制

建议客户端实现自动重连：

```javascript
function connect() {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect(
        { 'Authorization': 'Bearer ' + token },
        function(frame) {
            console.log('Connected');
            // 重新订阅主题
            subscribeToTopics();
        },
        function(error) {
            console.log('Connection error:', error);
            // 5秒后重连
            setTimeout(connect, 5000);
        }
    );
}
```

---

## 9. 错误处理

### 9.1 错误帧格式

```json
{
    "command": "ERROR",
    "headers": {
        "message": "错误类型",
        "content-type": "application/json"
    },
    "body": "错误详情"
}
```

### 9.2 常见错误

#### 认证失败

```json
{
    "command": "ERROR",
    "headers": {
        "message": "Unauthorized"
    },
    "body": "Invalid or expired token"
}
```

**处理方式**: 重新登录获取新 Token

#### 权限不足

```json
{
    "command": "ERROR",
    "headers": {
        "message": "Forbidden"
    },
    "body": "You are not a member of this room"
}
```

**处理方式**: 提示用户加入聊天室

#### 消息发送失败

```json
{
    "command": "ERROR",
    "headers": {
        "message": "Bad Request"
    },
    "body": "Message content cannot be empty"
}
```

**处理方式**: 检查消息内容

### 9.3 错误处理最佳实践

#### 9.3.1 客户端错误处理

```javascript
class ChatClient {
    constructor(token) {
        this.token = token;
        this.stompClient = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    connect() {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);

        const headers = {
            'Authorization': 'Bearer ' + this.token
        };

        this.stompClient.connect(
            headers,
            this.onConnect.bind(this),
            this.onError.bind(this)
        );
    }

    onConnect(frame) {
        console.log('Connected:', frame);
        this.reconnectAttempts = 0;
        this.subscribeToTopics();
    }

    onError(error) {
        console.error('Connection error:', error);
        
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
            console.log(`Reconnecting in ${delay}ms... (attempt ${this.reconnectAttempts})`);
            setTimeout(() => this.connect(), delay);
        } else {
            console.error('Max reconnection attempts reached');
            this.handleConnectionLost();
        }
    }

    handleConnectionLost() {
        // 显示连接丢失提示
        showNotification('连接已断开，请刷新页面重试', 'error');
        // 禁用发送功能
        disableSendButton();
    }
}

// 全局错误处理
stompClient.onreceive = function(frame) {
    if (frame.command === 'ERROR') {
        handleStompError(frame);
    }
};

function handleStompError(frame) {
    const errorMessage = frame.headers.message;
    const errorBody = frame.body;

    console.error('STOMP Error:', errorMessage, errorBody);

    switch (errorMessage) {
        case 'Unauthorized':
            showNotification('认证失败，请重新登录', 'error');
            redirectToLogin();
            break;
        case 'Forbidden':
            showNotification('权限不足', 'warning');
            break;
        default:
            showNotification('发生错误: ' + errorMessage, 'error');
    }
}
```

#### 9.3.2 错误重试策略

```javascript
class MessageSender {
    constructor(stompClient) {
        this.stompClient = stompClient;
        this.pendingMessages = [];
        this.maxRetries = 3;
    }

    sendWithRetry(destination, body, retries = 0) {
        try {
            this.stompClient.send(destination, {}, body);
            return Promise.resolve();
        } catch (error) {
            if (retries < this.maxRetries) {
                console.log(`Retrying message send (${retries + 1}/${this.maxRetries})`);
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        this.sendWithRetry(destination, body, retries + 1)
                            .then(resolve)
                            .catch(reject);
                    }, 1000 * (retries + 1));
                });
            } else {
                console.error('Failed to send message after retries:', error);
                return Promise.reject(error);
            }
        }
    }

    sendPendingMessages() {
        this.pendingMessages.forEach(msg => {
            this.sendWithRetry(msg.destination, msg.body)
                .catch(err => console.error('Failed to send pending message:', err));
        });
        this.pendingMessages = [];
    }
}
```

#### 9.3.3 网络状态监听

```javascript
class NetworkMonitor {
    constructor(chatClient) {
        this.chatClient = chatClient;
        this.isOnline = navigator.onLine;
        this.initListeners();
    }

    initListeners() {
        window.addEventListener('online', () => {
            console.log('Network is online');
            this.isOnline = true;
            this.chatClient.connect();
        });

        window.addEventListener('offline', () => {
            console.log('Network is offline');
            this.isOnline = false;
            showNotification('网络连接已断开', 'warning');
        });
    }

    checkConnection() {
        if (!this.isOnline) {
            throw new Error('Network is offline');
        }
    }
}
```

#### 9.3.4 消息去重

```javascript
class MessageDeduplicator {
    constructor() {
        this.receivedMessages = new Set();
        this.maxCacheSize = 1000;
    }

    shouldProcess(message) {
        const messageId = message.id;
        
        if (!messageId) {
            return true; // 系统消息没有ID，直接处理
        }

        if (this.receivedMessages.has(messageId)) {
            console.log('Duplicate message ignored:', messageId);
            return false;
        }

        this.receivedMessages.add(messageId);

        // 限制缓存大小
        if (this.receivedMessages.size > this.maxCacheSize) {
            const oldest = this.receivedMessages.values().next().value;
            this.receivedMessages.delete(oldest);
        }

        return true;
    }
}
```

---

## 10. 客户端示例

### 10.1 JavaScript (原生)

```javascript
class ChatClient {
    constructor(token) {
        this.token = token;
        this.stompClient = null;
        this.subscriptions = {};
    }
    
    connect() {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);
        
        const headers = {
            'Authorization': 'Bearer ' + this.token
        };
        
        this.stompClient.connect(
            headers,
            this.onConnect.bind(this),
            this.onError.bind(this)
        );
    }
    
    onConnect(frame) {
        console.log('Connected:', frame);
        this.subscribeToTopics();
    }
    
    onError(error) {
        console.error('Connection error:', error);
        setTimeout(() => this.connect(), 5000);
    }
    
    subscribeToTopics() {
        // 订阅用户状态
        this.subscriptions['userStatus'] = this.stompClient.subscribe(
            '/topic/user.status',
            this.onUserStatus.bind(this)
        );
    }
    
    joinRoom(roomId) {
        // 订阅聊天室消息
        this.subscriptions['room_' + roomId] = this.stompClient.subscribe(
            '/topic/room.' + roomId,
            this.onMessage.bind(this)
        );
        
        // 订阅输入状态
        this.subscriptions['typing_' + roomId] = this.stompClient.subscribe(
            '/topic/room.' + roomId + '.typing',
            this.onTyping.bind(this)
        );
        
        // 发送加入消息
        this.stompClient.send('/app/chat.join.' + roomId, {}, '');
    }
    
    leaveRoom(roomId) {
        // 取消订阅
        if (this.subscriptions['room_' + roomId]) {
            this.subscriptions['room_' + roomId].unsubscribe();
        }
        if (this.subscriptions['typing_' + roomId]) {
            this.subscriptions['typing_' + roomId].unsubscribe();
        }
        
        // 发送离开消息
        this.stompClient.send('/app/chat.leave.' + roomId, {}, '');
    }
    
    sendMessage(roomId, content, type = 'TEXT') {
        this.stompClient.send(
            '/app/chat.send.' + roomId,
            {},
            JSON.stringify({ content, type })
        );
    }
    
    sendTyping(roomId) {
        this.stompClient.send('/app/chat.typing.' + roomId, {}, '');
    }
    
    updateStatus(status) {
        this.stompClient.send(
            '/app/user.status',
            {},
            JSON.stringify({ status })
        );
    }
    
    onMessage(message) {
        const msg = JSON.parse(message.body);
        console.log('Message:', msg);
        // 更新UI...
    }
    
    onTyping(message) {
        const data = JSON.parse(message.body);
        console.log('Typing:', data);
        // 显示输入提示...
    }
    
    onUserStatus(message) {
        const data = JSON.parse(message.body);
        console.log('User status:', data);
        // 更新用户列表...
    }
    
    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// 使用示例
const chatClient = new ChatClient(localStorage.getItem('token'));
chatClient.connect();
```

---

### 10.2 Vue.js 集成

```vue
<template>
  <div class="chat-room">
    <div class="messages">
      <div v-for="msg in messages" :key="msg.id" class="message">
        <span class="sender">{{ msg.senderName }}:</span>
        <span class="content">{{ msg.content }}</span>
      </div>
    </div>
    <div class="typing-indicator" v-if="typingUser">
      {{ typingUser }} 正在输入...
    </div>
    <div class="input-area">
      <input 
        v-model="newMessage" 
        @input="onTyping"
        @keyup.enter="sendMessage"
        placeholder="输入消息..."
      />
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

export default {
  name: 'ChatRoom',
  props: {
    roomId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      stompClient: null,
      messages: [],
      newMessage: '',
      typingUser: null,
      typingTimeout: null
    };
  },
  mounted() {
    this.connect();
  },
  beforeUnmount() {
    this.disconnect();
  },
  methods: {
    connect() {
      const socket = new SockJS('http://localhost:8080/ws');
      this.stompClient = Stomp.over(socket);
      
      const headers = {
        'Authorization': 'Bearer ' + this.$store.state.token
      };
      
      this.stompClient.connect(
        headers,
        this.onConnect,
        this.onError
      );
    },
    
    onConnect() {
      // 订阅聊天室消息
      this.stompClient.subscribe(
        `/topic/room.${this.roomId}`,
        this.onMessage
      );
      
      // 订阅输入状态
      this.stompClient.subscribe(
        `/topic/room.${this.roomId}.typing`,
        this.onTyping
      );
      
      // 加入聊天室
      this.stompClient.send(`/app/chat.join.${this.roomId}`, {}, '');
    },
    
    onError(error) {
      console.error('WebSocket error:', error);
      setTimeout(() => this.connect(), 5000);
    },
    
    disconnect() {
      if (this.stompClient) {
        this.stompClient.send(`/app/chat.leave.${this.roomId}`, {}, '');
        this.stompClient.disconnect();
      }
    },
    
    sendMessage() {
      if (!this.newMessage.trim()) return;
      
      this.stompClient.send(
        `/app/chat.send.${this.roomId}`,
        {},
        JSON.stringify({
          content: this.newMessage,
          type: 'TEXT'
        })
      );
      
      this.newMessage = '';
    },
    
    onTyping(message) {
      const data = JSON.parse(message.body);
      this.typingUser = data.username;
      
      clearTimeout(this.typingTimeout);
      this.typingTimeout = setTimeout(() => {
        this.typingUser = null;
      }, 3000);
    },
    
    onMessage(message) {
      const msg = JSON.parse(message.body);
      this.messages.push(msg);
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },
    
    scrollToBottom() {
      const container = this.$el.querySelector('.messages');
      container.scrollTop = container.scrollHeight;
    }
  }
};
</script>
```

---

### 10.3 React 集成

```jsx
import React, { useEffect, useState, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function useWebSocket(token, roomId) {
  const [messages, setMessages] = useState([]);
  const [typingUser, setTypingUser] = useState(null);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);
  const typingTimeoutRef = useRef(null);
  
  const connect = useCallback(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect(
      { 'Authorization': `Bearer ${token}` },
      () => {
        setConnected(true);
        clientRef.current = stompClient;
        
        // 订阅消息
        stompClient.subscribe(`/topic/room.${roomId}`, (message) => {
          const msg = JSON.parse(message.body);
          setMessages(prev => [...prev, msg]);
        });
        
        // 订阅输入状态
        stompClient.subscribe(`/topic/room.${roomId}.typing`, (message) => {
          const data = JSON.parse(message.body);
          setTypingUser(data.username);
          
          clearTimeout(typingTimeoutRef.current);
          typingTimeoutRef.current = setTimeout(() => {
            setTypingUser(null);
          }, 3000);
        });
        
        // 加入聊天室
        stompClient.send(`/app/chat.join.${roomId}`, {}, '');
      },
      (error) => {
        console.error('Connection error:', error);
        setConnected(false);
        setTimeout(connect, 5000);
      }
    );
  }, [token, roomId]);
  
  const sendMessage = useCallback((content) => {
    if (clientRef.current && content.trim()) {
      clientRef.current.send(
        `/app/chat.send.${roomId}`,
        {},
        JSON.stringify({ content, type: 'TEXT' })
      );
    }
  }, [roomId]);
  
  const sendTyping = useCallback(() => {
    if (clientRef.current) {
      clientRef.current.send(`/app/chat.typing.${roomId}`, {}, '');
    }
  }, [roomId]);
  
  const disconnect = useCallback(() => {
    if (clientRef.current) {
      clientRef.current.send(`/app/chat.leave.${roomId}`, {}, '');
      clientRef.current.disconnect();
    }
  }, [roomId]);
  
  useEffect(() => {
    connect();
    return () => disconnect();
  }, [connect, disconnect]);
  
  return {
    messages,
    typingUser,
    connected,
    sendMessage,
    sendTyping,
    disconnect
  };
}

function ChatRoom({ token, roomId }) {
  const [input, setInput] = useState('');
  const { messages, typingUser, connected, sendMessage, sendTyping } = useWebSocket(token, roomId);
  
  const handleSubmit = (e) => {
    e.preventDefault();
    sendMessage(input);
    setInput('');
  };
  
  const handleInput = (e) => {
    setInput(e.target.value);
    sendTyping();
  };
  
  return (
    <div className="chat-room">
      <div className="status">
        {connected ? '🟢 已连接' : '🔴 未连接'}
      </div>
      
      <div className="messages">
        {messages.map((msg, index) => (
          <div key={index} className="message">
            <strong>{msg.senderName}:</strong> {msg.content}
          </div>
        ))}
      </div>
      
      {typingUser && (
        <div className="typing">{typingUser} 正在输入...</div>
      )}
      
      <form onSubmit={handleSubmit}>
        <input
          value={input}
          onChange={handleInput}
          placeholder="输入消息..."
        />
        <button type="submit">发送</button>
      </form>
    </div>
  );
}

export default ChatRoom;
```

---

## 11. 最佳实践

### 11.1 连接管理

1. **单例模式**: 整个应用只维护一个 WebSocket 连接
2. **自动重连**: 实现断线自动重连机制
3. **心跳保活**: 配置合理的心跳间隔
4. **优雅断开**: 页面卸载时主动断开连接

### 11.2 订阅管理

1. **按需订阅**: 只订阅当前需要的主题
2. **及时取消**: 离开页面时取消订阅
3. **避免重复**: 检查是否已订阅再订阅

### 11.3 消息处理

1. **消息去重**: 使用消息ID进行去重
2. **消息排序**: 按时间戳排序显示
3. **消息缓存**: 本地缓存历史消息
4. **错误处理**: 处理消息发送失败的情况

### 11.4 性能优化

1. **消息节流**: 输入状态使用节流发送
2. **虚拟列表**: 大量消息使用虚拟滚动
3. **懒加载**: 历史消息分页加载
4. **压缩传输**: 大消息使用压缩

### 11.5 安全建议

1. **Token 刷新**: Token 过期前主动刷新
2. **敏感数据**: 不在 WebSocket 中传输敏感信息
3. **输入验证**: 客户端和服务端都要验证消息内容
4. **限流控制**: 防止消息刷屏

### 11.6 性能优化详细方案

#### 11.6.1 消息节流

```javascript
class TypingThrottler {
    constructor(stompClient, roomId) {
        this.stompClient = stompClient;
        this.roomId = roomId;
        this.lastSent = 0;
        this.throttleDelay = 1000; // 1秒内只发送一次
    }

    sendTyping() {
        const now = Date.now();
        if (now - this.lastSent > this.throttleDelay) {
            this.stompClient.send(`/app/chat.typing.${this.roomId}`, {}, '');
            this.lastSent = now;
        }
    }
}
```

#### 11.6.2 虚拟列表

```javascript
class VirtualList {
    constructor(container, itemHeight, buffer = 5) {
        this.container = container;
        this.itemHeight = itemHeight;
        this.buffer = buffer;
        this.visibleStart = 0;
        this.visibleEnd = 0;
        this.init();
    }

    init() {
        this.container.addEventListener('scroll', () => {
            this.updateVisibleRange();
        });
        this.updateVisibleRange();
    }

    updateVisibleRange() {
        const scrollTop = this.container.scrollTop;
        const containerHeight = this.container.clientHeight;
        
        this.visibleStart = Math.max(0, Math.floor(scrollTop / this.itemHeight) - this.buffer);
        this.visibleEnd = Math.min(
            this.totalItems - 1,
            Math.ceil((scrollTop + containerHeight) / this.itemHeight) + this.buffer
        );
        
        this.render();
    }

    render() {
        const visibleItems = this.items.slice(this.visibleStart, this.visibleEnd + 1);
        this.container.innerHTML = visibleItems.map(item => this.renderItem(item)).join('');
    }
}
```

#### 11.6.3 消息分页加载

```javascript
class MessagePagination {
    constructor(messageService, pageSize = 50) {
        this.messageService = messageService;
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.hasMore = true;
        this.loading = false;
    }

    async loadMore(roomId) {
        if (this.loading || !this.hasMore) return;

        this.loading = true;
        try {
            const messages = await this.messageService.getMessages(
                roomId,
                this.currentPage * this.pageSize,
                this.pageSize
            );

            if (messages.length < this.pageSize) {
                this.hasMore = false;
            }

            this.currentPage++;
            return messages;
        } finally {
            this.loading = false;
        }
    }

    reset() {
        this.currentPage = 0;
        this.hasMore = true;
        this.loading = false;
    }
}
```

### 11.7 调试技巧

#### 11.7.1 启用STOMP调试

```javascript
const stompClient = Stomp.over(socket);
stompClient.debug = function(str) {
    console.log('STOMP:', str);
};
```

#### 11.7.2 消息追踪

```javascript
class MessageTracker {
    constructor() {
        this.sentMessages = new Map();
        this.receivedMessages = new Map();
    }

    trackSent(message) {
        const id = this.generateId();
        this.sentMessages.set(id, {
            message,
            timestamp: Date.now(),
            status: 'sent'
        });
        return id;
    }

    trackReceived(message) {
        const id = message.id;
        if (this.sentMessages.has(id)) {
            const sent = this.sentMessages.get(id);
            sent.status = 'delivered';
            sent.deliveryTime = Date.now() - sent.timestamp;
        }
        this.receivedMessages.set(id, message);
    }

    generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }

    getStats() {
        return {
            sent: this.sentMessages.size,
            received: this.receivedMessages.size,
            avgDeliveryTime: this.calculateAvgDeliveryTime()
        };
    }

    calculateAvgDeliveryTime() {
        const delivered = Array.from(this.sentMessages.values())
            .filter(msg => msg.status === 'delivered');
        
        if (delivered.length === 0) return 0;
        
        const totalTime = delivered.reduce((sum, msg) => sum + msg.deliveryTime, 0);
        return totalTime / delivered.length;
    }
}
```

### 11.8 测试策略

#### 11.8.1 单元测试

```javascript
describe('ChatClient', () => {
    let chatClient;
    let mockStompClient;

    beforeEach(() => {
        mockStompClient = {
            connect: jest.fn(),
            send: jest.fn(),
            subscribe: jest.fn(),
            disconnect: jest.fn()
        };
        chatClient = new ChatClient('test-token');
        chatClient.stompClient = mockStompClient;
    });

    test('should connect with correct headers', () => {
        chatClient.connect();
        expect(mockStompClient.connect).toHaveBeenCalledWith(
            { 'Authorization': 'Bearer test-token' },
            expect.any(Function),
            expect.any(Function)
        );
    });

    test('should send message to correct destination', () => {
        chatClient.sendMessage(100, 'Hello', 'TEXT');
        expect(mockStompClient.send).toHaveBeenCalledWith(
            '/app/chat.send.100',
            {},
            JSON.stringify({ roomId: 100, content: 'Hello', type: 'TEXT' })
        );
    });
});
```

#### 11.8.2 集成测试

```javascript
describe('WebSocket Integration', () => {
    let server;
    let client;

    beforeAll(async () => {
        server = await startTestServer();
    });

    afterAll(async () => {
        await server.close();
    });

    test('should receive messages in real-time', (done) => {
        client = new ChatClient('test-token');
        
        client.onMessage = (message) => {
            expect(message.content).toBe('Test message');
            done();
        };

        client.connect().then(() => {
            client.sendMessage(1, 'Test message', 'TEXT');
        });
    });
});
```

---

## 附录

### A. WebSocket API 接口参考

#### A.1 客户端发送接口

##### A.1.1 发送聊天消息

**接口**: `SEND /app/chat.send.{roomId}`

**描述**: 向指定聊天室发送消息

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
    "roomId": 100,
    "content": "大家好！",
    "type": "TEXT"
}
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 约束 |
|------|------|------|------|------|
| roomId | Long | 是 | 聊天室ID | 必须存在且用户已加入 |
| content | String | 是 | 消息内容 | 不能为空，最大5000字符 |
| type | String | 否 | 消息类型 | TEXT/IMAGE/FILE/EMOJI，默认TEXT |

**响应**: 无直接响应，消息会广播到 `/topic/room.{roomId}`

**广播消息格式**:
```json
{
    "id": 1,
    "roomId": 100,
    "senderId": 123,
    "senderName": "张三",
    "senderAvatar": "https://example.com/avatar.jpg",
    "content": "大家好！",
    "type": "TEXT",
    "createdAt": "2024-01-20T10:30:00"
}
```

**错误处理**:
- 未授权：静默失败，记录警告日志
- 不在聊天室：静默失败，记录警告日志
- 消息保存失败：记录错误日志

---

##### A.1.2 加入聊天室

**接口**: `SEND /app/chat.join.{roomId}`

**描述**: 通知其他用户当前用户加入聊天室

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**: 无（空字符串）

**响应**: 无直接响应，系统消息会广播到 `/topic/room.{roomId}`

**广播消息格式**:
```json
{
    "id": null,
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "content": "张三 joined the room",
    "type": "SYSTEM",
    "createdAt": "2024-01-20T10:32:00"
}
```

**注意事项**:
- 发送此消息前应先通过HTTP API加入聊天室
- 此消息仅用于通知其他用户

---

##### A.1.3 离开聊天室

**接口**: `SEND /app/chat.leave.{roomId}`

**描述**: 通知其他用户当前用户离开聊天室

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**: 无（空字符串）

**响应**: 无直接响应，系统消息会广播到 `/topic/room.{roomId}`

**广播消息格式**:
```json
{
    "id": null,
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "content": "张三 left the room",
    "type": "SYSTEM",
    "createdAt": "2024-01-20T10:35:00"
}
```

**注意事项**:
- 发送此消息前应先通过HTTP API离开聊天室
- 此消息仅用于通知其他用户

---

##### A.1.4 发送输入状态

**接口**: `SEND /app/chat.typing.{roomId}`

**描述**: 通知其他用户当前用户正在输入

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**: 无（空字符串）

**响应**: 无直接响应，输入状态会广播到 `/topic/room.{roomId}.typing`

**广播消息格式**:
```json
{
    "userId": 123,
    "username": "张三",
    "typing": true
}
```

**最佳实践**:
- 使用节流（throttle）控制发送频率
- 停止输入3秒后自动清除状态

---

##### A.1.5 更新用户状态

**接口**: `SEND /app/user.status`

**描述**: 更新当前用户的状态

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**:
```json
{
    "status": "BUSY"
}
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 可选值 |
|------|------|------|------|--------|
| status | String | 是 | 用户状态 | ONLINE/OFFLINE/BUSY/AWAY |

**响应**: 无直接响应，状态更新会广播到 `/topic/user.status`

**广播消息格式**:
```json
{
    "userId": 123,
    "status": "BUSY"
}
```

---

##### A.1.6 发送心跳

**接口**: `SEND /app/heartbeat`

**描述**: 发送心跳包保持连接活跃

**请求头**:
```
Authorization: Bearer {token}
```

**请求体**: 无（空字符串）

**响应**: 无直接响应

**说明**:
- 建议每30秒发送一次
- 任何消息发送都会自动更新心跳时间
- 心跳超时90秒后用户会被标记为离线

---

#### A.2 客户端订阅接口

##### A.2.1 订阅聊天室消息

**接口**: `SUBSCRIBE /topic/room.{roomId}`

**描述**: 接收指定聊天室的所有消息

**订阅示例**:
```javascript
stompClient.subscribe('/topic/room.100', function(message) {
    const msg = JSON.parse(message.body);
    console.log('收到消息:', msg);
});
```

**消息格式**:
```json
{
    "id": 1,
    "roomId": 100,
    "senderId": 123,
    "senderName": "张三",
    "senderAvatar": "https://example.com/avatar.jpg",
    "content": "大家好！",
    "type": "TEXT",
    "createdAt": "2024-01-20T10:30:00"
}
```

**消息字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 消息ID，系统消息为null |
| roomId | Long | 聊天室ID |
| senderId | Long | 发送者ID，系统消息为0 |
| senderName | String | 发送者昵称 |
| senderAvatar | String | 发送者头像URL |
| content | String | 消息内容 |
| type | String | 消息类型 |
| createdAt | DateTime | 消息创建时间 |

---

##### A.2.2 订阅输入状态

**接口**: `SUBSCRIBE /topic/room.{roomId}.typing`

**描述**: 接收聊天室内用户的输入状态

**订阅示例**:
```javascript
stompClient.subscribe('/topic/room.100.typing', function(message) {
    const data = JSON.parse(message.body);
    console.log(data.username + ' 正在输入...');
});
```

**消息格式**:
```json
{
    "userId": 123,
    "username": "张三",
    "typing": true
}
```

**最佳实践**:
- 显示3秒后自动清除输入提示
- 多人输入时显示"多人正在输入..."

---

##### A.2.3 订阅用户状态

**接口**: `SUBSCRIBE /topic/user.status`

**描述**: 接收所有用户的在线状态变化

**订阅示例**:
```javascript
stompClient.subscribe('/topic/user.status', function(message) {
    const data = JSON.parse(message.body);
    console.log('用户 ' + data.username + ' 状态: ' + data.status);
});
```

**消息格式**:
```json
{
    "userId": 123,
    "username": "张三",
    "status": "ONLINE"
}
```

**状态类型**:

| 状态 | 说明 |
|------|------|
| ONLINE | 在线 |
| OFFLINE | 离线 |
| BUSY | 忙碌 |
| AWAY | 离开 |

---

##### A.2.4 订阅私有消息

**接口**: `SUBSCRIBE /user/queue/private`

**描述**: 接收当前用户的私有消息（如系统通知、私信等）

**订阅示例**:
```javascript
stompClient.subscribe('/user/queue/private', function(message) {
    const notification = JSON.parse(message.body);
    console.log('收到私有消息:', notification);
});
```

**注意**: 此接口当前未实现，预留用于未来扩展

---

#### A.3 服务器事件

##### A.3.1 连接建立事件

**事件**: `SessionConnectedEvent`

**触发时机**: 客户端成功建立WebSocket连接

**服务器处理**:
1. 从STOMP CONNECT帧获取用户信息
2. 验证JWT Token
3. 将用户ID存储到Session映射
4. 初始化用户心跳时间
5. 更新用户状态为ONLINE
6. 向 `/topic/user.status` 广播用户上线

**广播消息格式**:
```json
{
    "userId": 123,
    "username": "张三",
    "status": "ONLINE"
}
```

---

##### A.3.2 连接断开事件

**事件**: `SessionDisconnectEvent`

**触发时机**: 客户端断开WebSocket连接

**服务器处理**:
1. 从Session映射中移除用户
2. 从心跳映射中移除用户
3. 更新用户状态为OFFLINE
4. 向 `/topic/user.status` 广播用户离线

**广播消息格式**:
```json
{
    "userId": 123,
    "username": "张三",
    "status": "OFFLINE"
}
```

---

##### A.3.3 心跳超时事件

**事件**: `@Scheduled(fixedRate = 60000)`

**触发时机**: 每60秒检查一次心跳超时

**服务器处理**:
1. 遍历所有用户的心跳时间
2. 超过90秒未收到心跳的用户标记为离线
3. 更新数据库中的用户状态
4. 向 `/topic/user.status` 广播离线状态
5. 从心跳映射中移除该用户

---

#### A.4 消息类型定义

##### A.4.1 消息类型枚举

| 类型 | 值 | 说明 |
|------|------|------|
| 文本消息 | TEXT | 普通文本消息 |
| 图片消息 | IMAGE | 图片URL消息 |
| 文件消息 | FILE | 文件URL消息 |
| 系统消息 | SYSTEM | 系统通知消息 |
| 表情消息 | EMOJI | 表情符号消息 |

##### A.4.2 用户状态枚举

| 状态 | 值 | 说明 |
|------|------|------|
| 在线 | ONLINE | 用户在线 |
| 离线 | OFFLINE | 用户离线 |
| 忙碌 | BUSY | 用户忙碌 |
| 离开 | AWAY | 用户暂时离开 |

---

#### A.5 错误码参考

| 错误类型 | HTTP状态码 | 说明 | 处理建议 |
|---------|------------|------|----------|
| Unauthorized | 401 | Token无效或已过期 | 重新登录获取新Token |
| Forbidden | 403 | 权限不足 | 检查用户权限 |
| Bad Request | 400 | 请求参数错误 | 检查请求参数 |
| Internal Server Error | 500 | 服务器内部错误 | 稍后重试或联系管理员 |

---

### B. STOMP 命令参考

| 命令 | 说明 | 方向 |
|------|------|------|
| CONNECT | 建立连接 | Client → Server |
| CONNECTED | 连接成功 | Server → Client |
| SEND | 发送消息 | Client → Server |
| SUBSCRIBE | 订阅主题 | Client → Server |
| UNSUBSCRIBE | 取消订阅 | Client → Server |
| MESSAGE | 消息通知 | Server → Client |
| ERROR | 错误 | Server → Client |
| DISCONNECT | 断开连接 | Client → Server |

### B. 常用库推荐

| 平台 | 库名 | 安装命令 |
|------|------|----------|
| JavaScript | @stomp/stompjs | `npm install @stomp/stompjs` |
| JavaScript | sockjs-client | `npm install sockjs-client` |
| Android | StompClientAndroid | Gradle 依赖 |
| iOS | StompClientLib | Pod 依赖 |
| Java | spring-websocket | Maven 依赖 |

### C. 调试工具

1. **浏览器开发者工具**: Network → WS 标签
2. **WebSocket King Client**: 图形化 WebSocket 客户端
3. **Postman**: 支持 WebSocket 测试

---

*文档版本: 1.1.0*
*最后更新: 2026年3月*