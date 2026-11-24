#!/usr/bin/env bash
set -euo pipefail

PACKAGE_NAME="com.aistudio.applet.gehjpf"
APK_SRC="app/build/outputs/apk/debug/app-debug.apk"
DIST_DIR="dist"
APK_OUT="$DIST_DIR/JARVIS-AI-debug.apk"

if [ -x "./gradlew" ]; then
  GRADLE_CMD="./gradlew"
elif command -v gradle >/dev/null 2>&1; then
  GRADLE_CMD="gradle"
else
  echo "Error: Neither ./gradlew nor gradle was found in PATH." >&2
  exit 1
fi

if [ ! -f "debug.keystore" ] && [ -f "debug.keystore.base64" ]; then
  echo "0. Decoding debug.keystore from debug.keystore.base64..."
  base64 -d debug.keystore.base64 > debug.keystore
fi

echo "1. Building debug APK with $GRADLE_CMD..."
"$GRADLE_CMD" :app:assembleDebug

echo "2. Copying APK to $DIST_DIR for easy access/download..."
mkdir -p "$DIST_DIR"
cp "$APK_SRC" "$APK_OUT"
echo "APK ready at: $APK_OUT"

if ! command -v adb >/dev/null 2>&1; then
  echo "Error: adb not found in PATH. Install Android Platform Tools to install on a device." >&2
  exit 1
fi

echo "3. Checking connected Android device via ADB..."
adb wait-for-device
adb devices

echo "4. Installing APK on device..."
adb install -r "$APK_OUT"

echo "5. Launching app ($PACKAGE_NAME)..."
adb shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1

echo "All done!"
