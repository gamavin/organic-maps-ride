#!/usr/bin/env bash
case "$1" in
  android_code)
    echo 1
    ;;
  android_name)
    echo "1.0"
    ;;
  *)
    echo "unknown"
    ;;
 esac
