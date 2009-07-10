#!/bin/sh
# This is a linux script to corrupt outgoing network traffic on interface ath0.
# It is meant to be used with a device emulator running on linux.

if [ `whoami` != root ]; then
  echo "Run this using sudo"
  exit 1
fi

# This command adds a ~30ms delay and drops ~30% of packets
# Substitute 'ath0' for your appropriate network interface
tc qdisc add dev ath0 root handle 1:0 netem delay 30msec 30msec 25% loss 30% 25%

#DELAY
# tc qdisc add dev ath0 root handle 1:0 netem delay 30msec 30msec 25%
#PACKET LOSS
# tc qdisc add dev ath0 root netem loss 30% 25%
#DUPLICATION
# tc qdisc change dev ath0 root netem duplicate 1%
#CORRUPTION
# tc qdisc change dev ath0 root netem corrupt 0.1%
#RE-ORDERING
# tc qdisc change dev ath0 root netem delay 10ms reorder 25% 50%

