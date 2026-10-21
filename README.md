# 胖乖生活 Android App1.2有问题，用1.1

将 [`3ryng1um/qiekj`](https://github.com/3ryng1um/qiekj) 的常用设备流程整理为 Android 应用。项目使用 Kotlin、Jetpack Compose、Retrofit 和 OkHttp。

## 当前功能

应用底部仅保留两个页面：

- **设备控制**：查看历史设备、刷新设备列表、点击设备开始使用，并可将设备添加到桌面快捷方式。
- **我的**：手机号验证码登录、查看当前 Token、查看历史订单，以及刷新小票、积分和积分抵扣金额。

积分自动化任务页面及其失效流程已从应用中移除。账户资产里的积分余额仍会正常显示。

## 使用方法

1. 打开“我的”，输入手机号并获取短信验证码，然后登录。
2. 登录后打开“设备控制”，等待历史设备同步。
3. 点击设备卡片开始使用；完成后可在“我的 → 历史订单”查看订单详情。
4. 在“我的”页面刷新账户资产，或查看当前登录 Token。

## 构建

在 Windows 上运行：

```powershell
.\gradlew.bat assembleDebug
```

Debug APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`。

## 注意事项

- 手机号、验证码和 Token 属于敏感信息，不要提交到公开仓库或截图分享。
- 登录、设备及资产接口可能随服务端变化而调整；异常时请检查应用提示和网络响应。
- 本项目用于个人设备流程与界面管理，请遵循相关平台规则。

## 项目

- 源码：[wzs0512/qiekj-android](https://github.com/wzs0512/qiekj-android)
- 发布版本：[GitHub Releases](https://github.com/wzs0512/qiekj-android/releases)
