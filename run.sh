#!/usr/bin/env bash

##########################################################################
# Run a "Local" docker image on the workstation
#
#    1) builds the image
#    2) starts the server and shuts down as soon as job completes
#    3) cleanly shuts down on ^C

SERVICE_NAME="AWS Secrets Agent"
SERVICE_LOG="AwsSecretsAgent.log"

# Container and Image name
NAME="awssec"
IMAGE="${NAME}:latest"

##########################################################################
# Changes terminal window name/tab
set_title() {
    # tab
    echo -ne "\033]30;$SERVICE_NAME ($1)\007"
    # window
    echo -ne "\033]0;$SERVICE_NAME ($1)\007"
}

##########################################################################
# Kills everything we started
kill_it() {
    # attempt to gracefully exit
    docker exec ${NAME} /app/bin/stop.sh
#    echo -e "\nSleeping for 10 seconds for graceful shutdown...\n"
#    sleep 10
    docker rm -f ${NAME}
    set_title "Stopped"
}

##########################################################################
# Script starts here
##########################################################################
DIR=$(dirname $0)
[ "$DIR" = "." ] && DIR=$(pwd)

# this is used to get aws credentials when running in a local docker container
AWS_DIR=$(cd $HOME/.aws ; pwd)

# stop any running containers with this name
docker rm -f ${NAME}

# build the image locally
#mvn -U clean install || exit 1

# set up the logs directory in a way that it can be easily deleted
mkdir -p ${DIR}/target/logs

# trap INT so we can kill the background server task
trap 'kill_it' 2

echo
echo "========================================================================="
echo "Starting Docker image $NAME"
echo "========================================================================="
set_title "Running"

# this runs and exits as soon as the job is complete
docker run  --rm --name=${NAME} \
	--user=$(id -u):$(id -g) \
    -v ${DIR}/local/config:/app/config \
    -v ${DIR}/local/secrets:/app/secrets \
    -v ${DIR}/target/logs:/app/dumps \
    -v ${AWS_DIR}:/.aws \
    ${IMAGE}
