#!/bin/bash -eu

cp /tmp/apoc.jar /plugins/

echo "dbms.security.procedures.unrestricted=apoc.*,algo.*" >> conf/neo4j.conf
