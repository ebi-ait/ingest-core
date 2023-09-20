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

```mermaid
sequenceDiagram

        participant authorization_update_service
        participant dac
        
        note left of dac: periodic update job<br>interface not clear yet
        authorization_update_service ->> dac: snapshot access lists
        authorization_update_service ->> authorization_update_service: update records
```



