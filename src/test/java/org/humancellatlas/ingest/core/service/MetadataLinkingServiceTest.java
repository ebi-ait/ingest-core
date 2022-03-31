package org.humancellatlas.ingest.core.service;


import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MetadataLinkingService.class})
public class MetadataLinkingServiceTest {
    @Autowired
    private MetadataLinkingService service;

    @MockBean
    private ValidationStateChangeService validationStateChangeService;

    @MockBean
    private MongoTemplate mongoTemplate;

    Protocol protocol;
    Protocol protocol2;
    Process process;
    SubmissionEnvelope submission;

    @BeforeEach
    void setUp() {
        submission = new SubmissionEnvelope(UUID.randomUUID().toString());
        submission.enactStateTransition(SubmissionState.GRAPH_VALID);
        protocol = new Protocol(UUID.randomUUID().toString());
        protocol2 = new Protocol(UUID.randomUUID().toString());
        process = new Process(UUID.randomUUID().toString());
        process.setSubmissionEnvelope(submission);
    }

    @Test
    public void testReplaceLink() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        process.addProtocol(protocol);

        // when
        service.replaceLinks(process, List.of(protocol2), "protocols");

        // then
        assertThat(process.getProtocols().size()).isEqualTo(1);
        assertThat(process.getProtocols().contains(protocol2)).isTrue();

        verify(validationStateChangeService, times(0)).changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService, times(0)).changeValidationState(protocol2.getType(), protocol2.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService).changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
        verify(mongoTemplate).save(process);

    }

    @Test
    public void testAddLink() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        // when
        service.addLinks(process, List.of(protocol), "protocols");

        // then
        assertThat(process.getProtocols().size()).isEqualTo(1);
        assertThat(process.getProtocols().contains(protocol)).isTrue();
        verify(validationStateChangeService, times(0)).changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService).changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
        verify(mongoTemplate).save(process);

    }

    @Test
    public void testAddLinkNotGraphValid() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        // when
        submission.enactStateTransition(SubmissionState.METADATA_VALID);
        service.addLinks(process, List.of(protocol), "protocols");

        // then
        assertThat(process.getProtocols().size()).isEqualTo(1);
        assertThat(process.getProtocols().contains(protocol)).isTrue();
        verify(validationStateChangeService, times(0)).changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService, times(0)).changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
        verify(mongoTemplate).save(process);

    }

    @Test
    public void testRetry() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // given

        when(validationStateChangeService.changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT))
                .thenThrow(new OptimisticLockingFailureException("Error"))
                .thenThrow(new OptimisticLockingFailureException("Error"))
                .thenReturn(process);


        // when
        service.addLinks(process, List.of(protocol), "protocols");

        // then
        assertThat(process.getProtocols().size()).isEqualTo(1);
        assertThat(process.getProtocols().contains(protocol)).isTrue();
        verify(validationStateChangeService,times(0)).changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService, times(3)).changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
        verify(mongoTemplate).save(process);
    }

}
