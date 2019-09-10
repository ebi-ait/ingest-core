package org.humancellatlas.ingest.stagingjob;

import org.humancellatlas.ingest.stagingjob.StagingJobService.JobAlreadyRegisteredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

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

    @Nested
    class Registration {

        @Test
        public void validJob() {
            // given:
            UUID stagingAreaUUid = UUID.randomUUID();
            String fileName = "test_1.fastq.gz";
            String metadataUuid = UUID.randomUUID().toString();
            StagingJob stagingJob = new StagingJob(stagingAreaUUid, metadataUuid, fileName);

            // and:
            StagingJob persistentJob = spy(stagingJob);
            doReturn("_generated_id_1").when(persistentJob).getId();
            doReturn(persistentJob).when(stagingJobRepository).save(any());

            //when:
            StagingJob resultingJob = stagingJobService.register(stagingJob);

            //then:
            verify(stagingJobRepository).save(stagingJob);
            assertThat(resultingJob).isEqualTo(persistentJob);
        }

        @Test
        public void duplicateJob() {
            // given:
            UUID stagingAreaUuid = UUID.randomUUID();
            String metadataUuid = UUID.randomUUID().toString();
            String fileName = "test.fastq.gz";
            StagingJob stagingJob = new StagingJob(stagingAreaUuid, metadataUuid, fileName);

            // and:
            doThrow(new DuplicateKeyException("duplicate key")).when(stagingJobRepository).save(any());

            // expect :
            assertThatExceptionOfType(JobAlreadyRegisteredException.class)
                    .isThrownBy(() -> stagingJobService.register(stagingJob));
        }
    }
}
