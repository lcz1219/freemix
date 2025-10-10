#!/bin/bash

# Windows Electron Application Build Script
echo "Building Windows Electron Application..."

# Set environment variables for faster downloads
export ELECTRON_MIRROR=https://npmmirror.com/mirrors/electron/
export ELECTRON_BUILDER_BINARIES_MIRROR=https://npmmirror.com/mirrors/electron-builder-binaries/
export ELECTRON_DOWNLOAD_MIRROR=https://npmmirror.com/mirrors/electron/

# Build the application
echo "Running build process..."
npm run build

# Build Windows application with mirrors and without signing
echo "Building Windows application with mirror sources (no code signing)..."
npx electron-builder --win --x64 --config.win.signAndEditExecutable=false --config.directories.output=dist-electron/win

echo "Windows application build completed!"
echo "Output files are located in dist-electron/win/ directory"