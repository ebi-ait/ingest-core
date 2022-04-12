package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectTest {
    @Test
    public void testGetOpenSubmissionEnvelopes() {
        //given:
        SubmissionEnvelope openSubmissionEnvelope = new SubmissionEnvelope();
        openSubmissionEnvelope.enactStateTransition(SubmissionState.DRAFT);

        SubmissionEnvelope openSubmissionEnvelope2 = new SubmissionEnvelope();
        openSubmissionEnvelope2.enactStateTransition(SubmissionState.DRAFT);
        openSubmissionEnvelope2.enactStateTransition(SubmissionState.METADATA_VALID);
        openSubmissionEnvelope2.enactStateTransition(SubmissionState.SUBMITTED);


        Project project = new Project(null);
        project.addToSubmissionEnvelopes(openSubmissionEnvelope);
        project.addToSubmissionEnvelopes(openSubmissionEnvelope2);

        //when:
        List<SubmissionEnvelope> openSubmissionEnvelopes = project.getOpenSubmissionEnvelopes();

        //then:
        assertThat(openSubmissionEnvelopes).hasSize(1);
    }

    @Test
    public void testGetOpenSubmissionEnvelopesNone() {
        //given:
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

        Project project = new Project(null);
        project.addToSubmissionEnvelopes(submittedSubmission);
        project.addToSubmissionEnvelopes(completeSubmission);

        //when:
        List<SubmissionEnvelope> openSubmissionEnvelopes = project.getOpenSubmissionEnvelopes();

        //then:
        assertThat(openSubmissionEnvelopes).hasSize(0);
    }

    @Test
    public void testIsEditable() {
        Project project = new Project(null);
        assertThat(project.isEditable()).isTrue();

        SubmissionEnvelope submissionOne = new SubmissionEnvelope();
        submissionOne.enactStateTransition(SubmissionState.METADATA_VALID);
        SubmissionEnvelope submissionTwo = new SubmissionEnvelope();
        submissionTwo.enactStateTransition(SubmissionState.METADATA_INVALID);
        project.addToSubmissionEnvelopes(submissionOne);
        project.addToSubmissionEnvelopes(submissionTwo);

        assertThat(project.isEditable()).isTrue();

        submissionOne.enactStateTransition(SubmissionState.GRAPH_VALIDATION_REQUESTED);
        assertThat(project.isEditable()).isFalse();

        submissionOne.enactStateTransition(SubmissionState.GRAPH_VALID);
        assertThat(project.isEditable()).isTrue();

        submissionTwo.enactStateTransition(SubmissionState.GRAPH_VALIDATION_REQUESTED);
        assertThat(project.isEditable()).isFalse();
    }
}
