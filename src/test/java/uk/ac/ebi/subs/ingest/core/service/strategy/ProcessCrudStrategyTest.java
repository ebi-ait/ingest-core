package uk.ac.ebi.subs.ingest.core.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.core.service.strategy.impl.ProcessCrudStrategy;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ProcessCrudStrategy.class})
public class ProcessCrudStrategyTest {
  @Autowired private ProcessCrudStrategy processCrudStrategy;

  @MockBean private BiomaterialRepository biomaterialRepository;
  @MockBean private ProcessRepository processRepository;
  @MockBean private FileRepository fileRepository;
  @MockBean private MessageRouter messageRouter;

  private Process testProcess;

  @BeforeEach
  void setUp() {
    testProcess = new Process(null);
  }

  @Test
  public void testRemoveLinksProcess() {
    // given
    File inputFile = spy(new File(null, "inputFile"));
    File derivedFile = spy(new File(null, "derivedFile"));
    inputFile.getInputToProcesses().add(testProcess);
    derivedFile.getDerivedByProcesses().add(testProcess);
    when(fileRepository.findByInputToProcessesContains(testProcess))
        .thenReturn(Stream.of(inputFile));
    when(fileRepository.findByDerivedByProcessesContains(testProcess))
        .thenReturn(Stream.of(derivedFile));

    Biomaterial inputBio = spy(new Biomaterial(null));
    Biomaterial derivedBio = spy(new Biomaterial(null));
    inputBio.getInputToProcesses().add(testProcess);
    derivedBio.getDerivedByProcesses().add(testProcess);
    when(biomaterialRepository.findByInputToProcessesContains(testProcess))
        .thenReturn(Stream.of(inputBio));
    when(biomaterialRepository.findByDerivedByProcessesContains(testProcess))
        .thenReturn(Stream.of(derivedBio));

    // when
    processCrudStrategy.removeLinksToDocument(testProcess);

    // then
    assertThat(inputFile.getInputToProcesses()).isEmpty();
    assertThat(derivedFile.getDerivedByProcesses()).isEmpty();
    assertThat(inputBio.getInputToProcesses()).isEmpty();
    assertThat(derivedBio.getDerivedByProcesses()).isEmpty();
    verify(fileRepository).save(inputFile);
    verify(fileRepository).save(derivedFile);
    verify(biomaterialRepository).save(inputBio);
    verify(biomaterialRepository).save(derivedBio);
  }

  @Test
  public void testDeleteProcess() {
    // when
    processCrudStrategy.deleteDocument(testProcess);
    // then
    verify(processRepository).delete(testProcess);
  }
}
