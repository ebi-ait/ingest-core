package org.humancellatlas.ingest.stagingjobs;

import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.humancellatlas.ingest.stagingjob.StagingJobRepository;
import org.humancellatlas.ingest.stagingjob.StagingJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DuplicateKeyException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class StagingJobServiceTest {

    private StagingJobRepository stagingJobRepository = mock(StagingJobRepository.class);
    private StagingJobService stagingJobService = new StagingJobService(stagingJobRepository);

    @BeforeEach
    public void setUp() {
        reset(stagingJobRepository);
    }

    @Test
    public void registerDuplicateJob() {
        // given:
        UUID testStagingAreaUuid = UUID.randomUUID();
        String testFileName = "test.fastq.gz";

        // and:
        doThrow(new DuplicateKeyException("duplicate key")).when(stagingJobRepository).save(any());

        // expect :
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> stagingJobService.registerNewJob(testStagingAreaUuid, testFileName));
    }

    @Test
    public void registerJob() {
        UUID testStagingAreaUuid_1 = UUID.randomUUID();
        String testFileName_1 = "test_1.fastq.gz";

        UUID testStagingAreaUuid_2 = UUID.randomUUID();
        String testFileName_2 = "test_2.fastq.gz";

        stagingJobService.registerNewJob(testStagingAreaUuid_1, testFileName_1);
        stagingJobService.registerNewJob(testStagingAreaUuid_1, testFileName_2);

        stagingJobService.registerNewJob(testStagingAreaUuid_2, testFileName_1);
        stagingJobService.registerNewJob(testStagingAreaUuid_2, testFileName_2);
    }
}
