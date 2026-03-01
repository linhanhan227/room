# Chat Room API 接口文档

## 目录

- [基础信息](#基础信息)
- [通用说明](#通用说明)
  - [认证方式](#认证方式)
  - [统一响应格式](#统一响应格式)
  - [分页响应格式](#分页响应格式)
  - [错误响应格式](#错误响应格式)
  - [HTTP 状态码](#http-状态码)
- [1. 认证模块 (Auth)](#1-认证模块-auth)
  - [1.1 用户注册](#11-用户注册)
  - [1.2 用户登录](#12-用户登录)
  - [1.3 用户登出](#13-用户登出)
- [2. 邮箱验证模块 (Email Verification)](#2-邮箱验证模块-email-verification)
  - [2.1 发送验证码](#21-发送验证码)
  - [2.2 验证邮箱](#22-验证邮箱)
  - [2.3 检查验证状态](#23-检查验证状态)
  - [2.4 获取每日邮件发送限制](#24-获取每日邮件发送限制)
- [3. 举报模块 (Reports)](#3-举报模块-reports)
  - [3.1 提交举报](#31-提交举报)
  - [3.2 获取我的举报记录](#32-获取我的举报记录)
  - [3.3 获取举报详情](#33-获取举报详情)
  - [3.4 获取所有举报列表（管理员）](#34-获取所有举报列表管理员)
  - [3.5 按状态获取举报列表（管理员）](#35-按状态获取举报列表管理员)
  - [3.6 按类型获取举报列表（管理员）](#36-按类型获取举报列表管理员)
  - [3.7 获取举报统计（管理员）](#37-获取举报统计管理员)
  - [3.8 处理举报（管理员）](#38-处理举报管理员)
  - [3.9 删除举报（管理员）](#39-删除举报管理员)
- [4. 用户模块 (Users)](#4-用户模块-users)
  - [4.1 获取当前用户信息](#41-获取当前用户信息)
  - [4.2 根据ID获取用户信息](#42-根据id获取用户信息)
  - [4.3 根据用户名获取用户信息](#43-根据用户名获取用户信息)
  - [4.4 搜索用户](#44-搜索用户)
  - [4.5 获取在线用户列表](#45-获取在线用户列表)
  - [4.6 获取在线用户数量](#46-获取在线用户数量)
  - [4.7 更新用户资料](#47-更新用户资料)
  - [4.8 修改密码](#48-修改密码)
  - [4.9 更新用户状态](#49-更新用户状态)
- [5. 聊天室模块 (Rooms)](#5-聊天室模块-rooms)
  - [5.1 创建聊天室](#51-创建聊天室)
  - [5.2 加入聊天室](#52-加入聊天室)
  - [5.3 退出聊天室](#53-退出聊天室)
  - [5.4 删除聊天室](#54-删除聊天室)
  - [5.5 获取聊天室详情](#55-获取聊天室详情)
  - [5.6 获取公开聊天室列表](#56-获取公开聊天室列表)
  - [5.7 获取我的聊天室列表](#57-获取我的聊天室列表)
  - [5.8 搜索聊天室](#58-搜索聊天室)
  - [5.9 获取聊天室成员列表](#59-获取聊天室成员列表)
  - [5.10 踢出成员](#510-踢出成员)
  - [5.11 设置成员角色](#511-设置成员角色)
  - [5.12 更新聊天室信息](#512-更新聊天室信息)
- [6. 消息模块 (Messages)](#6-消息模块-messages)
  - [6.1 发送消息](#61-发送消息)
  - [6.2 获取聊天室消息列表](#62-获取聊天室消息列表)
  - [6.3 获取最近消息](#63-获取最近消息)
  - [6.4 搜索消息](#64-搜索消息)
  - [6.5 删除消息](#65-删除消息)
  - [6.6 获取消息数量](#66-获取消息数量)
- [7. 管理模块 (Admin)](#7-管理模块)
  - [7.1 获取仪表盘统计](#71-获取仪表盘统计)
  - [7.2 获取所有用户](#72-获取所有用户)
  - [7.3 搜索用户](#73-搜索用户)
  - [7.4 设置用户角色](#74-设置用户角色)
  - [7.5 封禁用户](#75-封禁用户)
  - [7.6 解封用户](#76-解封用户)
  - [7.7 获取封禁用户列表](#77-获取封禁用户列表)
  - [7.8 获取用户封禁状态](#78-获取用户封禁状态)
  - [7.9 获取所有聊天室](#79-获取所有聊天室)
  - [7.10 删除聊天室](#710-删除聊天室)
  - [7.11 归档聊天室](#711-归档聊天室)
  - [7.12 删除消息](#712-删除消息)
  - [7.13 获取系统日志](#713-获取系统日志)
  - [7.14 获取日志操作类型列表](#714-获取日志操作类型列表)
  - [7.15 按操作类型查询日志](#715-按操作类型查询日志)
  - [7.16 按日期范围查询日志](#716-按日期范围查询日志)
  - [7.17 按用户查询日志](#717-按用户查询日志)
- [8. 公告模块 (Announcements)](#8-公告模块-announcements)
  - [8.1 创建公告](#81-创建公告)
  - [8.2 更新公告](#82-更新公告)
  - [8.3 删除公告](#83-删除公告)
  - [8.4 发布公告](#84-发布公告)
  - [8.5 取消发布公告](#85-取消发布公告)
  - [8.6 置顶公告](#86-置顶公告)
  - [8.7 取消置顶公告](#87-取消置顶公告)
  - [8.8 标记公告已读](#88-标记公告已读)
  - [8.9 标记全部已读](#89-标记全部已读)
  - [8.10 获取公告详情](#810-获取公告详情)
  - [8.11 获取已发布公告列表](#811-获取已发布公告列表)
  - [8.12 获取有效公告列表](#812-获取有效公告列表)
  - [8.13 获取置顶公告列表](#813-获取置顶公告列表)
  - [8.14 按类型获取公告](#814-按类型获取公告)
  - [8.15 获取所有公告（管理员）](#815-获取所有公告管理员)
  - [8.16 获取公告统计（管理员）](#816-获取公告统计管理员)
  - [8.17 获取用户阅读统计](#817-获取用户阅读统计)
- [9. 敏感词管理模块 (Sensitive Words)](#9-敏感词管理模块-sensitive-words)
  - [9.1 添加敏感词](#91-添加敏感词)
  - [9.2 批量添加敏感词](#92-批量添加敏感词)
  - [9.3 删除敏感词](#93-删除敏感词)
  - [9.4 批量删除敏感词](#94-批量删除敏感词)
  - [9.5 获取所有敏感词](#95-获取所有敏感词)
  - [9.6 获取敏感词数量](#96-获取敏感词数量)
  - [9.7 重新加载敏感词](#97-重新加载敏感词)
  - [9.8 保存敏感词到文件](#98-保存敏感词到文件)
  - [9.9 检查文本是否包含敏感词](#99-检查文本是否包含敏感词)
  - [9.10 过滤敏感词](#910-过滤敏感词)
  - [9.11 查找文本中的敏感词](#911-查找文本中的敏感词)
- [10. WebSocket 接口](#10-websocket-接口)
  - [10.1 连接端点](#101-连接端点)
  - [10.2 订阅主题](#102-订阅主题)

---

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

### 2.4 获取每日邮件发送限制

**接口地址**: `GET /email/daily-limit`

**权限要求**: 无

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | String | 是 | 邮箱地址 |

**请求示例**:
```
GET /email/daily-limit?email=user@example.com
```

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": 8,
  "timestamp": 1700000000000
}
```

**响应说明**:
- `data: 8` - 今日剩余可发送次数
- `data: -1` - 未启用每日限制
- `data: 0` - 今日已达上限

---

## 3. 举报模块 (Reports)

### 3.1 提交举报

**接口地址**: `POST /reports`

**权限要求**: 需要登录

**请求体**:
```json
{
  "type": "SPAM",
  "targetType": "USER",
  "reportedUserId": 123,
  "reason": "发送垃圾信息",
  "description": "该用户在聊天室中频繁发送广告信息"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 举报类型 |
| targetType | String | 是 | 举报目标类型 |
| reportedUserId | Long | 否 | 被举报用户ID（targetType=USER时必填） |
| reportedRoomId | Long | 否 | 被举报聊天室ID（targetType=ROOM时必填） |
| reportedMessageId | Long | 否 | 被举报消息ID（targetType=MESSAGE时必填） |
| reason | String | 是 | 举报原因 |
| description | String | 否 | 详细描述 |

**举报类型说明**:

| 类型 | 说明 |
|------|------|
| SPAM | 垃圾信息 |
| HARASSMENT | 骚扰 |
| INAPPROPRIATE_CONTENT | 不当内容 |
| VIOLENCE | 暴力 |
| FRAUD | 欺诈 |
| OTHER | 其他 |

**举报目标类型说明**:

| 类型 | 说明 |
|------|------|
| USER | 用户 |
| ROOM | 聊天室 |
| MESSAGE | 消息 |

**响应示例**:
```json
{
  "success": true,
  "message": "举报提交成功",
  "data": {
    "id": 1,
    "reporterId": 1,
    "reporterName": "testuser",
    "reportedUserId": 123,
    "reportedUserName": "baduser",
    "type": "SPAM",
    "targetType": "USER",
    "reason": "发送垃圾信息",
    "status": "PENDING",
    "createdAt": "2024-01-20T10:00:00"
  },
  "timestamp": 1700000000000
}
```

**限制说明**:
- 每个用户每日最多提交10次举报
- 同一目标不能重复举报（待处理状态下）

---

### 3.2 获取我的举报记录

**接口地址**: `GET /reports/my`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:
```
GET /reports/my?page=0&size=10
```

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "reporterId": 1,
        "reporterName": "testuser",
        "reportedUserId": 123,
        "reportedUserName": "baduser",
        "type": "SPAM",
        "targetType": "USER",
        "reason": "发送垃圾信息",
        "status": "RESOLVED",
        "handlerId": 2,
        "handlerName": "admin",
        "handleResult": "已封禁该用户",
        "handledAt": "2024-01-20T11:00:00",
        "createdAt": "2024-01-20T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 3.3 获取举报详情

**接口地址**: `GET /reports/{id}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 举报ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "reporterId": 1,
    "reporterName": "testuser",
    "reportedUserId": 123,
    "reportedUserName": "baduser",
    "type": "SPAM",
    "targetType": "USER",
    "reason": "发送垃圾信息",
    "description": "详细描述",
    "status": "RESOLVED",
    "handlerId": 2,
    "handlerName": "admin",
    "handleResult": "已封禁该用户",
    "handledAt": "2024-01-20T11:00:00",
    "createdAt": "2024-01-20T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.4 获取所有举报列表（管理员）

**接口地址**: `GET /reports`

**权限要求**: 管理员

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:
```
GET /reports?page=0&size=10
```

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 3.5 按状态获取举报列表（管理员）

**接口地址**: `GET /reports/status/{status}`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 举报状态 |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**举报状态说明**:

| 状态 | 说明 |
|------|------|
| PENDING | 待处理 |
| PROCESSING | 处理中 |
| RESOLVED | 已解决 |
| REJECTED | 已拒绝 |

**请求示例**:
```
GET /reports/status/PENDING?page=0&size=10
```

---

### 3.6 按类型获取举报列表（管理员）

**接口地址**: `GET /reports/type/{type}`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 举报类型 |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:
```
GET /reports/type/SPAM?page=0&size=10
```

---

### 3.7 获取举报统计（管理员）

**接口地址**: `GET /reports/statistics`

**权限要求**: 管理员

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "total": 150,
    "pending": 20,
    "processing": 5,
    "resolved": 100,
    "rejected": 25,
    "todayReports": 8
  },
  "timestamp": 1700000000000
}
```

---

### 3.8 处理举报（管理员）

**接口地址**: `PUT /reports/{id}/handle`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 举报ID |

**请求体**:
```json
{
  "status": "RESOLVED",
  "handleResult": "已封禁该用户7天"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 处理状态：PROCESSING/RESOLVED/REJECTED |
| handleResult | String | 是 | 处理结果说明 |

**响应示例**:
```json
{
  "success": true,
  "message": "举报处理成功",
  "data": {
    "id": 1,
    "status": "RESOLVED",
    "handlerId": 2,
    "handlerName": "admin",
    "handleResult": "已封禁该用户7天",
    "handledAt": "2024-01-20T11:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 3.9 删除举报（管理员）

**接口地址**: `DELETE /reports/{id}`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 举报ID |

**响应示例**:
```json
{
  "success": true,
  "message": "举报记录已删除",
  "data": null,
  "timestamp": 1700000000000
}
```

---

## 4. 用户模块 (Users)

### 4.1 获取当前用户信息

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

### 4.2 根据ID获取用户信息

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

### 4.3 根据用户名获取用户信息

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

### 4.7 更新用户资料

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

### 4.9 更新用户状态

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

## 5. 聊天室模块 (Rooms)

### 5.1 创建聊天室

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

### 5.5 获取聊天室详情

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

### 5.7 获取我的聊天室列表

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

### 5.8 搜索聊天室

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

## 6. 消息模块 (Messages)

### 6.1 发送消息

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

### 6.2 获取聊天室消息列表

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

### 6.3 获取最近消息

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

### 6.5 删除消息

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

## 7. 管理模块

> 所有管理接口需要 ADMIN 角色

### 7.1 获取仪表盘统计

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

### 7.4 设置用户角色

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

### 7.6 解封用户

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

### 7.9 获取所有聊天室

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

## 8. 公告模块 (Announcements)

### 8.1 创建公告

**接口地址**: `POST /announcements`

**权限要求**: 管理员

**请求体**:
```json
{
  "title": "系统维护通知",
  "content": "系统将于今晚22:00-23:00进行维护",
  "type": "MAINTENANCE",
  "priority": "HIGH",
  "isPinned": true,
  "isPublished": true,
  "expireAt": "2024-02-01T00:00:00"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 公告标题（最大200字符） |
| content | String | 是 | 公告内容 |
| type | String | 否 | 公告类型 |
| priority | String | 否 | 优先级 |
| isPinned | Boolean | 否 | 是否置顶，默认false |
| isPublished | Boolean | 否 | 是否发布，默认false |
| publishAt | String | 否 | 发布时间（ISO格式） |
| expireAt | String | 否 | 过期时间（ISO格式） |

**公告类型说明**:

| 类型 | 说明 |
|------|------|
| NORMAL | 普通公告 |
| IMPORTANT | 重要公告 |
| SYSTEM | 系统公告 |
| MAINTENANCE | 维护公告 |
| UPDATE | 更新公告 |

**优先级说明**:

| 优先级 | 说明 |
|------|------|
| LOW | 低 |
| NORMAL | 普通 |
| HIGH | 高 |
| URGENT | 紧急 |

**响应示例**:
```json
{
  "success": true,
  "message": "公告创建成功",
  "data": {
    "id": 1,
    "title": "系统维护通知",
    "content": "系统将于今晚22:00-23:00进行维护",
    "type": "MAINTENANCE",
    "priority": "HIGH",
    "authorId": 1,
    "authorName": "admin",
    "isPinned": true,
    "isPublished": true,
    "viewCount": 0,
    "readCount": 0,
    "hasRead": false,
    "createdAt": "2024-01-25T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 8.2 更新公告

**接口地址**: `PUT /announcements/{id}`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**请求体**: 同创建公告

**响应示例**:
```json
{
  "success": true,
  "message": "公告更新成功",
  "data": { },
  "timestamp": 1700000000000
}
```

---

### 8.3 删除公告

**接口地址**: `DELETE /announcements/{id}`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:
```json
{
  "success": true,
  "message": "公告删除成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 8.4 发布公告

**接口地址**: `POST /announcements/{id}/publish`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:
```json
{
  "success": true,
  "message": "公告发布成功",
  "data": { },
  "timestamp": 1700000000000
}
```

---

### 8.5 取消发布公告

**接口地址**: `POST /announcements/{id}/unpublish`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:
```json
{
  "success": true,
  "message": "公告已取消发布",
  "data": { },
  "timestamp": 1700000000000
}
```

---

### 8.6 置顶公告

**接口地址**: `POST /announcements/{id}/pin`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:
```json
{
  "success": true,
  "message": "公告置顶成功",
  "data": { },
  "timestamp": 1700000000000
}
```

---

### 8.7 取消置顶公告

**接口地址**: `POST /announcements/{id}/unpin`

**权限要求**: 管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:
```json
{
  "success": true,
  "message": "公告取消置顶成功",
  "data": { },
  "timestamp": 1700000000000
}
```

---

### 8.8 标记公告已读

**接口地址**: `POST /announcements/{id}/read`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:
```json
{
  "success": true,
  "message": "已标记为已读",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 8.9 标记全部已读

**接口地址**: `POST /announcements/read-all`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "success": true,
  "message": "全部公告已标记为已读",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 8.10 获取公告详情

**接口地址**: `GET /announcements/{id}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "title": "系统维护通知",
    "content": "系统将于今晚22:00-23:00进行维护",
    "type": "MAINTENANCE",
    "priority": "HIGH",
    "authorId": 1,
    "authorName": "admin",
    "isPinned": true,
    "isPublished": true,
    "viewCount": 100,
    "readCount": 50,
    "hasRead": true,
    "createdAt": "2024-01-25T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 8.11 获取已发布公告列表

**接口地址**: `GET /announcements/published`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 8.12 获取有效公告列表

**接口地址**: `GET /announcements/active`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**说明**: 返回已发布且未过期的公告

---

### 8.13 获取置顶公告列表

**接口地址**: `GET /announcements/pinned`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "title": "系统维护通知",
      "isPinned": true
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 8.14 按类型获取公告

**接口地址**: `GET /announcements/type/{type}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 公告类型 |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 8.15 获取所有公告（管理员）

**接口地址**: `GET /announcements/all`

**权限要求**: 管理员

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 8.16 获取公告统计（管理员）

**接口地址**: `GET /announcements/statistics`

**权限要求**: 管理员

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "total": 20,
    "published": 15,
    "pinned": 3
  },
  "timestamp": 1700000000000
}
```

---

### 8.17 获取用户阅读统计

**接口地址**: `GET /announcements/read-statistics`

**权限要求**: 需要登录

**响应示例**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "totalActive": 10,
    "readCount": 8,
    "unreadCount": 2
  },
  "timestamp": 1700000000000
}
```

---

## 9. 敏感词管理模块 (Sensitive Words)

> 所有敏感词管理接口需要 ADMIN 角色
> 敏感词支持三种高效过滤算法：KMP、Trie树、AC自动机
> 敏感词存储在txt文件中，支持热加载

### 算法说明

| 算法 | 说明 | 时间复杂度 | 适用场景 |
|------|------|-----------|---------|
| KMP | Knuth-Morris-Pratt字符串匹配算法 | O(n+m) | 单个敏感词匹配效率高 |
| Trie | 前缀树算法 | O(n×m) | 前缀匹配，适合大量敏感词 |
| AC | Aho-Corasick自动机算法 | O(n) | 多模式匹配，推荐使用 |

### 9.1 添加敏感词

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

### 9.2 批量添加敏感词

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

### 9.3 删除敏感词

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

### 9.4 批量删除敏感词

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

### 9.5 获取所有敏感词

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

### 9.6 获取敏感词数量

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

### 9.7 重新加载敏感词

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

### 9.9 检查文本是否包含敏感词

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

### 9.11 查找文本中的敏感词

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

## 10. WebSocket 接口

### 10.1 连接端点

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

### 10.2 订阅主题

| 主题 | 说明 |
|------|------|
| `/topic/room.{roomId}` | 接收聊天室消息 |
| `/topic/room.{roomId}.typing` | 接收输入状态 |
| `/topic/user.status` | 接收用户在线状态变化 |

### 10.3 发送消息

**发送目标**: `/app/chat.send.{roomId}`

**消息格式**:
```json
{
  "roomId": 1,
  "content": "大家好！",
  "type": "TEXT"
}
```

### 10.4 加入聊天室通知

**发送目标**: `/app/chat.join.{roomId}`

### 10.5 离开聊天室通知

**发送目标**: `/app/chat.leave.{roomId}`

### 10.6 输入状态

**发送目标**: `/app/chat.typing.{roomId}`

### 10.7 用户状态更新

**发送目标**: `/app/user.status`

**消息格式**:
```json
{
  "status": "ONLINE"
}
```

### 10.8 心跳机制

> 心跳机制用于保持WebSocket连接活跃，检测连接状态

**心跳间隔**: 30秒

**超时时间**: 90秒（无心跳则判定离线）

**发送目标**: `/app/heartbeat`

**客户端实现示例**:
```javascript
// 自动心跳（推荐）
// Spring WebSocket内置心跳，客户端无需手动发送

// 手动心跳（可选）
setInterval(() => {
  stompClient.send('/app/heartbeat', {});
}, 25000);
```

**心跳响应**:
- 服务端收到心跳后更新用户最后活跃时间
- 超时未收到心跳自动将用户设为离线状态
- 离线状态会广播到 `/topic/user.status`

**连接保活配置**:
```yaml
# application.yaml
websocket:
  heartbeat:
    interval: 30000      # 心跳间隔（毫秒）
    timeout: 90000       # 超时时间（毫秒）
```

---

## 11. 数据模型

### 11.1 User (用户)

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

### 11.2 ChatRoom (聊天室)

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

### 11.3 Message (消息)

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

### 11.4 BannedUser (封禁用户)

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

### 11.5 SensitiveWord (敏感词)

> 敏感词存储在txt文件中，不使用数据库存储
> 支持三种算法：KMP、Trie树、AC自动机

| 字段 | 类型 | 说明 |
|------|------|------|
| word | String | 敏感词 |

**配置文件**:
```yaml
sensitive-word:
  file-path: sensitive-words.txt
  algorithm: AC
  auto-reload: true
  reload-interval: 300000
```

### 11.6 SystemLog (系统日志)

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

### 11.7 EmailVerification (邮箱验证)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 验证记录ID |
| email | String | 邮箱地址 |
| code | String | 验证码 |
| type | String | 类型：REGISTER/RESET_PASSWORD/CHANGE_EMAIL |
| expiresAt | DateTime | 过期时间 |
| used | Boolean | 是否已使用 |
| createdAt | DateTime | 创建时间 |

### 11.8 Report (举报)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 举报ID |
| reporterId | Long | 举报人ID |
| reporterName | String | 举报人名称 |
| reportedUserId | Long | 被举报用户ID |
| reportedUserName | String | 被举报用户名称 |
| reportedRoomId | Long | 被举报聊天室ID |
| reportedRoomName | String | 被举报聊天室名称 |
| reportedMessageId | Long | 被举报消息ID |
| type | String | 举报类型：SPAM/HARASSMENT/INAPPROPRIATE_CONTENT/VIOLENCE/FRAUD/OTHER |
| targetType | String | 目标类型：USER/ROOM/MESSAGE |
| reason | String | 举报原因 |
| description | String | 详细描述 |
| status | String | 状态：PENDING/PROCESSING/RESOLVED/REJECTED |
| handlerId | Long | 处理人ID |
| handlerName | String | 处理人名称 |
| handleResult | String | 处理结果 |
| handledAt | DateTime | 处理时间 |
| createdAt | DateTime | 创建时间 |

### 11.9 EmailSendLog (邮件发送日志)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 日志ID |
| email | String | 邮箱地址 |
| type | String | 邮件类型：VERIFICATION/SIMPLE/HTML |
| sendDate | Date | 发送日期 |
| sendCount | Integer | 当日发送次数 |
| createdAt | DateTime | 创建时间 |

### 11.10 Announcement (公告)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 公告ID |
| title | String | 公告标题 |
| content | String | 公告内容 |
| type | String | 类型：NORMAL/IMPORTANT/SYSTEM/MAINTENANCE/UPDATE |
| priority | String | 优先级：LOW/NORMAL/HIGH/URGENT |
| authorId | Long | 作者ID |
| authorName | String | 作者名称 |
| isPinned | Boolean | 是否置顶 |
| isPublished | Boolean | 是否发布 |
| publishAt | DateTime | 发布时间 |
| expireAt | DateTime | 过期时间 |
| viewCount | Integer | 浏览次数 |
| readCount | Long | 已读人数 |
| hasRead | Boolean | 当前用户是否已读 |
| createdAt | DateTime | 创建时间 |
| updatedAt | DateTime | 更新时间 |

### 11.11 AnnouncementRead (公告已读记录)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录ID |
| announcementId | Long | 公告ID |
| userId | Long | 用户ID |
| readAt | DateTime | 阅读时间 |

---

## 12. 错误码说明

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
| 今日举报次数已达上限 | 每日举报次数超限 |
| 该用户已有待处理的举报 | 重复举报限制 |
| 该消息已有待处理的举报 | 重复举报限制 |
| 举报记录不存在 | 举报ID无效 |
| 该举报已处理完成 | 已处理的举报不可重复处理 |
| 无效的举报类型 | 举报类型参数错误 |
| 无效的举报目标类型 | 目标类型参数错误 |
| 无效的举报状态 | 状态参数错误 |
| 今日发送邮件次数已达上限 | 每日邮件发送次数超限 |

---

## 13. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2024-01-01 | 初始版本，包含基础聊天功能 |
| v1.1.0 | 2024-01-15 | 新增管理功能和敏感词过滤 |
| v1.2.0 | 2024-01-20 | 新增邮箱验证功能 |
| v1.3.0 | 2024-01-25 | 新增举报功能和邮件每日发送限制 |
| v1.4.0 | 2024-01-30 | 新增公告管理功能 |
