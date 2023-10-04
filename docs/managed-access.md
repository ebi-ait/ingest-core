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
    participant DACO
    participant authorization_update_service

    
    

    par application for access
        contributor ->> DACO: apply for upload <br> permissions to                         dataset
        note left of DACO: name, email, project
        DACO ->> DACO: assess, compare <br>to signed docs, <br> approve
    end 
    par get ACL data into ingest
        note right of DACO: periodic updates <br>interface not clear yet,<br> initially email
         DACO ->> authorization_update_service: update ACL for project
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

```mermaid
graph RL

   A:::someclass --> B
   classDef someclass fill:#f96;
   linkStyle default fill:none,stroke-width:3px,stroke:red

```
