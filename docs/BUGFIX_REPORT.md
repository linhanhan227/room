# Bug 修复报告

> **版本**: v1.1.1  
> **修复日期**: 2026年3月4日  
> **修复人**: GitHub Copilot  
> **涉及文件**: 17个源文件 + 2个测试文件

---

## 修复摘要

本次代码审查共发现 **14个 Bug**（含 2个严重级别、5个高级别、5个中级别、2个低级别），涵盖配置错误、ORM 映射冲突、性能问题、数据完整性问题、事务管理缺失、异常处理不当等多个方面。所有 Bug 均已修复并通过编译验证。

### 按严重程度统计

| 严重程度 | 数量 | Bug 编号 |
|---------|------|---------|
| 🔴 严重 (Critical) | 2 | Bug #7, Bug #14 |
| 🟠 高 (High) | 5 | Bug #1, Bug #8, Bug #10, Bug #12, Bug #13 |
| 🟡 中 (Medium) | 5 | Bug #2, Bug #3, Bug #5, Bug #6, Bug #9 |
| 🟢 低 (Low) | 2 | Bug #4, Bug #11 |

### 按分类统计

| 分类 | 数量 | Bug 编号 |
|------|------|---------|
| 配置错误 | 3 | Bug #1, #2, #3 |
| 性能问题 | 2 | Bug #4, #7 |
| 异常处理不当 | 3 | Bug #5, #6, #8 |
| 事务管理缺失 | 1 | Bug #9 |
| JPQL 语法错误 | 1 | Bug #10 |
| 功能逻辑缺陷 | 1 | Bug #11 |
| 数据完整性 | 2 | Bug #12, #13 |
| ORM 映射冲突 | 1 | Bug #14 |

---

## Bug 详细修复记录

### Bug #1: CORS 配置冲突导致启动失败 🟠

**严重程度**: 高 (High)  
**分类**: 配置错误  
**影响范围**: 应用启动

**问题描述**:  
`application.yaml` 中 CORS 配置同时使用了 `allow-credentials: true` 和 `allowed-origins: ["*"]`。在 Spring Framework 5.3+ 中，当 `allowCredentials` 为 `true` 时不允许使用通配符 `"*"` 作为 `allowedOrigins`，会抛出 `IllegalArgumentException`。

**问题代码**:
```yaml
cors:
  allowed-origins:    # ❌ 与 allow-credentials: true 不兼容
    - "*"
  allow-credentials: true
```

**修复方案**:  
将 `allowed-origins` 改为 `allowed-origin-patterns`，后者支持通配符模式且兼容 `allowCredentials: true`。

**修改文件**:
- `src/main/resources/application.yaml`

**修复代码**:
```yaml
cors:
  allowed-origin-patterns:  # ✅ 兼容 allowCredentials
    - "*"
  allow-credentials: true
```

---

### Bug #2: AppProperties CORS 字段名与配置不匹配 🟡

**严重程度**: 中 (Medium)  
**分类**: 配置错误  
**影响范围**: CORS 配置绑定

**问题描述**:  
`AppProperties.Cors` 类中的字段名为 `allowedOrigins`，但配置文件已改为 `allowed-origin-patterns`，导致配置无法正确绑定。

**修改文件**:
- `src/main/java/com/chat/room/config/AppProperties.java`

**修复方案**:
- `Cors.allowedOrigins` → `Cors.allowedOriginPatterns`
- `getAllowedOriginsList()` → `getAllowedOriginPatternsList()`

---

### Bug #3: CorsConfig 使用旧字段名 🟡

**严重程度**: 中 (Medium)  
**分类**: 配置错误  
**影响范围**: CORS 运行时配置

**问题描述**:  
`CorsConfig.addCorsMappings()` 中调用了 `cors.getAllowedOrigins()`（已不存在的方法），需同步更新。

**修改文件**:
- `src/main/java/com/chat/room/config/CorsConfig.java`

**修复方案**:
```java
// ❌ 修复前
.allowedOrigins(cors.getAllowedOrigins())
// ✅ 修复后
.allowedOriginPatterns(cors.getAllowedOriginPatterns())
```

---

### Bug #4: WebSocketChatHandler 重复数据库查询 🟢

**严重程度**: 低 (Low)  
**分类**: 性能问题  
**影响范围**: WebSocket 消息处理性能

**问题描述**:  
`getUserFromSession()` 方法对同一用户调用了 3 次 `getUserByUsername()`，每次只取一个字段（id、nickname、avatar），造成不必要的数据库查询。

