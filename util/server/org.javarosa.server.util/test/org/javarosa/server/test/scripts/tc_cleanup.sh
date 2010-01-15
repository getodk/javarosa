#!/bin/sh
# This is a linux script to clean up after tc.sh
# It is mean to be used in conjunction with a device emulator running on linux.

if [ `whoami` != root ]; then
  echo "Run this using sudo"
  exit 1
fi

#CLEANUP
# Substitute 'ath0' for your appropriate network interface
tc qdisc del dev ath0 root
