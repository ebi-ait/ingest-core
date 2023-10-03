# Managed Access

## Links
- [ticket 967](https://app.zenhub.com/workspaces/dcp-ingest-product-development-5f71ca62a3cb47326bdc1b5c/issues/gh/ebi-ait/dcp-ingest-central/967)

## Terms

* ACL - Access Control List
* DAC - Data Access Committee

```mermaid
sequenceDiagram

        participant client
        participant api
        participant authorization_service
        participant operation_service
        participant audit_service

        autonumber
    
        client ->> api: api operation
        note over authorization_service: new service, query <br>snapshot of DAC
        par new
           api ->> authorization_service: operation allowed for<br> user and document?
           authorization_service -->> api: true
           api ->> audit_service: record operation
        end 
        api ->> operation_service: perform operation
```

## ACL update
```mermaid
sequenceDiagram

    participant contributor
    participant hca_exec_office
    participant dac
    participant authorization_update_service

    par application for access
        contributor ->> hca_exec_office: apply for upload <br> permissions to dataset
        hca_exec_office ->> dac: submit access request
        dac ->> dac: assess & approve
    end 
    par get ACL data into ingest
        note right of dac: periodic update job<br>interface not clear yet
        hca_exec_office ->> authorization_update_service: update ACL for project
        authorization_update_service ->> ingest_db: update records
        note over ingest_db: update roles list, <br> ACL audit table
    end
```

## Representing ACLs in ingest

There are 2 options to consider. We will proceed with option 1.

1. Store a list of allowed datasets for each user, in addition to the
   wrangler or contributor roles

```mermaid
classDiagram
    direction LR
    class User {
        username
    }
    class Role {
        name
    }
    User  --> "1..*" Role : roles
    note for Role "Wrangler, Contrbutor, Dataset A, Dataset B"
```
2. Store a list of allowed users for each dataset.

```mermaid
classDiagram
    direction LR
    class Project {
        content
    }
    class User {
        name
        roles
    }
    Project  --> "1..*" User : allowed_users
```