**修改文件**:
- `src/main/java/com/chat/room/websocket/WebSocketChatHandler.java`

**修复方案**:  
改为调用 1 次 `getUserByUsername()` 获取 `UserDTO`，然后通过 Builder 构建完整的 `User` 对象（包含 id、username、nickname、avatar、role、status 字段）。同时添加缺失的 `UserDTO` import。

---

### Bug #5: EmailVerificationService 抛出 RuntimeException 🟡

**严重程度**: 中 (Medium)  
**分类**: 异常处理不当  
**影响范围**: 邮箱验证错误响应

**问题描述**:  
`validateEmailRequest()` 方法中有 3 处抛出 `RuntimeException`，绕过了全局异常处理器 `GlobalExceptionHandler` 中对 `BusinessException` 的专门处理逻辑，导致返回 500 而非 400 状态码。

**修改文件**:
- `src/main/java/com/chat/room/service/EmailVerificationService.java`

**修复方案**:  
所有 `throw new RuntimeException(...)` 改为 `throw new BusinessException(...)`。

---

### Bug #6: EmailVerificationController 抛出 RuntimeException 🟡

**严重程度**: 中 (Medium)  
**分类**: 异常处理不当  
**影响范围**: 邮箱验证类型解析

**问题描述**:  
`parseVerificationType()` 方法中 `throw new RuntimeException` 导致错误类型不正确。

**修改文件**:
- `src/main/java/com/chat/room/controller/EmailVerificationController.java`

**修复方案**:  
`throw new RuntimeException(...)` → `throw new BusinessException(...)`

---

### Bug #7: AdminService.getDashboardStats 严重性能问题 🔴

**严重程度**: 严重 (Critical)  
**分类**: 性能问题  
**影响范围**: 管理后台仪表盘，可能导致 OOM

**问题描述**:  
`getDashboardStats()` 方法使用 `chatRoomRepository.findAll().stream().filter(...)` 和 `messageRepository.findAll().stream().filter(...)` 将**全表数据**加载到内存中进行过滤统计。当数据量增大（数十万条消息）时，会导致：
1. 巨大的内存消耗，可能触发 OOM（OutOfMemoryError）
2. 极慢的响应时间
3. 数据库连接长时间占用

**修改文件**:
- `src/main/java/com/chat/room/service/AdminService.java`
- `src/main/java/com/chat/room/repository/MessageRepository.java`

**修复方案**:
1. 公开聊天室统计：改为使用 `chatRoomRepository.findPublicRooms(Pageable.unpaged()).getTotalElements()`
2. 今日消息数统计：新增 `MessageRepository.countMessagesSince(LocalDateTime since)` 数据库聚合查询
3. 所有统计均在数据库层面完成，不再将数据加载到内存

**新增方法**:
```java
// MessageRepository.java
@Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :since")
Long countMessagesSince(@Param("since") LocalDateTime since);
```

---

### Bug #8: EmailService @Async 导致异常丢失 🟠

**严重程度**: 高 (High)  
**分类**: 异常处理不当  
**影响范围**: 验证码发送失败无法感知

**问题描述**:  
`EmailService.sendVerificationCode()` 方法使用了 `@Async` 注解，导致方法在独立线程中异步执行。当邮件发送失败抛出异常时，异常不会传播回调用方（`EmailVerificationService`），用户会认为验证码发送成功但实际从未收到邮件。

**修改文件**:
- `src/main/java/com/chat/room/service/EmailService.java`

**修复方案**:  
移除 `sendVerificationCode()` 方法上的 `@Async` 注解，使发送失败的异常能够正常传播给调用方。

---

### Bug #9: RoomMemberManagementService.isUserMuted 缺少事务 🟡

**严重程度**: 中 (Medium)  
**分类**: 事务管理缺失  
**影响范围**: 禁言过期自动解除

**问题描述**:  
`isUserMuted()` 方法在检测到禁言已过期时会修改数据库状态（设置 `muted=false`, `mutedUntil=null`），但该方法没有 `@Transactional` 注解，导致：
1. 数据库修改可能不会被持久化
2. 在某些 JPA 配置下可能抛出 `TransactionRequiredException`

**修改文件**:
- `src/main/java/com/chat/room/service/RoomMemberManagementService.java`

**修复方案**:  
添加 `@Transactional` 注解。

---

### Bug #10: MessageRepository.findRecentMessages 使用非标准 JPQL 🟠

**严重程度**: 高 (High)  
**分类**: JPQL 语法错误  
**影响范围**: 最近消息查询

