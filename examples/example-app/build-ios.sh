#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Building Kotlin iOS framework..."
../../gradlew :examples:example-app:linkDebugFrameworkIosSimulatorArm64

echo "Finding available iOS simulator..."
DEVICE_LINE=$(xcrun simctl list devices available iPhone | grep "iPhone" | head -1)
SIMULATOR_NAME=$(echo "$DEVICE_LINE" | sed -E 's/^[[:space:]]+([^(]+) \(.*/\1/' | xargs)
SIMULATOR_ID=$(echo "$DEVICE_LINE" | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/')

if [ -z "$SIMULATOR_ID" ]; then
    echo "No iPhone simulator found, using generic target"
    DESTINATION="generic/platform=iOS Simulator"
else
    echo "Using simulator: $SIMULATOR_NAME (ID: $SIMULATOR_ID)"
    DESTINATION="platform=iOS Simulator,id=$SIMULATOR_ID"
fi

echo "Building iOS app..."
cd iosApp
xcodebuild -project ExampleApp.xcodeproj \
    -scheme ExampleApp \
    -configuration Debug \
    -destination "$DESTINATION" \
    -derivedDataPath build \
    build

echo ""
echo "Build complete! To run in simulator:"
echo "xcrun simctl boot '$SIMULATOR_ID' 2>/dev/null || true"
echo "xcrun simctl install booted iosApp/build/Build/Products/Debug-iphonesimulator/ExampleApp.app"
echo "xcrun simctl launch booted io.opentelemetry.kotlin.example.ExampleApp"
