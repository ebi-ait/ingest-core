package org.humancellatlas.ingest.submission;


import org.humancellatlas.ingest.state.SubmissionState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubmissionEnvelopeTest {

    @Test
    public void testAllowedSubmissionStateTransitionsForPending() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.PENDING);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT));

    }

    @Test
    public void testAllowedSubmissionStateTransitionsForDraft() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.DRAFT);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.METADATA_VALIDATING));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForMetadataValidating() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_VALIDATING);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.METADATA_INVALID, SubmissionState.METADATA_VALID));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForMetadataValid() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_VALID);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.GRAPH_VALIDATION_REQUESTED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForMetadataInvalid() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_INVALID);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.METADATA_VALIDATING, SubmissionState.GRAPH_VALIDATION_REQUESTED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphValidationRequested() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALIDATION_REQUESTED);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.GRAPH_VALIDATING, SubmissionState.DRAFT));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphValidating() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALIDATING);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.GRAPH_INVALID, SubmissionState.GRAPH_VALID, SubmissionState.DRAFT));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphValid() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALID);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT, SubmissionState.SUBMITTED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForGraphInvalid() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_INVALID);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.GRAPH_VALIDATION_REQUESTED, SubmissionState.DRAFT));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForSubmitted() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.SUBMITTED);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.PROCESSING, SubmissionState.EXPORTING));

    }

    @Test
    public void testAllowedSubmissionStateTransitionsForProcessing() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.PROCESSING);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.ARCHIVING));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForArchiving() {
        //given:
        List<SubmissionState> states = getAllowedStates(SubmissionState.ARCHIVING);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.ARCHIVED));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForArchived() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.ARCHIVED);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.EXPORTING));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForExported() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.EXPORTED);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.CLEANUP));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForCleanup() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.CLEANUP);

        //expect:
        assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.COMPLETE));
    }

    @Test
    public void testAllowedSubmissionStateTransitionsForComplete() {
        List<SubmissionState> states = getAllowedStates(SubmissionState.COMPLETE);

        //expect:
        assertThat(states).isEmpty();
    }

    private List<SubmissionState> getAllowedStates(SubmissionState state) {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);

        //when:
        return submissionEnvelope.allowedSubmissionStateTransitions();
    }
}
