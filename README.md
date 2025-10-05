# WearOTP - Android & Wear OS OTP应用

一个支持Android 11和Wear OS的双平台OTP（一次性密码）认证应用，支持armv7和x86_64架构。

## 功能特性

### 手机端 (Mobile)
- 📱 现代化的Material Design 3界面
- 📷 二维码扫描添加OTP账户
- ✏️ 手动添加OTP账户
- 🔐 支持TOTP和HOTP算法
- 💾 本地数据库存储（Room）
- 📋 一键复制OTP代码
- 🎨 支持深色/浅色主题
- 🔄 实时倒计时显示

### 手表端 (Wear OS)
- ⌚ 专为圆形表盘优化的界面
- 🔄 实时OTP代码显示
- 📋 点击复制到剪贴板
- 🎯 简洁的卡片式布局
- ⏱️ 圆形进度指示器
- 🌙 支持Wear OS深色主题

## 技术规格

- **最低Android版本**: Android 11 (API 30)
- **目标Android版本**: API 36
- **支持架构**: armv7, x86_64
- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **数据库**: Room
- **OTP算法**: TOTP/HOTP (SHA1/SHA256/SHA512)

## 项目结构

```
WearOTP/
├── mobile/                 # 手机端应用
│   ├── src/main/java/com/rcbs/wearotp/
│   │   ├── data/          # 数据层 (Room数据库)
│   │   ├── repository/    # 数据仓库
│   │   ├── viewmodel/     # ViewModel层
│   │   ├── ui/            # UI组件和界面
│   │   └── utils/         # 工具类 (OTP生成器)
│   └── build.gradle.kts
├── wear/                  # 手表端应用
│   ├── src/main/java/com/rcbs/wearotp/
│   │   ├── data/          # 数据模型
│   │   ├── presentation/  # UI界面
│   │   ├── viewmodel/     # ViewModel层
│   │   └── utils/         # 工具类
│   └── build.gradle.kts
└── gradle/
    └── libs.versions.toml # 依赖版本管理
```

## 主要依赖

- **Jetpack Compose**: 现代化UI开发
- **Room Database**: 本地数据存储
- **Navigation Compose**: 导航管理
- **ZXing**: 二维码扫描
- **CameraX**: 相机功能
- **Commons Codec**: Base32编码解码
- **Wear Compose**: Wear OS专用UI组件

## 安装和构建

1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 连接Android设备或启动模拟器
5. 运行mobile模块安装手机应用
6. 连接Wear OS设备或启动Wear OS模拟器
7. 运行wear模块安装手表应用

## 使用方法

### 添加OTP账户

1. **扫描二维码**:
   - 点击手机应用右上角的"+"按钮
   - 选择"扫描二维码"
   - 将摄像头对准OTP二维码
   - 应用会自动解析并添加账户

2. **手动添加**:
   - 点击"手动添加"
   - 填写账户名、发行商、密钥等信息
   - 选择算法、位数、周期等参数
   - 点击"保存"

### 查看OTP代码

- 手机端：在主界面查看所有账户的OTP代码
- 手表端：滑动查看不同账户的OTP代码
- 点击代码可复制到剪贴板
- 观察进度条倒计时，代码会自动刷新

## 安全特性

- 所有密钥本地存储，不上传云端
- 使用标准的TOTP/HOTP算法
- 支持多种哈希算法 (SHA1/SHA256/SHA512)
- 遵循RFC 6238和RFC 4226标准

## 支持的服务

支持所有遵循标准OTP协议的服务，包括但不限于：
- Google Authenticator
- Microsoft Authenticator
- GitHub
- Discord
- Steam
- AWS
- 其他支持TOTP/HOTP的服务

## 开发者信息

- 项目使用Kotlin和Jetpack Compose开发
- 遵循MVVM架构模式
- 支持Material Design 3设计规范
- 兼容Android 11+和Wear OS 3+

## 许可证

本项目仅供学习和研究使用。

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。