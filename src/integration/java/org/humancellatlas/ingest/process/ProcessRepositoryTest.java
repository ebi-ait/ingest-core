package org.humancellatlas.ingest.process;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@SpringBootTest
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
        Protocol protocol = new Protocol("protocol");
        protocol = protocolRepository.save(protocol);

        //and:
        Process process1 = new Process("process 1");
        process1.addProtocol(protocol);
        process1 = processRepository.save(process1);

        //and:
        Process process2 = new Process("process 2");
        process2.addProtocol(protocol);
        process2 = processRepository.save(process2);

        //and:
        assumeThat(processRepository.findAll()).hasSize(2);

        //when:
        Optional<Process> first = processRepository.findFirstByProtocolsContains(protocol);
        assertThat(first.isPresent()).isTrue();
        assertThat(first.get().getId()).isIn(asList(process1.getId(), process2.getId()));
    }

}
