#!/bin/sh

echo run accessed

export JAVA_HOME=/d/melusine/M501/Java/jdk1.8.0_60-x64/bin/java

export VM_ARGS="-Dapplication.properties=D:/temp/application.properties"

export JAR_HOME=/d/_codesource_M501/logchainer-base/logchainer/target/logchainer-0.0.11-SNAPSHOT.jar

export CLASSPATH=/d/melusine/M501/Maven/Repository/junit/junit/3.8.1/junit-3.8.1.jar:/d/melusine/M501/Maven/Repository/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar:/d/melusine/M501/Maven/Repository/ch/qos/logback/logback-core/1.1.9/logback-core-1.1.9.jar:/d/melusine/M501/Maven/Repository/org/slf4j/slf4j-api/1.7.22/slf4j-api-1.7.22.jar:/d/melusine/M501/Maven/Repository/org/springframework/boot/spring-boot-starter/1.5.1.RELEASE/spring-boot-starter-1.5.1.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/springframework/boot/spring-boot/1.5.1.RELEASE/spring-boot-1.5.1.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/springframework/spring-context/4.3.6.RELEASE/spring-context-4.3.6.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/springframework/spring-aop/4.3.6.RELEASE/spring-aop-4.3.6.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/springframework/spring-beans/4.3.6.RELEASE/spring-beans-4.3.6.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/springframework/spring-expression/4.3.6.RELEASE/spring-expression-4.3.6.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/springframework/boot/spring-boot-autoconfigure/1.5.1.RELEASE/spring-boot-autoconfigure-1.5.1.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/springframework/boot/spring-boot-starter-logging/1.5.1.RELEASE/spring-boot-starter-logging-1.5.1.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/slf4j/jcl-over-slf4j/1.7.22/jcl-over-slf4j-1.7.22.jar:/d/melusine/M501/Maven/Repository/org/slf4j/jul-to-slf4j/1.7.22/jul-to-slf4j-1.7.22.jar:/d/melusine/M501/Maven/Repository/org/slf4j/log4j-over-slf4j/1.7.22/log4j-over-slf4j-1.7.22.jar:/d/melusine/M501/Maven/Repository/org/springframework/spring-core/4.3.6.RELEASE/spring-core-4.3.6.RELEASE.jar:/d/melusine/M501/Maven/Repository/org/yaml/snakeyaml/1.17/snakeyaml-1.17.jar:/d/melusine/M501/Maven/Repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:/d/melusine/M501/Maven/Repository/org/testng/testng/6.7/testng-6.7.jar:/d/melusine/M501/Maven/Repository/org/beanshell/bsh/2.0b4/bsh-2.0b4.jar:/d/melusine/M501/Maven/Repository/com/beust/jcommander/1.12/jcommander-1.12.jar:/d/melusine/M501/Maven/Repository/commons-io/commons-io/1.3.2/commons-io-1.3.2.jar

echo "classpath ="$CLASSPATH

# $JAVA_HOME $VM_ARGS -jar $JAR_HOME -cp $CLASSPATH #ch.ge.cti.logchainer.LogChainer &
$JAVA_HOME $VM_ARGS -jar $JAR_HOME -cp $CLASSPATH #ch.ge.cti.logchainer.LogChainer