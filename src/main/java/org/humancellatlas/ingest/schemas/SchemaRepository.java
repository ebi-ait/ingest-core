package org.humancellatlas.ingest.schemas;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.schemas.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by rolando on 18/04/2018.
 */
@CrossOrigin
public interface SchemaRepository extends MongoRepository<Schema, String>{
    @RestResource(exported = false)
    <S extends Schema> S save(S schema);

    @RestResource(exported = false)
    <S extends Schema> List<S> save(Iterable<S> schemas);

    @RestResource(exported = false)
    void delete(Schema schema);

    @RestResource(exported = false)
    void delete(String schemaId);

    @RestResource(exported = false)
    List<Schema> findByUuidEquals(Uuid uuid);

    @RestResource
    Page<Schema> findBySchemaVersionAfter(@Param("schema-version-range") String schemaVersionRange, Pageable pageable);

    @RestResource(exported = false)
    Page<Schema> findByHighLevelEntityLikeAndConcreteEntityLikeAndDomainEntityLikeAndSubDomainEntityLikeAndSchemaVersionLikeOrderBySchemaVersionDesc(@Param("high-level-entity") String highLevelEntity,
                                                                                                                             @Param("concrete-entity") String concreteEntity,
                                                                                                                             @Param("domain-entity") String domainEntity,
                                                                                                                             @Param("sub-domain-entity") String subDomainEntity,
                                                                                                                             @Param("schema-version") String schemaVersion,
                                                                                                                             Pageable pageable);

    @RestResource
    <S extends Schema> Stream<S> findAllByOrderBySchemaVersionDesc();
}
