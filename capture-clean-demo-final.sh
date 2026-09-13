#!/usr/bin/env bash
set -euo pipefail
source /home/ubuntu/firemind-tooling/env.sh
cd /home/ubuntu/firemind
adb shell pm clear com.firemind >/tmp/firemind-clear-final.log
adb shell monkey -p com.firemind 1 >/tmp/firemind-clean-final-launch.log 2>&1
sleep 15
adb shell screenrecord --size 1280x720 --bit-rate 3000000 --time-limit 55 /sdcard/firemind-clean-demo-final.mp4 >/tmp/firemind-clean-final-record.log 2>&1 &
REC_PID=$!
sleep 3
adb shell input tap 1630 860
sleep 10
adb shell input tap 960 754
sleep 12
adb shell uiautomator dump /sdcard/final-details.xml >/dev/null
adb exec-out cat /sdcard/final-details.xml > /tmp/firemind-final-details.xml
rg -q 'text="Remove from Watchlist"' /tmp/firemind-final-details.xml
adb shell input tap 1600 80
sleep 12
adb shell uiautomator dump /sdcard/final-watchlist.xml >/dev/null
adb exec-out cat /sdcard/final-watchlist.xml > /tmp/firemind-final-watchlist.xml
rg -q 'text="WATCHLIST"' /tmp/firemind-final-watchlist.xml
rg -q 'text="Sing Street' /tmp/firemind-final-watchlist.xml
wait "$REC_PID" || true
adb pull -p /sdcard/firemind-clean-demo-final.mp4 media/firemind-clean-demo-final.mp4 >/tmp/firemind-clean-final-pull.log
ffprobe -v error -show_entries format=duration,size -of default=noprint_wrappers=1 media/firemind-clean-demo-final.mp4
