#!/bin/bash

file_env() {
  local var="$1"
  local fileVar="${var}_FILE"
  local def="${2:-}"
  if [ "${!var:-}" ] && [ "${!fileVar:-}" ]; then
  	echo >&2 "error: both $var and $fileVar are set (but are exclusive)"
  	exit 1
  fi
  local val="$def"
  if [ "${!var:-}" ]; then
  	val="${!var}"
  elif [ "${!fileVar:-}" ]; then
  	val="$(< "${!fileVar}")"
  fi
  export "$var"="$val"
  unset "$fileVar"
}

file_env 'NEO4J_AUTH'
IFS='/' read -r -a username_and_password <<< "$NEO4J_AUTH"

ORG_NEO4J_DRIVER_AUTHENTICATION_USERNAME="${username_and_password[0]}" ORG_NEO4J_DRIVER_AUTHENTICATION_PASSWORD="${username_and_password[1]}" exec java -jar app.jar
