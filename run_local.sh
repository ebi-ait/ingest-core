#!/bin/bash

docker-compose up -d mongo rabbitmq

export MONGO_URI=mongodb://localhost:27017/admin
export RABBIT_HOST=localhost
export RABBIT_PORT=5672
export SCHEMA_BASE_URI=https://schema.humancellatlas.org

export AUTH_ISSUER=https://login.elixir-czech.org/oidc
export SVC_AUTH_AUDIENCE=https://dev.data.humancellatlas.org/
export USR_AUTH_AUDIENCE=https://dev.data.humancellatlas.org/
export GCP_JWK_PROVIDER_BASE_URL=https://www.googleapis.com/service_accounts/v1/jwk/
export GCP_PROJECT_WHITELIST=hca-dcp-production.iam.gserviceaccount.com,human-cell-atlas-travis-test.iam.gserviceaccount.com,broad-dsde-mint-dev.iam.gserviceaccount.com,broad-dsde-mint-test.iam.gserviceaccount.com,broad-dsde-mint-staging.iam.gserviceaccount.com
export SCHEMA_BASE_URI=https://schema.humancellatlas.org/

./gradlew clean assemble

java \
    -XX:+UseG1GC \
    -jar build/libs/*.jar \
    --spring.data.mongodb.uri=$MONGO_URI \
    --spring.rabbitmq.host=$RABBIT_HOST \
    --spring.rabbitmq.port=$RABBIT_PORT \
    --schema.base-uri=$SCHEMA_BASE_URI

