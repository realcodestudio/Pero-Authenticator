# WearOTP - Android 11 手表兼容性说明

## 📱 兼容性配置

### 系统要求
- **最低Android版本**: Android 11 (API 30)
- **目标Android版本**: Android 14 (API 34)
- **支持架构**: ARM v7a, ARM64 v8a

### 优化配置
为确保在Android 11手表上的最佳兼容性，已进行以下优化：

#### 1. SDK版本调整
- `minSdk`: 30 (Android 11)
- `targetSdk`: 34 (降低到34以提高兼容性)
- `compileSdk`: 34 (使用稳定版本)

#### 2. 依赖版本优化
- **Wear Compose**: 使用1.3.1稳定版本
- **Compose BOM**: 使用2024.02.00稳定版本
- **Play Services**: 18.1.0版本
- **Core库**: 使用兼容版本

#### 3. 架构支持
- 支持`armeabi-v7a`和`arm64-v8a`架构
- 移除了过时的`x86_64`支持

#### 4. 清理过时配置
- 移除了`wear-sdk`库依赖（新版本不再需要）
- 移除了`extractNativeLibs`配置（由AGP自动处理）

## 🚀 安装说明

### APK文件位置
- **手机版**: `mobile/build/outputs/apk/release/mobile-release.apk`
- **手表版**: `wear/build/outputs/apk/release/wear-release.apk`

### 安装步骤

#### 方法一：通过ADB安装（推荐）
```bash
# 安装到手机
adb install mobile/build/outputs/apk/release/mobile-release.apk

# 安装到手表（需要先连接手表）
adb -s [手表设备ID] install wear/build/outputs/apk/release/wear-release.apk
```

#### 方法二：通过开发者模式安装
1. 在手表上启用开发者模式和ADB调试
2. 通过USB或WiFi连接手表
3. 使用Android Studio或命令行安装APK

### 验证安装
安装完成后，检查以下项目：
- [ ] 应用图标出现在手表应用列表中
- [ ] 应用可以正常启动
- [ ] 界面显示正常
- [ ] 与手机版本可以正常同步（如果安装了手机版）

## ⚠️ 已知问题

### 编译警告（不影响功能）
构建过程中会出现一些弃用API的警告，这些是由于Wear Compose库版本更新导致的，不影响应用在Android 11手表上的正常运行。

### 兼容性测试
建议在以下设备上进行测试：
- Samsung Galaxy Watch4/5 (Android 11+)
- Fossil Gen 6 (Android 11+)
- TicWatch Pro 3/4 (Android 11+)
- 其他运行Android 11+的Wear OS设备

## 🔧 故障排除

### 如果安装失败
1. 确认手表运行Android 11或更高版本
2. 检查是否启用了"安装未知来源应用"
3. 确认手表有足够的存储空间
4. 尝试重启手表后再次安装

### 如果应用无法启动
1. 检查手表的内存使用情况
2. 确认所有必要的系统服务正在运行
3. 查看系统日志获取详细错误信息

## 📞 技术支持

如果在Android 11手表上遇到任何问题，请提供以下信息：
- 手表型号和Android版本
- 错误日志（通过`adb logcat`获取）
- 具体的问题描述和重现步骤

---
**构建时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**版本**: 1.0 Release