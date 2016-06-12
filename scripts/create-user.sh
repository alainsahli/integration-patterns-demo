#!/usr/bin/env bash
WAIT_TIME=$1

if [ -z ${WAIT_TIME} ]
  then
    curl -X POST -d @payloads/create-user.json http://localhost:8080/users --header "Content-Type:application/json"
  else
    curl -X POST -d @payloads/create-user.json http://localhost:8080/users?waitTime=${WAIT_TIME} --header "Content-Type:application/json"
fi
echo ""