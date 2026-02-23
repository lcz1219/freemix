#!/bin/bash
set -e

# macOS Electron Application Build Script
echo "Building macOS Electron Application..."

# Clean previous build artifacts
if [ -d "dist-electron/mac" ]; then
    echo "Cleaning previous macOS build artifacts..."
    rm -rf dist-electron/mac
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

# Build macOS application with mirrors and without signing
echo "Building macOS application with mirror sources (no code signing)..."
npx electron-builder --config.directories.output=dist-electron/mac

echo "macOS application build completed!"
echo "Output files are located in dist-electron/mac directory"