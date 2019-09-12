package org.humancellatlas.ingest.stagingjob;

import org.humancellatlas.ingest.stagingjob.StagingJobService.JobAlreadyRegisteredException;
import org.humancellatlas.ingest.stagingjob.web.StagingJobCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DuplicateKeyException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class StagingJobServiceTest {

    private StagingJobRepository stagingJobRepository = mock(StagingJobRepository.class);
    private StagingJobService stagingJobService = new StagingJobService(stagingJobRepository);

    @BeforeEach
    public void setUp() {
        reset(stagingJobRepository);
    }

    @BeforeEach
    public void mockStagingJobRepositorySave() {
        when(stagingJobRepository.save(any(StagingJob.class)))
                .thenAnswer(new Answer<StagingJob>() {
                    private Set<StagingJob> savedJobs = new HashSet<>();
                    @Override
                    public StagingJob answer(InvocationOnMock invocation){
                        StagingJob stagingJob = invocation.getArgument(0);
                        if(savedJobs.contains(stagingJob)) {
                            throw new DuplicateKeyException("");
                        } else {
                            savedJobs.add(stagingJob);
                            return stagingJob;
                        }
                    }
                });
    }

    @Nested
    class Registration {

        @Test
        public void validJob() {
            UUID testStagingAreaUuid_1 = UUID.randomUUID();
            String testFileName_1 = "test_1.fastq.gz";

            UUID testStagingAreaUuid_2 = UUID.randomUUID();
            String testFileName_2 = "test_2.fastq.gz";

            stagingJobService.registerNewJob(new StagingJobCreateRequest(testStagingAreaUuid_1, testFileName_1, UUID.randomUUID()));
            stagingJobService.registerNewJob(new StagingJobCreateRequest(testStagingAreaUuid_1, testFileName_2, UUID.randomUUID()));

            stagingJobService.registerNewJob(new StagingJobCreateRequest(testStagingAreaUuid_2, testFileName_1, UUID.randomUUID()));
            stagingJobService.registerNewJob(new StagingJobCreateRequest(testStagingAreaUuid_2, testFileName_2, UUID.randomUUID()));
        }

        @Test
        public void duplicateJob() {
            StagingJobCreateRequest testStagingJobCreateRequest = new StagingJobCreateRequest(UUID.randomUUID(),"test.fastq.gz", UUID.randomUUID());
            stagingJobService.registerNewJob(testStagingJobCreateRequest);

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> stagingJobService.registerNewJob(testStagingJobCreateRequest));
        }
    }
}
