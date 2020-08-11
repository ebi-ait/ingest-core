package org.humancellatlas.ingest.export.job;

import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestinationName;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface ExportJobRepository extends MongoRepository<ExportJob, String> {
    @RestResource(exported = false)
    <T extends ExportJob> T findBySubmissionAndStatusAndDestinationNameAndDestinationVersion(
        SubmissionEnvelope submissionEnvelope, ExportState exportState,
        ExportDestinationName destinationName, String destinationVersion);
}
