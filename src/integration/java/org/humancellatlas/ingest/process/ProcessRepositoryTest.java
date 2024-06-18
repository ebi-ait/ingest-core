package org.humancellatlas.ingest.process;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.util.Optional;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@DataMongoTest
public class ProcessRepositoryTest {

  @Autowired private ProcessRepository processRepository;

  @Autowired private ProtocolRepository protocolRepository;

  @MockBean private MigrationConfiguration migrationConfiguration;

  @MockBean private MessageRouter messageRouter;

  @AfterEach
  private void tearDown() {
    processRepository.deleteAll();
    protocolRepository.deleteAll();
  }

  @Test
  public void findFirstByProtocolNonUnique() {
    // given:
    Protocol protocol = protocolRepository.save(new Protocol(null));

    Process process1 = new Process(null);
    process1.addProtocol(protocol);
    process1 = processRepository.save(process1);

    Process process2 = new Process(null);
    process2.addProtocol(protocol);
    process2 = processRepository.save(process2);

    // and:
    assumeThat(processRepository.findAll()).hasSize(2);

    // when:
    Optional<Process> first = processRepository.findFirstByProtocolsContains(protocol);

    // then
    assertThat(first.isPresent()).isTrue();
    assertThat(first.get().getId()).isIn(asList(process1.getId(), process2.getId()));
  }
}
