#!/bin/sh
BUILD_DIR="build"
DAEMON_SCRIPT="daemon"

[ -d "$BUILD_DIR" ] && rm -r "$BUILD_DIR"
[ -f ~/.local/bin/"$DAEMON_SCRIPT" ] && rm ~/.local/bin/"$DAEMON_SCRIPT"
