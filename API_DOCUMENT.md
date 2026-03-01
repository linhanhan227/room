# Chat Room API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **协议**: HTTP/HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Bearer Token

---

## 通用说明

### 认证方式

除认证接口外，其他接口均需要在请求头中携带JWT Token：

```
Authorization: Bearer <token>
```

### 统一响应格式

```json
{
  "success": true,
  "message": "操作成功",
  "data": {},
  "timestamp": 1700000000000
}
```

### 分页响应格式

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  },
  "timestamp": 1700000000000
}
```

### 错误响应格式

```json
{
  "success": false,
  "message": "错误信息描述",
  "data": null,
  "timestamp": 1700000000000
}
```

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 资源创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 认证模块 (Auth)

### 1.1 用户注册

**接口地址**: `POST /auth/register`

**权限要求**: 无

**请求体**:
```json
{
  "username": "testuser",
  "password": "password123",
  "nickname": "测试用户",
  "email": "test@example.com"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名，3-50字符 |
| password | String | 是 | 密码，6-100字符 |
| nickname | String | 否 | 昵称，最大50字符 |
| email | String | 否 | 邮箱，最大100字符 |

**响应示例**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "email": "test@example.com",
      "avatar": null,
      "status": "OFFLINE",
      "role": "USER",
      "createdAt": "2024-01-01T10:00:00"
    }
  },
  "timestamp": 1700000000000
}
```

---

### 1.2 用户登录

**接口地址**: `POST /auth/login`

**权限要求**: 无

**请求体**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "email": "test@example.com",
      "avatar": null,
      "status": "ONLINE",
      "role": "USER",
      "createdAt": "2024-01-01T10:00:00"
    }
  },
  "timestamp": 1700000000000
}
```

---

### 1.3 用户登出

**接口地址**: `POST /auth/logout`

