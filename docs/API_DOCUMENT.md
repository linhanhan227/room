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
  - [5.13 转让聊天室所有权](#513-转让聊天室所有权)
  - [5.14 获取推荐聊天室](#514-获取推荐聊天室)
  - [5.15 获取可用推荐策略](#515-获取可用推荐策略)
  - [5.16 获取默认推荐策略](#516-获取默认推荐策略)
- [6. 聊天室成员管理模块 (Room Member Management)](#6-聊天室成员管理模块-room-member-management)
  - [6.1 禁言成员](#61-禁言成员)
  - [6.2 解除禁言](#62-解除禁言)
  - [6.3 踢出成员](#63-踢出成员)
  - [6.4 拉黑成员](#64-拉黑成员)
  - [6.5 解除拉黑](#65-解除拉黑)
  - [6.6 获取黑名单](#66-获取黑名单)
  - [6.7 获取聊天室成员列表](#67-获取聊天室成员列表)
  - [6.8 设置成员角色](#68-设置成员角色)
- [7. 消息模块 (Messages)](#7-消息模块-messages)
  - [7.1 发送消息](#71-发送消息)
  - [7.2 获取聊天室消息列表](#72-获取聊天室消息列表)
  - [7.3 获取最近消息](#73-获取最近消息)
  - [7.4 搜索消息](#74-搜索消息)
  - [7.5 删除消息](#75-删除消息)
  - [7.6 获取消息数量](#76-获取消息数量)
- [8. 管理模块 (Admin)](#8-管理模块-admin)
  - [8.1 获取仪表盘统计](#81-获取仪表盘统计)
  - [8.2 获取所有用户](#82-获取所有用户)
  - [8.3 搜索用户](#83-搜索用户)
  - [8.4 设置用户角色](#84-设置用户角色)
  - [8.5 封禁用户](#85-封禁用户)
  - [8.6 解封用户](#86-解封用户)
  - [8.7 获取封禁用户列表](#87-获取封禁用户列表)
  - [8.8 获取用户封禁状态](#88-获取用户封禁状态)
  - [8.9 获取所有聊天室](#89-获取所有聊天室)
  - [8.10 删除聊天室](#810-删除聊天室)
  - [8.11 归档聊天室](#811-归档聊天室)
  - [8.12 删除消息](#812-删除消息)
  - [8.13 获取系统日志](#813-获取系统日志)
  - [8.14 获取日志操作类型列表](#814-获取日志操作类型列表)
  - [8.15 按操作类型查询日志](#815-按操作类型查询日志)
  - [8.16 按日期范围查询日志](#816-按日期范围查询日志)
  - [8.17 按用户查询日志](#817-按用户查询日志)
- [9. 公告模块 (Announcements)](#9-公告模块-announcements)
  - [9.1 创建公告](#91-创建公告)
  - [9.2 更新公告](#92-更新公告)
  - [9.3 删除公告](#93-删除公告)
  - [9.4 发布公告](#94-发布公告)
  - [9.5 取消发布公告](#95-取消发布公告)
  - [9.6 置顶公告](#96-置顶公告)
  - [9.7 取消置顶公告](#97-取消置顶公告)
  - [9.8 标记公告已读](#98-标记公告已读)
  - [9.9 标记全部已读](#99-标记全部已读)
  - [9.10 获取公告详情](#910-获取公告详情)
  - [9.11 获取已发布公告列表](#911-获取已发布公告列表)
  - [9.12 获取有效公告列表](#912-获取有效公告列表)
  - [9.13 获取置顶公告列表](#913-获取置顶公告列表)
  - [9.14 按类型获取公告](#914-按类型获取公告)
  - [9.15 获取所有公告（管理员）](#915-获取所有公告管理员)
  - [9.16 获取公告统计（管理员）](#916-获取公告统计管理员)
  - [9.17 获取用户阅读统计](#917-获取用户阅读统计)
- [10. 敏感词管理模块 (Sensitive Words)](#10-敏感词管理模块-sensitive-words)
  - [10.1 添加敏感词](#101-添加敏感词)
  - [10.2 批量添加敏感词](#102-批量添加敏感词)
  - [10.3 删除敏感词](#103-删除敏感词)
  - [10.4 批量删除敏感词](#104-批量删除敏感词)
  - [10.5 获取所有敏感词](#105-获取所有敏感词)
  - [10.6 获取敏感词数量](#106-获取敏感词数量)
  - [10.7 重新加载敏感词](#107-重新加载敏感词)
  - [10.8 保存敏感词到文件](#108-保存敏感词到文件)
  - [10.9 检查文本是否包含敏感词](#109-检查文本是否包含敏感词)
  - [10.10 过滤敏感词](#1010-过滤敏感词)
  - [10.11 查找文本中的敏感词](#1011-查找文本中的敏感词)
  - [10.12 查找所有匹配结果](#1012-查找所有匹配结果)
  - [10.13 获取算法信息](#1013-获取算法信息)
  - [10.14 设置默认算法](#1014-设置默认算法)
- [11. 数据模型](#11-数据模型)
  - [11.1 用户相关](#111-用户相关)
  - [11.2 聊天室相关](#112-聊天室相关)
  - [11.3 消息相关](#113-消息相关)
  - [11.4 举报相关](#114-举报相关)
  - [11.5 公告相关](#115-公告相关)
  - [11.6 黑名单相关](#116-黑名单相关)
  - [11.7 其他模型](#117-其他模型)

---

## 基础信息

| 项目 | 说明 |
|------|------|
| Base URL | `http://localhost:8080/api` |
| 协议 | HTTP/HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 认证方式 | JWT Bearer Token |

---

## 通用说明

### 认证方式

除认证接口外，其他接口均需要在请求头中携带JWT Token：

```
Authorization: Bearer <token>
```

**Token 说明**:
- Token 有效期：默认 24 小时（86400000 毫秒）
- Token 格式：JWT (JSON Web Token)
- Token 前缀：`Bearer `（注意 Bearer 后有空格）

### 统一响应格式

所有接口均采用统一的响应格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {},
  "timestamp": 1700000000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| success | Boolean | 请求是否成功 |
| message | String | 响应消息 |
| data | Object | 响应数据，可能为对象、数组或 null |
| timestamp | Long | 响应时间戳（毫秒） |

### 分页响应格式

分页接口返回 Spring Data 标准分页格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "content": [],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false,
    "numberOfElements": 10,
    "empty": false
  },
  "timestamp": 1700000000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| content | Array | 当前页数据列表 |
| totalElements | Long | 总记录数 |
| totalPages | Integer | 总页数 |
| size | Integer | 每页大小 |
| number | Integer | 当前页码（从0开始） |
| first | Boolean | 是否为第一页 |
| last | Boolean | 是否为最后一页 |
| numberOfElements | Integer | 当前页实际记录数 |

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

注册新用户账号。

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

| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| username | String | 是 | 用户名 | 3-50字符，不能为空 |
| password | String | 是 | 密码 | 6-100字符，不能为空 |
| nickname | String | 否 | 昵称 | 最大50字符 |
| email | String | 否 | 邮箱 | 最大100字符 |

**响应示例**:

```json
{
  "success": true,
  "message": "用户注册成功",
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

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | JWT Token |
| tokenType | String | Token类型，固定为 "Bearer" |
| expiresIn | Long | Token有效期（毫秒） |
| user | UserDTO | 用户信息对象 |

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| Username is required | 用户名不能为空 |
| Username must be between 3 and 50 characters | 用户名长度不符合要求 |
| Password is required | 密码不能为空 |
| Password must be between 6 and 100 characters | 密码长度不符合要求 |
| Username already exists | 用户名已存在 |
| Email already exists | 邮箱已被注册 |

---

### 1.2 用户登录

用户登录获取Token。

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
  "message": "登录成功",
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

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| Username is required | 用户名不能为空 |
| Password is required | 密码不能为空 |
| Invalid username or password | 用户名或密码错误 |
| User is banned | 用户已被封禁 |

---

### 1.3 用户登出

用户登出，清除登录状态。

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
  "message": "登出成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

## 2. 邮箱验证模块 (Email Verification)

### 2.1 发送验证码

发送邮箱验证码。

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
| type | String | 是 | 验证类型 |

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
- 验证码有效期为5分钟（300秒）
- 验证码长度为6位

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| 无效的验证类型 | type参数值不正确 |
| 该邮箱已被注册 | REGISTER类型时邮箱已存在 |
| 发送验证码过于频繁 | 两次发送间隔不足60秒 |
| 今日发送次数已达上限 | 超过每日发送限制 |

---

### 2.2 验证邮箱

验证邮箱验证码。

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

检查邮箱是否存在有效的验证码。

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

获取邮箱今日剩余可发送次数。

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

**配置说明**:
- 默认每日最大发送次数：10次
- 每日0点重置计数

---

## 3. 举报模块 (Reports)

### 3.1 提交举报

提交举报信息。

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
| reportedUserId | Long | 条件必填 | 被举报用户ID（targetType=USER时必填） |
| reportedRoomId | Long | 条件必填 | 被举报聊天室ID（targetType=ROOM时必填） |
| reportedMessageId | Long | 条件必填 | 被举报消息ID（targetType=MESSAGE时必填） |
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
    "reportedRoomId": null,
    "reportedRoomName": null,
    "reportedMessageId": null,
    "type": "SPAM",
    "targetType": "USER",
    "reason": "发送垃圾信息",
    "description": "该用户在聊天室中频繁发送广告信息",
    "status": "PENDING",
    "handlerId": null,
    "handlerName": null,
    "handleResult": null,
    "handledAt": null,
    "createdAt": "2024-01-20T10:00:00"
  },
  "timestamp": 1700000000000
}
```

**举报状态说明**:

| 状态 | 说明 |
|------|------|
| PENDING | 待处理 |
| PROCESSING | 处理中 |
| RESOLVED | 已解决 |
| REJECTED | 已拒绝 |

---

### 3.2 获取我的举报记录

获取当前用户提交的举报记录。

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
        "reportedRoomId": null,
        "reportedRoomName": null,
        "reportedMessageId": null,
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

根据ID获取举报详情。

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
    "reportedRoomId": null,
    "reportedRoomName": null,
    "reportedMessageId": null,
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

获取所有举报记录列表。

**接口地址**: `GET /reports`

**权限要求**: 管理员 (ADMIN)

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

按状态筛选举报列表。

**接口地址**: `GET /reports/status/{status}`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 举报状态 |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:

```
GET /reports/status/PENDING?page=0&size=10
```

---

### 3.6 按类型获取举报列表（管理员）

按举报类型筛选举报列表。

**接口地址**: `GET /reports/type/{type}`

**权限要求**: 管理员 (ADMIN)

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

获取举报统计数据。

**接口地址**: `GET /reports/statistics`

**权限要求**: 管理员 (ADMIN)

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

处理举报，更新状态和处理结果。

**接口地址**: `PUT /reports/{id}/handle`

**权限要求**: 管理员 (ADMIN)

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

删除举报记录。

**接口地址**: `DELETE /reports/{id}`

**权限要求**: 管理员 (ADMIN)

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

获取当前登录用户的详细信息。

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

**用户状态说明**:

| 状态 | 说明 |
|------|------|
| ONLINE | 在线 |
| OFFLINE | 离线 |
| BUSY | 忙碌 |
| AWAY | 离开 |

**用户角色说明**:

| 角色 | 说明 |
|------|------|
| USER | 普通用户 |
| ADMIN | 管理员 |

---

### 4.2 根据ID获取用户信息

根据用户ID获取用户公开信息。

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
    "avatar": "https://example.com/avatar.png",
    "status": "ONLINE",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 4.3 根据用户名获取用户信息

根据用户名获取用户信息。

**接口地址**: `GET /users/username/{username}`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |

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

### 4.4 搜索用户

根据关键词搜索用户。

**接口地址**: `GET /users/search`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:

```
GET /users/search?keyword=test&page=0&size=10
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
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 4.5 获取在线用户列表

获取所有在线用户列表。

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
    },
    {
      "id": 2,
      "username": "admin",
      "nickname": "管理员",
      "email": "admin@example.com",
      "avatar": null,
      "status": "ONLINE",
      "role": "ADMIN",
      "createdAt": "2024-01-01T09:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 4.6 获取在线用户数量

获取当前在线用户总数。

**接口地址**: `GET /users/online/count`

**权限要求**: 需要登录

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": 15,
  "timestamp": 1700000000000
}
```

---

### 4.7 更新用户资料

更新当前用户的个人资料。

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
PUT /users/me?nickname=新昵称&avatar=https://example.com/new-avatar.png
```

**响应示例**:

```json
{
  "success": true,
  "message": "个人资料更新成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "新昵称",
    "email": "test@example.com",
    "avatar": "https://example.com/new-avatar.png",
    "status": "ONLINE",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 4.8 修改密码

修改当前用户密码。

**接口地址**: `PUT /users/me/password`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 原密码 |
| newPassword | String | 是 | 新密码 |

**请求示例**:

```
PUT /users/me/password?oldPassword=old123&newPassword=new456
```

**响应示例**:

```json
{
  "success": true,
  "message": "密码修改成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| 原密码错误 | oldPassword不正确 |

---

### 4.9 更新用户状态

更新用户在线状态。

**接口地址**: `PUT /users/{id}/status`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 用户状态：ONLINE/OFFLINE/BUSY/AWAY |

**请求示例**:

```
PUT /users/1/status?status=BUSY
```

**响应示例**:

```json
{
  "success": true,
  "message": "状态更新成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "avatar": null,
    "status": "BUSY",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

## 5. 聊天室模块 (Rooms)

### 5.1 创建聊天室

创建新的聊天室。

**接口地址**: `POST /rooms`

**权限要求**: 需要登录

**请求体**:

```json
{
  "name": "技术交流群",
  "description": "讨论技术问题的聊天室",
  "avatar": "https://example.com/room-avatar.png",
  "type": "PUBLIC",
  "password": null,
  "maxMembers": 100
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| name | String | 是 | 聊天室名称 | 1-100字符 |
| description | String | 否 | 聊天室描述 | 最大500字符 |
| avatar | String | 否 | 聊天室头像URL | - |
| type | String | 否 | 聊天室类型 | PUBLIC/PRIVATE/GROUP，默认PUBLIC |
| password | String | 条件必填 | 聊天室密码 | 私密房间时可设置 |
| maxMembers | Integer | 否 | 最大成员数 | 默认100 |

**聊天室类型说明**:

| 类型 | 说明 |
|------|------|
| PUBLIC | 公开聊天室，所有人可见可加入 |
| PRIVATE | 私密聊天室，需要密码加入 |
| GROUP | 群组聊天室，仅邀请可加入 |

**响应示例**:

```json
{
  "success": true,
  "message": "聊天室创建成功",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "description": "讨论技术问题的聊天室",
    "avatar": "https://example.com/room-avatar.png",
    "ownerId": 1,
    "ownerName": "testuser",
    "type": "PUBLIC",
    "maxMembers": 100,
    "status": "ACTIVE",
    "memberCount": 1,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

**聊天室状态说明**:

| 状态 | 说明 |
|------|------|
| ACTIVE | 活跃 |
| INACTIVE | 不活跃 |
| ARCHIVED | 已归档 |

---

### 5.2 加入聊天室

加入指定聊天室。

**接口地址**: `POST /rooms/{roomId}/join`

**权限要求**: 需要登录

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| password | String | 条件必填 | 聊天室密码（私密房间需要） |

**请求示例**:

```
POST /rooms/1/join?password=room123
```

**响应示例**:

```json
{
  "success": true,
  "message": "加入聊天室成功",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "description": "讨论技术问题的聊天室",
    "avatar": null,
    "ownerId": 1,
    "ownerName": "testuser",
    "type": "PUBLIC",
    "maxMembers": 100,
    "status": "ACTIVE",
    "memberCount": 2,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| Room not found | 聊天室不存在 |
| 您已被该聊天室拉黑，无法加入 | 用户在聊天室黑名单中 |
| 聊天室密码错误 | 私密聊天室密码不正确 |
| 聊天室已满员 | 聊天室成员已达上限 |
| 您已经是该聊天室的成员 | 用户已经在聊天室中 |
| 聊天室未激活 | 聊天室状态不是 ACTIVE |

---

### 5.3 退出聊天室

退出指定聊天室。

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
  "message": "退出聊天室成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.4 删除聊天室

删除聊天室（仅创建者可操作）。

**接口地址**: `DELETE /rooms/{roomId}`

**权限要求**: 需要登录（聊天室创建者）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:

```json
{
  "success": true,
  "message": "聊天室删除成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.5 获取聊天室详情

获取聊天室详细信息。

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
    "ownerName": "testuser",
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

### 5.6 获取公开聊天室列表

获取所有公开聊天室列表（分页）。

**接口地址**: `GET /rooms/public`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:

```
GET /rooms/public?page=0&size=10
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
        "name": "技术交流群",
        "description": "讨论技术问题的聊天室",
        "avatar": null,
        "ownerId": 1,
        "ownerName": "testuser",
        "type": "PUBLIC",
        "maxMembers": 100,
        "status": "ACTIVE",
        "memberCount": 50,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 20,
    "totalPages": 2,
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 5.7 获取我的聊天室列表

获取当前用户加入的所有聊天室。

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
      "description": "讨论技术问题的聊天室",
      "avatar": null,
      "ownerId": 1,
      "ownerName": "testuser",
      "type": "PUBLIC",
      "maxMembers": 100,
      "status": "ACTIVE",
      "memberCount": 50,
      "createdAt": "2024-01-01T10:00:00"
    },
    {
      "id": 2,
      "name": "闲聊群",
      "description": "随便聊聊",
      "avatar": null,
      "ownerId": 2,
      "ownerName": "otheruser",
      "type": "PUBLIC",
      "maxMembers": 50,
      "status": "ACTIVE",
      "memberCount": 30,
      "createdAt": "2024-01-02T10:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 5.8 搜索聊天室

根据关键词搜索聊天室。

**接口地址**: `GET /rooms/search`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:

```
GET /rooms/search?keyword=技术&page=0&size=10
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
        "name": "技术交流群",
        "description": "讨论技术问题的聊天室",
        "avatar": null,
        "ownerId": 1,
        "ownerName": "testuser",
        "type": "PUBLIC",
        "maxMembers": 100,
        "status": "ACTIVE",
        "memberCount": 50,
        "createdAt": "2024-01-01T10:00:00"
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

### 5.9 获取聊天室成员列表

获取聊天室的所有成员。

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
      "email": "test@example.com",
      "avatar": null,
      "status": "ONLINE",
      "role": "USER",
      "createdAt": "2024-01-01T10:00:00"
    },
    {
      "id": 2,
      "username": "otheruser",
      "nickname": "其他用户",
      "email": "other@example.com",
      "avatar": null,
      "status": "ONLINE",
      "role": "USER",
      "createdAt": "2024-01-02T10:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 5.10 踢出成员

将成员踢出聊天室（仅创建者/管理员可操作）。

**接口地址**: `DELETE /rooms/{roomId}/members/{userId}`

**权限要求**: 需要登录（聊天室创建者或管理员）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 要踢出的用户ID |

**响应示例**:

```json
{
  "success": true,
  "message": "成员踢出成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.11 设置成员角色

设置聊天室成员角色（仅创建者可操作）。

**接口地址**: `PUT /rooms/{roomId}/members/{userId}/role`

**权限要求**: 需要登录（聊天室创建者）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 是 | 成员角色：OWNER/ADMIN/MEMBER |

**成员角色说明**:

| 角色 | 说明 |
|------|------|
| OWNER | 创建者 |
| ADMIN | 管理员 |
| MEMBER | 普通成员 |

**请求示例**:

```
PUT /rooms/1/members/2/role?role=ADMIN
```

**响应示例**:

```json
{
  "success": true,
  "message": "成员角色更新成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.12 更新聊天室信息

更新聊天室信息（仅创建者可操作）。

**接口地址**: `PUT /rooms/{roomId}`

**权限要求**: 需要登录（聊天室创建者）

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
  "type": "PUBLIC",
  "password": null,
  "maxMembers": 200
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 聊天室名称（1-100字符），若与当前名称不同需保证唯一 |
| description | String | 否 | 聊天室描述（最多500字符） |
| avatar | String | 否 | 聊天室头像URL |
| type | String | 否 | 聊天室类型：PUBLIC/PRIVATE/GROUP |
| password | String | 否 | 聊天室密码（仅PRIVATE类型有效，传空字符串可清除密码） |
| maxMembers | Integer | 否 | 最大成员数 |

> **说明**：所有字段均为可选，仅传入需要修改的字段即可。未传入的字段（null）不会被修改。

**响应示例**:

```json
{
  "success": true,
  "message": "聊天室更新成功",
  "data": {
    "id": 1,
    "name": "新名称",
    "description": "新描述",
    "avatar": "https://example.com/new-avatar.png",
    "ownerId": 1,
    "ownerName": "testuser",
    "type": "PUBLIC",
    "maxMembers": 200,
    "status": "ACTIVE",
    "memberCount": 50,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| Room not found | 聊天室不存在 |
| 聊天室名称已被使用 | 新名称与已有聊天室名称重复 |
| 只有聊天室所有者或系统管理员可以更新聊天室 | 权限不足 |

---

### 5.13 转让聊天室所有权

将聊天室所有权转让给其他成员。转让后，原所有者将降级为管理员，新所有者将升级为 OWNER。

**接口地址**: `PUT /rooms/{roomId}/transfer`

**权限要求**: 需要登录（聊天室所有者或系统管理员）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| newOwnerId | Long | 是 | 新所有者的用户ID |

**请求示例**:

```
PUT /rooms/1/transfer?newOwnerId=5
```

**响应示例**:

```json
{
  "success": true,
  "message": "聊天室所有权转让成功",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "description": "讨论技术问题的聊天室",
    "avatar": null,
    "ownerId": 5,
    "ownerName": "newowner",
    "type": "PUBLIC",
    "maxMembers": 100,
    "status": "ACTIVE",
    "memberCount": 50,
    "createdAt": "2024-01-01T10:00:00"
  },
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| Room not found | 聊天室不存在 |
| User not found | 目标用户不存在 |
| 只有聊天室所有者或系统管理员可以转让所有权 | 权限不足 |
| 该用户已经是聊天室所有者 | 目标用户已经是所有者 |
| 目标用户不是该聊天室的成员 | 新所有者必须是聊天室成员 |

---

### 5.14 获取推荐聊天室

根据推荐算法获取推荐聊天室列表。

**接口地址**: `GET /rooms/recommendations`

**权限要求**: 无（可选登录）

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| strategy | String | 否 | 推荐策略：ACTIVITY/POPULARITY/NEWEST/RANDOM/HYBRID |
| limit | Integer | 否 | 返回数量，默认10 |

**推荐策略说明**:

| 策略 | 说明 |
|------|------|
| ACTIVITY | 基于活跃度推荐，优先展示近期消息最多的聊天室 |
| POPULARITY | 基于热度推荐，优先展示成员最多的聊天室 |
| NEWEST | 基于时间推荐，优先展示最新创建的聊天室 |
| RANDOM | 随机推荐，随机展示公开聊天室 |
| HYBRID | 混合推荐，综合多种策略（默认） |

**请求示例**:

```
GET /rooms/recommendations
GET /rooms/recommendations?strategy=POPULARITY&limit=5
```

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "name": "技术交流群",
      "description": "讨论技术问题的聊天室",
      "avatar": null,
      "ownerId": 1,
      "ownerName": "testuser",
      "type": "PUBLIC",
      "maxMembers": 100,
      "status": "ACTIVE",
      "memberCount": 50,
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 5.15 获取可用推荐策略

获取所有可用的推荐策略列表。

**接口地址**: `GET /rooms/recommendations/strategies`

**权限要求**: 无

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": ["ACTIVITY", "HYBRID", "NEWEST", "POPULARITY", "RANDOM"],
  "timestamp": 1700000000000
}
```

---

### 5.16 获取默认推荐策略

获取系统配置的默认推荐策略。

**接口地址**: `GET /rooms/recommendations/default`

**权限要求**: 无

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": "HYBRID",
  "timestamp": 1700000000000
}
```

---

## 6. 聊天室成员管理模块 (Room Member Management)

聊天室成员管理模块提供聊天室创建者和管理员对成员的管理功能，包括禁言、踢出、拉黑等操作。

### 6.1 禁言成员

禁言聊天室成员，可设置禁言时长。

**接口地址**: `POST /rooms/{roomId}/members/mute`

**权限要求**: 需要登录，且为聊天室创建者或管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求体**:

```json
{
  "userId": 123,
  "duration": 3600,
  "reason": "违反聊天室规则"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 要禁言的用户ID |
| duration | Long | 否 | 禁言时长（秒），不填则永久禁言 |
| reason | String | 否 | 禁言原因 |

**响应示例**:

```json
{
  "success": true,
  "message": "成员禁言成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| 无权限执行此操作 | 当前用户不是聊天室创建者或管理员 |
| 不能禁言管理员 | 无法禁言聊天室管理员 |
| 不能禁言系统管理员 | 无法禁言系统管理员 |
| 用户不在聊天室中 | 目标用户不是聊天室成员 |

---

### 6.2 解除禁言

解除聊天室成员的禁言状态。

**接口地址**: `POST /rooms/{roomId}/members/{userId}/unmute`

**权限要求**: 需要登录，且为聊天室创建者或管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 要解禁的用户ID |

**响应示例**:

```json
{
  "success": true,
  "message": "成员解禁成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 6.3 踢出成员

将成员从聊天室中踢出。

**接口地址**: `DELETE /rooms/{roomId}/members/{userId}`

**权限要求**: 需要登录，且为聊天室创建者或管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 要踢出的用户ID |

**响应示例**:

```json
{
  "success": true,
  "message": "成员踢出成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| 无权限执行此操作 | 当前用户不是聊天室创建者或管理员 |
| 不能踢出管理员 | 无法踢出聊天室管理员 |
| 不能踢出系统管理员 | 无法踢出系统管理员 |
| 用户不在聊天室中 | 目标用户不是聊天室成员 |

---

### 6.4 拉黑成员

将成员拉黑，禁止其再次加入聊天室。

**接口地址**: `POST /rooms/{roomId}/members/blacklist`

**权限要求**: 需要登录，且为聊天室创建者或管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**请求体**:

```json
{
  "userId": 123,
  "reason": "多次违反聊天室规则"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 要拉黑的用户ID |
| reason | String | 否 | 拉黑原因 |

**响应示例**:

```json
{
  "success": true,
  "message": "成员拉黑成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| 无权限执行此操作 | 当前用户不是聊天室创建者或管理员 |
| 不能拉黑管理员 | 无法拉黑聊天室管理员 |
| 不能拉黑系统管理员 | 无法拉黑系统管理员 |
| 用户已在黑名单中 | 目标用户已在黑名单中 |

---

### 6.5 解除拉黑

将成员从黑名单中移除。

**接口地址**: `DELETE /rooms/{roomId}/members/blacklist/{userId}`

**权限要求**: 需要登录，且为聊天室创建者或管理员

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 要解除拉黑的用户ID |

**响应示例**:

```json
{
  "success": true,
  "message": "成员解除拉黑成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 6.6 获取黑名单

获取聊天室的黑名单列表。

**接口地址**: `GET /rooms/{roomId}/members/blacklist`

**权限要求**: 需要登录，且为聊天室创建者或管理员

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
      "roomId": 1,
      "userId": 123,
      "username": "baduser",
      "nickname": "违规用户",
      "avatar": null,
      "addedById": 1,
      "addedByName": "admin",
      "reason": "多次违反聊天室规则",
      "createdAt": "2024-01-20T10:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 黑名单记录ID |
| roomId | Long | 聊天室ID |
| userId | Long | 被拉黑用户ID |
| username | String | 被拉黑用户名 |
| nickname | String | 被拉黑用户昵称 |
| avatar | String | 被拉黑用户头像 |
| addedById | Long | 操作者ID |
| addedByName | String | 操作者用户名 |
| reason | String | 拉黑原因 |
| createdAt | DateTime | 创建时间 |

---

### 6.7 获取聊天室成员列表

获取聊天室的所有成员列表。

**接口地址**: `GET /rooms/{roomId}/members`

**权限要求**: 需要登录，且为聊天室成员

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
      "username": "admin",
      "nickname": "管理员",
      "email": "admin@example.com",
      "avatar": null,
      "status": "ONLINE",
      "role": "ADMIN",
      "createdAt": "2024-01-01T10:00:00"
    },
    {
      "id": 2,
      "username": "user1",
      "nickname": "用户1",
      "email": "user1@example.com",
      "avatar": null,
      "status": "OFFLINE",
      "role": "USER",
      "createdAt": "2024-01-01T11:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 6.8 设置成员角色

设置聊天室成员的角色（仅创建者或系统管理员可操作）。

**接口地址**: `PUT /rooms/{roomId}/members/{userId}/role`

**权限要求**: 需要登录（聊天室所有者或系统管理员）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| userId | Long | 是 | 目标用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 是 | 成员角色：OWNER/ADMIN/MEMBER |

**成员角色说明**:

| 角色 | 说明 |
|------|------|
| OWNER | 所有者（创建者） |
| ADMIN | 管理员 |
| MEMBER | 普通成员 |

**请求示例**:

```
PUT /rooms/1/members/5/role?role=ADMIN
```

**响应示例**:

```json
{
  "success": true,
  "message": "成员角色更新成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**错误响应**:

| 错误信息 | 说明 |
|----------|------|
| Room not found | 聊天室不存在 |
| Member not found in room | 目标用户不是聊天室成员 |
| 只有聊天室所有者或系统管理员可以修改成员角色 | 权限不足 |

---

## 7. 消息模块 (Messages)

### 7.1 发送消息

发送消息到指定聊天室。

**接口地址**: `POST /messages`

**权限要求**: 需要登录且已加入聊天室

**请求体**:

```json
{
  "roomId": 1,
  "content": "Hello, World!",
  "type": "TEXT"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |
| content | String | 是 | 消息内容 |
| type | String | 否 | 消息类型，默认TEXT |

**消息类型说明**:

| 类型 | 说明 |
|------|------|
| TEXT | 文本消息 |
| IMAGE | 图片消息 |
| FILE | 文件消息 |
| SYSTEM | 系统消息 |
| EMOJI | 表情消息 |

**响应示例**:

```json
{
  "success": true,
  "message": "消息发送成功",
  "data": {
    "id": 1,
    "roomId": 1,
    "senderId": 1,
    "senderName": "测试用户",
    "senderAvatar": null,
    "content": "Hello, World!",
    "type": "TEXT",
    "createdAt": "2024-01-01T12:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 7.2 获取聊天室消息列表

获取聊天室消息列表（分页）。

**接口地址**: `GET /messages/room/{roomId}`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认20 |

**请求示例**:

```
GET /messages/room/1?page=0&size=20
```

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 100,
        "roomId": 1,
        "senderId": 1,
        "senderName": "测试用户",
        "senderAvatar": null,
        "content": "Hello!",
        "type": "TEXT",
        "createdAt": "2024-01-01T12:00:00"
      },
      {
        "id": 99,
        "roomId": 1,
        "senderId": 2,
        "senderName": "其他用户",
        "senderAvatar": null,
        "content": "Hi!",
        "type": "TEXT",
        "createdAt": "2024-01-01T11:59:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  },
  "timestamp": 1700000000000
}
```

---

### 7.3 获取最近消息

获取聊天室最近的消息。

**接口地址**: `GET /messages/room/{roomId}/recent`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 数量限制，默认50 |

**请求示例**:

```
GET /messages/room/1/recent?limit=50
```

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 100,
      "roomId": 1,
      "senderId": 1,
      "senderName": "测试用户",
      "senderAvatar": null,
      "content": "Hello!",
      "type": "TEXT",
      "createdAt": "2024-01-01T12:00:00"
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 7.4 搜索消息

在聊天室中搜索消息。

**接口地址**: `GET /messages/room/{roomId}/search`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认20 |

**请求示例**:

```
GET /messages/room/1/search?keyword=Hello&page=0&size=20
```

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 100,
        "roomId": 1,
        "senderId": 1,
        "senderName": "测试用户",
        "senderAvatar": null,
        "content": "Hello, World!",
        "type": "TEXT",
        "createdAt": "2024-01-01T12:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "number": 0,
    "size": 20
  },
  "timestamp": 1700000000000
}
```

---

### 7.5 删除消息

删除消息（发送者或管理员可操作）。

**接口地址**: `DELETE /messages/{messageId}`

**权限要求**: 需要登录（消息发送者或管理员）

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| messageId | Long | 是 | 消息ID |

**响应示例**:

```json
{
  "success": true,
  "message": "消息删除成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 7.6 获取消息数量

获取聊天室消息总数。

**接口地址**: `GET /messages/room/{roomId}/count`

**权限要求**: 需要登录且已加入聊天室

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": 1234,
  "timestamp": 1700000000000
}
```

---

## 8. 管理模块 (Admin)

> 所有管理模块接口均需要管理员权限 (ADMIN)

### 8.1 获取仪表盘统计

获取系统统计数据。

**接口地址**: `GET /admin/dashboard`

**权限要求**: 管理员 (ADMIN)

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
    "topActiveRooms": [
      {
        "roomId": 1,
        "roomName": "技术交流群",
        "messageCount": 5000,
        "memberCount": 100
      },
      {
        "roomId": 2,
        "roomName": "闲聊群",
        "messageCount": 3000,
        "memberCount": 80
      }
    ]
  },
  "timestamp": 1700000000000
}
```

---

### 8.2 获取所有用户

获取所有用户列表（分页）。

**接口地址**: `GET /admin/users`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:

```
GET /admin/users?page=0&size=10
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
        "username": "testuser",
        "nickname": "测试用户",
        "email": "test@example.com",
        "avatar": null,
        "status": "ONLINE",
        "role": "USER",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 1000,
    "totalPages": 100,
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 8.3 搜索用户

搜索用户。

**接口地址**: `GET /admin/users/search`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:

```
GET /admin/users/search?keyword=test&page=0&size=10
```

---

### 8.4 设置用户角色

设置用户角色。

**接口地址**: `PUT /admin/users/{userId}/role`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 是 | 用户角色：USER/ADMIN |

**请求示例**:

```
PUT /admin/users/1/role?role=ADMIN
```

**响应示例**:

```json
{
  "success": true,
  "message": "用户角色更新成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 8.5 封禁用户

封禁用户。

**接口地址**: `POST /admin/users/ban`

**权限要求**: 管理员 (ADMIN)

**请求体**:

```json
{
  "userId": 123,
  "reason": "违反社区规定",
  "type": "TEMPORARY",
  "endTime": "2024-02-01T00:00:00"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| reason | String | 否 | 封禁原因 |
| type | String | 是 | 封禁类型 |
| endTime | DateTime | 条件必填 | 解封时间（TEMPORARY类型必填） |

**封禁类型说明**:

| 类型 | 说明 |
|------|------|
| PERMANENT | 永久封禁 |
| TEMPORARY | 临时封禁 |
| WARNING | 警告 |

**响应示例**:

```json
{
  "success": true,
  "message": "用户封禁成功",
  "data": {
    "id": 1,
    "userId": 123,
    "username": "baduser",
    "userNickname": "坏用户",
    "bannedById": 1,
    "bannedByName": "admin",
    "reason": "违反社区规定",
    "type": "TEMPORARY",
    "endTime": "2024-02-01T00:00:00",
    "active": true,
    "createdAt": "2024-01-20T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 8.6 解封用户

解除用户封禁。

**接口地址**: `DELETE /admin/users/{userId}/ban`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:

```json
{
  "success": true,
  "message": "用户解封成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 8.7 获取封禁用户列表

获取所有被封禁的用户列表。

**接口地址**: `GET /admin/users/banned`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

**请求示例**:

```
GET /admin/users/banned?page=0&size=10
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
        "userId": 123,
        "username": "baduser",
        "userNickname": "坏用户",
        "bannedById": 1,
        "bannedByName": "admin",
        "reason": "违反社区规定",
        "type": "TEMPORARY",
        "endTime": "2024-02-01T00:00:00",
        "active": true,
        "createdAt": "2024-01-20T10:00:00"
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 8.8 获取用户封禁状态

获取指定用户的封禁状态。

**接口地址**: `GET /admin/users/{userId}/ban`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "userId": 123,
    "username": "baduser",
    "userNickname": "坏用户",
    "bannedById": 1,
    "bannedByName": "admin",
    "reason": "违反社区规定",
    "type": "TEMPORARY",
    "endTime": "2024-02-01T00:00:00",
    "active": true,
    "createdAt": "2024-01-20T10:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 8.9 获取所有聊天室

获取所有聊天室列表。

**接口地址**: `GET /admin/rooms`

**权限要求**: 管理员 (ADMIN)

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
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  },
  "timestamp": 1700000000000
}
```

---

### 8.10 删除聊天室

删除指定聊天室。

**接口地址**: `DELETE /admin/rooms/{roomId}`

**权限要求**: 管理员 (ADMIN)

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

### 8.11 归档聊天室

归档指定聊天室。

**接口地址**: `PUT /admin/rooms/{roomId}/archive`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 聊天室ID |

**响应示例**:

```json
{
  "success": true,
  "message": "聊天室归档成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 8.12 删除消息

删除指定消息。

**接口地址**: `DELETE /admin/messages/{messageId}`

**权限要求**: 管理员 (ADMIN)

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

### 8.13 获取系统日志

获取系统日志列表。

**接口地址**: `GET /admin/logs`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认20 |

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "action": "USER_LOGIN",
        "entityType": "User",
        "entityId": 1,
        "operatorId": 1,
        "operatorName": "admin",
        "targetUserId": null,
        "details": "用户登录成功",
        "ipAddress": "192.168.1.1",
        "level": "INFO",
        "createdAt": "2024-01-20T10:00:00"
      }
    ],
    "totalElements": 1000,
    "totalPages": 50,
    "number": 0,
    "size": 20
  },
  "timestamp": 1700000000000
}
```

---

### 8.14 获取日志操作类型列表

获取所有日志操作类型。

**接口地址**: `GET /admin/logs/actions`

**权限要求**: 管理员 (ADMIN)

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    "USER_LOGIN",
    "USER_LOGOUT",
    "USER_REGISTER",
    "ROOM_CREATE",
    "ROOM_DELETE",
    "MESSAGE_SEND",
    "MESSAGE_DELETE",
    "USER_BAN",
    "USER_UNBAN"
  ],
  "timestamp": 1700000000000
}
```

---

### 8.15 按操作类型查询日志

按操作类型筛选系统日志。

**接口地址**: `GET /admin/logs/by-action`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 操作类型 |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认20 |

**请求示例**:

```
GET /admin/logs/by-action?action=USER_LOGIN&page=0&size=20
```

---

### 8.16 按日期范围查询日志

按日期范围筛选系统日志。

**接口地址**: `GET /admin/logs/by-date`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| start | Date | 是 | 开始日期 (ISO格式: YYYY-MM-DD) |
| end | Date | 是 | 结束日期 (ISO格式: YYYY-MM-DD) |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认20 |

**请求示例**:

```
GET /admin/logs/by-date?start=2024-01-01&end=2024-01-31&page=0&size=20
```

---

### 8.17 按用户查询日志

按用户筛选系统日志。

**接口地址**: `GET /admin/logs/by-user/{userId}`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认20 |

**请求示例**:

```
GET /admin/logs/by-user/1?page=0&size=20
```

---

## 9. 公告模块 (Announcements)

### 9.1 创建公告

创建新公告。

**接口地址**: `POST /announcements`

**权限要求**: 管理员 (ADMIN)

**请求体**:

```json
{
  "title": "系统维护通知",
  "content": "系统将于今晚22:00-23:00进行维护",
  "type": "MAINTENANCE",
  "priority": "HIGH",
  "publishAt": "2024-01-20T10:00:00",
  "expireAt": "2024-01-21T10:00:00"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 公告标题 |
| content | String | 是 | 公告内容 |
| type | String | 否 | 公告类型 |
| priority | String | 否 | 优先级 |
| publishAt | DateTime | 否 | 发布时间 |
| expireAt | DateTime | 否 | 过期时间 |

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
|--------|------|
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
    "isPinned": false,
    "isPublished": false,
    "publishAt": "2024-01-20T10:00:00",
    "expireAt": "2024-01-21T10:00:00",
    "viewCount": 0,
    "readCount": 0,
    "hasRead": false,
    "createdAt": "2024-01-20T09:00:00",
    "updatedAt": "2024-01-20T09:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 9.2 更新公告

更新公告内容。

**接口地址**: `PUT /announcements/{id}`

**权限要求**: 管理员 (ADMIN)

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

### 9.3 删除公告

删除公告。

**接口地址**: `DELETE /announcements/{id}`

**权限要求**: 管理员 (ADMIN)

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

### 9.4 发布公告

发布公告。

**接口地址**: `POST /announcements/{id}/publish`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:

```json
{
  "success": true,
  "message": "公告发布成功",
  "data": {
    "id": 1,
    "isPublished": true
  },
  "timestamp": 1700000000000
}
```

---

### 9.5 取消发布公告

取消发布公告。

**接口地址**: `POST /announcements/{id}/unpublish`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:

```json
{
  "success": true,
  "message": "公告已取消发布",
  "data": {
    "id": 1,
    "isPublished": false
  },
  "timestamp": 1700000000000
}
```

---

### 9.6 置顶公告

置顶公告。

**接口地址**: `POST /announcements/{id}/pin`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:

```json
{
  "success": true,
  "message": "公告置顶成功",
  "data": {
    "id": 1,
    "isPinned": true
  },
  "timestamp": 1700000000000
}
```

---

### 9.7 取消置顶公告

取消置顶公告。

**接口地址**: `POST /announcements/{id}/unpin`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例**:

```json
{
  "success": true,
  "message": "公告取消置顶成功",
  "data": {
    "id": 1,
    "isPinned": false
  },
  "timestamp": 1700000000000
}
```

---

### 9.8 标记公告已读

标记公告为已读。

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

### 9.9 标记全部已读

标记所有公告为已读。

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

### 9.10 获取公告详情

获取公告详情。

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
    "publishAt": "2024-01-20T10:00:00",
    "expireAt": "2024-01-21T10:00:00",
    "viewCount": 100,
    "readCount": 50,
    "hasRead": true,
    "createdAt": "2024-01-20T09:00:00",
    "updatedAt": "2024-01-20T09:00:00"
  },
  "timestamp": 1700000000000
}
```

---

### 9.11 获取已发布公告列表

获取已发布公告列表。

**接口地址**: `GET /announcements/published`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 9.12 获取有效公告列表

获取有效公告列表（已发布且未过期）。

**接口地址**: `GET /announcements/active`

**权限要求**: 需要登录

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 9.13 获取置顶公告列表

获取置顶公告列表。

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
      "content": "系统将于今晚进行维护",
      "type": "MAINTENANCE",
      "priority": "HIGH",
      "isPinned": true,
      "isPublished": true
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 9.14 按类型获取公告

按类型获取公告列表。

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

### 9.15 获取所有公告（管理员）

获取所有公告列表（包括未发布）。

**接口地址**: `GET /announcements/all`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 9.16 获取公告统计（管理员）

获取公告统计数据。

**接口地址**: `GET /announcements/statistics`

**权限要求**: 管理员 (ADMIN)

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "total": 50,
    "published": 30,
    "unpublished": 20,
    "pinned": 5,
    "todayViews": 200,
    "todayReads": 150
  },
  "timestamp": 1700000000000
}
```

---

### 9.17 获取用户阅读统计

获取当前用户的公告阅读统计。

**接口地址**: `GET /announcements/read-statistics`

**权限要求**: 需要登录

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "totalAnnouncements": 30,
    "readCount": 25,
    "unreadCount": 5
  },
  "timestamp": 1700000000000
}
```

---

## 10. 敏感词管理模块 (Sensitive Words)

> **重要说明**：本模块的敏感词管理基于本地文件（`sensitive_words.txt`），不涉及数据库存储。所有敏感词操作直接读写本地文件，支持动态加载和定时自动重载。

> 所有敏感词管理接口均需要管理员权限 (ADMIN)

> **存储方式**：本地文本文件
> **文件路径**：`src/main/resources/sensitive_words.txt`
> **自动重载**：每5分钟自动重新加载敏感词库
> **支持算法**：KMP、Trie、AC自动机（默认）

### 10.1 添加敏感词

添加单个敏感词到本地文件。

**接口地址**: `POST /admin/sensitive-words`

**权限要求**: 管理员 (ADMIN)

**请求体**: 

```
敏感词文本
```

**响应示例**:

```json
{
  "success": true,
  "message": "敏感词添加成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**说明**:
- 敏感词会被添加到内存中的敏感词库
- 需要调用"保存敏感词到文件"接口才能持久化到本地文件
- 添加后会自动重新加载过滤算法

---

### 10.2 批量添加敏感词

批量添加敏感词到本地文件。

**接口地址**: `POST /admin/sensitive-words/batch`

**权限要求**: 管理员 (ADMIN)

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

### 10.3 删除敏感词

从本地文件中删除单个敏感词。

**接口地址**: `DELETE /admin/sensitive-words`

**权限要求**: 管理员 (ADMIN)

**请求体**: 

```
敏感词文本
```

**响应示例**:

```json
{
  "success": true,
  "message": "敏感词删除成功",
  "data": null,
  "timestamp": 1700000000000
}
```

**说明**:
- 从内存中的敏感词库删除该词
- 需要调用"保存敏感词到文件"接口才能持久化到本地文件
- 删除后会自动重新加载过滤算法

---

### 10.4 批量删除敏感词

从本地文件中批量删除敏感词。

**接口地址**: `DELETE /admin/sensitive-words/batch`

**权限要求**: 管理员 (ADMIN)

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

### 10.5 获取所有敏感词

获取本地文件中的所有敏感词列表。

**接口地址**: `GET /admin/sensitive-words`

**权限要求**: 管理员 (ADMIN)

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": ["敏感词1", "敏感词2", "敏感词3"],
  "timestamp": 1700000000000
}
```

**说明**:
- 返回当前加载的敏感词列表
- 敏感词存储在本地文件 `sensitive_words.txt` 中
- 列表会随着文件重新加载而更新

---

### 10.6 获取敏感词数量

获取本地文件中的敏感词总数。

**接口地址**: `GET /admin/sensitive-words/count`

**权限要求**: 管理员 (ADMIN)

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": 200,
  "timestamp": 1700000000000
}
```

**说明**:
- 返回当前加载的敏感词数量
- 敏感词存储在本地文件 `sensitive_words.txt` 中
- 数量会随着文件重新加载而更新

---

### 10.7 重新加载敏感词

从本地文件重新加载敏感词库。

**接口地址**: `POST /admin/sensitive-words/reload`

**权限要求**: 管理员 (ADMIN)

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

### 10.8 保存敏感词到文件

将当前内存中的敏感词保存到本地文件。

**接口地址**: `POST /admin/sensitive-words/save`

**权限要求**: 管理员 (ADMIN)

**响应示例**:

```json
{
  "success": true,
  "message": "敏感词已保存到文件",
  "data": null,
  "timestamp": 1700000000000
}
```

**说明**:
- 将内存中的敏感词库持久化到本地文件 `sensitive_words.txt`
- 添加或删除敏感词后，需要调用此接口才能保存到文件
- 系统每5分钟会自动重新加载文件，但不会自动保存

---

### 10.9 检查文本是否包含敏感词

检查文本是否包含敏感词。

**接口地址**: `POST /admin/sensitive-words/check`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| algorithm | String | 否 | 算法类型：KMP/TRIE/AC |

**请求体**: 

```
要检查的文本内容
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

---

### 10.10 过滤敏感词

过滤文本中的敏感词。

**接口地址**: `POST /admin/sensitive-words/filter`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| algorithm | String | 否 | 算法类型：KMP/TRIE/AC |

**请求体**: 

```
要过滤的文本内容
```

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

### 10.11 查找文本中的敏感词

查找文本中包含的所有敏感词。

**接口地址**: `POST /admin/sensitive-words/find`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| algorithm | String | 否 | 算法类型：KMP/TRIE/AC |

**请求体**: 

```
要查找的文本内容
```

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": ["敏感词1", "敏感词2"],
  "timestamp": 1700000000000
}
```

---

### 10.12 查找所有匹配结果

查找文本中所有敏感词的匹配位置。

**接口地址**: `POST /admin/sensitive-words/matches`

**权限要求**: 管理员 (ADMIN)

**请求体**: 

```
要查找的文本内容
```

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "word": "敏感词1",
      "startIndex": 5,
      "endIndex": 8
    },
    {
      "word": "敏感词2",
      "startIndex": 15,
      "endIndex": 18
    }
  ],
  "timestamp": 1700000000000
}
```

---

### 10.13 获取算法信息

获取敏感词过滤算法信息。

**接口地址**: `GET /admin/sensitive-words/algorithm`

**权限要求**: 管理员 (ADMIN)

**响应示例**:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "defaultAlgorithm": "AC",
    "availableAlgorithms": ["KMP", "TRIE", "AC"]
  },
  "timestamp": 1700000000000
}
```

**算法说明**:

| 算法 | 说明 |
|------|------|
| KMP | KMP字符串匹配算法 |
| TRIE | Trie树算法 |
| AC | AC自动机算法（默认，性能最优） |

---

### 10.14 设置默认算法

设置默认敏感词过滤算法。

**接口地址**: `PUT /admin/sensitive-words/algorithm`

**权限要求**: 管理员 (ADMIN)

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| algorithm | String | 是 | 算法类型：KMP/TRIE/AC |

**请求示例**:

```
PUT /admin/sensitive-words/algorithm?algorithm=TRIE
```

**响应示例**:

```json
{
  "success": true,
  "message": "默认算法设置成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

## 11. 数据模型

### 11.1 用户相关

#### UserDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| nickname | String | 昵称 |
| email | String | 邮箱 |
| avatar | String | 头像URL |
| status | String | 状态：ONLINE/OFFLINE/BUSY/AWAY |
| role | String | 角色：USER/ADMIN |
| createdAt | DateTime | 创建时间 |

#### AuthResponse

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | JWT Token |
| tokenType | String | Token类型 |
| expiresIn | Long | 有效期（毫秒） |
| user | UserDTO | 用户信息 |

---

### 11.2 聊天室相关

#### ChatRoomDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 聊天室ID |
| name | String | 聊天室名称 |
| description | String | 描述 |
| avatar | String | 头像URL |
| ownerId | Long | 创建者ID |
| ownerName | String | 创建者名称 |
| type | String | 类型：PUBLIC/PRIVATE/GROUP |
| maxMembers | Integer | 最大成员数 |
| status | String | 状态：ACTIVE/INACTIVE/ARCHIVED |
| memberCount | Integer | 成员数量 |
| createdAt | DateTime | 创建时间 |

---

### 11.3 消息相关

#### MessageDTO

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

---

### 11.4 举报相关

#### ReportDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 举报ID |
| reporterId | Long | 举报者ID |
| reporterName | String | 举报者名称 |
| reportedUserId | Long | 被举报用户ID |
| reportedUserName | String | 被举报用户名称 |
| reportedRoomId | Long | 被举报聊天室ID |
| reportedRoomName | String | 被举报聊天室名称 |
| reportedMessageId | Long | 被举报消息ID |
| type | String | 举报类型 |
| targetType | String | 目标类型 |
| reason | String | 举报原因 |
| description | String | 详细描述 |
| status | String | 状态 |
| handlerId | Long | 处理者ID |
| handlerName | String | 处理者名称 |
| handleResult | String | 处理结果 |
| handledAt | DateTime | 处理时间 |
| createdAt | DateTime | 创建时间 |

---

### 11.5 公告相关

#### AnnouncementDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 公告ID |
| title | String | 标题 |
| content | String | 内容 |
| type | String | 类型 |
| priority | String | 优先级 |
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

---

### 11.6 黑名单相关

#### BlacklistMemberDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 黑名单记录ID |
| roomId | Long | 聊天室ID |
| userId | Long | 被拉黑用户ID |
| username | String | 被拉黑用户名 |
| nickname | String | 被拉黑用户昵称 |
| avatar | String | 被拉黑用户头像 |
| addedById | Long | 操作者ID |
| addedByName | String | 操作者用户名 |
| reason | String | 拉黑原因 |
| createdAt | DateTime | 创建时间 |

#### MuteMemberRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 要禁言的用户ID |
| duration | Long | 否 | 禁言时长（秒），不填则永久禁言 |
| reason | String | 否 | 禁言原因 |

#### BlacklistMemberRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 要拉黑的用户ID |
| reason | String | 否 | 拉黑原因 |

### 11.7 其他模型

#### BannedUserDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录ID |
| userId | Long | 用户ID |
| username | String | 用户名 |
| userNickname | String | 用户昵称 |
| bannedById | Long | 封禁者ID |
| bannedByName | String | 封禁者名称 |
| reason | String | 封禁原因 |
| type | String | 封禁类型 |
| endTime | DateTime | 解封时间 |
| active | Boolean | 是否生效 |
| createdAt | DateTime | 创建时间 |

#### SystemLogDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 日志ID |
| action | String | 操作类型 |
| entityType | String | 实体类型 |
| entityId | Long | 实体ID |
| operatorId | Long | 操作者ID |
| operatorName | String | 操作者名称 |
| targetUserId | Long | 目标用户ID |
| details | String | 详细信息 |
| ipAddress | String | IP地址 |
| level | String | 日志级别 |
| createdAt | DateTime | 创建时间 |

#### DashboardStats

| 字段 | 类型 | 说明 |
|------|------|------|
| totalUsers | Long | 总用户数 |
| onlineUsers | Long | 在线用户数 |
| totalRooms | Long | 总聊天室数 |
| activeRooms | Long | 活跃聊天室数 |
| totalMessages | Long | 总消息数 |
| todayMessages | Long | 今日消息数 |
| bannedUsers | Long | 封禁用户数 |
| topActiveRooms | List | 活跃聊天室列表 |

---

## 附录

### A. 配置参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| jwt.expiration | 86400000 | Token有效期（毫秒） |
| email.verification.codeExpiration | 300 | 验证码有效期（秒） |
| email.verification.codeLength | 6 | 验证码长度 |
| email.verification.maxSendPerHour | 5 | 每小时最大发送次数 |
| email.dailyLimit.maxCount | 10 | 每日最大发送次数 |
| websocket.heartbeatInterval | 30000 | 心跳间隔（毫秒） |
| websocket.heartbeatTimeout | 90000 | 心跳超时（毫秒） |
| chatRoom.maxMembers | 100 | 聊天室最大成员数 |
| chatRoom.maxRoomsPerUser | 10 | 用户最大创建聊天室数 |
| chatRoom.maxMessageLength | 5000 | 消息最大长度 |
| sensitiveWord.algorithm | AC | 默认敏感词过滤算法 |
| sensitiveWord.replacement | *** | 敏感词替换字符 |
| roomRecommendation.enabled | true | 是否启用推荐功能 |
| roomRecommendation.defaultStrategy | HYBRID | 默认推荐策略 |
| roomRecommendation.recommendationLimit | 10 | 默认推荐数量 |
| roomRecommendation.cacheDuration | 300 | 推荐结果缓存时间（秒） |
| roomRecommendation.hybridWeights.activityWeight | 0.3 | 混合策略-活跃度权重 |
| roomRecommendation.hybridWeights.popularityWeight | 0.3 | 混合策略-热度权重 |
| roomRecommendation.hybridWeights.newestWeight | 0.2 | 混合策略-最新权重 |
| roomRecommendation.hybridWeights.randomWeight | 0.2 | 混合策略-随机权重 |

### B. 错误码对照表

| 错误类型 | HTTP状态码 | 说明 |
|----------|------------|------|
| UnauthorizedException | 401 | 未授权 |
| ForbiddenException | 403 | 权限不足 |
| ResourceNotFoundException | 404 | 资源不存在 |
| BusinessException | 400 | 业务异常 |
| ValidationException | 400 | 参数验证失败 |

---

*文档版本: 1.2.0*
*最后更新: 2026年3月*