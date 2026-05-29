# Qiekj Android

这是一个把 [`3ryng1um/qiekj`](https://github.com/3ryng1um/qiekj) 相关 Python 脚本流程改造成 Android App 的项目。当前 App 使用 Kotlin + Jetpack Compose + Retrofit/OkHttp 实现。

> 说明：本项目用于复刻个人脚本的请求流程和移动端界面。请自行承担账号、设备、接口变更和平台规则风险。不要把个人 token、抓包文件、签名密钥上传到公开仓库。

## 安装包下载

最新版 Debug APK：

```text
https://github.com/wzs0512/qiekj-android/releases/download/v1.0.0-debug/qiekj-android-debug.apk
```

Release 页面：

```text
https://github.com/wzs0512/qiekj-android/releases/tag/v1.0.0-debug
```

本地编译后 APK 位置：

```text
C:\Users\33059\Documents\pg\app\build\outputs\apk\debug\app-debug.apk
```

## 当前功能

### 三底栏导航

App 当前为三底栏结构：

- 设备控制：原首页，展示历史设备并执行设备启动/解锁流程。
- 积分任务：新增页面，执行积分自动化任务并实时显示日志。
- 我的：手机号验证码登录、资产、Token 查看、历史订单入口。

### 设备控制

- 冷启动读取本地加密保存的 Token。
- 有 Token 时自动调用 `goods/latestUsed` 查询历史设备。
- 设备列表只显示 `goodsName`。
- 点击设备后按原脚本顺序执行：

```text
goods/normal/skus
goods/normal/details
userIntegral/checkUserIsRisk
payChannelRoute/addUserAfterPayChannel
orderRisk/isCheckLocation
goods/water/unlock
goods/water/sync
order/afterPay/creating
order/detail
```

- `goods/water/sync` 轮询到 `workStatus != 2` 后，才查询订单详情并弹窗展示。
- 订单详情包括订单号、订单 ID、订单原价、小票抵扣、积分抵扣和其他优惠。
- 成功查询到订单详情后会保存到本地历史订单。

### 积分任务

积分任务页包含：

- 顶部按钮：`开始执行自动化任务`
- 下方终端风格日志窗口
- 日志逐行追加
- 新日志自动滚动到底部

任务逻辑来自桌面 `积分.txt` 的 Python 脚本迁移：

- 自动通过 `WebSettings.getDefaultUserAgent(context)` 获取手机系统 UA，不需要手动粘贴。
- 直接读取“我的”页面登录后保存在本地的 Token。
- 复刻 `sign()` / `signzfb()` SHA-256 签名逻辑。
- 请求版本使用 `1.60.3`。
- 支持签到、首页浏览、任务列表、APP 视频任务、支付宝视频任务和积分统计。
- 所有等待使用 Kotlin `delay()`，不会阻塞 UI。
- 网络请求放到 `Dispatchers.IO` 执行。
- 不再手动设置 `Accept-Encoding: gzip`，交给 OkHttp 自动处理，避免 App 端响应解析异常。

日志中会显示 Token 前后 8 位，方便和“我的”页面完整 Token 对照。

### 我的页面

- 手机号验证码登录。
- 登录成功后 Token 加密保存到本地。
- 冷启动自动复用本地 Token。
- 资产展示：小票、积分、积分抵扣金额。
- `查看当前 Token`：弹窗展示当前本地保存的完整 Token，用于排查重新登录后 Token 是否变化。
- `历史订单`：位于 Token 按钮下方，点击后打开二级弹窗，展示最近保存的订单。
- 我的页面支持垂直滚动，避免小屏幕按钮被底部导航遮挡。

## 使用步骤

### 1. 登录

1. 打开 App，进入底部导航的“我的”。
2. 输入手机号。
3. 点击“发送验证码”。
4. 输入短信验证码。
5. 点击“确认登录”。
6. 登录成功后可点击“查看当前 Token”确认本地 Token。

### 2. 启动设备

1. 进入“设备控制”。
2. 等待历史设备加载。
3. 点击目标设备。
4. 等待设备使用结束。
5. App 会在设备结束后弹出订单详情，并保存到历史订单。

### 3. 查看历史订单

1. 进入“我的”。
2. 点击“历史订单”。
3. 在弹出的二级界面查看最近订单。
4. 点击某条订单可再次查看订单详情。

### 4. 执行积分任务

1. 先在“我的”页面完成登录。
2. 进入“积分任务”。
3. 点击“开始执行自动化任务”。
4. 查看日志窗口中的实时输出。

## 开发与编译

### 环境

- JDK 17
- Android SDK 35
- Gradle 8.10.2

### 编译 Debug APK

```powershell
cd C:\Users\33059\Documents\pg
$env:ANDROID_HOME='C:\Users\33059\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat assembleDebug
```

如果本机 Gradle Wrapper 下载慢，也可以使用项目内已缓存的 Gradle：

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
$env:PATH="$env:JAVA_HOME\bin;C:\Users\33059\Documents\pg\.build-tools\gradle-8.10.2\bin;$env:PATH"
gradle assembleDebug
```

## 关键目录

```text
app/src/main/java/com/example/devicecontrol/MainActivity.kt
app/src/main/java/com/example/devicecontrol/ui/AppViewModel.kt
app/src/main/java/com/example/devicecontrol/data/AppRepository.kt
app/src/main/java/com/example/devicecontrol/data/DeviceApi.kt
app/src/main/java/com/example/devicecontrol/data/PointsTaskRunner.kt
app/src/main/java/com/example/devicecontrol/data/TokenStore.kt
app/src/main/java/com/example/devicecontrol/data/OrderHistoryStore.kt
```

## 注意事项

- Token 会随手机号重新登录变化，这是平台正常行为；App 会保存最新登录得到的 Token。
- 如果积分任务在 Python 中正常、App 中失败，优先查看积分任务日志中的 HTTP 状态码、响应解析错误或 Token 前后 8 位。
- 接口返回结构变化时，可能需要同步更新 `Models.kt`、`DeviceApi.kt` 或 `PointsTaskRunner.kt`。