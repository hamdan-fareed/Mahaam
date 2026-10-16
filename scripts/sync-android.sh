#!/usr/bin/env bash
# Sync web assets to Android project
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Syncing web assets to Android project..."
mkdir -p "$ROOT_DIR/android/app/src/main/assets/www"
cp -R "$ROOT_DIR/src/"* "$ROOT_DIR/android/app/src/main/assets/www/"
echo "Assets successfully synced to android/app/src/main/assets/www/"
