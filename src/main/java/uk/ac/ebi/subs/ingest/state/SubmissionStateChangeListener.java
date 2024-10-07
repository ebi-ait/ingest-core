package uk.ac.ebi.subs.ingest.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@RequiredArgsConstructor
@Getter
public class SubmissionStateChangeListener extends AbstractMongoEventListener<SubmissionEnvelope> {
  @Autowired @NonNull private final MessageRouter messageRouter;

  private final Logger log = LoggerFactory.getLogger(getClass());

  protected Logger getLog() {
    return log;
  }

  @Override
  public void onAfterSave(AfterSaveEvent<SubmissionEnvelope> event) {
    SubmissionEnvelope submissionEnvelope = event.getSource();

    if (submissionEnvelope.getSubmissionState().equals(SubmissionState.CLEANUP)) {
      log.info(
          String.format("Requesting cleanup for envelope with ID %s", submissionEnvelope.getId()));
      this.messageRouter.routeRequestUploadAreaCleanup(submissionEnvelope);
    }
  }
}