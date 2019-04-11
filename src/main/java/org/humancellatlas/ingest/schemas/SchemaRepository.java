package org.humancellatlas.ingest.schemas;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
    List<Schema> findByUuidEquals(Uuid uuid);

    @RestResource
    Page<Schema> findBySchemaVersionAfter(@Param("schema-version-range") String schemaVersionRange, Pageable pageable);

    @RestResource(rel = "querySchemas")
    @Query("{$and :["
            + "?#{ [0] == null ? { $where : 'true'} : { 'highLevelEntity' : {'$regex' : [0]} } },"
            + "?#{ [1] == null ? { $where : 'true'} : { 'concreteEntity' : {'$regex' : [1]} } },"
            + "?#{ [2] == null ? { $where : 'true'} : { 'domainEntity' : {'$regex' : [2]} } },"
            + "?#{ [3] == null ? { $where : 'true'} : { 'subDomainEntity' : {'$regex' : [3]} } },"
            + "?#{ [4] == null ? { $where : 'true'} : { 'schemaVersion' : {'$regex' : [4]} } }"
            + "]}")
    Page<Schema> querySchemas(@Param("high-level-entity") String highLevelEntity,
                              @Param("concrete-entity") String concreteEntity,
                              @Param("domain-entity") String domainEntity,
                              @Param("sub-domain-entity") String subDomainEntity,
                              @Param("schema-version") String schemaVersion,
                              Pageable pageable);

    @RestResource(exported = false)
    <S extends Schema> Stream<S> findAllByOrderBySchemaVersionDesc();

    <S extends Schema> Page<S> findAllByOrderBySchemaVersionDesc(Pageable pageable);

}
