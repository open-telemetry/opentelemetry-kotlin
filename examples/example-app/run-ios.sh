#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Finding available iOS simulator..."
DEVICE_LINE=$(xcrun simctl list devices available iPhone | grep "iPhone" | head -1)
SIMULATOR_NAME=$(echo "$DEVICE_LINE" | sed -E 's/^[[:space:]]+([^(]+) \(.*/\1/' | xargs)
SIMULATOR_ID=$(echo "$DEVICE_LINE" | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/')

if [ -z "$SIMULATOR_ID" ]; then
    echo "Error: No iPhone simulator found. Please create one in Xcode."
    exit 1
fi
echo "Using simulator: $SIMULATOR_NAME (ID: $SIMULATOR_ID)"

# Build the app
./build-ios.sh

# Launch in simulator
echo ""
echo "Launching iOS simulator..."
xcrun simctl boot "$SIMULATOR_ID" 2>/dev/null || echo "Simulator already running"
open -a Simulator
xcrun simctl install booted iosApp/build/Build/Products/Debug-iphonesimulator/ExampleApp.app
APP_PID=$(xcrun simctl launch booted io.opentelemetry.kotlin.example.ExampleApp | awk '{print $NF}')
echo "App launched successfully (PID: $APP_PID)"
echo "The app is now running in the iOS Simulator."
