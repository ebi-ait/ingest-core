package org.humancellatlas.ingest.study;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;

public class StudyTest {
  @Test
  public void testGetOpenSubmissionEnvelopes() {
    // given:
    SubmissionEnvelope openSubmissionEnvelope = new SubmissionEnvelope();
    openSubmissionEnvelope.enactStateTransition(SubmissionState.DRAFT);

    SubmissionEnvelope openSubmissionEnvelope2 = new SubmissionEnvelope();
    openSubmissionEnvelope2.enactStateTransition(SubmissionState.DRAFT);
    openSubmissionEnvelope2.enactStateTransition(SubmissionState.METADATA_VALID);
    openSubmissionEnvelope2.enactStateTransition(SubmissionState.SUBMITTED);

    Study study = new Study("Schema URL", "1.1", "Specific", "{\"name\": \"Updated Study Name\"}");
    study.addToSubmissionEnvelopes(openSubmissionEnvelope);
    study.addToSubmissionEnvelopes(openSubmissionEnvelope2);

    // when:
    List<SubmissionEnvelope> openSubmissionEnvelopes = study.getOpenSubmissionEnvelopes();

    // then:
    assertThat(openSubmissionEnvelopes).hasSize(1);
  }

  @Test
  public void testGetOpenSubmissionEnvelopesNone() {
    // given:
    SubmissionEnvelope completeSubmission = new SubmissionEnvelope();
    completeSubmission.enactStateTransition(SubmissionState.DRAFT);
    completeSubmission.enactStateTransition(SubmissionState.METADATA_VALID);
    completeSubmission.enactStateTransition(SubmissionState.SUBMITTED);
    completeSubmission.enactStateTransition(SubmissionState.PROCESSING);
    completeSubmission.enactStateTransition(SubmissionState.COMPLETE);

    SubmissionEnvelope submittedSubmission = new SubmissionEnvelope();
    submittedSubmission.enactStateTransition(SubmissionState.DRAFT);
    submittedSubmission.enactStateTransition(SubmissionState.METADATA_VALID);
    submittedSubmission.enactStateTransition(SubmissionState.SUBMITTED);

    Study study = new Study("Schema URL", "1.1", "Specific", "{\"name\": \"Updated Study Name\"}");
    study.addToSubmissionEnvelopes(submittedSubmission);
    study.addToSubmissionEnvelopes(completeSubmission);

    // when:
    List<SubmissionEnvelope> openSubmissionEnvelopes = study.getOpenSubmissionEnvelopes();

    // then:
    assertThat(openSubmissionEnvelopes).hasSize(0);
  }

  @Test
  public void testIsEditable() {
    Study study = new Study("Schema URL", "1.1", "Specific", "{\"name\": \"Updated Study Name\"}");
    assertThat(study.isEditable()).isTrue();

    SubmissionEnvelope submissionOne = new SubmissionEnvelope();
    submissionOne.enactStateTransition(SubmissionState.METADATA_VALID);
    SubmissionEnvelope submissionTwo = new SubmissionEnvelope();
    submissionTwo.enactStateTransition(SubmissionState.METADATA_INVALID);
    study.addToSubmissionEnvelopes(submissionOne);
    study.addToSubmissionEnvelopes(submissionTwo);

    assertThat(study.isEditable()).isTrue();

    submissionOne.enactStateTransition(SubmissionState.GRAPH_VALID);
    assertThat(study.isEditable()).isTrue();

    submissionTwo.enactStateTransition(SubmissionState.GRAPH_VALID);
    assertThat(study.isEditable()).isFalse();
  }
}
