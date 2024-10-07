package uk.ac.ebi.subs.ingest.submission;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import uk.ac.ebi.subs.ingest.state.SubmissionState;

public class SubmissionEnvelopeTest {

  @Test
  public void testAllowedSubmissionStateTransitions() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.PENDING);
    assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForDraft() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.DRAFT);
    assertThat(states)
        .containsExactlyInAnyOrderElementsOf(
            List.of(SubmissionState.METADATA_VALID, SubmissionState.METADATA_INVALID));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForMetadataValidating() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_VALID);
    assertThat(states)
        .containsExactlyInAnyOrderElementsOf(
            List.of(
                SubmissionState.DRAFT,
                SubmissionState.METADATA_INVALID,
                SubmissionState.GRAPH_VALID,
                SubmissionState.GRAPH_INVALID));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForMetadataValid() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_VALID);
    assertThat(states)
        .containsExactlyInAnyOrderElementsOf(
            List.of(
                SubmissionState.DRAFT,
                SubmissionState.GRAPH_VALID,
                SubmissionState.METADATA_INVALID,
                SubmissionState.GRAPH_INVALID));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForMetadataInvalid() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.METADATA_INVALID);
    assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForGraphValidationRequested() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALID);
    assertThat(states)
        .containsExactlyInAnyOrderElementsOf(
            List.of(SubmissionState.SUBMITTED, SubmissionState.DRAFT));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForGraphValidating() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALID);
    assertThat(states)
        .containsExactlyInAnyOrderElementsOf(
            List.of(SubmissionState.SUBMITTED, SubmissionState.DRAFT));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForGraphValid() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_VALID);
    assertThat(states)
        .containsExactlyInAnyOrderElementsOf(
            List.of(SubmissionState.DRAFT, SubmissionState.SUBMITTED));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForGraphInvalid() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.GRAPH_INVALID);
    assertThat(states).containsExactlyInAnyOrderElementsOf(List.of(SubmissionState.DRAFT));
  }

  @Test
  public void testAllowedSubmissionStateTransitionsForSubmitted() {
    List<SubmissionState> states = getAllowedStates(SubmissionState.SUBMITTED);
    assertThat(states)
        .containsExactlyInAnyOrderElementsOf(
            List.of(SubmissionState.PROCESSING, SubmissionState.EXPORTING));
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
  @EnumSource(
      value = SubmissionState.class,
      names = {
        /*"PENDING",*/
        "EXPORTING",
        "PROCESSING",
        "CLEANUP",
        "ARCHIVED",
        "SUBMITTED"
      })
  public void testIsNotEditable(SubmissionState state) {
    // given:
    SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.enactStateTransition(state);
    assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

    // then:
    assertThat(submissionEnvelope.isEditable()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(
      value = SubmissionState.class,
      names = {
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
    // given:
    SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.enactStateTransition(state);
    assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

    // then:
    assertThat(submissionEnvelope.isEditable()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = SubmissionState.class,
      names = {"EXPORTING", "PROCESSING", "ARCHIVED", "SUBMITTED"})
  public void testCannotAddTo(SubmissionState state) {
    // given:
    SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.enactStateTransition(state);
    assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

    // then:
    assertThat(submissionEnvelope.isSystemEditable()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(
      value = SubmissionState.class,
      names = {
        "PENDING",
        "METADATA_VALID",
        "METADATA_INVALID",
        "EXPORTED",
        "GRAPH_VALID",
        "GRAPH_INVALID",
        "COMPLETE",
        "DRAFT",
        "ARCHIVING",
        "CLEANUP"
      })
  public void testCanAddTo(SubmissionState state) {
    // given:
    SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.enactStateTransition(state);
    assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(state);

    // then:
    assertThat(submissionEnvelope.isSystemEditable()).isTrue();
  }

  private List<SubmissionState> getAllowedStates(SubmissionState state) {
    SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
    submissionEnvelope.enactStateTransition(state);
    return submissionEnvelope.allowedSubmissionStateTransitions();
  }
}
