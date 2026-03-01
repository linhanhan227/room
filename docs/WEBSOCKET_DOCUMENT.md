# WebSocket 接口文档

## 目录

1. [概述](#概述)
2. [连接配置](#连接配置)
3. [认证方式](#认证方式)
4. [消息订阅](#消息订阅)
5. [消息发送](#消息发送)
6. [心跳机制](#心跳机制)
7. [使用示例](#使用示例)
8. [错误处理](#错误处理)
9. [配置参数](#配置参数)

---

## 概述

本项目使用 STOMP 协议 over WebSocket 实现实时通信功能，支持以下特性：

- 实时消息收发
- 聊天室加入/离开通知
- 用户在线状态管理
- 输入状态提示
- 心跳检测机制
- 自动重连支持

### 技术栈

| 技术 | 说明 |
|------|------|
| WebSocket | 双向通信协议 |
| STOMP | 简单文本定向消息协议 |
| SockJS | WebSocket 降级方案 |

---

## 连接配置

### 连接端点

| 端点 | 说明 |
|------|------|
| `/ws` | WebSocket 主端点 |
| `/ws` (SockJS) | 支持 SockJS 降级的端点 |

### 连接 URL

```
ws://localhost:8080/api/ws
```

生产环境：
```
wss://your-domain.com/api/ws
```

---

## 认证方式

### JWT Token 认证

WebSocket 连接时需要在 STOMP CONNECT 帧中携带 JWT Token：

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

### 认证流程

```
┌─────────────┐     CONNECT + Token     ┌─────────────┐
│   Client    │ ──────────────────────> │   Server    │
└─────────────┘                         └─────────────┘
                                              │
                                              ▼
                                        验证 Token
                                              │
                     ┌────────────────────────┴────────────────────────┐
                     │                                                  │
                     ▼                                                  ▼
              Token 有效                                          Token 无效
                     │                                                  │
                     ▼                                                  ▼
              连接成功                                           连接被拒绝
```

---

## 消息订阅

### 订阅地址列表

| 订阅地址 | 说明 | 消息类型 |
|----------|------|----------|
| `/topic/room.{roomId}` | 聊天室消息 | MessageDTO |
| `/topic/room.{roomId}.typing` | 输入状态提示 | TypingEvent |
| `/topic/user.status` | 用户状态变更 | StatusEvent |
| `/user/queue/notifications` | 个人通知 | NotificationDTO |

### 订阅聊天室消息

```javascript
stompClient.subscribe('/topic/room.' + roomId, function(message) {
    const msg = JSON.parse(message.body);
    console.log('Received message:', msg);
});
```

### 订阅输入状态

```javascript
stompClient.subscribe('/topic/room.' + roomId + '.typing', function(message) {
    const typingEvent = JSON.parse(message.body);
    console.log(typingEvent.username + ' is typing...');
});
```

### 订阅用户状态

```javascript
stompClient.subscribe('/topic/user.status', function(message) {
    const statusEvent = JSON.parse(message.body);
    console.log('User ' + statusEvent.userId + ' is now ' + statusEvent.status);
});
```

---

## 消息发送

### 发送聊天消息

**目的地**: `/app/chat.send.{roomId}`

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | String | 是 | 消息内容 |
| type | String | 否 | 消息类型: TEXT/IMAGE/FILE，默认 TEXT |

**示例**:

```javascript
stompClient.send('/app/chat.send.' + roomId, {}, JSON.stringify({
    content: 'Hello, World!',
    type: 'TEXT'
}));
```

**响应消息格式 (MessageDTO)**:

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

### 加入聊天室

**目的地**: `/app/chat.join.{roomId}`

**请求体**: 无

**示例**:

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

### 离开聊天室

**目的地**: `/app/chat.leave.{roomId}`

**请求体**: 无

**示例**:

```javascript
stompClient.send('/app/chat.leave.' + roomId, {}, JSON.stringify({}));
```

### 发送输入状态

**目的地**: `/app/chat.typing.{roomId}`

**请求体**: 无

**示例**:

```javascript
// 用户开始输入时发送
stompClient.send('/app/chat.typing.' + roomId, {}, JSON.stringify({}));
```

**响应消息格式**:

```json
{
    "userId": 1,
    "username": "张三",
    "typing": true
}
```

### 更新用户状态

**目的地**: `/app/user.status`

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 状态: ONLINE/OFFLINE/AFK/BUSY |

**示例**:

```javascript
stompClient.send('/app/user.status', {}, JSON.stringify({
    status: 'BUSY'
}));
```

---

## 心跳机制

### 心跳配置

| 参数 | 默认值 | 说明 |
|------|--------|------|
| 心跳间隔 | 30000ms | 客户端/服务端心跳发送间隔 |
| 心跳超时 | 90000ms | 超过此时间未收到心跳则判定离线 |

### 心跳流程

```
┌─────────────┐                         ┌─────────────┐
│   Client    │                         │   Server    │
└─────────────┘                         └─────────────┘
       │                                      │
       │  ────────  CONNECT  ──────────────>  │
       │                                      │
       │  <───────  CONNECTED  ───────────    │
       │                                      │
       │  <───────  HEARTBEAT  ───────────    │
       │  ────────  HEARTBEAT  ────────────>  │
       │                                      │
       │  <───────  HEARTBEAT  ───────────    │
       │  ────────  HEARTBEAT  ────────────>  │
       │                                      │
       │         (持续心跳交换)                 │
       │                                      │
```

### 自动心跳

STOMP 客户端会自动处理心跳，无需手动发送。但可以发送手动心跳：

**目的地**: `/app/heartbeat`

```javascript
stompClient.send('/app/heartbeat', {}, JSON.stringify({}));
```

### 超时处理

当用户超过 90 秒未发送心跳，服务端将：

1. 将用户状态更新为 `OFFLINE`
2. 广播用户离线状态到 `/topic/user.status`
3. 从心跳监控列表中移除用户

---

## 使用示例

### 完整 JavaScript 示例

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
        let typingTimeout;
        document.getElementById('messageInput').addEventListener('input', function() {
            if (stompClient) {
                stompClient.send('/app/chat.typing.' + roomId, {}, JSON.stringify({}));
            }
        });
    </script>
</body>
</html>
```

### Vue 3 + TypeScript 示例

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

### React Hook 示例

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

                // 订阅消息
                stompClient.current?.subscribe(`/topic/room.${roomId}`, (message) => {
                    onMessage?.(JSON.parse(message.body));
                });

                // 订阅输入状态
                stompClient.current?.subscribe(`/topic/room.${roomId}.typing`, (message) => {
                    onTyping?.(JSON.parse(message.body));
                });

                // 订阅用户状态
                stompClient.current?.subscribe('/topic/user.status', (message) => {
                    onUserStatus?.(JSON.parse(message.body));
                });

                // 加入房间
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

## 错误处理

### 常见错误

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 无 Token | 连接时未提供认证信息 | 在连接头中添加 Authorization |
| Token 无效 | JWT Token 过期或无效 | 重新登录获取新 Token |
| 权限不足 | 用户不在聊天室中 | 先调用 REST API 加入聊天室 |
| 消息过大 | 消息超过大小限制 | 减小消息内容大小 |

### 错误处理示例

```javascript
stompClient.connect(headers, 
    function(frame) {
        // 连接成功
        console.log('Connected');
    },
    function(error) {
        // 连接失败
        console.error('Connection failed:', error);
        
        if (error.headers && error.headers.message) {
            if (error.headers.message.includes('Unauthorized')) {
                // Token 无效，需要重新登录
                window.location.href = '/login';
            }
        }
    }
);
```

### 自动重连

```javascript
function connectWithRetry(maxRetries = 5, retryDelay = 3000) {
    let retries = 0;

    function connect() {
        const socket = new SockJS('http://localhost:8080/api/ws');
        const client = Stomp.over(socket);

        client.connect(
            { 'Authorization': 'Bearer ' + token },
            function(frame) {
                retries = 0; // 重置重试计数
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

## 配置参数

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

## 消息类型枚举

### Message.MessageType

| 值 | 说明 |
|------|------|
| `TEXT` | 文本消息 |
| `IMAGE` | 图片消息 |
| `FILE` | 文件消息 |
| `SYSTEM` | 系统消息 |

### User.UserStatus

| 值 | 说明 |
|------|------|
| `ONLINE` | 在线 |
| `OFFLINE` | 离线 |
| `AFK` | 离开 |
| `BUSY` | 忙碌 |

---

## 最佳实践

### 1. 连接管理

- 在用户登录成功后建立 WebSocket 连接
- 在用户退出登录时断开连接
- 实现自动重连机制

### 2. 消息发送

- 发送消息前检查连接状态
- 对消息内容进行敏感词过滤（服务端已实现）
- 限制消息发送频率

### 3. 输入状态

- 使用防抖（debounce）控制输入状态发送频率
- 在用户停止输入一段时间后发送停止输入状态

### 4. 错误处理

- 捕获并处理所有可能的错误
- 提供用户友好的错误提示
- 记录错误日志便于排查

### 5. 性能优化

- 避免频繁订阅/取消订阅
- 合理设置心跳间隔
- 离开页面时及时断开连接
