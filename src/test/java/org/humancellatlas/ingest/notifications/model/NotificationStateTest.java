package org.humancellatlas.ingest.notifications.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.humancellatlas.ingest.notifications.model.NotificationState.*;


public class NotificationStateTest {
  @Test
  public void testLegalStateTransitions() {
    assertThat(PENDING.legalTransitions()).contains(QUEUED);
    assertThat(QUEUED.legalTransitions()).contains(PROCESSING);
    assertThat(PROCESSING.legalTransitions()).contains(PROCESSED);
    assertThat(PROCESSED.legalTransitions()).contains(FAILED);
    assertThat(FAILED.legalTransitions()).contains(QUEUED);

    for(NotificationState state: NotificationState.values()) {
      assertThat(state.legalTransitions()).contains(FAILED);
    }
  }

}