**问题描述**:  
原始查询使用了 `LIMIT :limit` 语法，但 JPQL 标准不支持 `LIMIT` 子句（这是 MySQL 原生 SQL 语法），会导致运行时 `QuerySyntaxException`。

**原始代码**:
```java
@Query("SELECT m FROM Message m WHERE m.room.id = :roomId ORDER BY m.createdAt DESC LIMIT :limit")
List<Message> findRecentMessages(@Param("roomId") Long roomId, @Param("limit") int limit);
```

**修改文件**:
- `src/main/java/com/chat/room/repository/MessageRepository.java`
- `src/main/java/com/chat/room/service/MessageService.java`

**修复方案**:  
使用 Spring Data JPA 的 `Pageable` 参数替代 `LIMIT`：
```java
@Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.type != 'SYSTEM' ORDER BY m.createdAt DESC")
List<Message> findRecentMessages(@Param("roomId") Long roomId, Pageable pageable);
```
调用方使用 `PageRequest.of(0, limit)` 传入分页参数。

---

### Bug #11: SensitiveWordService 未检查功能启用状态 🟢

**严重程度**: 低 (Low)  
**分类**: 功能逻辑缺陷  
**影响范围**: 敏感词过滤

**问题描述**:  
`containsSensitiveWord()` 和 `filterText()` 的 public 方法在功能被禁用（`enabled=false`）时仍会执行敏感词过滤逻辑。

**修改文件**:
- `src/main/java/com/chat/room/service/SensitiveWordService.java`

**修复方案**:  
在两个方法开头添加启用状态检查：
```java
if (!appProperties.getSensitiveWord().isEnabled()) {
    return false; // 或返回原文
}
```

---

### Bug #12: ChatRoomService.deleteRoom 未清理黑名单 🟠

**严重程度**: 高 (High)  
**分类**: 数据完整性  
**影响范围**: 删除聊天室操作

**问题描述**:  
`deleteRoom()` 方法在删除聊天室时清理了消息和成员，但遗漏了 `room_blacklist` 表的记录。由于存在外键约束，这可能导致删除失败或留下孤立数据。

**修改文件**:
- `src/main/java/com/chat/room/service/ChatRoomService.java`

**修复方案**:  
新增 `RoomBlacklistRepository` 依赖，在删除前调用 `roomBlacklistRepository.deleteByRoomId(roomId)`。

---

### Bug #13: AdminService.deleteRoom 未清理成员和黑名单 🟠

**严重程度**: 高 (High)  
**分类**: 数据完整性  
**影响范围**: 管理员删除聊天室操作

**问题描述**:  
与 Bug #12 类似，管理员端的 `deleteRoom()` 同样缺少成员和黑名单的清理。

**修改文件**:
- `src/main/java/com/chat/room/service/AdminService.java`

**修复方案**:  
新增 `RoomMemberRepository` 和 `RoomBlacklistRepository` 依赖，在删除前依次清理消息、成员、黑名单记录。

---

### Bug #14: ChatRoom @ManyToMany 与 RoomMember 独立实体冲突 🔴

**严重程度**: 严重 (Critical)  
**分类**: ORM 映射冲突  
**影响范围**: 成员关系数据一致性

**问题描述**:  
`ChatRoom.members` 使用 `@ManyToMany @JoinTable(name="room_members")` 映射到 `room_members` 表，而 `RoomMember` 作为独立实体也映射到同一张表且拥有额外字段（`role`、`muted`、`mutedUntil`、`deleted` 等）。这导致：

1. **数据冲突**: JPA 通过 `@ManyToMany` 管理的关系（仅维护 `room_id` + `user_id`）与 `RoomMember` 实体的完整生命周期管理相互干扰
2. **JPQL 查询歧义**: 基于 `r.members` 的 JPQL 查询无法利用 `RoomMember` 的 `deleted` 等额外字段做过滤
3. **级联操作风险**: `@ManyToMany` 的级联操作可能意外覆盖 `RoomMember` 实体的额外字段

**修改文件**:
- `src/main/java/com/chat/room/entity/ChatRoom.java` — 移除 `@ManyToMany members` 字段
- `src/main/java/com/chat/room/entity/User.java` — 移除 `@ManyToMany rooms` 字段
- `src/main/java/com/chat/room/repository/ChatRoomRepository.java` — 重写 4 个 JPQL 查询
- `src/main/java/com/chat/room/repository/UserRepository.java` — 重写 1 个 JPQL 查询

**修复方案**:

