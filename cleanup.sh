#!/bin/sh
#echo $0
#echo $1

a=1000000
name=Daemon.Daemon
info=$(ps aux | grep $name | awk '{if(NR > 0 && $2 < $a){a=$2; print($2)}}')
echo $info

kill -9 $info  #work
