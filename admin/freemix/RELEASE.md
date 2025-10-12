# Release Instructions

## Uploading DMG Files

Due to Gitee's limitation on LFS support for free accounts, we cannot directly commit large DMG files to the repository. Instead, follow these steps to upload release versions:

1. Generate the DMG file using the build scripts:
   ```
   ./build-mac.sh
   ```

2. The DMG file will be generated in `dist-electron/mac/` directory.

3. Upload the DMG file manually to Gitee Releases:
   - Go to https://gitee.com/lcz1219/freemix/releases
   - Click "Create Release"
   - Upload the DMG file as an attachment

## Alternative Hosting Options

If Gitee Releases has size limitations, consider these alternatives:
- GitHub Releases (if you have a GitHub account)
- Cloud storage services (Google Drive, Dropbox, etc.)
- CDN services for better download speeds

## Build Scripts

We have several build scripts available:
- `./build-mac.sh` - Standard macOS build
- `./build-optimized.sh` - Optimized macOS build with size reduction
- `./build-minimal.sh` - Minimal macOS build with maximum size reduction