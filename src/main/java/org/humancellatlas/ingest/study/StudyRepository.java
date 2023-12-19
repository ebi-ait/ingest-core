package org.humancellatlas.ingest.study;

import org.humancellatlas.ingest.core.Uuid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Optional;

@CrossOrigin
public interface StudyRepository extends MongoRepository<Study, String> {

    @RestResource(rel = "findAllByUuid", path = "findAllByUuid")
    Page<Study> findByUuid(@Param("uuid") Uuid uuid, Pageable pageable);

    @RestResource(exported = false)
    Optional<Study> findByUuid(Uuid uuid);

    @RestResource(exported = false)
    Optional<Study> findById(String id);

}