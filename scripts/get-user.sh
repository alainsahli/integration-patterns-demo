#!/usr/bin/env bash
USER_ID=${1:-x}
curl http://localhost:8080/users/${USER_ID}
echo ""