#!/bin/bash
set -e

cd "$(dirname "$0")"
source ./ios-simulator.sh

BUNDLE_ID=io.opentelemetry.kotlin.example.ExampleApp
APP_PATH=iosApp/build/Build/Products/Debug-iphonesimulator/ExampleApp.app

check_xcode_version

echo "Finding available iOS simulator..."
find_simulator
if [ -z "$SIMULATOR_ID" ]; then
    fail "No iPhone simulator with iOS $MIN_IOS_MAJOR+ found. Install an iOS runtime and create an iPhone simulator in Xcode."
fi
echo "Using simulator: $SIMULATOR_NAME (ID: $SIMULATOR_ID)"

# Build the app for the same simulator
SIMULATOR_ID="$SIMULATOR_ID" SIMULATOR_NAME="$SIMULATOR_NAME" ./build-ios.sh

# Launch in simulator
echo ""
echo "Launching iOS simulator..."

# Boots the device if needed and blocks until boot completes; non-zero on failure.
xcrun simctl bootstatus "$SIMULATOR_ID" -b || fail "Simulator $SIMULATOR_NAME ($SIMULATOR_ID) failed to boot."

open_simulator_ui

xcrun simctl install "$SIMULATOR_ID" "$APP_PATH" || fail "Failed to install $APP_PATH on $SIMULATOR_NAME."

LAUNCH_OUTPUT=$(xcrun simctl launch "$SIMULATOR_ID" "$BUNDLE_ID") || fail "Failed to launch $BUNDLE_ID: $LAUNCH_OUTPUT"
APP_PID=$(echo "$LAUNCH_OUTPUT" | awk '{print $NF}')
case "$APP_PID" in
    ''|*[!0-9]*) fail "Unexpected output from simctl launch: $LAUNCH_OUTPUT" ;;
esac

# Catch apps that crash on startup, e.g. an unsupported SdkMode in AppConfig.
sleep 3
if ! xcrun simctl spawn "$SIMULATOR_ID" launchctl list | grep "UIKitApplication:$BUNDLE_ID" >/dev/null; then
    fail "App exited shortly after launch (PID $APP_PID). Check crash logs in ~/Library/Logs/DiagnosticReports."
fi

echo "App launched successfully on $SIMULATOR_NAME (PID: $APP_PID)"
