#!/bin/sh

kill `/bin/ps -fu $USER | grep "logchainer-" | grep -v "grep" | awk '{print $2}'`