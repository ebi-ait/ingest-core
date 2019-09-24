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
## Privacy
Usage of the Ingest Core API require limited processing of personal data. For more information, please read our [Privacy Policy](http://www.ebi.ac.uk/data-protection/privacy-notice/human-cell-atlas-ingest-submission).
