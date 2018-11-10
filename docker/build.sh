#!/bin/bash -eu

(cd ../knowledge && ./mvnw clean verify)
cp ../knowledge/target/knowledge-0.0.1-SNAPSHOT.jar knowledge/knowledge.jar

docker-compose build
