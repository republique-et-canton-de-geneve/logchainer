#!/bin/sh

echo run accessed

export VM_ARGS="-Dapplication.properties=../conf/application.properties"
export VM_ARGS="${VM_ARGS} -DerrorMessages.properties=../conf/errorMessages.properties"
export VM_ARGS="${VM_ARGS} -Dproperties_file=../conf/logback.properties"

export JAR_HOME=../lib/logchainer-1.0.6-SNAPSHOT.jar

# $JAVA_HOME $VM_ARGS -jar $JAR_HOME  #ch.ge.cti.logchainer.LogChainer &
$JAVA_HOME/bin/java $VM_ARGS -jar $JAR_HOME 