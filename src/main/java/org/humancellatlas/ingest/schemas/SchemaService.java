package org.humancellatlas.ingest.schemas;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.humancellatlas.ingest.schemas.schemascraper.impl.SchemaScrapeException;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
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

    private static final int EVERY_24_HOURS = 1000 * 60 * 60 * 24;


    public List<Schema> filterLatestSchemas(String highLevelEntity) {
        return getLatestSchemas().stream()
                                 .filter(schema -> schema.getHighLevelEntity().matches(highLevelEntity))
                                 .collect(Collectors.toList());
    }

    public List<Schema> getLatestSchemas() {
        Set<LatestSchema> latestSchemas = new LinkedHashSet<>();

        schemaRepository.findAllByOrderBySchemaVersionDesc()
                        .forEach(schema -> latestSchemas.add(new LatestSchema(schema)));

        return latestSchemas.stream()
                            .map(LatestSchema::getSchema)
                            .collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = EVERY_24_HOURS)
    public void updateSchemasCollection() {
        String schemaBaseUri = environment.getProperty("SCHEMA_BASE_URI");

        if (schemaBaseUri.endsWith("/")) {
            schemaBaseUri = schemaBaseUri.substring(0, schemaBaseUri.length() - 1);
        }

        String schemaBucketUrl = schemaBaseUri + ".s3.amazonaws.com";

        schemaScraper.getAllSchemaURIs(URI.create(schemaBucketUrl)).stream()
                     .filter(schemaUri -> ! schemaUri.toString().contains("index.html"))
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

    /**
     *
     * A wrapper for Schema documents used to define a looser equals()/hashCode()
     * to determine equivalence of Schemas based only on a Schema's high level entity,
     * type, etc.
     *
     */
    private class LatestSchema {
        @Getter
        private final Schema schema;

        LatestSchema(Schema schema) {
            this.schema = schema;
        }

        @Override
        public boolean equals(Object to) {
            if (to == this) return true;
            if (!(to instanceof LatestSchema)) {
                return false;
            }

            LatestSchema schema = (LatestSchema) to;
            return schema.hashCode() == this.hashCode();
        }

        @Override
        public int hashCode(){
            int result = 17;
            result = 31 * result + this.schema.getConcreteEntity().hashCode();
            result = 31 * result + this.schema.getHighLevelEntity().hashCode();
            result = 31 * result + this.schema.getDomainEntity().hashCode();
            result = 31 * result + this.schema.getSubDomainEntity().hashCode();
            return result;
        }
    }
}
