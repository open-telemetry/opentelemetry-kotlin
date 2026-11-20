#!/bin/bash
set -e

cd "$(dirname "$0")"
source ./ios-simulator.sh

check_xcode_version

echo "Building Kotlin iOS framework..."
../../gradlew :examples:example-app:linkDebugFrameworkIosSimulatorArm64

# run-ios.sh passes the simulator it picked; otherwise pick one here.
if [ -z "$SIMULATOR_ID" ]; then
    echo "Finding available iOS simulator..."
    find_simulator
fi

if [ -z "$SIMULATOR_ID" ]; then
    echo "No iPhone simulator with iOS $MIN_IOS_MAJOR+ found, using generic target"
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
echo "Build complete! To run in simulator: ./run-ios.sh"