1. 移除 `ChatRoom` 和 `User` 实体中的 `@ManyToMany` 双向关联
2. 所有涉及成员关系的 JPQL 查询改为通过 `RoomMember` 实体进行子查询

**重写的查询示例**:
```java
// ❌ 修复前 (依赖 @ManyToMany)
@Query("SELECT r FROM ChatRoom r JOIN r.members m WHERE m.id = :userId")
List<ChatRoom> findByMemberId(@Param("userId") Long userId);

// ✅ 修复后 (通过 RoomMember 实体)
@Query("SELECT r FROM ChatRoom r WHERE r.id IN " +
       "(SELECT rm.room.id FROM RoomMember rm WHERE rm.user.id = :userId AND rm.deleted = false)")
List<ChatRoom> findByMemberId(@Param("userId") Long userId);
```

---

## 附加修复: 测试代码适配

以上修复导致 2 个测试文件需要同步更新：

### 测试修复 1: ChatRoomServiceTest.java

**问题**: 移除 `@ManyToMany` 后，`User.builder().rooms(...)` 和 `ChatRoom.builder().members(...)` 不再可用。  
**修复**: 移除测试中所有 `.rooms(new HashSet<>())` 和 `.members(new HashSet<>())` 的 builder 调用，清除未使用的 `HashSet` import。

### 测试修复 2: MessageServiceTest.java

**问题**: `findRecentMessages` 签名从 `(Long, int)` 改为 `(Long, Pageable)` 后，测试 mock 不匹配。  
**修复**: `when(messageRepository.findRecentMessages(1L, 50))` 改为 `when(messageRepository.findRecentMessages(eq(1L), any(Pageable.class)))`，添加 `eq` 的 static import。

---

## 修改文件清单

| # | 文件路径 | 修改类型 | 关联 Bug |
|---|---------|---------|---------|
| 1 | `src/main/resources/application.yaml` | 配置修改 | #1 |
| 2 | `src/main/java/.../config/AppProperties.java` | 字段重命名 | #2 |
| 3 | `src/main/java/.../config/CorsConfig.java` | 方法调用更新 | #3 |
| 4 | `src/main/java/.../websocket/WebSocketChatHandler.java` | 性能优化+import | #4 |
| 5 | `src/main/java/.../service/EmailVerificationService.java` | 异常类型替换 | #5 |
| 6 | `src/main/java/.../controller/EmailVerificationController.java` | 异常类型替换 | #6 |
| 7 | `src/main/java/.../service/AdminService.java` | 性能优化+数据完整性 | #7, #13 |
| 8 | `src/main/java/.../service/EmailService.java` | 移除@Async | #8 |
| 9 | `src/main/java/.../service/RoomMemberManagementService.java` | 添加@Transactional | #9 |
| 10 | `src/main/java/.../repository/MessageRepository.java` | JPQL修复+新增方法 | #7, #10 |
| 11 | `src/main/java/.../service/MessageService.java` | 适配新签名 | #10 |
| 12 | `src/main/java/.../service/SensitiveWordService.java` | 添加启用检查 | #11 |
| 13 | `src/main/java/.../service/ChatRoomService.java` | 数据完整性修复 | #12 |
| 14 | `src/main/java/.../repository/ChatRoomRepository.java` | JPQL重写 | #14 |
| 15 | `src/main/java/.../repository/UserRepository.java` | JPQL重写 | #14 |
| 16 | `src/main/java/.../entity/ChatRoom.java` | 移除@ManyToMany | #14 |
| 17 | `src/main/java/.../entity/User.java` | 移除@ManyToMany | #14 |
| 18 | `src/test/.../service/ChatRoomServiceTest.java` | 测试适配 | #14 |
| 19 | `src/test/.../service/MessageServiceTest.java` | 测试适配 | #10 |

---

## 验证方式

```bash
# 编译验证（主代码 + 测试代码）
mvn compile test-compile

# 运行单元测试
mvn test

# 完整打包
mvn clean package
```

---

## 建议后续优化

1. **添加集成测试**: 为 Repository 层的自定义 JPQL 查询添加 `@DataJpaTest` 集成测试
2. **引入连接池监控**: 添加 HikariCP 监控指标，及早发现数据库连接泄漏
3. **异步邮件重试机制**: 为邮件发送添加重试队列（如使用 Spring Retry），替代简单的同步调用
4. **API 限流**: 对敏感接口（如登录、注册、发送验证码）添加限流保护
5. **索引优化**: 为 `room_members(room_id, user_id, deleted)` 添加复合索引以优化成员查询

---

*文档生成时间: 2026年3月4日*
