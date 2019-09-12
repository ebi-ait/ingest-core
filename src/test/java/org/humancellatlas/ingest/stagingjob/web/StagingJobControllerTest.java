package org.humancellatlas.ingest.stagingjob.web;

import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.humancellatlas.ingest.stagingjob.StagingJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StagingJobControllerTest {

    private StagingJobService stagingJobService;

    private StagingJobController controller;

    @BeforeEach
    public void setUp(@Mock StagingJobService stagingJobService) {
        this.stagingJobService = stagingJobService;
        controller = new StagingJobController(stagingJobService);
    }

    @Test
    public void createStagingJob() {
        //given:
        UUID testStagingAreaUuid = UUID.randomUUID();
        UUID mockMetadataUuid = UUID.randomUUID();
        String testFileName = "test.fastq.gz";

        StagingJobCreateRequest stagingJob = new StagingJobCreateRequest(testStagingAreaUuid, testFileName, mockMetadataUuid);
        StagingJob persistentJob = new StagingJob(testStagingAreaUuid, testFileName, mockMetadataUuid.toString());
        given(stagingJobService.registerNewJob(any(StagingJobCreateRequest.class))).willReturn(persistentJob);

        //and:
        PersistentEntityResourceAssembler resourceAssembler = mock(PersistentEntityResourceAssembler.class);
        given(resourceAssembler.toFullResource(any())).willAnswer(invocation -> {
            Object entity = invocation.getArgument(0);
            return PersistentEntityResource.build(entity, mock(PersistentEntity.class)).build();
        });

        //when:
        ResponseEntity<?> response = controller.createStagingJob(stagingJob, resourceAssembler);

        //then:
        verify(stagingJobService).registerNewJob(any(StagingJobCreateRequest.class));

        //and:
        assertThat(response).isNotNull()
                .extracting("status").containsExactly(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(PersistentEntityResource.class);
        PersistentEntityResource responseBody = (PersistentEntityResource) response.getBody();
        assertThat(responseBody.getContent()).isEqualTo(persistentJob);
    }

}
