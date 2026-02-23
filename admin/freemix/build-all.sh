#!/bin/bash
set -e

# Freemix Unified Build Script
# Builds for macOS, Windows, and iOS (IPA) sequentially.

echo "========================================================"
echo "   🚀 Starting Freemix Unified Build Process"
echo "   Platforms: macOS, Windows, iOS (IPA)"
echo "========================================================"

# Step 1: Build the frontend web resources once
echo ""
echo "📦 [1/4] Building Frontend Web Resources..."
echo "--------------------------------------------------------"
npm run build
echo "✅ Frontend build completed."

# Set environment variable to skip rebuilds in sub-scripts
export SKIP_BUILD=true

# Step 2: Build macOS Application
echo ""
echo "🍏 [2/4] Building macOS Application..."
echo "--------------------------------------------------------"
./build-mac.sh
echo "✅ macOS build completed."

# Step 3: Build Windows Application
echo ""
echo "🪟 [3/4] Building Windows Application..."
echo "--------------------------------------------------------"
./build-win.sh
echo "✅ Windows build completed."

# Step 4: Build iOS IPA
echo ""
echo "📱 [4/4] Building iOS IPA..."
echo "--------------------------------------------------------"
./build_ipa.sh
echo "✅ iOS IPA build completed."

echo ""
echo "========================================================"
echo "   🎉 All builds completed successfully!"
echo "========================================================"
echo "Outputs:"
echo "  - macOS:   dist-electron/mac/"
echo "  - Windows: dist-electron/win/"
echo "  - iOS:     output/App.ipa"
