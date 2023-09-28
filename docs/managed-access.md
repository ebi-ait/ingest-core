# Managed Access

## Links
- [ticket 967](https://app.zenhub.com/workspaces/dcp-ingest-product-development-5f71ca62a3cb47326bdc1b5c/issues/gh/ebi-ait/dcp-ingest-central/967)

```mermaid
sequenceDiagram

        participant client
        participant api
        participant authorization_service
        participant operation_service

        autonumber
    
        client ->> api: api operation
        note over authorization_service: new service, query <br>snapshot of DAC
        api ->> authorization_service: operation allowed for<br> user and document?
        
        authorization_service -->> api: true
        
        api ->> operation_service: perform operation
```

## ACL update
```mermaid
sequenceDiagram

    participant contributor
    participant hca_data_portal
    participant dac
    participant authorization_update_service


    par application for access
        contributor ->> hca_data_portal: apply for access to dataset
        hca_data_portal ->> dac: submit access request
        dac ->> dac: assess & approve    
    end 
    par get ACL data into ingest
        note right of dac: periodic update job<br>interface not clear yet
        authorization_update_service ->> dac: snapshot access lists
        authorization_update_service ->> ingest_db: update records
    end
```

## Representing ACLs in ingest

Options to consider and choose one:
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


