package org.humancellatlas.ingest.schemas;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.humancellatlas.ingest.schemas.schemascraper.impl.SchemaScrapeException;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by rolando on 19/04/2018.
 */
@Service
@RequiredArgsConstructor
@Getter
public class SchemaService {
    private final @NonNull SchemaRepository schemaRepository;
    private final @NonNull SchemaScraper schemaScraper;
    private final @NonNull Environment environment;

    public void updateSchemasCollection() {
        schemaScraper.getAllSchemaURIs(URI.create(environment.getProperty("SCHEMA_BASE_URI")))
                     .forEach(schemaUri -> {
                         Schema schemaDocument = schemaDescriptionFromSchemaUri(schemaUri);

                         // generate a uuid from the schema namespace
                         UUID schemaUuid = UUID.nameUUIDFromBytes(schemaUri.toString().getBytes());
                         schemaDocument.setUuid(new Uuid(schemaUuid.toString()));

                         // delete/update matching schemas
                         Collection<Schema> matchingSchemas = schemaRepository.findByUuidEquals(new Uuid(schemaUuid.toString()));
                         schemaRepository.delete(matchingSchemas);

                         schemaRepository.save(schemaDocument);
                     });
    }

    public Collection<Schema> schemaDescriptionFromSchemaUris(Collection<URI> schemaUris) {
        return schemaUris.stream()
                         .map(this::schemaDescriptionFromSchemaUri)
                         .collect(Collectors.toList());
    }

    private Schema schemaDescriptionFromSchemaUri(URI schemaUri) {
        String[] splitString = schemaUri.toString().split("/");
        String schemaFullUri = environment.getProperty("SCHEMA_BASE_URI") + schemaUri;

        if(splitString.length == 3) { // then this is a bundle schema
            return new Schema(splitString[0], splitString[1], "", "", splitString[2], schemaFullUri);
        } else if(splitString.length == 4) {
            return new Schema(splitString[0], splitString[2], splitString[1], "", splitString[3], schemaFullUri);
        } else if(splitString.length == 5) {
            return new Schema(splitString[0], splitString[3], splitString[1], splitString[2], splitString[4], schemaFullUri);
        } else {
            throw new SchemaScrapeException("Couldn't construct a Schema document from URI: " + schemaFullUri);
        }
    }
}
