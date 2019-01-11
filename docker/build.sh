#!/bin/bash -eu

(cd ../etl && ./mvnw clean verify)
cp ../etl/target/etl-0.0.1-SNAPSHOT.jar var/knowledgedb/plugins/

(cd ../knowledge && ./mvnw clean verify)
cp ../knowledge/target/knowledge-0.0.1-SNAPSHOT.jar knowledge/knowledge.jar

docker-compose build
