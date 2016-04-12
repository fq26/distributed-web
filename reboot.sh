#!/bin/bash

HOME_PATH="/var/tmp/"
REBOOT_FILE=rebootNum.txt

# get current reboot number
echo ">>>>>> Getting current reboot number"
REBOOT_NUM="$(cat $HOME_PATH$REBOOT_FILE)"

# increment reboot number by 1
echo ">>>>>> Increment current reboot number and store back"
REBOOT_NUM=$((REBOOT_NUM+1))

# rewrite rebootNum.txt
echo $REBOOT_NUM > "$HOME_PATH$REBOOT_FILE"

# restart tomcat
echo ">>>>>> Restart tomcat service"
sudo service tomcat8 stop
sudo service tomcat8 start