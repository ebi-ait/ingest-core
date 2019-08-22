package org.humancellatlas.ingest.stagingjobs;

import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.humancellatlas.ingest.stagingjob.StagingJobRepository;
import org.humancellatlas.ingest.stagingjob.StagingJobService;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

public class StagingJobServiceTest {

    StagingJobRepository stagingJobRepository = mock(StagingJobRepository.class);
    StagingJobService stagingJobService = new StagingJobService(stagingJobRepository);

    @Test
    public void testIllegalStateExceptionOnConflictingJobs() {
        UUID testStagingAreaUuid = UUID.randomUUID();
        String testFileName = "test.fastq.gz";

        when(stagingJobRepository.save(new StagingJob(testStagingAreaUuid, testFileName)))
                .thenAnswer(new Answer<StagingJob>() {
                    private int callCount = 0;
                    @Override
                    public StagingJob answer(InvocationOnMock invocation){
                        if(callCount < 1) {
                            callCount++;
                            return null;
                        } else {
                            throw new DuplicateKeyException("");
                        }
                    }
                });

        stagingJobService.registerNewJob(testStagingAreaUuid, testFileName);
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> stagingJobService.registerNewJob(testStagingAreaUuid, testFileName));
    }
}
