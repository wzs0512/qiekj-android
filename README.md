# Qiekj Android

一个把 [`3ryng1um/qiekj`](https://github.com/3ryng1um/qiekj) Python 脚本流程改造成 Android App 的项目。原仓库说明这个流程主要用于胖乖生活饮水设备：通过登录获取 token，读取最近使用设备，获取设备 `skuId` / `imei`，再执行饮水解锁、同步状态和订单详情查询。

> 说明：本项目只是在 Android 端复刻个人脚本的请求流程。订单仍应按平台规则支付，请自行承担账号、设备和接口变更带来的风险。

## 功能

- 手机号验证码登录，登录成功后本地加密保存 token
- 冷启动自动读取 token，不需要每次重新登录
- 自动查询最近使用过的饮水设备
- 首页点击设备即可启动完整解锁流程
- 解锁后轮询设备状态，结束后查询订单原价和小票消耗
- 我的页展示小票、积分、积分抵扣金额

## 使用教程

### 1. 安装 APK

安装调试包：

```text
app/build/outputs/apk/debug/app-debug.apk
```

如果手机提示“禁止安装未知来源应用”，在系统设置里允许当前文件管理器或浏览器安装应用即可。

### 2. 登录

1. 打开 App，进入底部导航的“我的”页面。
2. 输入手机号。
3. 点击“发送验证码”。
4. 收到短信后输入验证码。
5. 点击“确认登录”。

登录成功后，App 会把 token 加密保存到本机。之后冷启动会优先读取本地 token，并自动刷新历史设备和资产信息。

### 3. 查看资产

登录成功后，“我的”页面底部会显示：

- 小票：来自接口中的 `tokenCoin`，按源码逻辑除以 100 展示
- 积分：来自 `integral`
- 积分抵扣金额：来自 `integralAmount`

如果资产没有刷新，点击“刷新”即可重新查询。

### 4. 启动设备

1. 进入底部导航的“设备控制”页面。
2. 等待历史设备加载完成。
3. 列表中每一行只展示设备名，即接口返回的 `goodsName`。
4. 点击需要启动的设备。
5. App 会按顺序自动执行：

```text
goods/latestUsed
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

当 `goods/water/sync` 返回的 `workStatus != 2` 时，App 会退出轮询，并提示订单原价与花费小票。

## 和原脚本的对应关系

| Android App | 原 Python 脚本函数 |
| --- | --- |
| 发送验证码 | `login()` 中的 `common/sms/sendCode` |
| 确认登录 | `login()` 中的 `user/reg` |
| 查询资产 | `query_balance()` |
| 查询历史设备 | `get_latest_used()` |
| 获取 SKU | `goodsid2sku()` |
| 获取 IMEI | `get_imei()` |
| 检查积分/风控 | `use_intergral()` |
| 核心解锁 | `whole_unlock_water()` / `unlock_water()` |
| 同步和支付详情 | `afterpay()` |

## 开发与编译

本项目使用 Kotlin + Jetpack Compose + Retrofit。

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

APK 输出：

```text
C:\Users\33059\Documents\pg\app\build\outputs\apk\debug\app-debug.apk
```

## 目录结构

```text
.
├── app
│   └── src/main
│       ├── AndroidManifest.xml
│       ├── java/com/example/devicecontrol
│       │   ├── MainActivity.kt
│       │   ├── data
│       │   │   ├── AppRepository.kt
│       │   │   ├── DeviceApi.kt
│       │   │   ├── HeaderInterceptor.kt
│       │   │   ├── Models.kt
│       │   │   └── TokenStore.kt
│       │   └── ui
│       │       ├── AppViewModel.kt
│       │       └── theme/Theme.kt
│       └── res
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 注意事项

- 参考仓库提到，新用户接水后可能无法打印支付信息；本项目已尽量按脚本流程查询订单详情，但接口返回仍可能因账号状态不同而变化。
- 如果接口返回结构变化，App 可能需要同步更新 `Models.kt` 和 `DeviceApi.kt`。
- 不要把个人 token、抓包文件、签名密钥上传到公开仓库。
