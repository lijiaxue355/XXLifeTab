# LifeLab

## 项目简介

LifeLab 是一款个人量化实验 Android 应用。用户可以从模板创建实验、设置观察指标、完成每日打卡，并通过趋势与阶段对比报告观察干预效果。

应用采用本地优先的数据策略，支持离线记录和联网自动同步。配套的 Kotlin/Ktor 服务端提供用户认证、实验模板、实验及打卡数据接口。

## 技术栈

### Android 客户端

| 技术 | 项目落点 |
|---|---|
| Kotlin + XML + ViewBinding | Fragment 页面、列表 Item、表单和图表容器 |
| MVVM / UDF | 每个核心页面使用 `UiState`、`UiAction`、`UiEffect`，ViewModel 持有页面状态 |
| Coroutine + Flow | 异步 IO、Room 可观察查询，以及 `combine`、`map`、`stateIn` 数据转换 |
| StateFlow + SharedFlow | 分离持续页面状态和 Snackbar、导航等一次性事件 |
| Navigation + Safe Args | 单 Activity 多 Fragment，统一传递实验本地 ID |
| Room | 实验、指标、每日记录、模板缓存和 Outbox；作为 SSOT |
| WorkManager | 有网络约束的最终一致同步、唯一任务、失败重试和指数退避 |
| Retrofit + OkHttp | 模板、实验和打卡 API，JWT 拦截器及网络错误处理 |
| Coil | 模板和实验封面、占位图与加载失败图 |
| ListAdapter + DiffUtil | 实验、模板和打卡历史列表的局部更新 |
| MPAndroidChart | 指标折线图和基线/干预阶段柱状图 |
| Gradle + KSP | 依赖管理、项目构建与 Room 代码生成 |

### 服务端

- Kotlin + Ktor
- Exposed + H2
- JWT Bearer Authentication
- PBKDF2WithHmacSHA256 密码哈希
- Kotlinx Serialization

## 本地运行

### 启动服务端

在项目根目录运行：

```powershell
.\gradlew.bat :server:run
```

健康检查：

```text
http://localhost:8080/health
```

服务端监听 `0.0.0.0:8080`。Android 模拟器访问电脑使用 `10.0.2.2`；真机调试需要让手机与电脑连接同一个局域网，并使用电脑的局域网 IP。

更多服务端配置和 API 说明见 [server/README.md](server/README.md)。

### 配置客户端 API 地址

在项目根目录的 `gradle.properties` 中设置 `LIFELAB_BASE_URL`。

Android 模拟器：

```text
LIFELAB_BASE_URL=http://10.0.2.2:8080/api/v1/
```

Android 真机：

```text
LIFELAB_BASE_URL=http://<电脑局域网IP>:8080/api/v1/
```

### 构建 Android 客户端

可以直接使用 Android Studio 运行 `app`，也可以在项目根目录执行：

```powershell
.\gradlew.bat :app:assembleDebug
```

### 运行服务端测试

```powershell
.\gradlew.bat :server:test
```

服务端测试覆盖注册、登录、JWT 鉴权、模板、实验、打卡、用户数据隔离和幂等重试。
