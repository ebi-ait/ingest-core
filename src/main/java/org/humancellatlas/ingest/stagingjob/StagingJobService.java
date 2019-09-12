package org.humancellatlas.ingest.stagingjob;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.stagingjob.web.StagingJobCreateRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StagingJobService {

    @NonNull
    private final StagingJobRepository stagingJobRepository;

    public StagingJob registerNewJob(StagingJobCreateRequest stagingJobCreateRequest) {
        UUID stagingAreaUuid = stagingJobCreateRequest.getStagingAreaUuid();
        UUID metadataUuid = stagingJobCreateRequest.getMetadataUuid();
        String stagingAreaFileName = stagingJobCreateRequest.getStagingAreaFileName();

        try {
            StagingJob stagingJob = new StagingJob(stagingAreaUuid, stagingAreaFileName, metadataUuid.toString());
            return stagingJobRepository.save(stagingJob);
        } catch (DuplicateKeyException e) {
            throw new JobAlreadyRegisteredException(stagingAreaUuid, stagingAreaFileName);
        }
    }

    public StagingJob completeJob(StagingJob stagingJob, String stagingAreaUri) {
        stagingJob.setStagingAreaFileUri(stagingAreaUri);
        return stagingJobRepository.save(stagingJob);
    }

    public void deleteJobsForStagingArea(UUID stagingAreaUuid) {
        stagingJobRepository.deleteAllByStagingAreaUuid(stagingAreaUuid);
    }

    public static class JobAlreadyRegisteredException extends IllegalStateException {

        public JobAlreadyRegisteredException(UUID stagingAreaUuid, String fileName) {
            super(String.format("Staging job request already exists for file %s at upload area %s",
                    fileName, stagingAreaUuid));
        }

    }
}
