package org.humancellatlas.ingest.schemas;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

/**
 * Created by rolando on 18/04/2018.
 */
@CrossOrigin
public interface SchemaRepository extends MongoRepository<Schema, String> {
    @RestResource(exported = false)
    <S extends Schema> S save(S schema);

    @RestResource(exported = false)
    <S extends Schema> List<S> save(Iterable<S> schemas);

    @RestResource(exported = false)
    void delete(Schema schema);

    @RestResource(exported = false)
    void delete(String schemaId);
}
