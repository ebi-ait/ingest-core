package org.humancellatlas.ingest.core.service;


import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.state.ValidationState;
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

    @BeforeEach
    void setUp() {
        protocol = new Protocol(UUID.randomUUID().toString());
        protocol2 = new Protocol(UUID.randomUUID().toString());
        process = new Process(UUID.randomUUID().toString());
    }

    @Test
    public void testReplaceLink() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        process.addProtocol(protocol);

        // when
        service.replaceLinks(process, List.of(protocol2), "protocols");

        // then
        assertThat(process.getProtocols().size()).isEqualTo(1);
        assertThat(process.getProtocols().contains(protocol2)).isTrue();

        verify(validationStateChangeService).changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService).changeValidationState(protocol2.getType(), protocol2.getId(), ValidationState.DRAFT);
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
        verify(validationStateChangeService).changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService).changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
        verify(mongoTemplate).save(process);

    }

    @Test
    public void testRetry() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // given
        when(validationStateChangeService.changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT))
                .thenThrow(new OptimisticLockingFailureException("Error"))
                .thenReturn(protocol);

        when(validationStateChangeService.changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT))
                .thenThrow(new OptimisticLockingFailureException("Error"))
                .thenThrow(new OptimisticLockingFailureException("Error"))
                .thenReturn(process);


        // when
        service.addLinks(process, List.of(protocol), "protocols");

        // then
        assertThat(process.getProtocols().size()).isEqualTo(1);
        assertThat(process.getProtocols().contains(protocol)).isTrue();
        verify(validationStateChangeService,times(2)).changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
        verify(validationStateChangeService, times(3)).changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
        verify(mongoTemplate).save(process);
    }

}
