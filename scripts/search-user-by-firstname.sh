#!/usr/bin/env bash
FIRST_NAME=${1:-foo}
curl http://localhost:8080/users/search-by-firstname?firstName=${FIRST_NAME}
echo ""