**权限要求**: 需要登录

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null,
  "timestamp": 1700000000000
}
```

---

## 2. 邮箱验证模块 (Email Verification)

### 2.1 发送验证码

**接口地址**: `POST /email/verification/send`

**权限要求**: 无

**请求体**:
```json
{
  "email": "user@example.com",
  "type": "REGISTER"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| type | String | 是 | 验证类型：REGISTER/RESET_PASSWORD/CHANGE_EMAIL |

**验证类型说明**:

| 类型 | 说明 |
|------|------|
| REGISTER | 注册验证 |
| RESET_PASSWORD | 重置密码验证 |
| CHANGE_EMAIL | 更改邮箱验证 |

**响应示例**:
```json
{
  "success": true,
  "message": "验证码发送成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**错误响应示例**:
```json
{
  "success": false,
  "message": "该邮箱已被注册",
  "data": null,
  "timestamp": 1700000000000
}
```

**限制说明**:
- 每小时最多发送5次验证码
- 两次发送间隔至少60秒
- 验证码有效期为5分钟

---

### 2.2 验证邮箱

**接口地址**: `POST /email/verification/verify`

**权限要求**: 无

**请求体**:
```json
{
  "email": "user@example.com",
  "code": "123456",
  "type": "REGISTER"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| code | String | 是 | 验证码 |
| type | String | 是 | 验证类型 |

**响应示例（成功）**:
```json
{
  "success": true,
  "message": "邮箱验证成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**响应示例（失败）**:
```json
{
  "success": false,
  "message": "验证码无效或已过期",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 2.3 检查验证状态

**接口地址**: `GET /email/verification/status`

**权限要求**: 无

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |
| type | String | 是 | 验证类型 |

**请求示例**:
```
GET /email/verification/status?email=user@example.com&type=REGISTER
```

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": true,
  "timestamp": 1700000000000
}
```

**响应说明**:
- `data: true` - 存在有效的验证码
- `data: false` - 不存在有效验证码

---

## 3. 用户模块 (Users)

### 3.1 获取当前用户信息

**接口地址**: `GET /users/me`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "avatar": "https://example.com/avatar.png",
    "status": "ONLINE",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.2 根据ID获取用户信息

**接口地址**: `GET /users/{id}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "avatar": null,
    "status": "ONLINE",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.3 根据用户名获取用户信息

**接口地址**: `GET /users/username/{username}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |

**响应示例**: 同上

---

### 3.4 搜索用户

**接口地址**: `GET /users/search`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "email": "test@example.com",
        "avatar": null,
        "status": "ONLINE",
        "role": "USER",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  },
  "timestamp": 1700000000000
}
```

---

### 3.5 获取在线用户列表

**接口地址**: `GET /users/online`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "email": "test@example.com",
      "avatar": null,
      "status": "ONLINE",
      "role": "USER",
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 3.6 获取在线用户数量

**接口地址**: `GET /users/online/count`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": 5,
  "timestamp": 1700000000000
}
```

---

### 3.7 更新用户资料

**接口地址**: `PUT /users/me`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称 |
| avatar | String | 否 | 头像URL |
| email | String | 否 | 邮箱 |

**请求示例**:
```
PUT /users/me?nickname=新昵称&avatar=https://example.com/avatar.png
```

**响应示例**:
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "新昵称",
    "email": "test@example.com",
    "avatar": "https://example.com/avatar.png",
    "status": "ONLINE",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.8 修改密码

**接口地址**: `PUT /users/me/password`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 原密码 |
| newPassword | String | 是 | 新密码 |

**请求示例**:
```
PUT /users/me/password?oldPassword=oldpass&newPassword=newpass123
```

**响应示例**:
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 3.9 更新用户状态

**接口地址**: `PUT /users/{id}/status`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 状态：ONLINE/OFFLINE/BUZY/AWAY |

**响应示例**:
```json
{
  "success": true,
  "message": "Status updated successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "status": "AWAY",
    "role": "USER"
  },
  "timestamp": 1700000000000
}
```

---

## 4. 聊天室模块 (Rooms)

### 4.1 创建聊天室

**接口地址**: `POST /rooms`

**权限要求**: 需要登录

**请求体**:
```json
{
  "name": "技术交流群",
  "description": "讨论技术问题的聊天室",
  "avatar": "https://example.com/room.png",
  "type": "PUBLIC",
  "password": null,
  "maxMembers": 100
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 聊天室名称，1-100字符 |
| description | String | 否 | 描述，最大500字符 |
| avatar | String | 否 | 头像URL |
| type | String | 否 | 类型：PUBLIC/PRIVATE/GROUP，默认PUBLIC |
| password | String | 否 | 私密房间密码 |
| maxMembers | Integer | 否 | 最大成员数，默认100 |

**响应示例**:
```json
{
  "success": true,
  "message": "Room created successfully",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "description": "讨论技术问题的聊天室",
    "avatar": null,
    "ownerId": 1,
    "ownerName": "测试用户",
    "type": "PUBLIC",
    "maxMembers": 100,
    "status": "ACTIVE",
    "memberCount": 1,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.2 加入聊天室

**接口地址**: `POST /rooms/{roomId}/join`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| password | String | 否 | 私密房间密码 |

**响应示例**:
```json
{
  "success": true,
  "message": "Joined room successfully",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "memberCount": 2
  },
  "timestamp": 1700000000000
}
```

---

### 3.3 退出聊天室

**接口地址**: `POST /rooms/{roomId}/leave`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Left room successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 3.4 删除聊天室

**接口地址**: `DELETE /rooms/{roomId}`

**权限要求**: 聊天室所有者

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Room deleted successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 3.5 获取聊天室详情

**接口地址**: `GET /rooms/{roomId}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "description": "讨论技术问题的聊天室",
    "avatar": null,
    "ownerId": 1,
    "ownerName": "测试用户",
    "type": "PUBLIC",
    "maxMembers": 100,
    "status": "ACTIVE",
    "memberCount": 50,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.6 获取公开聊天室列表

