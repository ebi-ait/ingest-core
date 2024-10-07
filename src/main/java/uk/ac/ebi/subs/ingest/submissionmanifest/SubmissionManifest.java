package uk.ac.ebi.subs.ingest.submissionmanifest;

import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.core.AbstractEntity;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

/** Created by rolando on 30/05/2018. */
@AllArgsConstructor
@Getter
public class SubmissionManifest extends AbstractEntity {
  private final Integer expectedBiomaterials;
  private final Integer expectedProcesses;
  private final Integer expectedFiles;
  private final Integer expectedProtocols;
  private final Integer expectedProjects;

  private @Setter Integer actualLinks = 0;
  private final Integer expectedLinks;

  private final Integer totalCount;

  @Setter
  private @DBRef(lazy = true) SubmissionEnvelope submissionEnvelope;
}
