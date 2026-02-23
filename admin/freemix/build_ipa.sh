#!/bin/bash

# 遇到错误立即停止
set -e

echo "🚀 [1/5] 开始构建前端 Web 资源..."
if [ -z "$SKIP_BUILD" ]; then
  npm run build
else
  echo "Skipping npm run build (SKIP_BUILD is set)..."
fi

echo "🔄 [2/5] 同步 Capacitor 配置和资源..."
npx cap sync

echo "🧹 [3/5] 清理旧的构建产物..."
rm -rf output ios_build Payload
mkdir -p output

echo "🔨 [4/5] 使用 xcodebuild 构建 iOS 项目 (未签名)..."
# 使用 -derivedDataPath 指定构建输出路径，避免处理随机哈希路径
xcodebuild -workspace ios/App/App.xcworkspace \
  -scheme App \
  -configuration Release \
  -sdk iphoneos \
  -derivedDataPath ios_build \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  CODE_SIGNING_ALLOWED=NO \
  clean build

echo "📦 [5/5] 打包 IPA 文件..."
mkdir Payload
# 复制 .app 包到 Payload 目录
cp -r ios_build/Build/Products/Release-iphoneos/App.app Payload/

# 压缩生成 ipa
zip -r output/App.ipa Payload > /dev/null

# 清理临时文件
rm -rf Payload ios_build

echo "✅ 构建成功！"
echo "📂 IPA 文件路径: $(pwd)/output/App.ipa"
echo "⚠️  注意: 此 IPA 为未签名版本(Unsigned)，请使用 Sideloadly 或其他签名工具签名后安装。"
