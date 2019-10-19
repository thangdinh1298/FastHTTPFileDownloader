#!/bin/bash

BUILD_DIR="build"
SOURCE_PATH="src"
DAEMON_SCRIPT="daemon"
if [ -d $BUILD_DIR ]; then
	rm -r $BUILD_DIR
fi
mkdir -p $BUILD_DIR
javac -d $BUILD_DIR -sourcepath $SOURCE_PATH  $SOURCE_PATH/Client/Client.java
javac -d $BUILD_DIR -sourcepath $SOURCE_PATH  $SOURCE_PATH/Daemon/Daemon.java
echo 'cd "$(dirname "$(realpath "$0")")" && nohup java Daemon.Daemon &'  > $PWD/$BUILD_DIR/$DAEMON_SCRIPT
chmod +x $BUILD_DIR/$DAEMON_SCRIPT 
ln -s $PWD/$BUILD_DIR/$DAEMON_SCRIPT /usr/local/bin/$DAEMON_SCRIPT

