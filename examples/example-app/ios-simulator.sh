#!/bin/bash
# Shared helpers for build-ios.sh and run-ios.sh. Source this file from the example-app directory.

# Oldest Xcode that can open iosApp/ExampleApp.xcodeproj (its compatibilityVersion).
MIN_XCODE_MAJOR=14
# Oldest iOS runtime the app can be installed on, taken from the Xcode project.
MIN_IOS_MAJOR=$(grep -m1 -oE 'IPHONEOS_DEPLOYMENT_TARGET = [0-9]+' iosApp/ExampleApp.xcodeproj/project.pbxproj | awk '{print $3}')

fail() {
    echo "Error: $1" >&2
    exit 1
}

check_xcode_version() {
    XCODE_VERSION=$(xcodebuild -version 2>/dev/null | awk 'NR == 1 {print $2}')
    if [ -z "$XCODE_VERSION" ]; then
        fail "xcodebuild not found. Install Xcode and run 'sudo xcode-select -s /Applications/Xcode.app'."
    fi
    if [ "${XCODE_VERSION%%.*}" -lt "$MIN_XCODE_MAJOR" ]; then
        fail "Xcode $XCODE_VERSION is too old. Xcode $MIN_XCODE_MAJOR or newer is required."
    fi
    echo "Using Xcode $XCODE_VERSION"
}

# Sets SIMULATOR_ID and SIMULATOR_NAME to an available iPhone on the newest iOS runtime that
# satisfies the deployment target. Leaves both empty if there is none.
find_simulator() {
    local device_line
    device_line=$(xcrun simctl list devices available | awk -v min="$MIN_IOS_MAJOR" '
        /^-- iOS / { split($3, version, "."); usable = (version[1] + 0 >= min); next }
        /^--/ { usable = 0; next }
        usable && /iPhone/ { line = $0 }
        END { print line }')
    SIMULATOR_ID=$(echo "$device_line" | sed -nE 's/.*\(([A-F0-9-]{36})\).*/\1/p')
    SIMULATOR_NAME=$(echo "$device_line" | sed -nE 's/^[[:space:]]*(.*) \([A-F0-9-]{36}\).*/\1/p')
}

# Opens the simulator UI: Simulator.app up to Xcode 26, DeviceHub.app from Xcode 27.
open_simulator_ui() {
    local developer_dir="${DEVELOPER_DIR:-$(xcode-select -p)}"
    local app
    for app in "$developer_dir/Applications/Simulator.app" "$developer_dir/../Applications/DeviceHub.app"; do
        if [ -d "$app" ] && open -a "$app"; then
            return 0
        fi
    done
    echo "Warning: neither Simulator.app nor DeviceHub.app was found in $developer_dir, so the simulator is running headless and the app won't be visible." >&2
}
