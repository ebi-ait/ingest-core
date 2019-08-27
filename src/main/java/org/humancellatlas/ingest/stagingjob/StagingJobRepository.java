package org.humancellatlas.ingest.stagingjob;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.UUID;

@CrossOrigin
public interface StagingJobRepository extends MongoRepository<StagingJob, String> {
    @RestResource(exported = false)
    <T extends StagingJob> T save(T stagingJob);

    @RestResource
    void delete(StagingJob stagingJob);

    @RestResource(exported = false)
    void deleteAllByStagingAreaUuid(UUID stagingAreaUuid);

    @RestResource(rel = "findByStagingAreaAndFileName")
    <T extends StagingJob> T findByStagingAreaUuidAndStagingAreaFileName(@Param("stagingAreaUuid") UUID stagingAreaUuid,
                                                                         @Param("stagingAreaFileName") String stagingAreaFileName);
}
