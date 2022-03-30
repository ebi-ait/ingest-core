package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@DataMongoTest
public class ProcessRepositoryTest {

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProtocolRepository protocolRepository;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    @AfterEach
    private void tearDown() {
        processRepository.deleteAll();
        protocolRepository.deleteAll();
    }

    @Test
    public void findFirstByProtocolNonUnique() {
        //given:
        Protocol protocol = new Protocol(null);
        Process process1 = new Process(null);
        Process process2 = new Process(null);

        protocol.setUuid(Uuid.newUuid());
        process1.setUuid(Uuid.newUuid());
        process2.setUuid(Uuid.newUuid());

        process1.addProtocol(protocol);
        process2.addProtocol(protocol);

        protocol = protocolRepository.save(protocol);
        process1 = processRepository.save(process1);
        process2 = processRepository.save(process2);

        //and:
        assumeThat(processRepository.findAll()).hasSize(2);

        //when:
        Optional<Process> first = processRepository.findFirstByProtocolsContains(protocol);
        assertThat(first.isPresent()).isTrue();
        assertThat(first.get().getUuid()).isIn(asList(process1.getUuid(), process2.getUuid()));
    }
}
