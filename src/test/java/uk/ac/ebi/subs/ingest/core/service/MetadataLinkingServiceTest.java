package uk.ac.ebi.subs.ingest.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
import uk.ac.ebi.subs.ingest.state.SubmissionState;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MetadataLinkingService.class})
public class MetadataLinkingServiceTest {
  @Autowired private MetadataLinkingService service;

  @MockBean private ValidationStateChangeService validationStateChangeService;

  @MockBean private MongoTemplate mongoTemplate;

  Protocol protocol;
  Protocol protocol2;
  Process process;
  SubmissionEnvelope submission;

  @BeforeEach
  void setUp() {
    submission = new SubmissionEnvelope();
    submission.enactStateTransition(SubmissionState.GRAPH_VALID);
    protocol = spy(new Protocol(null));
    doReturn("protocol1").when(protocol).getId();
    protocol2 = spy(new Protocol(null));
    doReturn("protocol2").when(protocol2).getId();
    process = spy(new Process(null));
    doReturn("process").when(process).getId();
    process.setSubmissionEnvelope(submission);
  }

  @Test
  public void testReplaceLink()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    process.addProtocol(protocol);

    // when
    service.replaceLinks(process, List.of(protocol2), "protocols");

    // then
    assertThat(process.getProtocols().size()).isEqualTo(1);
    assertThat(process.getProtocols().contains(protocol2)).isTrue();

    verify(validationStateChangeService, times(0))
        .changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
    verify(validationStateChangeService, times(0))
        .changeValidationState(protocol2.getType(), protocol2.getId(), ValidationState.DRAFT);
    verify(validationStateChangeService)
        .changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
    verify(mongoTemplate).save(process);
  }

  @Test
  public void testReplaceLinkNotGraphValid()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    process.addProtocol(protocol);
    submission.enactStateTransition(SubmissionState.METADATA_VALID);

    // when
    service.replaceLinks(process, List.of(protocol2), "protocols");

    // then
    assertThat(process.getProtocols().size()).isEqualTo(1);
    assertThat(process.getProtocols().contains(protocol2)).isTrue();

    verify(validationStateChangeService, times(0))
        .changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
    verify(validationStateChangeService, times(0))
        .changeValidationState(protocol2.getType(), protocol2.getId(), ValidationState.DRAFT);
    verify(validationStateChangeService, times(0))
        .changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
    verify(mongoTemplate).save(process);
  }

  @Test
  public void testAddLink()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

    // when
    service.addLinks(process, List.of(protocol), "protocols");

    // then
    assertThat(process.getProtocols().size()).isEqualTo(1);
    assertThat(process.getProtocols().contains(protocol)).isTrue();
    verify(validationStateChangeService, times(0))
        .changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
    verify(validationStateChangeService)
        .changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
    verify(mongoTemplate).save(process);
  }

  @Test
  public void testAddLinkNotGraphValid()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    submission.enactStateTransition(SubmissionState.METADATA_VALID);

    // when
    service.addLinks(process, List.of(protocol), "protocols");

    // then
    assertThat(process.getProtocols().size()).isEqualTo(1);
    assertThat(process.getProtocols().contains(protocol)).isTrue();
    verify(validationStateChangeService, times(0))
        .changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
    verify(validationStateChangeService, times(0))
        .changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
    verify(mongoTemplate).save(process);
  }

  @Test
  public void testRetry()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    // given

    when(validationStateChangeService.changeValidationState(
            process.getType(), process.getId(), ValidationState.DRAFT))
        .thenThrow(new OptimisticLockingFailureException("Error"))
        .thenThrow(new OptimisticLockingFailureException("Error"))
        .thenReturn(process);

    // when
    service.addLinks(process, List.of(protocol), "protocols");

    // then
    assertThat(process.getProtocols().size()).isEqualTo(1);
    assertThat(process.getProtocols().contains(protocol)).isTrue();
    verify(validationStateChangeService, times(0))
        .changeValidationState(protocol.getType(), protocol.getId(), ValidationState.DRAFT);
    verify(validationStateChangeService, times(3))
        .changeValidationState(process.getType(), process.getId(), ValidationState.DRAFT);
    verify(mongoTemplate).save(process);
  }
}
