package org.humancellatlas.ingest.stagingjobs;

import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.humancellatlas.ingest.stagingjob.StagingJobRepository;
import org.humancellatlas.ingest.stagingjob.StagingJobService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


@RunWith(SpringRunner.class)
@SpringBootTest
public class StagingJobRepositoryTest {

    @Autowired
    StagingJobRepository stagingJobRepository;

    @Test
    public void testJpaExceptionWhenInsertingMultipleCompoundKey() {
        UUID testStagingAreaUuid = UUID.randomUUID();
        String testFileName = "test.fastq.gz";

        stagingJobRepository.save(new StagingJob(testStagingAreaUuid, testFileName));

        assertThatExceptionOfType(DuplicateKeyException.class).isThrownBy(() -> {
            stagingJobRepository.save(new StagingJob(testStagingAreaUuid, testFileName));
        });
    }

    @Test
    public void testRegisteringJobsWithDifferentCompoundKey() {
        UUID testStagingAreaUuid_1 = UUID.randomUUID();
        String testFileName_1 = "test_1.fastq.gz";

        UUID testStagingAreaUuid_2 = UUID.randomUUID();
        String testFileName_2 = "test_2.fastq.gz";

        stagingJobRepository.save(new StagingJob(testStagingAreaUuid_1, testFileName_1));
        stagingJobRepository.save(new StagingJob(testStagingAreaUuid_1, testFileName_2));

        stagingJobRepository.save(new StagingJob(testStagingAreaUuid_2, testFileName_1));
        stagingJobRepository.save(new StagingJob(testStagingAreaUuid_2, testFileName_2));
    }
}
