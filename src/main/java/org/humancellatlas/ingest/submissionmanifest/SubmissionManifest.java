package org.humancellatlas.ingest.submissionmanifest;

import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
