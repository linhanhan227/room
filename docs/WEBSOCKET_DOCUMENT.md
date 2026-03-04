# WebSocket API 接口文档

## 概述

### 基本信息

| 项目 | 说明 |
|------|------|
| 协议 | STOMP over WebSocket |
| 端点 | `/ws` |
| 认证方式 | Bearer Token (JWT) |
| 消息格式 | JSON |

### 连接地址

**原生 WebSocket**：

| 环境 | 地址 |
|------|------|
| 开发环境 | `ws://localhost:8080/api/ws` |
| 生产环境 | `wss://your-domain.com/api/ws` |

**SockJS（推荐）**：

| 环境 | 地址 |
|------|------|
| 开发环境 | `http://localhost:8080/api/ws` |
| 生产环境 | `https://your-domain.com/api/ws` |

> **注意**：由于服务器配置了 `context-path: /api`，所有端点都需要加上 `/api` 前缀。SockJS 不支持 `ws://` 协议，必须使用 `http://` 或 `https://`。

> **注意**：SockJS 不支持 `ws://` 协议，必须使用 `http://` 或 `https://`，SockJS 会自动处理 WebSocket 升级。

### 认证说明

WebSocket 连接支持两种认证方式：

**方式一：HTTP 握手阶段认证（推荐）**

在连接 URL 中通过查询参数传递 Token，服务器在 HTTP → WebSocket 协议升级阶段即完成认证：

```
ws://localhost:8080/api/ws?token={access_token}
http://localhost:8080/api/ws?token={access_token}  (SockJS)
```

**方式二：STOMP CONNECT 帧认证**

在 STOMP CONNECT 帧中携带 Authorization 头：

```
Authorization: Bearer {access_token}
```

> **说明**：方式一在 HTTP 握手阶段即完成认证并将用户信息缓存到 Session 中，后续 STOMP 消息处理无需再查询数据库，性能更优。方式二兼容不支持自定义查询参数的客户端。两种方式可同时使用。

---

## 接口列表

### 发送接口

| 方法 | 路径 | 说明 |
|------|------|------|
| SEND | `/app/chat.send.{roomId}` | 发送聊天消息 |
| SEND | `/app/chat.join.{roomId}` | 加入聊天室 |
| SEND | `/app/chat.leave.{roomId}` | 离开聊天室 |
| SEND | `/app/chat.typing.{roomId}` | 发送输入状态 |
| SEND | `/app/user.status` | 更新用户状态 |
| SEND | `/app/heartbeat` | 发送心跳 |

### 订阅接口

| 方法 | 路径 | 说明 |
|------|------|------|
| SUBSCRIBE | `/topic/room.{roomId}` | 订阅聊天室消息 |
| SUBSCRIBE | `/topic/room.{roomId}.typing` | 订阅输入状态 |
| SUBSCRIBE | `/topic/user.status` | 订阅用户状态变化 |
| SUBSCRIBE | `/user/queue/private` | 订阅私有消息 |
| SUBSCRIBE | `/user/queue/errors` | 订阅错误消息 |

---

## 发送接口详情

### 1. 发送聊天消息

**接口地址**：`SEND /app/chat.send.{roomId}`

**接口描述**：向指定聊天室发送消息

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |
| content-type | String | 是 | application/json |

**请求体**：

