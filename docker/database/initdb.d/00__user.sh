#!/bin/sh
database_statsdb_password=$(cat "/run/secrets/database_statsdb_password")

psql -c "CREATE USER \"statsdb-dev\" WITH PASSWORD 'dev';"
psql -c "CREATE USER \"statsdb\"     WITH PASSWORD '$database_statsdb_password';"
