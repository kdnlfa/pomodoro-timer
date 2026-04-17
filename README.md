# Pomodoro Timer

一个从简单版本开始逐步演进的 Android 番茄钟项目。

## V3 范围

- Kotlin + Android View(XML) 实现
- 支持 25 分钟专注和 5 分钟休息两个阶段
- 支持开始、暂停、重置
- 支持通过开关决定是否自动进入下一阶段
- 暂不加入通知、历史记录和设置

## 本地构建

在项目根目录执行：

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

需要本机已安装：

- JDK 17
- Android SDK
