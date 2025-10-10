# 更换应用图标指南

## 准备工作

为了更换 Electron 应用的图标，您需要准备以下格式的图标文件：

### 1. macOS (ICNS 格式)
- 文件名: `icon.icns`
- 位置: `build/icons/icon.icns`

### 2. Windows (ICO 格式)
- 文件名: `icon.ico`
- 尺寸: 至少 256x256 像素
- 位置: `build/icons/icon.ico`

### 3. Linux (PNG 格式)
- 文件名: `icon.png`
- 推荐尺寸: 512x512 像素
- 位置: `build/icons/icon.png`

## 制作图标文件的方法

### 方法一：在线转换工具
1. 访问在线图标转换网站（如 https://convertICO.com 或 https://cloudconvert.com）
2. 上传您的 PNG 图片（推荐 1024x1024 像素）
3. 转换为所需格式（ICNS、ICO、PNG）

### 方法二：使用图像编辑软件
1. 使用 Photoshop、GIMP 等软件创建多种尺寸的图标
2. 使用专门的图标制作工具（如 IconForge、Axialis IconGenerator）

### 方法三：命令行工具
对于 macOS 用户，可以使用 sips 和 iconutil：
```bash
# 创建 ICNS 文件需要先创建图标集目录
mkdir -p icon.iconset
# 添加不同尺寸的图片
cp icon_16x16.png icon.iconset/icon_16x16.png
cp icon_32x32.png icon.iconset/icon_16x16@2x.png
cp icon_32x32.png icon.iconset/icon_32x32.png
cp icon_64x64.png icon.iconset/icon_32x32@2x.png
# ... 继续添加其他尺寸
# 创建 ICNS 文件
iconutil -c icns icon.iconset
```

## 替换图标文件

1. 将您准备好的图标文件按以下路径放置：
   ```
   build/
     icons/
       icon.icns  (macOS)
       icon.ico   (Windows)
       icon.png   (Linux)
   ```

2. 运行构建命令重新生成应用：
   ```bash
   # macOS
   ./build-mac.sh
   
   # Windows
   ./build-win.sh
   
   # Linux
   ./build-linux.sh
   ```

## 注意事项

1. 确保图标文件具有适当的读取权限
2. 图标文件名必须与配置文件中指定的名称完全一致
3. 建议使用透明背景的 PNG 图片来制作图标
4. 构建前删除 `dist-electron` 目录以确保使用新图标