**接口地址**: `GET /rooms/public`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "技术交流群",
        "description": "讨论技术问题",
        "ownerId": 1,
        "ownerName": "测试用户",
        "type": "PUBLIC",
        "memberCount": 50,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "size": 10,
    "number": 0
  },
  "timestamp": 1700000000000
}
```

---

### 3.7 获取我的聊天室列表

**接口地址**: `GET /rooms/my`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "name": "技术交流群",
      "description": "讨论技术问题",
      "ownerId": 1,
      "ownerName": "测试用户",
      "type": "PUBLIC",
      "memberCount": 50,
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 3.8 搜索聊天室

**接口地址**: `GET /rooms/search`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

**响应示例**: 同获取公开聊天室列表

---

### 3.9 获取聊天室成员列表

**接口地址**: `GET /rooms/{roomId}/members`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": null,
      "status": "ONLINE"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 3.10 踢出成员

**接口地址**: `DELETE /rooms/{roomId}/members/{userId}`

**权限要求**: 聊天室管理员或所有者

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Member kicked successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 3.11 设置成员角色

**接口地址**: `PUT /rooms/{roomId}/members/{userId}/role`

**权限要求**: 聊天室所有者

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 是 | 角色：OWNER/ADMIN/MEMBER |

**响应示例**:
```json
{
  "success": true,
  "message": "Member role updated successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 3.12 更新聊天室信息

**接口地址**: `PUT /rooms/{roomId}`

**权限要求**: 聊天室所有者

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求体**:
```json
{
  "name": "新名称",
  "description": "新描述",
  "avatar": "https://example.com/new-avatar.png",
  "maxMembers": 200
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "Room updated successfully",
  "data": {
    "id": 1,
    "name": "新名称",
    "description": "新描述",
    "memberCount": 50
  },
  "timestamp": 1700000000000
}
```

---

## 5. 消息模块 (Messages)

### 4.1 发送消息

**接口地址**: `POST /messages`

**权限要求**: 需要登录

**请求体**:
```json
{
  "roomId": 1,
  "content": "大家好！",
  "type": "TEXT"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| content | String | 是 | 消息内容 |
| type | String | 否 | 类型：TEXT/IMAGE/FILE/SYSTEM/EMOJI，默认TEXT |

**响应示例**:
```json
{
  "success": true,
  "message": "Message sent successfully",
  "data": {
    "id": 1,
    "roomId": 1,
    "senderId": 1,
    "senderName": "测试用户",
    "senderAvatar": null,
    "content": "大家好！",
    "type": "TEXT",
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 4.2 获取聊天室消息列表

**接口地址**: `GET /messages/room/{roomId}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 20 | 每页数量 |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "roomId": 1,
        "senderId": 1,
        "senderName": "测试用户",
        "senderAvatar": null,
        "content": "大家好！",
        "type": "TEXT",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  },
  "timestamp": 1700000000000
}
```

---

### 4.3 获取最近消息

**接口地址**: `GET /messages/room/{roomId}/recent`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| limit | int | 否 | 50 | 消息数量限制 |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "roomId": 1,
      "senderId": 1,
      "senderName": "测试用户",
      "content": "大家好！",
      "type": "TEXT",
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 4.4 搜索消息

**接口地址**: `GET /messages/room/{roomId}/search`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 20 | 每页数量 |

**响应示例**: 同获取聊天室消息列表

---

### 4.5 删除消息

**接口地址**: `DELETE /messages/{messageId}`

**权限要求**: 消息发送者

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageId | Long | 是 | 消息ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Message deleted successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 4.6 获取消息数量

**接口地址**: `GET /messages/room/{roomId}/count`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": 1500,
  "timestamp": 1700000000000
}
```

---

## 6. 管理模块 (Admin)

> 所有管理接口需要 ADMIN 角色

### 5.1 获取仪表盘统计

**接口地址**: `GET /admin/dashboard`

**权限要求**: ADMIN

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "totalUsers": 1000,
    "onlineUsers": 50,
    "totalRooms": 100,
    "activeRooms": 80,
    "totalMessages": 50000,
    "todayMessages": 500,
    "bannedUsers": 10,
    "sensitiveWordCount": 200,
    "topActiveRooms": [
      {
        "roomId": 1,
        "roomName": "技术交流群",
        "messageCount": 5000,
        "memberCount": 100
      }
    ]
  },
  "timestamp": 1700000000000
}
```

---

### 5.2 获取所有用户

**接口地址**: `GET /admin/users`

**权限要求**: ADMIN

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

**响应示例**: 分页用户列表

---

### 5.3 搜索用户

**接口地址**: `GET /admin/users/search`

**权限要求**: ADMIN

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

**响应示例**: 分页用户列表

---

### 5.4 设置用户角色

**接口地址**: `PUT /admin/users/{userId}/role`

**权限要求**: ADMIN

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 是 | 角色：USER/ADMIN |

**响应示例**:
```json
{
  "success": true,
  "message": "User role updated successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.5 封禁用户

**接口地址**: `POST /admin/users/ban`

**权限要求**: ADMIN

**请求体**:
```json
{
  "userId": 1,
  "reason": "违规发言",
  "type": "TEMPORARY",
  "endTime": "2024-02-01T00:00:00"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| reason | String | 否 | 封禁原因 |
| type | String | 是 | 类型：PERMANENT/TEMPORARY/WARNING |
| endTime | DateTime | 否 | 结束时间（临时封禁必填） |

**响应示例**:
```json
{
  "success": true,
  "message": "User banned successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "username": "testuser",
    "userNickname": "测试用户",
    "bannedById": 2,
    "bannedByName": "管理员",
    "reason": "违规发言",
    "type": "TEMPORARY",
    "endTime": "2024-02-01T00:00:00",
    "active": true,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 5.6 解封用户

**接口地址**: `DELETE /admin/users/{userId}/ban`

**权限要求**: ADMIN

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
  "success": true,
  "message": "User unbanned successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.7 获取封禁用户列表

**接口地址**: `GET /admin/users/banned`

**权限要求**: ADMIN

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

**响应示例**: 分页封禁用户列表

---

### 5.8 获取用户封禁状态

**接口地址**: `GET /admin/users/{userId}/ban`

**权限要求**: ADMIN

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**: 封禁信息详情

---

### 5.9 获取所有聊天室

**接口地址**: `GET /admin/rooms`

**权限要求**: ADMIN

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

**响应示例**: 分页聊天室列表

---

### 5.10 删除聊天室

**接口地址**: `DELETE /admin/rooms/{roomId}`

**权限要求**: ADMIN

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Room deleted successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.11 归档聊天室

**接口地址**: `PUT /admin/rooms/{roomId}/archive`

**权限要求**: ADMIN

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Room archived successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.12 删除消息

**接口地址**: `DELETE /admin/messages/{messageId}`

**权限要求**: ADMIN

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageId | Long | 是 | 消息ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Message deleted successfully",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.13 获取系统日志

**接口地址**: `GET /admin/logs`

**权限要求**: ADMIN

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 20 | 每页数量 |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "action": "BAN_USER",
        "entityType": "User",
        "entityId": 1,
        "operatorId": 2,
        "operatorName": "管理员",
        "targetUserId": 1,
        "details": "Banned user: testuser, reason: 违规发言",
        "ipAddress": null,
        "level": "INFO",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5
  },
  "timestamp": 1700000000000
}
```

---

### 5.14 获取日志操作类型列表

**接口地址**: `GET /admin/logs/actions`

**权限要求**: ADMIN

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": ["BAN_USER", "UNBAN_USER", "DELETE_ROOM", "DELETE_MESSAGE", "CHANGE_ROLE"],
  "timestamp": 1700000000000
}
```

---

### 5.15 按操作类型查询日志

**接口地址**: `GET /admin/logs/by-action`

**权限要求**: ADMIN

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| action | String | 是 | - | 操作类型 |
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 20 | 每页数量 |

**响应示例**: 分页日志列表

---

### 5.16 按日期范围查询日志

**接口地址**: `GET /admin/logs/by-date`

**权限要求**: ADMIN

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| start | Date | 是 | 开始日期 (ISO格式: 2024-01-01) |
| end | Date | 是 | 结束日期 (ISO格式: 2024-01-31) |
| page | int | 否 | 页码 |
| size | int | 否 | 每页数量 |

**响应示例**: 分页日志列表

---

### 5.17 按用户查询日志

**接口地址**: `GET /admin/logs/by-user/{userId}`

**权限要求**: ADMIN

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 20 | 每页数量 |

**响应示例**: 分页日志列表

---

## 7. 敏感词管理模块 (Sensitive Words)

> 所有敏感词管理接口需要 ADMIN 角色
> 敏感词支持三种高效过滤算法：KMP、Trie树、AC自动机
> 敏感词存储在txt文件中，支持热加载

### 算法说明

| 算法 | 说明 | 时间复杂度 | 适用场景 |
|------|------|-----------|---------|
| KMP | Knuth-Morris-Pratt字符串匹配算法 | O(n+m) | 单个敏感词匹配效率高 |
| Trie | 前缀树算法 | O(n×m) | 前缀匹配，适合大量敏感词 |
| AC | Aho-Corasick自动机算法 | O(n) | 多模式匹配，推荐使用 |

### 7.1 添加敏感词

**接口地址**: `POST /admin/sensitive-words`

**权限要求**: ADMIN

**请求体**: 纯文本字符串（敏感词）

**响应示例**:
```json
{
  "success": true,
  "message": "敏感词添加成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 7.2 批量添加敏感词

**接口地址**: `POST /admin/sensitive-words/batch`

**权限要求**: ADMIN

**请求体**:
```json
["敏感词1", "敏感词2", "敏感词3"]
```

**响应示例**:
```json
{
  "success": true,
  "message": "批量添加敏感词成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 7.3 删除敏感词

**接口地址**: `DELETE /admin/sensitive-words`

**权限要求**: ADMIN

**请求体**: 纯文本字符串（敏感词）

**响应示例**:
```json
{
  "success": true,
  "message": "敏感词删除成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 7.4 批量删除敏感词

**接口地址**: `DELETE /admin/sensitive-words/batch`

**权限要求**: ADMIN

**请求体**:
```json
["敏感词1", "敏感词2"]
```

**响应示例**:
```json
{
  "success": true,
  "message": "批量删除敏感词成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 7.5 获取所有敏感词

**接口地址**: `GET /admin/sensitive-words`

**权限要求**: ADMIN

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": ["敏感词1", "敏感词2", "敏感词3"],
  "timestamp": 1700000000000
}
```

---

### 7.6 获取敏感词数量

**接口地址**: `GET /admin/sensitive-words/count`

**权限要求**: ADMIN

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": 200,
  "timestamp": 1700000000000
}
```

---

### 7.7 重新加载敏感词

**接口地址**: `POST /admin/sensitive-words/reload`

**权限要求**: ADMIN

**说明**: 从文件重新加载敏感词到内存

**响应示例**:
```json
{
  "success": true,
  "message": "敏感词库重新加载成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 7.8 保存敏感词到文件

**接口地址**: `POST /admin/sensitive-words/save`

**权限要求**: ADMIN

**说明**: 将当前内存中的敏感词保存到文件

**响应示例**:
```json
{
  "success": true,
  "message": "敏感词已保存到文件",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 7.9 检查文本是否包含敏感词

**接口地址**: `POST /admin/sensitive-words/check`

**权限要求**: ADMIN

**请求体**: 纯文本字符串

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": true,
  "timestamp": 1700000000000
}
```

---

### 7.10 过滤敏感词

**接口地址**: `POST /admin/sensitive-words/filter`

**权限要求**: ADMIN

**请求体**: 纯文本字符串

**响应示例**:
```json
{
  "success": true,
  "message": "过滤完成",
  "data": "这是一段***过滤后的文本",
  "timestamp": 1700000000000
}
```

---

### 7.11 查找文本中的敏感词

**接口地址**: `POST /admin/sensitive-words/find`

**权限要求**: ADMIN

**请求体**: 纯文本字符串

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": ["敏感词1", "敏感词2"],
  "timestamp": 1700000000000
}
```
}
```

---

## 8. WebSocket 接口

### 7.1 连接端点

**WebSocket URL**: `ws://localhost:8080/api/ws`

**认证方式**: 在连接时传递Token

```javascript
// SockJS 连接示例
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, function(frame) {
  console.log('Connected: ' + frame);
});
```

### 7.2 订阅主题

| 主题 | 说明 |
|------|------|
| `/topic/room.{roomId}` | 接收聊天室消息 |
| `/topic/room.{roomId}.typing` | 接收输入状态 |
| `/topic/user.status` | 接收用户在线状态变化 |

### 7.3 发送消息

**发送目标**: `/app/chat.send.{roomId}`

**消息格式**:
```json
{
  "roomId": 1,
  "content": "大家好！",
  "type": "TEXT"
}
```

### 7.4 加入聊天室通知

**发送目标**: `/app/chat.join.{roomId}`

### 7.5 离开聊天室通知

**发送目标**: `/app/chat.leave.{roomId}`

### 7.6 输入状态

**发送目标**: `/app/chat.typing.{roomId}`

### 7.7 用户状态更新

**发送目标**: `/app/user.status`

**消息格式**:
```json
{
  "status": "ONLINE"
}
```

---

## 9. 数据模型

### 8.1 User (用户)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| nickname | String | 昵称 |
| email | String | 邮箱 |
| avatar | String | 头像URL |
| status | String | 状态：ONLINE/OFFLINE/BUZY/AWAY |
| role | String | 角色：USER/ADMIN |
| createdAt | DateTime | 创建时间 |

### 8.2 ChatRoom (聊天室)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 聊天室ID |
| name | String | 名称 |
| description | String | 描述 |
| avatar | String | 头像URL |
| ownerId | Long | 所有者ID |
| ownerName | String | 所有者名称 |
| type | String | 类型：PUBLIC/PRIVATE/GROUP |
| maxMembers | Integer | 最大成员数 |
| status | String | 状态：ACTIVE/INACTIVE/ARCHIVED |
| memberCount | Integer | 成员数量 |
| createdAt | DateTime | 创建时间 |

### 8.3 Message (消息)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 消息ID |
| roomId | Long | 聊天室ID |
| senderId | Long | 发送者ID |
| senderName | String | 发送者名称 |
| senderAvatar | String | 发送者头像 |
| content | String | 消息内容 |
| type | String | 类型：TEXT/IMAGE/FILE/SYSTEM/EMOJI |
| createdAt | DateTime | 创建时间 |

### 8.4 BannedUser (封禁用户)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录ID |
| userId | Long | 用户ID |
| username | String | 用户名 |
| userNickname | String | 用户昵称 |
| bannedById | Long | 封禁者ID |
| bannedByName | String | 封禁者名称 |
| reason | String | 封禁原因 |
| type | String | 类型：PERMANENT/TEMPORARY/WARNING |
| endTime | DateTime | 结束时间 |
| active | Boolean | 是否生效 |
| createdAt | DateTime | 创建时间 |

### 8.5 SensitiveWord (敏感词)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 敏感词ID |
| word | String | 敏感词 |
| category | String | 分类 |
| level | Integer | 等级 |
| replacement | String | 替换文本 |
| enabled | Boolean | 是否启用 |

### 8.6 SystemLog (系统日志)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 日志ID |
| action | String | 操作类型 |
| entityType | String | 实体类型 |
| entityId | Long | 实体ID |
| operatorId | Long | 操作者ID |
| operatorName | String | 操作者名称 |
| targetUserId | Long | 目标用户ID |
| details | String | 详情 |
| ipAddress | String | IP地址 |
| level | String | 级别：INFO/WARNING/ERROR/CRITICAL |
| createdAt | DateTime | 创建时间 |

### 9.7 EmailVerification (邮箱验证)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 验证记录ID |
| email | String | 邮箱地址 |
| code | String | 验证码 |
| type | String | 类型：REGISTER/RESET_PASSWORD/CHANGE_EMAIL |
| expiresAt | DateTime | 过期时间 |
| used | Boolean | 是否已使用 |
| createdAt | DateTime | 创建时间 |

---

## 10. 错误码说明

| 错误信息 | 说明 |
|----------|------|
| Username is already taken | 用户名已被使用 |
| Email is already in use | 邮箱已被使用 |
| Invalid username or password | 用户名或密码错误 |
| Your account has been banned | 账号已被封禁 |
| You are banned from sending messages | 您已被禁止发送消息 |
| User not found | 用户不存在 |
| Room not found | 聊天室不存在 |
| Message not found | 消息不存在 |
| You are not a member of this room | 您不是该聊天室成员 |
| Room is full | 聊天室已满 |
| Invalid room password | 聊天室密码错误 |
| Only room owner can delete the room | 只有聊天室所有者才能删除 |
| Cannot ban admin user | 不能封禁管理员用户 |
| User is already banned | 用户已被封禁 |
| Sensitive word already exists | 敏感词已存在 |
| 该邮箱已被注册 | 注册时邮箱已存在 |
| 发送验证码次数过多，请1小时后再试 | 验证码发送频率超限 |
| 请等待X秒后再发送验证码 | 验证码发送间隔限制 |
| 验证码无效或已过期 | 验证码错误或过期 |
| 无效的验证类型 | 验证类型参数错误 |

---

## 11. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2024-01-01 | 初始版本，包含基础聊天功能 |
| v1.1.0 | 2024-01-15 | 新增管理功能和敏感词过滤 |
| v1.2.0 | 2024-01-20 | 新增邮箱验证功能 |
