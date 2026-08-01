# LifeLab Server

LifeLab Android 客户端对应的精简 Ktor REST 服务。服务端使用 H2 文件数据库持久化用户、实验、指标和每日打卡数据。

## 技术栈

- Kotlin 2.4.10
- Ktor 3.5.1
- Exposed 1.3.1
- H2 2.4.240
- JWT Bearer Authentication
- PBKDF2WithHmacSHA256 密码哈希

## 启动

在项目根目录执行：

```powershell
.\gradlew.bat :server:run
```

默认监听 `http://0.0.0.0:8080`。Android 模拟器访问宿主机时使用：

```text
http://10.0.2.2:8080/api/v1/
```

真机调试时需要将 `10.0.2.2` 换成电脑在局域网中的 IP 地址。

## 环境变量

本地开发可以直接使用默认配置。部署前必须设置新的 JWT 密钥：

```powershell
$env:LIFELAB_JWT_SECRET="replace-with-a-long-random-production-secret"
$env:LIFELAB_DATABASE_URL="jdbc:h2:file:./server-data/lifelab;DB_CLOSE_ON_EXIT=FALSE"
.\gradlew.bat :server:run
```

H2 文件默认写入项目根目录的 `server-data`，该目录已加入 `.gitignore`。

## API

| 方法 | 路径 | 是否需要 JWT | 作用 |
|---|---|---:|---|
| GET | `/health` | 否 | 服务健康检查 |
| POST | `/api/v1/auth/register` | 否 | 注册并返回 JWT |
| POST | `/api/v1/auth/login` | 否 | 登录并返回 JWT |
| GET | `/api/v1/templates` | 否 | 获取在线实验模板 |
| GET | `/api/v1/experiments` | 是 | 获取当前用户的实验 |
| POST | `/api/v1/experiments` | 是 | 创建或幂等更新实验 |
| GET | `/api/v1/records` | 是 | 查询当前用户的打卡记录 |
| POST | `/api/v1/records` | 是 | 创建或幂等更新打卡记录 |

JWT 通过请求头发送：

```text
Authorization: Bearer <accessToken>
```

实验和打卡由 Android 客户端生成 UUID。WorkManager 使用同一个 UUID 重试上传时，服务端会更新原记录，不会插入重复数据。

## 验证

运行后端端到端测试：

```powershell
.\gradlew.bat :server:test
```

测试覆盖注册、JWT 鉴权、模板、实验、打卡、幂等重试和用户数据隔离。
