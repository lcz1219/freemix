# iOS IPA 构建指南 (命令行版)

本文档介绍如何在没有 Apple 开发者账号的情况下，使用命令行工具构建未签名的 IPA 包。

## 🚀 快速开始

我已经为您创建了一个自动化脚本，您可以直接运行它来完成所有步骤：

```bash
./build_ipa.sh
```

构建成功后，IPA 文件将位于：`output/App.ipa`

---

## 🛠 手动构建步骤详解

如果您想了解脚本背后的具体命令，请参考以下步骤：

### 1. 构建前端资源
首先需要将 Vue 代码编译为静态资源。
```bash
npm run build
```

### 2. 同步到原生项目
将编译好的 web 资源同步到 iOS 工程目录中。
```bash
npx cap sync
```

### 3. 构建 iOS 项目 (xcodebuild)
使用 xcodebuild 命令编译 iOS 项目。关键参数说明：
- `-derivedDataPath ios_build`: 指定构建输出目录，方便后续查找。
- `CODE_SIGNING_REQUIRED=NO`: 禁用强制签名。
- `CODE_SIGN_ALLOWED=NO`: 禁止签名过程。

```bash
xcodebuild -workspace ios/App/App.xcworkspace \
  -scheme App \
  -configuration Release \
  -sdk iphoneos \
  -derivedDataPath ios_build \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  CODE_SIGNING_ALLOWED=NO \
  clean build
```

### 4. 打包 IPA
iOS 的 IPA 包本质上是一个包含 `Payload` 文件夹的 zip 压缩包。

1. 创建 Payload 文件夹：
   ```bash
   mkdir Payload
   ```

2. 将编译生成的 `.app` 文件复制进去：
   ```bash
   cp -r ios_build/Build/Products/Release-iphoneos/App.app Payload/
   ```

3. 压缩生成 `.ipa` 文件：
   ```bash
   mkdir -p output
   zip -r output/App.ipa Payload
   ```

4. 清理临时文件：
   ```bash
   rm -rf Payload ios_build
   ```

---

## 📱 如何安装此 IPA？

由于此 IPA 是**未签名 (Unsigned)** 的，无法直接通过 iTunes 或 AirDrop 安装。您需要根据您的设备情况选择安装方式：

### 方法 A：非越狱设备 (推荐)
使用个人 Apple ID 进行自签名安装（有效期 7 天）。
1. 下载并安装 **Sideloadly** (Windows/Mac) 或 **AltStore**。
2. 连接手机到电脑。
3. 将 `output/App.ipa` 拖入工具中。
4. 输入您的 Apple ID 完成签名并安装。

### 方法 B：越狱设备
如果您的设备已越狱并安装了 **AppSync Unified** 插件，您可以直接通过 Filza 等工具安装此 IPA。

### 方法 C：企业签名
如果您有第三方企业签名服务，上传此 IPA 签名后即可分发安装。