```json
{
    "roomId": 100,
    "content": "大家好！",
    "type": "TEXT"
}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| roomId | Long | 是 | - | 聊天室ID |
| content | String | 是 | - | 消息内容，最大5000字符 |
| type | String | 否 | TEXT | 消息类型：TEXT/IMAGE/FILE/EMOJI |

**响应**：

消息将广播到 `/topic/room.{roomId}` 主题，格式如下：

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

**响应字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 消息ID |
| roomId | Long | 聊天室ID |
| senderId | Long | 发送者ID |
| senderName | String | 发送者昵称 |
| senderAvatar | String | 发送者头像URL |
| content | String | 消息内容 |
| type | String | 消息类型 |
| createdAt | String | 创建时间 |

**错误码**：

| 错误码 | 说明 |
|--------|------|
| 401 | 未授权，Token 无效或过期 |
| 403 | 无权限，用户未加入该聊天室 |
| 400 | 请求参数错误 |

> **错误反馈**：当消息发送失败时（如被禁言、敏感词检测等），服务器会通过 `/user/queue/errors` 向发送者推送错误消息，请确保客户端订阅了该队列以接收错误反馈。

**请求示例**：

```javascript
stompClient.send('/app/chat.send.100', {}, JSON.stringify({
    roomId: 100,
    content: '大家好！',
    type: 'TEXT'
}));
```

---

### 2. 加入聊天室

**接口地址**：`SEND /app/chat.join.{roomId}`

**接口描述**：通知其他用户有人加入聊天室

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |

**请求体**：无

**响应**：

系统消息将广播到 `/topic/room.{roomId}` 主题：

```json
{
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "senderAvatar": null,
    "content": "张三 joined the room",
    "type": "SYSTEM",
    "createdAt": "2024-01-20T10:30:00"
}
```

**响应字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| roomId | Long | 聊天室ID |
| senderId | Long | 发送者ID（系统消息为0） |
| senderName | String | 发送者名称 |
| senderAvatar | String | 发送者头像（系统消息为null） |
| content | String | 消息内容 |
| type | String | 消息类型（SYSTEM） |
| createdAt | String | 创建时间 |

**请求示例**：

```javascript
stompClient.send('/app/chat.join.100', {}, '');
```

---

### 3. 离开聊天室

**接口地址**：`SEND /app/chat.leave.{roomId}`

**接口描述**：通知其他用户有人离开聊天室

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |

**请求体**：无

**响应**：

系统消息将广播到 `/topic/room.{roomId}` 主题：

```json
{
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "senderAvatar": null,
    "content": "张三 left the room",
    "type": "SYSTEM",
    "createdAt": "2024-01-20T10:30:00"
}
```

**请求示例**：

```javascript
stompClient.send('/app/chat.leave.100', {}, '');
```

---

### 4. 发送输入状态

**接口地址**：`SEND /app/chat.typing.{roomId}`

**接口描述**：通知其他用户正在输入

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |

**请求体**：无

**响应**：

输入状态将广播到 `/topic/room.{roomId}.typing` 主题：

```json
{
    "userId": 123,
    "username": "张三",
    "typing": true
}
```

**响应字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| typing | Boolean | 是否正在输入 |

**请求示例**：

```javascript
stompClient.send('/app/chat.typing.100', {}, '');
```

---

### 5. 更新用户状态

**接口地址**：`SEND /app/user.status`

**接口描述**：更新当前用户的在线状态

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |
| content-type | String | 是 | application/json |

**请求体**：

```json
{
    "status": "BUSY"
}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 用户状态：ONLINE/OFFLINE/BUSY/AWAY |

**状态枚举**：

| 值 | 说明 |
|------|------|
| ONLINE | 在线 |
| OFFLINE | 离线 |
| BUSY | 忙碌 |
| AWAY | 离开 |

**响应**：

状态更新将广播到 `/topic/user.status` 主题：

```json
{
    "userId": 123,
    "username": "张三",
    "status": "BUSY"
}
```

**响应字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| status | String | 用户状态 |

**请求示例**：

```javascript
stompClient.send('/app/user.status', {}, JSON.stringify({
    status: 'BUSY'
}));
```

---

### 6. 发送心跳

**接口地址**：`SEND /app/heartbeat`

**接口描述**：发送心跳以保持在线状态

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer Token |

**请求体**：无

**响应**：无直接响应，服务器更新用户心跳时间戳

**请求示例**：

```javascript
stompClient.send('/app/heartbeat', {}, '');
```

---

## 订阅接口详情

### 1. 订阅聊天室消息

**接口地址**：`SUBSCRIBE /topic/room.{roomId}`

**接口描述**：订阅指定聊天室的所有消息

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**消息格式**：

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

**消息字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 消息ID |
| roomId | Long | 聊天室ID |
| senderId | Long | 发送者ID |
| senderName | String | 发送者昵称 |
| senderAvatar | String | 发送者头像URL |
| content | String | 消息内容 |
| type | String | 消息类型：TEXT/IMAGE/FILE/SYSTEM/EMOJI |
| createdAt | String | 创建时间 |

**订阅示例**：

```javascript
stompClient.subscribe('/topic/room.100', function(message) {
    const msg = JSON.parse(message.body);
    console.log('收到消息:', msg);
});
```

---

### 2. 订阅输入状态

**接口地址**：`SUBSCRIBE /topic/room.{roomId}.typing`

**接口描述**：订阅指定聊天室的输入状态通知

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**消息格式**：

```json
{
    "userId": 123,
    "username": "张三",
    "typing": true
}
```

**消息字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| typing | Boolean | 是否正在输入 |

**订阅示例**：

```javascript
stompClient.subscribe('/topic/room.100.typing', function(message) {
    const data = JSON.parse(message.body);
    console.log(data.username + ' 正在输入...');
});
```

