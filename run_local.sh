#!/bin/bash

docker-compose up -d mongo rabbitmq

export MONGO_URI=mongodb://localhost:27017/admin
export RABBIT_HOST=localhost
export RABBIT_PORT=5672
export SCHEMA_BASE_URI=https://schema.humancellatlas.org

./gradlew clean assemble

java \
    -XX:+UseG1GC \
    -jar build/libs/*.jar \
    --spring.data.mongodb.uri=$MONGO_URI \
    --spring.rabbitmq.host=$RABBIT_HOST \
    --spring.rabbitmq.port=$RABBIT_PORT \
    --schema.base-uri=$SCHEMA_BASE_URI