# WebSocket 接口文档

## 基础信息

- **WebSocket URL**: `ws://localhost:8080/api/ws`
- **协议**: WebSocket over STOMP
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Bearer Token

---

## 通用说明

### 连接认证

WebSocket 连接时需要在 STOMP CONNECT 帧中携带 JWT Token：

```
Authorization: Bearer <token>
```

### 消息格式

**发送消息格式**:
```json
{
  "content": "消息内容",
  "type": "TEXT"
}
```

**接收消息格式**:
```json
{
  "id": 1,
  "roomId": 100,
  "senderId": 1,
  "senderName": "张三",
  "senderAvatar": "http://example.com/avatar.jpg",
  "content": "Hello, World!",
  "type": "TEXT",
  "createdAt": "2024-01-01T12:00:00"
}
```

### 错误响应格式

```json
{
  "error": "错误类型",
  "message": "错误信息描述"
}
```

### 连接状态码

| 状态码 | 说明 |
|--------|------|
| 1000 | 正常关闭 |
| 1001 | 端点离开 |
| 1002 | 协议错误 |
| 1003 | 不支持的数据类型 |
| 1008 | 策略违规 |
| 1011 | 内部错误 |
| 1012 | 服务重启 |
| 1013 | 稍后重试 |

---

## 目录

