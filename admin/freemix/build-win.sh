#!/bin/bash
set -e

# Windows Electron Application Build Script
echo "Building Windows Electron Application..."

# Clean previous build artifacts
if [ -d "dist-electron/win" ]; then
    echo "Cleaning previous Windows build artifacts..."
    rm -rf dist-electron/win
fi

# Set environment variables for faster downloads
export ELECTRON_MIRROR=https://npmmirror.com/mirrors/electron/
export ELECTRON_BUILDER_BINARIES_MIRROR=https://npmmirror.com/mirrors/electron-builder-binaries/
export ELECTRON_DOWNLOAD_MIRROR=https://npmmirror.com/mirrors/electron/

# Build the application
if [ -z "$SKIP_BUILD" ]; then
  echo "Running build process..."
  npm run build
else
  echo "Skipping build process (SKIP_BUILD is set)..."
fi

# Build Windows application with mirrors and without signing
echo "Building Windows application with mirror sources (no code signing)..."
npx electron-builder --win --x64 --config.win.signAndEditExecutable=false --config.directories.output=dist-electron/win

echo "Windows application build completed!"
echo "Output files are located in dist-electron/win/ directory"