---

### 3. 订阅用户状态变化

**接口地址**：`SUBSCRIBE /topic/user.status`

**接口描述**：订阅所有用户的在线状态变化

**消息格式**：

```json
{
    "userId": 123,
    "username": "张三",
    "status": "ONLINE"
}
```

**消息字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| status | String | 用户状态：ONLINE/OFFLINE/BUSY/AWAY |

**订阅示例**：

```javascript
stompClient.subscribe('/topic/user.status', function(message) {
    const data = JSON.parse(message.body);
    console.log('用户 ' + data.username + ' 状态: ' + data.status);
});
```

---

### 4. 订阅私有消息

**接口地址**：`SUBSCRIBE /user/queue/private`

**接口描述**：订阅当前用户的私有消息

**消息格式**：

```json
{
    "type": "NOTIFICATION",
    "title": "系统通知",
    "content": "您有新的消息",
    "createdAt": "2024-01-20T10:30:00"
}
```

**订阅示例**：

```javascript
stompClient.subscribe('/user/queue/private', function(message) {
    const notification = JSON.parse(message.body);
    console.log('收到私有消息:', notification);
});
```

---

### 5. 订阅错误消息

**接口地址**：`SUBSCRIBE /user/queue/errors`

**接口描述**：订阅当前用户的操作错误消息。当用户通过 WebSocket 发送消息失败时（如被禁言、敏感词检测不通过、不是聊天室成员等），服务器会通过该队列向用户推送错误信息。

**消息格式**：

```json
{
    "error": "您已被禁言，无法发送消息",
    "timestamp": "2024-01-20T10:30:00"
}
```

**消息字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| error | String | 错误信息描述 |
| timestamp | String | 错误发生的时间戳（ISO 8601格式） |

**常见错误消息**：

| 错误信息 | 触发场景 |
|----------|----------|
| 您已被禁言，无法发送消息 | 用户在聊天室中被禁言 |
| 消息包含敏感词，无法发送 | 消息内容触发敏感词过滤 |
| 您不是该聊天室的成员 | 用户未加入目标聊天室 |
| 聊天室不存在 | 目标聊天室已被删除 |

**订阅示例**：

```javascript
stompClient.subscribe('/user/queue/errors', function(message) {
    const error = JSON.parse(message.body);
    console.error('操作失败:', error.error);
    // 可以在UI上展示错误提示
    showErrorNotification(error.error);
});
```

---

## 数据模型

### MessageDTO

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 否 | 消息ID |
| roomId | Long | 是 | 聊天室ID |
| senderId | Long | 是 | 发送者ID |
| senderName | String | 是 | 发送者昵称 |
| senderAvatar | String | 否 | 发送者头像URL |
| content | String | 是 | 消息内容 |
| type | String | 是 | 消息类型 |
| createdAt | String | 否 | 创建时间 |

### SendMessageRequest

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| roomId | Long | 是 | - | 聊天室ID |
| content | String | 是 | - | 消息内容 |
| type | String | 否 | TEXT | 消息类型 |

### TypingEvent

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| username | String | 是 | 用户昵称 |
| typing | Boolean | 是 | 是否正在输入 |

### UserStatusEvent

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| username | String | 否 | 用户昵称 |
| status | String | 是 | 用户状态 |

---

## 枚举定义

### MessageType 消息类型

| 值 | 说明 |
|------|------|
| TEXT | 文本消息 |
| IMAGE | 图片消息 |
| FILE | 文件消息 |
| SYSTEM | 系统消息 |
| EMOJI | 表情消息 |

### UserStatus 用户状态

| 值 | 说明 |
|------|------|
| ONLINE | 在线 |
| OFFLINE | 离线 |
| BUSY | 忙碌 |
| AWAY | 离开 |

---

## 心跳机制

### 配置参数

| 参数 | 默认值 | 说明 |
|--------|--------|------|
| heartbeatInterval | 30000ms | 心跳间隔 |
| heartbeatTimeout | 90000ms | 心跳超时 |
| checkInterval | 60000ms | 检查频率 |

### 心跳更新时机

| 操作 | 接口 |
|------|------|
| 发送消息 | `/app/chat.send.{roomId}` |
| 加入聊天室 | `/app/chat.join.{roomId}` |
| 离开聊天室 | `/app/chat.leave.{roomId}` |
| 发送输入状态 | `/app/chat.typing.{roomId}` |
| 更新用户状态 | `/app/user.status` |
| 发送心跳 | `/app/heartbeat` |