- [1. 连接管理](#1-连接管理)
  - [1.1 建立连接](#11-建立连接)
  - [1.2 断开连接](#12-断开连接)
  - [1.3 心跳检测](#13-心跳检测)
- [2. 消息订阅](#2-消息订阅)
  - [2.1 订阅聊天室消息](#21-订阅聊天室消息)
  - [2.2 订阅输入状态](#22-订阅输入状态)
  - [2.3 订阅用户状态](#23-订阅用户状态)
  - [2.4 订阅个人通知](#24-订阅个人通知)
- [3. 消息发送](#3-消息发送)
  - [3.1 发送聊天消息](#31-发送聊天消息)
  - [3.2 加入聊天室](#32-加入聊天室)
  - [3.3 离开聊天室](#33-离开聊天室)
  - [3.4 发送输入状态](#34-发送输入状态)
  - [3.5 更新用户状态](#35-更新用户状态)
- [4. 数据模型](#4-数据模型)
- [5. 使用示例](#5-使用示例)
- [6. 错误处理](#6-错误处理)
- [7. 配置参数](#7-配置参数)

---

## 1. 连接管理

### 1.1 建立连接

**连接端点**: `ws://localhost:8080/api/ws`

**协议**: STOMP over WebSocket / SockJS

**权限要求**: 需要登录

**连接头参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | JWT Token，格式: `Bearer <token>` |

**连接示例**:

```javascript
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

const headers = {
    'Authorization': 'Bearer ' + yourJwtToken
};

stompClient.connect(headers, function(frame) {
    console.log('Connected: ' + frame);
}, function(error) {
    console.log('Error: ' + error);
});
```

**连接成功响应**:
```
CONNECTED
version:1.2
heart-beat:30000,30000
server:ActiveMQ/5.12.1
```

**连接失败响应**:
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

---

### 1.2 断开连接

**操作类型**: DISCONNECT

**权限要求**: 需要已连接

**断开示例**:

```javascript
stompClient.disconnect(function() {
    console.log('Disconnected');
});
```

**断开流程**:
1. 客户端发送 DISCONNECT 帧
2. 服务端更新用户状态为 OFFLINE
3. 广播用户离线状态到 `/topic/user.status`
4. 关闭 WebSocket 连接

---

### 1.3 心跳检测

**目的地址**: `/app/heartbeat`

**权限要求**: 需要登录

**请求体**: 无

**说明**:
- STOMP 客户端会自动处理心跳，无需手动发送
- 心跳间隔默认 30 秒
- 超时时间默认 90 秒

**手动心跳示例**:

```javascript
stompClient.send('/app/heartbeat', {}, JSON.stringify({}));
```

**心跳配置**:

| 参数 | 默认值 | 说明 |
|------|--------|------|
| 心跳间隔 | 30000ms | 客户端/服务端心跳发送间隔 |
| 心跳超时 | 90000ms | 超过此时间未收到心跳则判定离线 |

**超时处理**:
1. 将用户状态更新为 `OFFLINE`
2. 广播用户离线状态到 `/topic/user.status`
3. 从心跳监控列表中移除用户

---

## 2. 消息订阅

### 2.1 订阅聊天室消息

**订阅地址**: `/topic/room.{roomId}`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**订阅示例**:

```javascript
stompClient.subscribe('/topic/room.' + roomId, function(message) {
    const msg = JSON.parse(message.body);
    console.log('Received message:', msg);
});
```

**消息格式 (MessageDTO)**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 消息ID |
| roomId | Long | 聊天室ID |
| senderId | Long | 发送者ID |
| senderName | String | 发送者昵称 |
| senderAvatar | String | 发送者头像URL |
| content | String | 消息内容 |
| type | String | 消息类型: TEXT/IMAGE/FILE/SYSTEM |
| createdAt | DateTime | 发送时间 |

**消息示例**:
```json
{
    "id": 1,
    "roomId": 100,
    "senderId": 1,
    "senderName": "张三",
    "senderAvatar": "http://example.com/avatar.jpg",
    "content": "Hello, World!",
    "type": "TEXT",
    "createdAt": "2024-01-01T12:00:00"
}
```

---

### 2.2 订阅输入状态

**订阅地址**: `/topic/room.{roomId}.typing`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**订阅示例**:

```javascript
stompClient.subscribe('/topic/room.' + roomId + '.typing', function(message) {
    const typingEvent = JSON.parse(message.body);
    console.log(typingEvent.username + ' is typing...');
});
```

**消息格式 (TypingEvent)**:

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| typing | Boolean | 是否正在输入 |

**消息示例**:
```json
{
    "userId": 1,
    "username": "张三",
    "typing": true
}
```

---

### 2.3 订阅用户状态

**订阅地址**: `/topic/user.status`

**权限要求**: 需要登录

**订阅示例**:

```javascript
stompClient.subscribe('/topic/user.status', function(message) {
    const statusEvent = JSON.parse(message.body);
    console.log('User ' + statusEvent.userId + ' is now ' + statusEvent.status);
});
```

**消息格式 (StatusEvent)**:

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| status | String | 状态: ONLINE/OFFLINE/AFK/BUSY |

**消息示例**:
```json
{
    "userId": 1,
    "username": "张三",
    "status": "ONLINE"
}
```

**状态说明**:

| 状态 | 说明 |
|------|------|
| ONLINE | 在线 |
| OFFLINE | 离线 |
| AFK | 离开 |
| BUSY | 忙碌 |

---

### 2.4 订阅个人通知

**订阅地址**: `/user/queue/notifications`

**权限要求**: 需要登录

**订阅示例**:

```javascript
stompClient.subscribe('/user/queue/notifications', function(message) {
    const notification = JSON.parse(message.body);
    console.log('Notification:', notification);
});
```

**消息格式 (NotificationDTO)**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 通知ID |
| type | String | 通知类型 |
| title | String | 通知标题 |
| content | String | 通知内容 |
| read | Boolean | 是否已读 |
| createdAt | DateTime | 创建时间 |

---

## 3. 消息发送

### 3.1 发送聊天消息

**目的地址**: `/app/chat.send.{roomId}`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | String | 是 | 消息内容 |
| type | String | 否 | 消息类型: TEXT/IMAGE/FILE，默认 TEXT |

**请求示例**:
```json
{
    "content": "Hello, World!",
    "type": "TEXT"
}
```

**发送示例**:

```javascript
stompClient.send('/app/chat.send.' + roomId, {}, JSON.stringify({
    content: 'Hello, World!',
    type: 'TEXT'
}));
```

**响应**: 消息广播到 `/topic/room.{roomId}`，格式见 [2.1 订阅聊天室消息](#21-订阅聊天室消息)

**消息类型说明**:

| 类型 | 说明 |
|------|------|
| TEXT | 文本消息 |
| IMAGE | 图片消息 |
| FILE | 文件消息 |
| SYSTEM | 系统消息 |

---

### 3.2 加入聊天室

**目的地址**: `/app/chat.join.{roomId}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求体**: 无

**发送示例**:

```javascript
stompClient.send('/app/chat.join.' + roomId, {}, JSON.stringify({}));
```

**响应**: 系统消息广播到 `/topic/room.{roomId}`

```json
{
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "content": "张三 joined the room",
    "type": "SYSTEM",
    "createdAt": "2024-01-01T12:00:00"
}
```

---

### 3.3 离开聊天室

**目的地址**: `/app/chat.leave.{roomId}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求体**: 无

**发送示例**:

```javascript
stompClient.send('/app/chat.leave.' + roomId, {}, JSON.stringify({}));
```

**响应**: 系统消息广播到 `/topic/room.{roomId}`

```json
{
    "roomId": 100,
    "senderId": 0,
    "senderName": "System",
    "content": "张三 left the room",
    "type": "SYSTEM",
    "createdAt": "2024-01-01T12:00:00"
}
```

---

### 3.4 发送输入状态

**目的地址**: `/app/chat.typing.{roomId}`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求体**: 无

**发送示例**:

```javascript
stompClient.send('/app/chat.typing.' + roomId, {}, JSON.stringify({}));
```

**响应**: 输入状态广播到 `/topic/room.{roomId}.typing`

```json
{
    "userId": 1,
    "username": "张三",
    "typing": true
}
```

**建议**: 使用防抖（debounce）控制发送频率，避免频繁发送

---

### 3.5 更新用户状态

**目的地址**: `/app/user.status`

**权限要求**: 需要登录

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 状态: ONLINE/OFFLINE/AFK/BUSY |

**请求示例**:
```json
{
    "status": "BUSY"
}
```

**发送示例**:

```javascript
stompClient.send('/app/user.status', {}, JSON.stringify({
    status: 'BUSY'
}));
```

**响应**: 状态变更广播到 `/topic/user.status`

```json
{
    "userId": 1,
    "status": "BUSY"
}
```

---

## 4. 数据模型

### MessageDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 消息ID |
| roomId | Long | 聊天室ID |
| senderId | Long | 发送者ID |
| senderName | String | 发送者昵称 |
| senderAvatar | String | 发送者头像URL |
| content | String | 消息内容 |
| type | String | 消息类型: TEXT/IMAGE/FILE/SYSTEM |
| createdAt | DateTime | 发送时间 |

### TypingEvent

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| typing | Boolean | 是否正在输入 |

### StatusEvent

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| username | String | 用户昵称 |
| status | String | 状态: ONLINE/OFFLINE/AFK/BUSY |

### NotificationDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 通知ID |
| type | String | 通知类型 |
| title | String | 通知标题 |
| content | String | 通知内容 |
| read | Boolean | 是否已读 |
| createdAt | DateTime | 创建时间 |

---

## 5. 使用示例

### 5.1 完整 JavaScript 示例

```html
<!DOCTYPE html>
<html>
<head>
    <title>Chat Room WebSocket Demo</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <div id="messages"></div>
    <input type="text" id="messageInput" placeholder="Type a message...">
    <button onclick="sendMessage()">Send</button>
    <button onclick="connect()">Connect</button>
    <button onclick="disconnect()">Disconnect</button>

    <script>
        let stompClient = null;
        const roomId = 1;
        const token = 'your-jwt-token-here';

        function connect() {
            const socket = new SockJS('http://localhost:8080/api/ws');
            stompClient = Stomp.over(socket);
            
            const headers = {
                'Authorization': 'Bearer ' + token
            };

            stompClient.connect(headers, function(frame) {
                console.log('Connected: ' + frame);
                
                // 订阅聊天室消息
                stompClient.subscribe('/topic/room.' + roomId, function(message) {
                    showMessage(JSON.parse(message.body));
                });

                // 订阅输入状态
                stompClient.subscribe('/topic/room.' + roomId + '.typing', function(message) {
                    const data = JSON.parse(message.body);
                    console.log(data.username + ' is typing...');
                });

                // 订阅用户状态
                stompClient.subscribe('/topic/user.status', function(message) {
                    const data = JSON.parse(message.body);
                    console.log('User ' + data.userId + ' is ' + data.status);
                });

                // 加入聊天室
                stompClient.send('/app/chat.join.' + roomId, {}, JSON.stringify({}));

            }, function(error) {
                console.log('Error: ' + error);
            });
        }

        function disconnect() {
            if (stompClient !== null) {
                stompClient.send('/app/chat.leave.' + roomId, {}, JSON.stringify({}));
                stompClient.disconnect();
            }
            console.log('Disconnected');
        }

        function sendMessage() {
            const content = document.getElementById('messageInput').value;
            if (content && stompClient) {
                stompClient.send('/app/chat.send.' + roomId, {}, JSON.stringify({
                    content: content,
                    type: 'TEXT'
                }));
                document.getElementById('messageInput').value = '';
            }
        }

        function showMessage(message) {
            const div = document.createElement('div');
            div.textContent = message.senderName + ': ' + message.content;
            document.getElementById('messages').appendChild(div);
        }

        // 输入状态提示
        document.getElementById('messageInput').addEventListener('input', function() {
            if (stompClient) {
                stompClient.send('/app/chat.typing.' + roomId, {}, JSON.stringify({}));
            }
        });
    </script>
</body>
</html>
```

### 5.2 Vue 3 + TypeScript 示例

```typescript
// websocket.service.ts
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

export interface MessageDTO {
    id: number;
    roomId: number;
    senderId: number;
    senderName: string;
    senderAvatar: string;
    content: string;
    type: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
    createdAt: string;
}

export interface TypingEvent {
    userId: number;
    username: string;
    typing: boolean;
}

export interface StatusEvent {
    userId: number;
    status: 'ONLINE' | 'OFFLINE' | 'AFK' | 'BUSY';
    username?: string;
}

export class WebSocketService {
    private stompClient: Stomp.Client | null = null;
    private connected = false;

    connect(token: string): Promise<void> {
        return new Promise((resolve, reject) => {
            const socket = new SockJS('http://localhost:8080/api/ws');
            this.stompClient = Stomp.over(socket);

            const headers = {
                'Authorization': `Bearer ${token}`
            };

            this.stompClient.connect(
                headers,
                (frame) => {
                    this.connected = true;
                    console.log('Connected:', frame);
                    resolve();
                },
                (error) => {
                    this.connected = false;
                    console.error('Connection error:', error);
                    reject(error);
                }
            );
        });
    }

    disconnect(): void {
        if (this.stompClient) {
            this.stompClient.disconnect(() => {
                this.connected = false;
                console.log('Disconnected');
            });
        }
    }

    subscribeToRoom(roomId: number, callback: (message: MessageDTO) => void): void {
        if (this.stompClient) {
            this.stompClient.subscribe(`/topic/room.${roomId}`, (message) => {
                callback(JSON.parse(message.body));
            });
        }
    }

    subscribeToTyping(roomId: number, callback: (event: TypingEvent) => void): void {
        if (this.stompClient) {
            this.stompClient.subscribe(`/topic/room.${roomId}.typing`, (message) => {
                callback(JSON.parse(message.body));
            });
        }
    }

    subscribeToUserStatus(callback: (event: StatusEvent) => void): void {
        if (this.stompClient) {
            this.stompClient.subscribe('/topic/user.status', (message) => {
                callback(JSON.parse(message.body));
            });
        }
    }

    sendMessage(roomId: number, content: string, type: string = 'TEXT'): void {
        if (this.stompClient && this.connected) {
            this.stompClient.send(
                `/app/chat.send.${roomId}`,
                {},
                JSON.stringify({ content, type })
            );
        }
    }

    joinRoom(roomId: number): void {
        if (this.stompClient && this.connected) {
            this.stompClient.send(`/app/chat.join.${roomId}`, {}, JSON.stringify({}));
        }
    }

    leaveRoom(roomId: number): void {
        if (this.stompClient && this.connected) {
            this.stompClient.send(`/app/chat.leave.${roomId}`, {}, JSON.stringify({}));
        }
    }

    sendTypingIndicator(roomId: number): void {
        if (this.stompClient && this.connected) {
            this.stompClient.send(`/app/chat.typing.${roomId}`, {}, JSON.stringify({}));
        }
    }

    updateStatus(status: 'ONLINE' | 'OFFLINE' | 'AFK' | 'BUSY'): void {
        if (this.stompClient && this.connected) {
            this.stompClient.send('/app/user.status', {}, JSON.stringify({ status }));
        }
    }

    isConnected(): boolean {
        return this.connected;
    }
}
```

### 5.3 React Hook 示例

```typescript
// useWebSocket.ts
import { useEffect, useRef, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

interface UseWebSocketOptions {
    token: string;
    roomId: number;
    onMessage?: (message: any) => void;
    onUserStatus?: (status: any) => void;
    onTyping?: (typing: any) => void;
}

export function useWebSocket({
    token,
    roomId,
    onMessage,
    onUserStatus,
    onTyping
}: UseWebSocketOptions) {
    const stompClient = useRef<Stomp.Client | null>(null);
    const [connected, setConnected] = useState(false);

    const connect = useCallback(() => {
        const socket = new SockJS('http://localhost:8080/api/ws');
        stompClient.current = Stomp.over(socket);

        stompClient.current.connect(
            { 'Authorization': `Bearer ${token}` },
            () => {
                setConnected(true);

                stompClient.current?.subscribe(`/topic/room.${roomId}`, (message) => {
                    onMessage?.(JSON.parse(message.body));
                });

                stompClient.current?.subscribe(`/topic/room.${roomId}.typing`, (message) => {
                    onTyping?.(JSON.parse(message.body));
                });

                stompClient.current?.subscribe('/topic/user.status', (message) => {
                    onUserStatus?.(JSON.parse(message.body));
                });

                stompClient.current?.send(`/app/chat.join.${roomId}`, {}, JSON.stringify({}));
            },
            (error) => {
                console.error('WebSocket error:', error);
                setConnected(false);
            }
        );
    }, [token, roomId, onMessage, onUserStatus, onTyping]);

    const disconnect = useCallback(() => {
        if (stompClient.current) {
            stompClient.current.send(`/app/chat.leave.${roomId}`, {}, JSON.stringify({}));
            stompClient.current.disconnect(() => {
                setConnected(false);
            });
        }
    }, [roomId]);

    const sendMessage = useCallback((content: string) => {
        if (stompClient.current && connected) {
            stompClient.current.send(
                `/app/chat.send.${roomId}`,
                {},
                JSON.stringify({ content, type: 'TEXT' })
            );
        }
    }, [roomId, connected]);

    const sendTyping = useCallback(() => {
        if (stompClient.current && connected) {
            stompClient.current.send(`/app/chat.typing.${roomId}`, {}, JSON.stringify({}));
        }
    }, [roomId, connected]);

    useEffect(() => {
        connect();
        return () => disconnect();
    }, [connect, disconnect]);

    return {
        connected,
        sendMessage,
        sendTyping,
        disconnect
    };
}
```

---

## 6. 错误处理

### 6.1 常见错误

| 错误类型 | 说明 | 解决方案 |
|----------|------|----------|
| Unauthorized | Token 无效或过期 | 重新登录获取新 Token |
| Forbidden | 权限不足 | 确认用户已加入聊天室 |
| Bad Request | 请求格式错误 | 检查 JSON 格式是否正确 |
| Message Too Large | 消息超过大小限制 | 减小消息内容大小 |
| Rate Limited | 发送频率过高 | 降低消息发送频率 |

### 6.2 错误处理示例

```javascript
stompClient.connect(headers, 
    function(frame) {
        console.log('Connected');
    },
    function(error) {
        console.error('Connection failed:', error);
        
        if (error.headers && error.headers.message) {
            if (error.headers.message.includes('Unauthorized')) {
                window.location.href = '/login';
            }
        }
    }
);
```

### 6.3 自动重连

```javascript
function connectWithRetry(maxRetries = 5, retryDelay = 3000) {
    let retries = 0;

    function connect() {
        const socket = new SockJS('http://localhost:8080/api/ws');
        const client = Stomp.over(socket);

        client.connect(
            { 'Authorization': 'Bearer ' + token },
            function(frame) {
                retries = 0;
                console.log('Connected');
                stompClient = client;
            },
            function(error) {
                console.error('Connection error:', error);
                
                if (retries < maxRetries) {
                    retries++;
                    console.log(`Retrying... (${retries}/${maxRetries})`);
                    setTimeout(connect, retryDelay);
                } else {
                    console.error('Max retries reached');
                }
            }
        );
    }

    connect();
}
```

---

## 7. 配置参数

### application.yaml 配置

```yaml
app:
  web-socket:
    endpoint: /ws                    # WebSocket 端点路径
    heartbeat-interval: 30000        # 心跳间隔(毫秒)
    heartbeat-timeout: 90000         # 心跳超时(毫秒)
    message-size-limit: 131072       # 消息大小限制(字节), 128KB
    send-buffer-size-limit: 524288   # 发送缓冲区大小(字节), 512KB
    send-time-limit: 20000           # 发送超时(毫秒)
    time-to-first-message: 30000     # 首条消息超时(毫秒)
    allowed-origins:                 # 允许的跨域来源
      - "*"
```

### 参数说明

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `endpoint` | `/ws` | WebSocket 连接端点 |
| `heartbeat-interval` | 30000 | 心跳发送间隔，单位毫秒 |
| `heartbeat-timeout` | 90000 | 心跳超时时间，超过此时间未收到心跳则判定离线 |
| `message-size-limit` | 131072 | 单条消息最大大小，单位字节 |
| `send-buffer-size-limit` | 524288 | 发送缓冲区最大大小 |
| `send-time-limit` | 20000 | 消息发送超时时间 |
| `time-to-first-message` | 30000 | 连接后等待首条消息的超时时间 |
| `allowed-origins` | `*` | 允许的跨域来源列表 |

---

## 附录: 接口汇总

### 发送接口

| 目的地址 | 操作 | 说明 |
|----------|------|------|
| `/app/chat.send.{roomId}` | SEND | 发送聊天消息 |
| `/app/chat.join.{roomId}` | SEND | 加入聊天室 |
| `/app/chat.leave.{roomId}` | SEND | 离开聊天室 |
| `/app/chat.typing.{roomId}` | SEND | 发送输入状态 |
| `/app/user.status` | SEND | 更新用户状态 |
| `/app/heartbeat` | SEND | 手动心跳 |

### 订阅接口

| 订阅地址 | 说明 | 消息类型 |
|----------|------|----------|
| `/topic/room.{roomId}` | 聊天室消息 | MessageDTO |
| `/topic/room.{roomId}.typing` | 输入状态提示 | TypingEvent |
| `/topic/user.status` | 用户状态变更 | StatusEvent |
| `/user/queue/notifications` | 个人通知 | NotificationDTO |
