package org.humancellatlas.ingest.submission;


import org.humancellatlas.ingest.state.SubmissionState;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SubmissionEnvelopeTest {

    @Test
    public void testAllowedSubmissionStateTransitions() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.PENDING);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForDraft() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.DRAFT);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.METADATA_VALIDATING));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForMetadataValidating() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_VALIDATING);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.METADATA_INVALID, SubmissionState.METADATA_VALID));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForMetadataValid() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_VALID);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.GRAPH_VALIDATION_REQUESTED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForMetadataInvalid() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_INVALID);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.METADATA_VALIDATING, SubmissionState.GRAPH_VALIDATION_REQUESTED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphValidationRequested() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALIDATION_REQUESTED);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.GRAPH_VALIDATING, SubmissionState.DRAFT));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphValidating() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALIDATING);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.GRAPH_INVALID, SubmissionState.GRAPH_VALID, SubmissionState.DRAFT));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphValid() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALID);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.SUBMITTED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphInvalid() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_INVALID);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.GRAPH_VALIDATION_REQUESTED, SubmissionState.DRAFT));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForSubmitted() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.SUBMITTED);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.PROCESSING, SubmissionState.EXPORTING));

    }

    @Test
    public void testAllowedSubmissionStateTransitionsForProcessing() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.PROCESSING);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.ARCHIVING));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForArchiving() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.ARCHIVING);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.ARCHIVED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForArchived() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.ARCHIVED);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.EXPORTING));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForExported() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.EXPORTED);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.CLEANUP));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForCleanup() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.CLEANUP);
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.COMPLETE));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForComplete() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.COMPLETE);
        assertThat(states).isEmpty();
    }


    @Test
    public void testIsEditable() {
        Arrays.asList(
                SubmissionState.PENDING,
                SubmissionState.METADATA_VALIDATING,
                SubmissionState.GRAPH_VALIDATION_REQUESTED,
                SubmissionState.GRAPH_VALIDATING,
                SubmissionState.EXPORTING,
                SubmissionState.PROCESSING,
                SubmissionState.CLEANUP,
                SubmissionState.ARCHIVED,
                SubmissionState.SUBMITTED
        ).forEach(state -> {
            //given:
            SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
            submissionEnvelope.enactStateTransition(state);
            assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

            //then:
            assertThat(submissionEnvelope.isEditable()).isFalse();
        });

        Arrays.asList(
                SubmissionState.METADATA_VALID,
                SubmissionState.METADATA_INVALID,
                SubmissionState.EXPORTED,
                SubmissionState.GRAPH_VALID,
                SubmissionState.GRAPH_INVALID,
                SubmissionState.COMPLETE,
                SubmissionState.DRAFT,
                SubmissionState.ARCHIVING
        ).forEach(state -> {
            //given:
            SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
            submissionEnvelope.enactStateTransition(state);
            assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

            //then:
            assertThat(submissionEnvelope.isEditable()).isTrue();
        });
    }

    @Test
    public void testCanAddTo() {
        Arrays.asList(
                SubmissionState.GRAPH_VALIDATION_REQUESTED,
                SubmissionState.GRAPH_VALIDATING,
                SubmissionState.EXPORTING,
                SubmissionState.PROCESSING,
                SubmissionState.CLEANUP,
                SubmissionState.ARCHIVED,
                SubmissionState.SUBMITTED
        ).forEach(state -> {
            //given:
            SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
            submissionEnvelope.enactStateTransition(state);
            assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

            //then:
            assertThat(submissionEnvelope.canAddTo()).isFalse();
        });

        Arrays.asList(
                SubmissionState.METADATA_VALIDATING,
                SubmissionState.PENDING,
                SubmissionState.METADATA_VALID,
                SubmissionState.METADATA_INVALID,
                SubmissionState.EXPORTED,
                SubmissionState.GRAPH_VALID,
                SubmissionState.GRAPH_INVALID,
                SubmissionState.COMPLETE,
                SubmissionState.DRAFT,
                SubmissionState.ARCHIVING
        ).forEach(state -> {
            //given:
            SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
            submissionEnvelope.enactStateTransition(state);
            assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

            //then:
            assertThat(submissionEnvelope.canAddTo()).isTrue();
        });
    }

    private List<SubmissionState> getAllowedStates(SubmissionState state) {
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);
        return submissionEnvelope.allowedSubmissionStateTransitions();
    }
}
