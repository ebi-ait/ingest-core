package uk.ac.ebi.subs.ingest.state;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.core.ValidationEvent;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.project.ProjectEventHandler;

@Component
@RequiredArgsConstructor
public class ValidationStateChangeListener implements ApplicationListener<ValidationEvent> {
  private final @NonNull ProjectEventHandler projectEventHandler;

  @Override
  public void onApplicationEvent(ValidationEvent event) {
    MetadataDocument document = (MetadataDocument) event.getSource();
    // messageRouter.routeStateTrackingUpdateMessageFor(document);

    if (document.getType().equals(EntityType.PROJECT)) {
      projectEventHandler.validatedProject((Project) document);
    }
  }
}
