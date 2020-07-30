#!/bin/sh

#==============================================================================================
# Build the full class-path of all the JARs found
CP=""
for I in /app/agent/*.jar; do
    CP="$CP:$I"
done

#==============================================================================================
# Source environment variables for environment properties
. /app/config/env.properties

# set defaults for variables not defined
[ -z "$LOG_LEVEL" ] && LOG_LEVEL="WARN"
[ -z "$LOG_CONFIG" ] && LOG_CONFIG="/app/monitors/log4j2.xml"

#==============================================================================================
# System properties
# Logback configuration file and default level
SYS_OPTS="-Dlog4j.configurationFile=file:$LOG_CONFIG -DLogLevel=$LOG_LEVEL"

# Check on available log directories
if [ -d "/app/log" ] ; then   # Kubernetes style
    LOGDIR="/app/log"
else
    LOGDIR="/app/dumps"       # "default" for local deployments
fi
SYS_OPTS="$SYS_OPTS -Dlog.dir=$LOGDIR"
#echo "SYS_OPTS = $SYS_OPTS"

#==============================================================================================
# Run
echo "java ${SYS_OPTS} -cp ${CP} com.secretsagent.Main $@"
java ${SYS_OPTS} -cp ${CP} com.secretsagent.Main $@