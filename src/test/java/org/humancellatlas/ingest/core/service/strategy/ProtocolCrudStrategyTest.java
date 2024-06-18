package org.humancellatlas.ingest.core.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.humancellatlas.ingest.core.service.strategy.impl.ProtocolCrudStrategy;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ProtocolCrudStrategy.class})
public class ProtocolCrudStrategyTest {
  @Autowired private ProtocolCrudStrategy protocolCrudStrategy;

  @MockBean private ProtocolRepository protocolRepository;
  @MockBean private ProcessRepository processRepository;
  @MockBean private MessageRouter messageRouter;

  private Protocol testProtocol;

  @BeforeEach
  void setUp() {
    testProtocol = new Protocol(null);
  }

  @Test
  public void testRemoveLinksProject() {
    // given
    Process processWithProtocol = new Process(null);
    processWithProtocol.getProtocols().add(testProtocol);
    when(processRepository.findByProtocolsContains(testProtocol))
        .thenReturn(Stream.of(processWithProtocol));

    // when
    protocolCrudStrategy.removeLinksToDocument(testProtocol);

    // then
    assertThat(processWithProtocol.getProtocols()).isEmpty();
    verify(processRepository).save(processWithProtocol);
  }

  @Test
  public void testDeleteProject() {
    // when
    protocolCrudStrategy.deleteDocument(testProtocol);
    // then
    verify(protocolRepository).delete(testProtocol);
  }
}
