#!/bin/sh

echo run accessed

export VM_ARGS="-Dapplication.properties=../conf/application.properties"
export VM_ARGS="${VM_ARGS} -DerrorMessages.properties=../conf/errorMessages.properties"
export VM_ARGS="${VM_ARGS} -Dproperties_file=../conf/logback.properties"

export JAR_HOME=../lib/logchainer-*.jar

$JAVA_HOME/bin/java $VM_ARGS -jar $JAR_HOME &