### 超时处理

用户超过 90 秒未发送心跳或任何消息，服务器将：
1. 更新用户状态为 OFFLINE
2. 广播状态到 `/topic/user.status`
3. 移除用户心跳记录

---

## 错误处理

### 错误帧格式

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

### 错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| Unauthorized | 认证失败 | 重新登录获取新 Token |
| Forbidden | 权限不足 | 提示用户加入聊天室 |
| Bad Request | 请求参数错误 | 检查请求参数 |

### 错误示例

**认证失败**：

```json
{
    "command": "ERROR",
    "headers": {
        "message": "Unauthorized"
    },
    "body": "Invalid or expired token"
}
```

**权限不足**：

```json
{
    "command": "ERROR",
    "headers": {
        "message": "Forbidden"
    },
    "body": "You are not a member of this room"
}
```

---

## 连接配置

### 传输配置

| 参数 | 默认值 | 说明 |
|------|--------|------|
| messageSizeLimit | 131072 (128KB) | 消息大小限制 |
| sendBufferSizeLimit | 524288 (512KB) | 发送缓冲区大小 |
| sendTimeLimit | 20000ms | 发送超时 |
| timeToFirstMessage | 30000ms | 首条消息超时 |

### SockJS 降级

支持 SockJS 降级连接：

```javascript
const socket = new SockJS('http://localhost:8080/ws');
```

**降级策略**：
1. WebSocket
2. HTTP Streaming
3. HTTP Long Polling

---

## 客户端示例

### JavaScript 完整示例

```javascript
const token = 'your-jwt-token';
const roomId = 100;
let stompClient = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect(
        { 'Authorization': 'Bearer ' + token },
        onConnected,
        onError
    );
}

function onConnected(frame) {
    console.log('Connected:', frame);
    
    // 订阅聊天室消息
    stompClient.subscribe('/topic/room.' + roomId, onMessage);
    // 订阅输入状态
    stompClient.subscribe('/topic/room.' + roomId + '.typing', onTyping);
    // 订阅用户状态变化
    stompClient.subscribe('/topic/user.status', onUserStatus);
    // 订阅错误消息（重要：用于接收发送失败的反馈）
    stompClient.subscribe('/user/queue/errors', onError​Message);
    
    stompClient.send('/app/chat.join.' + roomId, {}, '');
}

function onError(error) {
    console.error('Error:', error);
    setTimeout(connect, 5000);
}

function sendMessage(content) {
    stompClient.send('/app/chat.send.' + roomId, {}, JSON.stringify({
        roomId: roomId,
        content: content,
        type: 'TEXT'
    }));
}

function onMessage(message) {
    const msg = JSON.parse(message.body);
    console.log('Message:', msg);
}

function onTyping(message) {
    const data = JSON.parse(message.body);
    console.log(data.username + ' is typing...');
}

function onUserStatus(message) {
    const data = JSON.parse(message.body);
    console.log('User status:', data);
}

function onError​Message(message) {
    const error = JSON.parse(message.body);
    console.error('操作失败:', error.error);
    // 在UI上展示错误提示
    showErrorNotification(error.error);
}

function disconnect() {
    if (stompClient) {
        stompClient.send('/app/chat.leave.' + roomId, {}, '');
        stompClient.disconnect();
    }
}

connect();
```

---

## 附录

### 主题前缀说明

| 前缀 | 说明 |
|------|------|
| `/topic` | 广播主题，所有订阅者都能收到消息 |
| `/queue` | 点对点队列，消息只发送给特定用户 |
| `/app` | 应用目的地，客户端发送消息的前缀 |
| `/user` | 用户专属，用户私有消息前缀 |

### 源码位置

| 文件 | 说明 |
|------|------|
| [WebSocketConfig.java](../src/main/java/com/chat/room/config/WebSocketConfig.java) | WebSocket 配置类 |
| [WebSocketChatHandler.java](../src/main/java/com/chat/room/websocket/WebSocketChatHandler.java) | 消息处理器 |
| [WebSocketEventListener.java](../src/main/java/com/chat/room/websocket/WebSocketEventListener.java) | 事件监听器 |
| [WebSocketAuthChannelInterceptor.java](../src/main/java/com/chat/room/security/WebSocketAuthChannelInterceptor.java) | STOMP 认证拦截器 |
| [WebSocketHandshakeInterceptor.java](../src/main/java/com/chat/room/security/WebSocketHandshakeInterceptor.java) | HTTP 握手拦截器 |
