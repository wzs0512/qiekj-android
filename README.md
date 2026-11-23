# 胖乖生活

这是一个把 [`3ryng1um/qiekj`](https://github.com/3ryng1um/qiekj) 常用设备流程改造成 Android App 的项目。当前 App 使用 Kotlin + Jetpack Compose + Retrofit/OkHttp 实现。

> 说明：本项目用于复刻个人脚本的请求流程和移动端界面。请自行承担账号、设备、接口变更和平台规则风险。不要把个人 token、抓包文件、签名密钥上传到公开仓库。

## 安装包下载

最新版 Debug APK：

```text
https://github.com/wzs0512/qiekj-android/releases/download/v1.0.0-debug/qiekj-android-debug.apk
```

## 使用步骤

### 1. 登录

1. 打开 App，进入底部导航的“我的”。App 仅保留“设备控制”和“我的”两个页面。
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

## 注意事项

- Token 会随手机号重新登录变化，这是平台正常行为；App 会保存最新登录得到的 Token。
- 积分自动化任务模块已移除；“我的”页面中的积分余额仍会正常显示。
- 接口返回结构变化时，可能需要同步更新 `Models.kt` 或 `DeviceApi.kt`。


