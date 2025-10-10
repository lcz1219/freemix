#!/bin/bash

# macOS Electron Application Build Script
echo "Building macOS Electron Application..."

# Set environment variables for faster downloads
export ELECTRON_MIRROR=https://npmmirror.com/mirrors/electron/
export ELECTRON_BUILDER_BINARIES_MIRROR=https://npmmirror.com/mirrors/electron-builder-binaries/

# Build the application
echo "Running build process..."
npm run build

# Build macOS application with mirrors and without signing
echo "Building macOS application with mirror sources (no code signing)..."
npx electron-builder --config.directories.output=dist-electron/mac

echo "macOS application build completed!"
echo "Output files are located in dist-electron/ directory"