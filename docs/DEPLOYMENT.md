# 部署和运行指南

## 目录

1. [环境要求](#环境要求)
2. [数据库配置](#数据库配置)
3. [项目配置](#项目配置)
4. [编译和打包](#编译和打包)
5. [运行项目](#运行项目)
6. [常见问题](#常见问题)

---

## 环境要求

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | Java开发工具包 |
| Maven | 3.6+ | 项目构建工具 |
| MySQL | 8.0+ | 数据库 |
| Git | 2.0+ | 版本控制（可选） |

### 验证环境

```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 检查MySQL版本
mysql --version
```

---

## 数据库配置

### 1. 安装MySQL

#### Windows

1. 下载MySQL Installer: https://dev.mysql.com/downloads/installer/
2. 运行安装程序，选择"Server only"或"Developer Default"
3. 设置root密码（建议：`root`）
4. 完成安装

#### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

#### macOS

```bash
brew install mysql
brew services start mysql
```

### 2. 启动MySQL服务

#### Windows

```powershell
# 查看MySQL服务状态
Get-Service | Where-Object {$_.Name -like "*mysql*"}

# 启动MySQL服务（根据实际服务名称调整）
Start-Service -Name MySQL80
```

#### Linux

```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### macOS

```bash
brew services start mysql
```

### 3. 创建数据库

```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE chat_room CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'chat_user'@'localhost' IDENTIFIED BY 'chat_password';

-- 授权
GRANT ALL PRIVILEGES ON chat_room.* TO 'chat_user'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

### 4. 初始化数据库

```bash
# 执行数据库脚本
mysql -u root -p chat_room < src/main/resources/db/database_v1.1.0.sql
```

---

## 项目配置

### 1. 配置文件位置

主配置文件：`src/main/resources/application.yaml`

### 2. 数据库配置

编辑 `application.yaml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chat_room?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: root  # 修改为你的数据库用户名
    password: root  # 修改为你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 其他配置

根据需要修改以下配置：

#### JWT配置

```yaml
app:
  jwt:
    secret: your-256-bit-secret-key-for-jwt-token-generation-must-be-long-enough
    expiration: 86400000  # Token过期时间（毫秒）
```

#### 邮件配置

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 465
    username: your-email@example.com
    password: your-email-password
```

#### WebSocket配置

```yaml
app:
  web-socket:
    endpoint: /ws
    heartbeat-interval: 30000
    heartbeat-timeout: 90000
```

---

## 编译和打包

### 1. 清理并编译

```bash
mvn clean compile
```

### 2. 运行测试

```bash
mvn test
```

### 3. 打包项目

```bash
# 打包（跳过测试）
mvn clean package -DskipTests

# 打包（包含测试）
mvn clean package
```

### 4. 打包产物

打包成功后，在 `target` 目录下生成：

- `chatroom.jar` - 可执行的Spring Boot JAR包
- `chatroom.jar.original` - 原始JAR包（不含依赖）

---

## 运行项目

### 1. 使用Maven运行

```bash
mvn spring-boot:run
```

### 2. 使用JAR包运行

```bash
java -jar target/chatroom.jar
```

### 3. 指定配置文件运行

```bash
java -jar target/chatroom.jar --spring.config.location=application.yaml
```

### 4. 指定Profile运行

```bash
java -jar target/chatroom.jar --spring.profiles.active=dev
```

### 5. 后台运行（Linux）

```bash
nohup java -jar target/chatroom.jar > app.log 2>&1 &
```

### 6. 使用服务运行（Windows）

创建Windows服务：

```powershell
# 下载NSSM: https://nssm.cc/download
# 安装服务
nssm install ChatRoom "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "C:\path\to\chatroom.jar"

# 启动服务
nssm start ChatRoom
```

---

## 验证运行

### 1. 检查服务状态

访问健康检查端点：

```bash
curl http://localhost:8080/api/actuator/health
```

预期响应：

```json
{
    "status": "UP"
}
```

### 2. 访问API文档

访问Swagger UI：

```
http://localhost:8080/api/swagger-ui.html
```

### 3. 测试WebSocket连接

```javascript
const socket = new WebSocket('ws://localhost:8080/api/ws');
```

---

## 常见问题

### 1. 数据库连接失败

**错误信息**：
```
Communications link failure
Connection refused
```

**解决方案**：

1. 检查MySQL服务是否运行
   ```bash
   # Windows
   Get-Service | Where-Object {$_.Name -like "*mysql*"}
   
   # Linux
   sudo systemctl status mysql
   ```

2. 检查数据库是否存在
   ```sql
   SHOW DATABASES;
   ```

3. 检查用户名和密码是否正确

4. 检查防火墙设置

### 2. 端口被占用

**错误信息**：
```
Port 8080 was already in use
```

**解决方案**：

1. 查找占用端口的进程
   ```bash
   # Windows
   netstat -ano | findstr :8080
   
   # Linux
   lsof -i :8080
   ```

2. 终止进程或修改端口
   ```yaml
   server:
     port: 8081  # 修改为其他端口
   ```

### 3. 内存不足

**错误信息**：
```
Java heap space
OutOfMemoryError
```

**解决方案**：

增加JVM内存：

```bash
java -Xms512m -Xmx1024m -jar target/chatroom.jar
```

### 4. 依赖下载失败

**错误信息**：
```
Could not resolve dependencies
Connection timeout
```

**解决方案**：

1. 检查网络连接

2. 配置Maven镜像（阿里云）
   ```xml
   <mirror>
       <id>aliyun</id>
       <mirrorOf>central</mirrorOf>
       <name>Aliyun Maven</name>
       <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```

3. 清理Maven缓存
   ```bash
   mvn clean
   rm -rf ~/.m2/repository
   ```

### 5. Spring Security Bean创建失败

**错误信息**：
```
Error creating bean with name 'org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration'
Injection of autowired dependencies failed
```

**解决方案**：

这个问题通常是由于SecurityConfig中的Bean定义冲突导致的。

1. 检查SecurityConfig.java中的authenticationManager方法
2. 确保方法参数名不与类字段名冲突
3. 正确的配置示例：

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }
}
```

**注意**：不要在authenticationManager方法参数中声明UserDetailsService，应使用类字段userDetailsService。

### 6. Controller路由映射冲突

**错误信息**：
```
Ambiguous mapping. Cannot map 'roomMemberManagementController' method
There is already 'chatRoomController' bean method mapped
```

**解决方案**：

这个问题是由于多个Controller中存在相同的路由映射导致的。

1. 检查所有Controller中的`@GetMapping`、`@PostMapping`等注解
2. 确保每个路由路径在整个应用中唯一
3. 成员管理相关接口应统一放在`RoomMemberManagementController`中：
   - `GET /rooms/{roomId}/members` - 获取成员列表
   - `DELETE /rooms/{roomId}/members/{userId}` - 踢出成员
   - `PUT /rooms/{roomId}/members/{userId}/role` - 设置成员角色
   - `POST /rooms/{roomId}/members/mute` - 禁言成员
   - `POST /rooms/{roomId}/members/{userId}/unmute` - 解除禁言
   - `POST /rooms/{roomId}/members/blacklist` - 拉黑成员
   - `DELETE /rooms/{roomId}/members/blacklist/{userId}` - 解除拉黑
   - `GET /rooms/{roomId}/members/blacklist` - 获取黑名单

### 7. WebSocket连接失败

**错误信息**：
```
WebSocket connection failed
403 Forbidden
```

**解决方案**：

1. 检查CORS配置
   ```yaml
   app:
     web-socket:
       allowed-origins:
         - "http://localhost:3000"
         - "http://localhost:8080"
   ```

2. 检查JWT Token是否有效

3. 检查WebSocket端点配置
   ```yaml
   app:
     web-socket:
       endpoint: /ws
   ```

---

## 生产环境部署

### 1. 使用Docker部署

#### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/chatroom.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: chat_room
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/chat_room
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root

volumes:
  mysql-data:
```

#### 构建和运行

```bash
docker-compose up -d
```

### 2. 使用Nginx反向代理

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 3. 使用Systemd服务（Linux）

创建服务文件 `/etc/systemd/system/chatroom.service`：

```ini
[Unit]
Description=Chat Room Application
After=network.target mysql.service

[Service]
Type=simple
User=chatroom
WorkingDirectory=/opt/chatroom
ExecStart=/usr/bin/java -jar /opt/chatroom/chatroom.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable chatroom
sudo systemctl start chatroom
sudo systemctl status chatroom
```

---

## 监控和日志

### 1. 查看日志

```bash
# 实时查看日志
tail -f logs/application.log

# 查看错误日志
grep ERROR logs/application.log
```

### 2. 配置日志级别

```yaml
logging:
  level:
    com.chat.room: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
```

### 3. 使用监控工具

- Spring Boot Actuator: `/api/actuator`
- Prometheus: 集成Prometheus监控
- Grafana: 可视化监控面板

---

## 性能优化

### 1. JVM参数优化

```bash
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar target/chatroom.jar
```

### 2. 数据库连接池优化

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
```

### 3. 启用缓存

```yaml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```

---

## 安全建议

1. **修改默认密码**：修改数据库密码和JWT密钥
2. **启用HTTPS**：使用SSL/TLS加密通信
3. **配置防火墙**：限制数据库和API端口访问
4. **定期备份**：定期备份数据库
5. **更新依赖**：及时更新依赖库版本
6. **监控日志**：定期检查异常日志

---

## 联系支持

如有问题，请联系：

- 邮箱：support@example.com
- 文档：查看项目README.md
- Issue：提交GitHub Issue

---

*最后更新: 2026年3月*
