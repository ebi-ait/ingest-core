package org.humancellatlas.ingest.submission;


import org.humancellatlas.ingest.state.SubmissionState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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


    @ParameterizedTest
    @EnumSource(value = SubmissionState.class, names = {
        "PENDING",
        "METADATA_VALIDATING",
        "GRAPH_VALIDATION_REQUESTED",
        "GRAPH_VALIDATING",
        "EXPORTING",
        "PROCESSING",
        "CLEANUP",
        "ARCHIVED",
        "SUBMITTED"
    })
    public void testIsNotEditable(SubmissionState state) {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

        //then:
        assertThat(submissionEnvelope.isEditable()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = SubmissionState.class, names = {
        "METADATA_VALID",
        "METADATA_INVALID",
        "EXPORTED",
        "GRAPH_VALID",
        "GRAPH_INVALID",
        "COMPLETE",
        "DRAFT",
        "ARCHIVING"
    })
    public void testIsEditable(SubmissionState state) {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

        //then:
        assertThat(submissionEnvelope.isEditable()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = SubmissionState.class, names = {
        "GRAPH_VALIDATION_REQUESTED",
        "GRAPH_VALIDATING",
        "EXPORTING",
        "PROCESSING",
        "CLEANUP",
        "ARCHIVED",
        "SUBMITTED"
    })
    public void testCannotAddTo(SubmissionState state) {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

        //then:
        assertThat(submissionEnvelope.isSystemEditable()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = SubmissionState.class, names = {
        "METADATA_VALIDATING",
        "PENDING",
        "METADATA_VALID",
        "METADATA_INVALID",
        "EXPORTED",
        "GRAPH_VALID",
        "GRAPH_INVALID",
        "COMPLETE",
        "DRAFT",
        "ARCHIVING"
    })
    public void testCanAddTo(SubmissionState state) {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);
        assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

        //then:
        assertThat(submissionEnvelope.isSystemEditable()).isTrue();
    }

    private List<SubmissionState> getAllowedStates(SubmissionState state) {
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);
        return submissionEnvelope.allowedSubmissionStateTransitions();
    }
}
