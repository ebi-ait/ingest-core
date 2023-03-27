[![Build Status](https://travis-ci.org/HumanCellAtlas/ingest-core.svg?branch=master)](https://travis-ci.org/HumanCellAtlas/ingest-core)
[![Maintainability](https://api.codeclimate.com/v1/badges/024864c09e56bd43a7e9/maintainability)](https://codeclimate.com/github/HumanCellAtlas/ingest-core/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/024864c09e56bd43a7e9/test_coverage)](https://codeclimate.com/github/HumanCellAtlas/ingest-core/test_coverage)
[![Docker Repository on Quay](https://quay.io/repository/humancellatlas/ingest-core/status "Docker Repository on Quay")](https://quay.io/repository/humancellatlas/ingest-core)

# Ingest Core API
Core Ingest Service for submitting experimental data to the DCP.

The API exposes a number of REST resources for representing experiment data:
* `/projects`
* `/biomaterials`
* `/protocols`
* `/processes`
* `/files`

See [this document](https://github.com/HumanCellAtlas/metadata-schema/blob/master/docs/structure.md) for an overview of how experimental data is structured together.


An example of the process of making a submission is in [docs/primary-submission-walkthrough.md](docs/primary-submission-walkthrough.md).

## Tests
`./gradlew test`

You will need a mongo database running on localhost with default ports to run the tests

## Running locally
You can run ingest-core on your local system by using the `local` Spring profile. You would need to have a local mongodb 
and rabbitmq running, which you can run using docker-compose.

```shell
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun

```

### populating the local DB

You could populate the local DB with prod data using the following script:

```bash
mkdir -p ~/dev/ait/data/mongodb/
cd ~/dev/ait/data/mongodb/
latest_backup=$(aws s3 ls s3://ingest-db-backup/prod/ | awk '{print $4}' | sort | tail -n 1)
aws s3 cp "s3://ingest-db-backup/prod/${latest_backup}" ${latest_backup}
tar -xzvf $latest_backup
backup_dir=$(echo "$latest_backup" | sed "s/\.tar\.gz//g")
mongorestore --drop "./data/db/dump/${backup_dir}"
```

`./run_local.sh`

### Populating the local docker db

```bash
mkdir -p ~/dev/ait/data/mongodb/
cd ~/dev/ait/data/mongodb/
latest_backup=$(aws s3 ls s3://ingest-db-backup/prod/ | awk '{print $4}' | sort | tail -n 1)
aws s3 cp "s3://ingest-db-backup/prod/${latest_backup}" ${latest_backup}
mongo_container_name=data-cube-mongodb-1
docker cp $latest_backup $mongo_container_name:/$latest_backup
docker exec -i $mongo_container_name tar -xzvf $latest_backup
backup_dir=$(echo "$latest_backup" | sed "s/\.tar\.gz//g")
docker exec -i $mongo_container_name /usr/bin/mongorestore /data/db/dump/$backup_dir --drop
```

## Privacy
Usage of the Ingest Core API require limited processing of personal data. For more information, please read our [Privacy Policy](http://www.ebi.ac.uk/data-protection/privacy-notice/human-cell-atlas-ingest-submission).
